# 「数据图例」模块优化方案文档

> 项目：WuZhu · 加密货币资产管理
> 模块：`org.lifxue.wuzhu.modules.piechart`
> 文档版本：v1.1
> 适用代码版本：`release-1.0.1` (commit `0cd1c4f`)
> 框架：Spring Boot 3.2 + JavaFX 21 + WorkbenchFX 11.3.1 + FxWeaver 2.0.1
> 文档目的：沉淀界面现状、问题、改造方案、落地步骤与回滚预案，作为后续迭代的参考基线。

> **v1.1 修订说明**（2025-08-20）：依据代码库实际核对结果修订。主要变化：
> ① 删除 4 个子 FXML 拆分方案，改为单一 FXML 分区组织（消除 `fx:include` 控制器接线问题）；
> ② 「现价/成本价」切换补充成本口径计算方案（v1.0 误称 `PortfolioValuation` 已有数据）；
> ③ 纠正"24h/7d 字段需新增"的错误——`CMCQuotesLatest` 已含 `percentChange24h/7d`；
> ④ 深色主题改用 stylesheet 替换机制（`InterfaceTheme`），废除 `[data-theme]`/`.dark-mode` 选择器；
> ⑤ 修正 FXML/CSS/监听器伪代码中的非法 API（`ToggleButtonGroup`、`-fx-margin`、effect 变量、`SetChangeListener` 等）；
> ⑥ 单位切换 USD/CNY/EUR 因无汇率数据源，移出本版范围；
> ⑦ 灰度发布默认方案改为"直接替换 + revert"。修订明细见 §13.5 变更记录。

---

## 目录

1. [背景与目标](#1-背景与目标)
2. [现状分析](#2-现状分析)
3. [问题诊断](#3-问题诊断)
4. [设计原则与风格基线](#4-设计原则与风格基线)
5. [信息架构与原型](#5-信息架构与原型)
6. [详细设计方案](#6-详细设计方案)
7. [配色与样式规范](#7-配色与样式规范)
8. [交互与状态机](#8-交互与状态机)
9. [代码改动清单](#9-代码改动清单)
10. [实施步骤与验收标准](#10-实施步骤与验收标准)
11. [回滚与风险控制](#11-回滚与风险控制)
12. [未来扩展](#12-未来扩展)
13. [附录](#13-附录)

---

## 1. 背景与目标

### 1.1 背景

`TypePieChartView` 是 WuZhu 中**唯一**展示投资组合整体分布的视图，承担以下职责：

- 直观展示各币种在总投资中的占比
- 提示用户当前持仓的风险集中度
- 与「交易信息」「盈亏分析」「资金流水」共同构成"看 → 记 → 算 → 决策"的闭环

但目前该模块仅由 1 个 `Label`（总额）+ 1 个 `PieChart` 组成，**信息密度低、交互缺失、视觉风格偏离项目调性**。

### 1.2 目标

| 维度 | 当前 | 目标 |
|----|----|----|
| 信息密度 | 2 项（总额 + 饼图） | 8+ 项（总额/品种数/最大持仓/集中度/明细表/时间戳） |
| 交互能力 | 鼠标 hover tooltip | 切换显示方式 / 隐藏小额 / 阈值调整 / 刷新 / 导出 / 联动高亮 |
| 视觉一致性 | 硬编码粉色，与主题脱节 | 完全跟随主题变量，深浅色无缝切换 |
| 信息架构 | 单饼图 | 工具栏 + KPI 卡片 + 饼图 + 自绘图例 + 详细数据表 |
| 用户体验 | 缩放/分辨率变化时布局错乱 | 锚点布局 + 响应式拉伸 + 滚动保护 |
| 可维护性 | 单 FXML 17 行硬编码 | 单 FXML 分区组织 + 独立 CSS + ViewModel 状态完整 |

### 1.3 非目标

- **不**替换 WorkbenchFX / FxWeaver 框架（见 §6.1 框架兼容性说明）
- **不**引入新依赖（ikonli 已由 WorkbenchFX 传递引入并已在项目中使用）
- **不**改变数据来源（依然来自 `PortfolioCalculationService` + H2 的 `cmc_quotes_latest`）
- **不**改动 `PortfolioCalculationService` 与数据层（成本口径在 ViewModel 内计算，见 §6.4）
- **不**在本版实现多币种本币显示（USD/CNY/EUR，无汇率数据源，见 §12.1）
- **不**实现模块间深度跳转（双击跳 `PATableView` 列为 P3 远期项）

---

## 2. 现状分析

### 2.1 文件清单

| 路径 | 行数 | 职责 |
|----|----|----|
| `src/main/resources/org/lifxue/wuzhu/modules/piechart/TypePieChartView.fxml` | 17 | 界面布局（FXML） |
| `src/main/java/org/lifxue/wuzhu/modules/piechart/TypePieChartViewController.java` | 86 | 控制器 + tooltip 装配 |
| `src/main/java/org/lifxue/wuzhu/modules/piechart/TypePieChartViewModule.java` | 56 | WorkbenchModule 包装 |
| `src/main/java/org/lifxue/wuzhu/viewmodel/TypePieChartViewModel.java` | 242 | 数据装配 + 偏好读取 |

### 2.2 当前数据流

```
TypePieChartViewModel.loadPortfolioData()
  ├─ tradeInfoService.findOrderByTradeDate()      // 从 H2 读所有交易
  ├─ quotesLatestService.queryLatest()            // 从 H2 读最新报价
  ├─ portfolioCalculationService.calculate(...)   // 计算持仓与价值
  └─ buildPieData(valuation, quotes)              // 构造 PieChart.Data
       ├─ 过滤 USDT
       ├─ 应用「隐藏小额」偏好 (PrefsHelper.NOTSMALLCOIN)
       ├─ 按价值降序
       └─ 聚合到「其他」分类
```

### 2.3 当前偏好读取

`PrefsHelper`（`java.util.prefs.Preferences`，节点 `/org/lifxue/wuzhu`）：

- `NOTSMALLCOIN` = 是否隐藏小额品种（`YES`/`NO`，默认 `NO`）
- `NOTSMALLCOINNUM` = 小额阈值（字符串数字，默认 `"100"`）

UI 入口在「首选项 → 品种比例图」：`notSmallCheck`（CheckBox）+ `numSpinner`（`Spinner<Integer>`），
写入走 `PrefsHelper.updatePreferencesValue(...)` + `flushPreferences()`。

**问题**：入口远离数据图例模块，调整需切换模块；改造后本模块工具栏成为新入口，
两处写入**同一个偏好 key**（`PrefsHelper` 不改，向后兼容）。双入口并存期以
「后写者生效」为准，建议在 P3 阶段移除首选项区块（见 §12.4）。

### 2.4 当前界面截图描述

- 顶部紫色 WorkbenchFX 侧边栏头（`#6200EE` 浅 / `#2c3649` 深）
- 中部为 1000×700 主区域
- 一个粉色 `Label`（`#c91590`）显示"当前总价值约:$117523"，左上角
- 圆形饼图占据主区，BTC 占 96.22% 几乎占满整个圆
- 右侧图例列出 8 项，但 0.5% 量级的小币种标签互相挤压，可读性差
- 鼠标 hover 出现 4 行 tooltip，**字号 20px**（Arial），与界面其他 12-14px 严重不协调

---

## 3. 问题诊断

| # | 问题 | 严重度 | 佐证 | 修复优先级 |
|---|----|------|----|------|
| P0-1 | **FXML 布局错位**：`PieChart` 同时设了 `layoutX=175, layoutY=130` 和 `AnchorPane *Anchor=0`，缩放时位置漂移 | 高 | `TypePieChartView.fxml:10` | P0 |
| P0-2 | **硬编码粉红色**：`textFill="#c91590"` 写死，深色主题下对比刺眼 | 高 | `TypePieChartView.fxml:11` | P0 |
| P0-3 | **Tooltip 字号 20px** 与项目其他 12-14px 不符 | 中 | `TypePieChartViewController.java:71` | P0 |
| P0-4 | **饼图色板是 JavaFX 默认 8 色**，与项目紫色调不搭，且深色模式下与背景对比突兀 | 中 | `PieChart` 默认 `ColorPalette` | P0 |
| P1-1 | **信息密度低**：单饼图 + 单 Label；其他模块（PATableView）都有"汇总区+详细表"双层 | 中 | 与 `PATableView` 对比 | P1 |
| P1-2 | **图例无交互**：默认图例不能点击、不能联动 | 中 | JavaFX `Legend` 默认行为 | P1 |
| P1-3 | **"隐藏小额"无快捷入口** | 中 | `PrefsHelper` 已存偏好，但 UI 在「首选项」 | P1 |
| P1-4 | **无现价/成本价切换**：用户看不到基于成本价的分布 | 中 | 需新增成本口径计算（`PortfolioValuation` **不含**成本数据，见 §6.4） | P1 |
| P2-1 | **缺加载/空/错误状态** | 低 | ViewModel 有 `loading`、`errorMessage` 但 UI 无响应 | P2 |
| P2-2 | **缺"上次更新时间"提示** | 低 | H2 `cmc_quotes_latest.LAST_UPDATED` 已存报价时间 | P2 |
| P2-3 | **缺集中度风险提示**（HHI / 最大持仓占比） | 低 | 数据可计算 | P2 |
| P3-1 | **无导出能力** | 低 | `CSVHelper.writeCsv(...)` 已有，可直接复用 | P3 |
| P3-2 | **无多币种本币显示**（CNY/EUR） | 低 | 无汇率数据源，本版不做（见 §12.1） | P3 |

---

## 4. 设计原则与风格基线

### 4.1 设计原则

1. **品牌一致**：所有颜色通过主题变量传递，深浅色无缝切换
2. **信息分层**：KPI（一眼）→ 饼图（直观）→ 图例（关联）→ 表格（细节）
3. **可读性优先**：12-14px 基准字号，tabular-nums 等宽数字，3:1+ 对比度
4. **状态可见**：加载、空、错误态都有明确视觉
5. **渐进增强**：P0 修复 bug → P1 增强信息 → P2 提升质感 → P3 拓展能力

### 4.2 项目已有设计语言

| 维度 | 当前值 | 来源 |
|----|----|----|
| 主色（浅） | `#6200EE` / `#3700B3` | `customTheme.css` |
| 辅色（浅） | `#02ABF5` / `#005fa3`（`-secondary-color`） | `customTheme.css` |
| 主色（深） | `#02ABF5`（`-on-background-color`） | `darkTheme.css` |
| 背景（深） | `#202530`（`-background-color`） | `darkTheme.css` |
| 表面（深） | `#2E3540`（`-surface-color`） | `darkTheme.css` |
| 错误色 | `#B00020`（`-error-color`） | 主题文件 |
| 主题机制 | stylesheet 整体替换（`InterfaceTheme.setNightMode`：customTheme.css ↔ darkTheme.css） | `InterfaceTheme.java` |
| 字体 | 系统默认（`System`），`"System Bold" 14-15px` 用于强调 | `PATableView.fxml` |
| 按钮尺寸 | 80×30 px 圆角 4px | `CashView.fxml` |
| 图标库 | Kordamp.ikonli `materialdesign`（WorkbenchFX 传递依赖，`TypePieChartViewModule` 已在用） | `TypePieChartViewModule.java` |
| 表格 | `TableView` + `CONSTRAINED_RESIZE_POLICY` | `CashView`、`TradeInfoView` |
| 模块布局 | 顶部 `SplitPane` 双段：表单 + 表格 | `CashView.fxml:20` |
| 模块级 CSS | 根节点 `getStylesheets().add(...)` 挂载，作用域限于模块子树 | `RichTextView.java:239` |

### 4.3 品牌色板（饼图专用，10 色）

**浅色模式**（从 Material Design 调色板筛选，与 `-primary` / `-secondary` 同源）：

```
#6200EE  #02ABF5  #03DAC6  #FF6E40  #FFC107
#7C4DFF  #00B8D4  #B388FF  #FF4081  #69F0AE
```

**深色模式**（提升饱和度，避免与暗背景混淆）：

```
#BB86FC  #03DAC6  #CF6679  #FFB74D  #4FC3F7
#81C784  #BA68C8  #FFD54F  #F06292  #A5D6A7
```

**配色机制（v1.1 明确）**：`-fx-pie-color` 只按**切片索引**着色，无法按币名映射，
因此「BTC 固定第 1 色 / USDT 固定第 2 色」等特殊规则**由代码分配**，CSS 色板仅作兜底：

1. ViewModel 在 `buildPieData` 排序后按规则生成色板：
   - **BTC** → 第 1 色；**USDT** → 第 2 色；**「其他」** → 第 10 色
   - 其余币种按价值降序依次取第 3..9 色（超出 10 色则循环取色）
2. 颜色写入 `PieTableVO` / `PieLegendVO` 的 `color` 属性（图例色点、表格行首色点共用）
3. 饼图切片颜色在节点创建后由代码设置：`data.getNode().setStyle("-fx-pie-color: " + hex)`（见 §6.3）
4. 深浅色取色：监听主题切换或在数据重建时按当前主题选取（浅色表/深色表见上）
5. CSS `.chart-pie` 色板（§7.2）仅作为节点未被代码着色前的兜底

---

## 5. 信息架构与原型

### 5.1 信息架构对比

**改造前**（2 段）：

```
┌────────────────────────────────────────┐
│  Label: 当前总价值约:$117523          │
│  PieChart: 品种比例(现价)             │
│    ├─ 圆饼                              │
│    └─ 右侧默认图例（拥挤）              │
└────────────────────────────────────────┘
```

**改造后**（4 段）：

```
┌──────────────────────────────────────────────────────────────┐
│  工具栏 (52px)                                               │
│  [现价|成本价] [□隐藏小额 Spinner] [刷新] [导出]  |  更新时间│
├──────────────────────────────────────────────────────────────┤
│  KPI 卡片区 (84px)                                            │
│  [总价值]  [持仓品种]  [最大持仓]  [集中度 HHI]              │
├──────────────────────────────────────┬───────────────────────┤
│                                      │  持仓明细卡片          │
│     环形图 (Donut Chart)             │  ●BTC  96.22%  $113K  │
│     中心镂空显示总额                  │  ●USDT 0.97%   $1.1K  │
│                                      │  ●ENJ  0.71%   $834   │
│                                      │  ▶ 其他(4项) 0.06%   │
├──────────────────────────────────────┴───────────────────────┤
│  详细数据表                                                 │
│  货币 | 当前价 | 持仓数量 | 总价值 | 占比 | 24h | 7d       │
└──────────────────────────────────────────────────────────────┘
```

> 与 v1.0 原型的差异：表格头**不含** USD/CNY/EUR 单位切换（本版不实现，见 §12.1）；
> 工具栏用两个 `ToggleButton` 实现现价/成本价切换（JavaFX 无 `ToggleButtonGroup` 控件）。

### 5.2 HTML 原型

**位置**：`docs/piechart-mockup.html`（559 行，单文件，零本地依赖）

**功能**：

- 默认浅色 + 4 个黄色便签标注改造点
- 顶部控制条可切换：浅色/深色、显示/隐藏标注、打印
- KPI 卡片、饼图、图例、表格完整可交互（hover 高亮、tab 排序、单位切换）

**打开方式**：

```bash
# 本地浏览器
xdg-open docs/piechart-mockup.html        # Linux
open docs/piechart-mockup.html            # macOS
start docs/piechart-mockup.html           # Windows

# 本地服务（适合远程评审）
python3 -m http.server 8000 --directory docs
# 访问 http://localhost:8000/piechart-mockup.html
```

**与实现的出入**：原型中的单位切换（USD/CNY/EUR）、FontAwesome CDN 图标仅用于 HTML 演示；
JavaFX 实现以本文档为准，图标改用 `org.kordamp.ikonli.javafx.FontIcon` + `MaterialDesign.MDI_*`，
不引入新依赖。Sprint 4 将原型与本方案同步。

---

## 6. 详细设计方案

### 6.1 FXML 结构（单一文件 + 分区注释）

**框架兼容性说明**：本模块由 FxWeaver 2.0.1（`javafx-weaver-spring-boot-starter`）
通过 `@FxmlView("TypePieChartView.fxml")` 加载。FxWeaver 对 `fx:include` 子文件
要求**独立的 `@FxmlController` 子控制器类**，对当前 17 行的视图属于过度设计，
且会引入新的接线面。因此 v1.1 采用**单一 FXML + 注释分区**，与 `CashView`、
`TradeInfoView` 等模块的既有模式保持一致。

`TypePieChartView.fxml` 完整结构（分区注释 `<!-- ===== 1. 工具栏 ===== -->` 等）：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.chart.PieChart?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.shape.Circle?>

<BorderPane xmlns:fx="http://javafx.com/fxml"
            stylesheets="@piechart.css"
            fx:controller="org.lifxue.wuzhu.modules.piechart.TypePieChartViewController">

  <!-- ===== 1. 工具栏 ===== -->
  <top>
    <HBox styleClass="piechart-toolbar" alignment="CENTER_LEFT" spacing="12">
      <Label text="显示方式" styleClass="tb-label"/>
      <ToggleButton fx:id="currentPriceBtn" text="现价"/>
      <ToggleButton fx:id="costPriceBtn" text="成本价"/>
      <Separator orientation="VERTICAL"/>
      <CheckBox fx:id="hideSmallCheck" text="隐藏小额品种"/>
      <Spinner fx:id="thresholdSpinner" prefWidth="90" editable="false"/>
      <Separator orientation="VERTICAL"/>
      <Button fx:id="refreshBtn" text="刷新" styleClass="tb-btn,secondary"/>
      <Button fx:id="exportBtn" text="导出 CSV" styleClass="tb-btn,secondary"/>
      <Pane HBox.hgrow="ALWAYS"/>
      <Label fx:id="updateTimeLabel" styleClass="tb-label"/>
    </HBox>
  </top>

  <center>
    <VBox spacing="12" VBox.vgrow="ALWAYS">

      <!-- ===== 2. KPI 卡片区 ===== -->
      <HBox styleClass="kpi-row" spacing="12">
        <VBox fx:id="cardTotalValue" styleClass="kpi-card,k1" HBox.hgrow="ALWAYS">
          <Label styleClass="kpi-head" text="资产总价值"/>
          <Label fx:id="kpiTotalValue" styleClass="kpi-value" text="--"/>
          <Label fx:id="kpiTotalSub" styleClass="kpi-sub" text="--"/>
        </VBox>
        <VBox fx:id="cardCoinCount" styleClass="kpi-card,k2" HBox.hgrow="ALWAYS">
          <Label styleClass="kpi-head" text="持仓品种"/>
          <Label fx:id="kpiCoinCount" styleClass="kpi-value" text="--"/>
          <Label styleClass="kpi-sub" text="非零持仓币种数"/>
        </VBox>
        <VBox fx:id="cardLargestHolding" styleClass="kpi-card,k3" HBox.hgrow="ALWAYS">
          <Label styleClass="kpi-head" text="最大持仓"/>
          <Label fx:id="kpiLargestName" styleClass="kpi-value" text="--"/>
          <Label fx:id="kpiLargestSub" styleClass="kpi-sub" text="--"/>
        </VBox>
        <VBox fx:id="cardConcentration" styleClass="kpi-card,k4" HBox.hgrow="ALWAYS">
          <Label styleClass="kpi-head" text="集中度 HHI"/>
          <Label fx:id="kpiHhiValue" styleClass="kpi-value" text="--"/>
          <Label fx:id="kpiHhiLevel" styleClass="kpi-sub" text="--"/>
        </VBox>
      </HBox>

      <!-- ===== 3. 饼图 + 图例 ===== -->
      <HBox spacing="12" VBox.vgrow="ALWAYS">
        <StackPane styleClass="chart-panel" HBox.hgrow="ALWAYS">
          <VBox spacing="8" StackPane.alignment="TOP_LEFT">
            <Label fx:id="chartTitle" styleClass="chart-title" text="资产分布（按现价）"/>
          </VBox>
          <PieChart fx:id="pieChart" legendVisible="false"
                    title="" VBox.vgrow="ALWAYS" HBox.hgrow="ALWAYS"/>
          <!-- 中心镂空（donut）与居中文字 -->
          <Circle fx:id="donutHole" styleClass="donut-hole" radius="120" mouseTransparent="true"/>
          <VBox fx:id="pieCenter" styleClass="pie-center" mouseTransparent="true"
                alignment="CENTER" spacing="2">
            <Label text="总价值" styleClass="total-label"/>
            <Label fx:id="centerValue" styleClass="total-value" text="--"/>
            <Label fx:id="centerSub" styleClass="total-sub" text="--"/>
          </VBox>
          <!-- 加载/空/错误状态覆盖层 -->
          <StackPane fx:id="overlay" visible="false" styleClass="chart-overlay">
            <ProgressIndicator fx:id="loader" visible="false"/>
            <VBox fx:id="errorState" visible="false" alignment="CENTER" spacing="8">
              <Label text="❌ 加载失败"/>
              <Button text="重试" onAction="#handleRetry"/>
            </VBox>
            <VBox fx:id="emptyState" visible="false" alignment="CENTER" spacing="8">
              <Label text="📭 暂无持仓"/>
              <Button text="去交易信息添加 →" onAction="#handleGoTradeInfo"/>
            </VBox>
          </StackPane>
        </StackPane>
        <VBox styleClass="legend-panel" prefWidth="320">
          <VBox styleClass="legend-card" VBox.vgrow="ALWAYS">
            <HBox styleClass="legend-head">
              <Label text="持仓明细"/>
              <Pane HBox.hgrow="ALWAYS"/>
              <Label styleClass="tb-label" text="按占比降序"/>
            </HBox>
            <ListView fx:id="legendList" VBox.vgrow="ALWAYS"/>
          </VBox>
        </VBox>
      </HBox>

      <!-- ===== 4. 详细数据表 ===== -->
      <VBox styleClass="table-card">
        <HBox styleClass="table-head">
          <Label text="详细数据 · 点击表头排序"/>
          <Pane HBox.hgrow="ALWAYS"/>
          <Label styleClass="tb-label" text="单位: USD"/>
        </HBox>
        <TableView fx:id="detailTable">
          <columns>
            <TableColumn text="货币"    fx:id="colSymbol"/>
            <TableColumn text="当前价"  fx:id="colPrice"    styleClass="num"/>
            <TableColumn text="持仓数量" fx:id="colQuantity" styleClass="num"/>
            <TableColumn text="总价值"  fx:id="colValue"    styleClass="num"/>
            <TableColumn text="占比"    fx:id="colPercent"  styleClass="num"/>
            <TableColumn text="24h 涨跌" fx:id="colChg24h"  styleClass="num"/>
            <TableColumn text="7d 涨跌"  fx:id="colChg7d"   styleClass="num"/>
          </columns>
          <columnResizePolicy>
            <TableView fx:constant="CONSTRAINED_RESIZE_POLICY"/>
          </columnResizePolicy>
        </TableView>
      </VBox>

    </VBox>
  </center>
</BorderPane>
```

**要点**：

- 根节点 `stylesheets="@piechart.css"`：CSS 随 FXML 位置解析，作用域限于本模块子树，
  切换模块后不残留（与 `RichTextView` 挂载 rich-text.css 的方式同理，见 §4.2）。
- 彻底移除 `layoutX/layoutY` 与绝对定位，全部用 `HBox.hgrow` / `VBox.vgrow` 锚定。
- `donutHole`（`Circle`）与 `pieCenter` 均 `mouseTransparent="true"`，不拦截扇形 hover；
  镂空半径在控制器中按图尺寸调整（见 §6.3），若视觉不理想可回退为实心饼图 + 左上角标题。
- 两个 `ToggleButton`（`currentPriceBtn`/`costPriceBtn`）在控制器中共享一个 `ToggleGroup`
  （模式同 `PreferencesViewController.modeGroup` / `RichTextView.createToggleButton`）。

### 6.2 ViewModel 扩展

**新增枚举 `ValuationMode`**（`modules/piechart/ValuationMode.java`）：

```java
public enum ValuationMode {
    CURRENT_PRICE, // 现价（默认）
    COST           // 成本价
}
```

**新增/调整属性**（在 `TypePieChartViewModel` 中）：

```java
private final ObjectProperty<ValuationMode> displayMode =
    new SimpleObjectProperty<>(ValuationMode.CURRENT_PRICE);
private final ObjectProperty<LocalDateTime> lastUpdateTime = new SimpleObjectProperty<>();
private final IntegerProperty coinCount = new SimpleIntegerProperty(0);
private final StringProperty largestCoinName = new SimpleStringProperty("--");
private final StringProperty largestCoinPercent = new SimpleStringProperty("--");
private final StringProperty largestCoinChg24h = new SimpleStringProperty("--");
private final StringProperty hhiValue = new SimpleStringProperty("--");
private final StringProperty hhiLevel = new SimpleStringProperty("--"); // 极高/高/中/低
private final ObservableList<PieTableVO> tableData = FXCollections.observableArrayList();
private final ObservableList<PieLegendVO> legendData = FXCollections.observableArrayList();

public ObjectProperty<ValuationMode> displayModeProperty() { return displayMode; }
public ObjectProperty<LocalDateTime> lastUpdateTimeProperty() { return lastUpdateTime; }
public IntegerProperty coinCountProperty() { return coinCount; }
// ... 其余 getter/setter（命名与 §6.3 绑定一致）
```

**偏好属性与 PrefsHelper 的闭环（v1.1 明确）**：

- `hideSmallCoins` / `minValueThreshold` 两个属性在**构造函数**中从 `PrefsHelper`
  读初始值（替代当前硬编码 `false` / `"100"`）。
- `loadPortfolioData()` 开头调用 `syncPreferences()`：把 PrefsHelper 当前值写入两属性
  （`Simple*Property` 仅在值真正变化时才触发监听器，不会造成死循环）。
- 两个属性各自挂监听器：变化时 ① `PrefsHelper.updatePreferencesValue(...)` +
  `flushPreferences()` 写回；② 触发数据重建（Spinner 走 300ms debounce，见 §8.3）。
  这样「首选项」与「工具栏」两个入口始终操作同一份偏好，且 UI 改动即时生效。
- `buildPieData` / `buildTableData` 一律**读属性**，不再直接读 `PrefsHelper`。

**新增方法**：

```java
/** 按聚合前明细计算 HHI（含 USDT，不受「其他」桶影响），返回 0~10000 */
private double calculateHHI(Map<Integer, BigDecimal> coinValues, BigDecimal usdtBalance,
                            BigDecimal totalValue) { ... }

/** 切换显示模式：更新属性 → 按新口径重建数据 */
public void switchDisplayMode(ValuationMode mode) {
    if (displayMode.get() != mode) {
        displayMode.set(mode);
        rebuildData();   // 仅重建内存数据，不重新查库（见 §8.3）
    }
}

/** 导出明细为 CSV：控制器选文件，本方法写数据（复用 CSVHelper，见 §6.3） */
public boolean exportToCsv(File file) { ... }

/** 构建明细表数据（含 24h/7d，直接读 CMCQuotesLatest.percentChange24h/7d） */
private void buildTableData(PortfolioValuation valuation, List<CMCQuotesLatest> quotes,
                            Map<Integer, Color> colorMap) { ... }

/** 成本口径计算（见 §6.4），包级可见以便单元测试 */
static CostBasis calculateCostBasis(List<TradeInfo> trades,
                                    Map<Integer, BigDecimal> holdings) { ... }
```

**`PieTableVO`**（`modules/piechart/vo/PieTableVO.java`，新增）：

```java
public class PieTableVO {
    private final SimpleStringProperty symbol;
    private final SimpleObjectProperty<BigDecimal> price;   // 显示时格式化
    private final SimpleObjectProperty<BigDecimal> quantity;
    private final SimpleObjectProperty<BigDecimal> value;
    private final SimpleObjectProperty<BigDecimal> percent;
    private final SimpleObjectProperty<BigDecimal> chg24h;  // 数值，非格式化字符串
    private final SimpleObjectProperty<BigDecimal> chg7d;
    private final SimpleObjectProperty<Color> color;        // 行首色点，与图例/切片同源
    // + PropertyValueFactory 需要的 getter / Property 方法
}
```

> 涨跌列存 `BigDecimal` 数值，单元格自行格式化并依据符号套用 `.up`/`.down` 样式类，
> 不做 `"+"/"-"` 字符串前缀判断（见 §6.3）。

**`PieLegendVO`**（`modules/piechart/vo/PieLegendVO.java`，新增）：

```java
public class PieLegendVO {
    private final SimpleStringProperty symbol;   // 用于切片↔图例↔表格三向联动的匹配键
    private final SimpleStringProperty name;     // 展示名（含占比，或原始币名）
    private final SimpleStringProperty percent;
    private final SimpleStringProperty value;
    private final SimpleObjectProperty<Color> color;
    // + getter / Property 方法
}
```

**`PieLegendCell`**（`modules/piechart/PieLegendCell.java`，新增，自定义 `ListCell`）：

```java
public class PieLegendCell extends ListCell<PieLegendVO> {
    @Override
    protected void updateItem(PieLegendVO item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setGraphic(null); return; }
        // 渲染：色点(Circle, fill=item.color) + 名称 + 占比 + 价值
        // :hover 状态由 CSS .legend-row:hover 处理；行内点击/悬停联动由控制器负责（§6.3）
    }
}
```

### 6.3 Controller 扩展

**`TypePieChartViewController` 新增绑定**（在 `initialize` 中）：

```java
// 0. 主题色板（浅/深）与偏好初始值在 VM 构造时已就绪

// 1. 工具栏
ToggleGroup modeGroup = new ToggleGroup();
currentPriceBtn.setToggleGroup(modeGroup);
costPriceBtn.setToggleGroup(modeGroup);
currentPriceBtn.setSelected(true);
currentPriceBtn.setOnAction(e -> viewModel.switchDisplayMode(ValuationMode.CURRENT_PRICE));
costPriceBtn.setOnAction(e -> viewModel.switchDisplayMode(ValuationMode.COST));
// 反向同步：displayMode 变化 → 选中对应按钮
viewModel.displayModeProperty().addListener((obs, o, n) -> {
    currentPriceBtn.setSelected(n == ValuationMode.CURRENT_PRICE);
    costPriceBtn.setSelected(n == ValuationMode.COST);
});

hideSmallCheck.selectedProperty().bindBidirectional(viewModel.hideSmallCoinsProperty());
thresholdSpinner.setValueFactory(
    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 100, 10));
thresholdSpinner.setDisable(!hideSmallCheck.isSelected());
hideSmallCheck.selectedProperty().addListener((obs, o, n) ->
    thresholdSpinner.setDisable(!n));
// Spinner<Integer> ↔ ObjectProperty<BigDecimal> 双向同步（类型不同，不能 bindBidirectional）
thresholdSpinner.getValueFactory().valueProperty().addListener((obs, o, n) ->
    viewModel.setMinValueThreshold(BigDecimal.valueOf(n)));       // 触发写回 + debounce 重建
viewModel.minValueThresholdProperty().addListener((obs, o, n) ->
    thresholdSpinner.getValueFactory().setValue(n.intValue()));   // 反向同步（值相等时不触发）

refreshBtn.setOnAction(e -> viewModel.loadPortfolioData());
exportBtn.setOnAction(e -> {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV 文件", "*.csv"));
    File file = fc.showSaveDialog(pieChart.getScene().getWindow());
    if (file != null) {
        boolean ok = viewModel.exportToCsv(file);   // 复用 CSVHelper.writeCsv(...)
        // 成功/失败 Toast 提示
    }
});

// 2. KPI 卡片（bind 到 VM 属性，见 §6.2）
centerValue.textProperty().bind(viewModel.totalValueProperty().asString("$%,.2f"));
centerSub.textProperty().bind(
    Bindings.createStringBinding(() -> viewModel.getCoinCount() + " 个品种",
        viewModel.coinCountProperty()));
kpiTotalValue.textProperty().bind(viewModel.totalValueProperty().asString("$%,.2f"));
// ... 其余卡片：kpiCoinCount / kpiLargestName / kpiLargestSub / kpiHhiValue / kpiHhiLevel
chartTitle.textProperty().bind(Bindings.createStringBinding(() ->
    viewModel.getDisplayMode() == ValuationMode.CURRENT_PRICE
        ? "资产分布（按现价）" : "资产分布（按成本价）",
    viewModel.displayModeProperty()));

// 3. 饼图：共享同一 ObservableList，不重复 setData
pieChart.setData(viewModel.getPieData());
pieChart.setLegendVisible(false);
// 数据变化 → 重新绑定切片 hover/颜色（ListChangeListener，不是 SetChangeListener）
viewModel.getPieData().addListener((ListChangeListener<PieChart.Data>) change ->
    setupSliceHoverAndColor());

// 4. 图例 ListView
legendList.setItems(viewModel.getLegendData());
legendList.setCellFactory(lv -> new PieLegendCell());
// 联动：图例 hover → 切片高亮；切片 hover → 图例行高亮（按 symbol 匹配）

// 5. 详细表
detailTable.setItems(viewModel.getTableData());
colSymbol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
// ... 其余列；price/quantity/value/percent 用格式化 Cell
colChg24h.setCellFactory(col -> new PercentCell());   // 数值 → "+1.23% / −3.45%"，按符号套 .up/.down
colChg7d.setCellFactory(col -> new PercentCell());

// 6. 状态机（见 §8.1）
viewModel.loadingProperty().addListener((obs, o, loading) -> updateOverlayState());
viewModel.errorMessageProperty().addListener((obs, o, e) -> updateOverlayState());
viewModel.getTableData().addListener((ListChangeListener<PieTableVO>) c -> updateOverlayState());
// updateOverlayState():
//   loading        → overlay 可见 + loader 可见
//   error != null  → overlay 可见 + errorState 可见
//   tableData 空   → overlay 可见 + emptyState 可见
//   其余           → overlay 隐藏
```

**`setupSliceHoverAndColor`（v1.1 修正版）**：

```java
private void setupSliceHoverAndColor() {
    for (PieChart.Data data : pieChart.getData()) {
        // 关键：切片节点在布局后才创建，直接 getNode() 为 null。
        // 必须监听 nodeProperty，在节点出现时挂处理器与颜色。
        data.nodeProperty().addListener((obs, o, node) -> {
            if (node == null) return;
            // 颜色：优先取 VO 中代码分配的颜色，CSS 兜底
            PieLegendVO vo = viewModel.findLegendBySymbol(extractSymbol(data));
            if (vo != null) node.setStyle("-fx-pie-color: " + toHex(vo.getColor()) + ";");
            node.setOnMouseEntered(e -> {
                node.setEffect(new DropShadow(20, Color.BLACK));
                highlightLegendRow(extractSymbol(data));   // 按 symbol 匹配，不依赖 equals
            });
            node.setOnMouseExited(e -> node.setEffect(null));
        });
    }
}
```

> `PieChart.Data` 提供 `nodeProperty()`（`ReadOnlyObjectProperty<Node>`），
> 是 JavaFX 官方支持的切片节点观测点；v1.0 的
> "`setData` 后立即遍历 `getNode()` + `SetChangeListener`" 写法不成立。

**联动匹配键**：`PieChart.Data.name` 为展示名（含占比后缀），不能直接做匹配键。
约定 `PieChart.Data.name` 以币种符号开头（如 `"BTC (96.22%)"`），`extractSymbol()`
截取第一个空格前的部分作为 symbol，与 `PieLegendVO.symbol` / `PieTableVO.symbol` 对齐。

**`handleGoTradeInfo`**：注入 `Workbench` 与 `TradeInfoViewModule`，
调用 `workbench.openModule(tradeInfoViewModule)`（P2，空状态 CTA）。

### 6.4 成本口径计算（v1.1 新增）

**数据现实**：`PortfolioValuation`（`holdings`/`usdtBalance`/`coinValues`/`totalValue`）
**不含任何成本数据**，现价口径之外需要新算。

**方案**：在 `TypePieChartViewModel` 内新增包级可见静态方法（便于单测），
基于已查出的 `List<TradeInfo>` 计算**加权平均成本**（移动平均，不做 FIFO）：

```
对每个非 USDT 币种 coinId：
  costPaid  = Σ(所有「买」交易的 quoteNum)          // 累计买入花费（USDT 计价）
  costQty   = Σ(所有「买」交易的 baseNum)           // 累计买入数量
  avgCost   = costPaid / costQty                   // 加权平均成本价（costQty=0 时记 0）
  costValue = holdings.get(coinId) × avgCost       // 当前持仓成本口径价值

USDT：costValue = usdtBalance（稳定币成本按 1:1）
总成本价值 = Σ costValue + usdtBalance
```

- 「卖」交易不改变平均成本（移动平均口径）；不足：全卖后重新买入不重置成本，FIFO 留待远期扩展（§12.2）。
- 结果以 `CostBasis` record 承载：`Map<Integer, BigDecimal> costValues`、`BigDecimal totalCost`、
  `Map<Integer, BigDecimal> avgCost`。
- 现价/成本价切换**不重新查库**：`loadPortfolioData` 一次查出 `tradeInfos` + `quotes`，
  `rebuildData()` 按当前 `displayMode` 选择口径重建 `pieData`/`legendData`/`tableData`。
- 成本价口径下 `centerValue` 绑定 `totalCost`，图例/表格金额列同口径，标题切为「按成本价」。

---

## 7. 配色与样式规范

### 7.1 主题变量扩展（分别写入两个主题文件）

**主题机制**：`InterfaceTheme.setNightMode` 在 Workbench 上**整体替换** stylesheet
（`customTheme.css` ↔ `darkTheme.css`），项目中不存在 `[data-theme]` 属性或 `.dark-mode`
样式类。因此深色变量**必须直接写入 `darkTheme.css`**，浅色写入 `customTheme.css`，
两文件各自完整（v1.0 的 `[data-theme="dark"], .dark-mode` 选择器永远不会命中，已删除）。

**`customTheme.css`（浅色）**，在 `.root > *` 块中追加：

```css
/* 文本层级 */
-text-secondary: rgba(0,0,0,0.6);
-text-tertiary:  rgba(0,0,0,0.4);
-hover-bg:       rgba(98,0,238,0.08);
/* 涨/跌色 */
-color-up:   #1A8754;
-color-down: #D93025;
```

**`darkTheme.css`（深色）**，在 `.root > *` 块中追加：

```css
/* 文本层级 */
-text-secondary: rgba(255,255,255,0.6);
-text-tertiary:  rgba(255,255,255,0.4);
-hover-bg:       rgba(187,134,252,0.12);
/* 涨/跌色 */
-color-up:   #4CAF7D;
-color-down: #EF6B6B;
```

> 说明：JavaFX CSS 变量（looked-up color）只能代入**颜色**值，不能代入
> `-fx-effect` 的 effect 值，也不能组合阴影；阴影一律在 `piechart.css` 内联书写（见下）。

### 7.2 新增 `piechart.css`

**位置**：`src/main/resources/org/lifxue/wuzhu/modules/piechart/piechart.css`
**加载**：由 FXML 根节点 `stylesheets="@piechart.css"` 声明（见 §6.1），无需改 `InterfaceTheme`。

```css
/* 工具栏 */
.piechart-toolbar {
    -fx-background-color: -surface-color;
    -fx-border-color: rgba(128,128,128,0.25);
    -fx-border-width: 0 0 1 0;
    -fx-padding: 8 16;
}
.tb-label { -fx-text-fill: -text-secondary; -fx-font-size: 12; }
.tb-btn {
    -fx-background-color: transparent;
    -fx-text-fill: -on-surface-color;
    -fx-border-color: rgba(128,128,128,0.4);
    -fx-border-radius: 4; -fx-background-radius: 4;
    -fx-padding: 5 12; -fx-cursor: hand;
}
.tb-btn:hover { -fx-background-color: -hover-bg; }

/* KPI 卡片 */
.kpi-row { -fx-padding: 16 16 0 16; }
.kpi-card {
    -fx-background-color: -surface-color;
    -fx-background-radius: 8; -fx-border-radius: 8;
    -fx-border-color: rgba(128,128,128,0.25);
    -fx-padding: 14 16;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.05, 0, 2);
    -fx-spacing: 4;
}
.kpi-card .kpi-head { -fx-text-fill: -text-secondary; -fx-font-size: 11; }
.kpi-card .kpi-value { -fx-text-fill: -on-surface-color; -fx-font-size: 22; -fx-font-weight: bold; }
.kpi-card .kpi-sub { -fx-text-fill: -text-tertiary; -fx-font-size: 11; }

/* 饼图容器 */
.chart-panel {
    -fx-background-color: -surface-color;
    -fx-border-color: rgba(128,128,128,0.25);
    -fx-border-radius: 8; -fx-background-radius: 8;
    -fx-padding: 16;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.05, 0, 2);
}
.chart-title { -fx-text-fill: -on-surface-color; -fx-font-size: 16; -fx-font-weight: bold; }
.pie-center .total-label { -fx-text-fill: -text-secondary; -fx-font-size: 11; }
.pie-center .total-value { -fx-text-fill: -primary-color; -fx-font-size: 22; -fx-font-weight: bold; }
.pie-center .total-sub { -fx-text-fill: -text-tertiary; -fx-font-size: 10; }

/* 中心镂空圆：跟随主题表面色，制造 donut 效果（半径在代码中随图尺寸调整） */
.donut-hole { -fx-fill: -surface-color; }

/* 覆盖 JavaFX PieChart 默认色板（仅作代码未着色前的兜底） */
.chart-pie {
    -fx-pie-color:
        #6200EE, #02ABF5, #03DAC6, #FF6E40, #FFC107,
        #7C4DFF, #00B8D4, #B388FF, #FF4081, #69F0AE;
}

/* 自绘图例 */
.legend-panel { -fx-padding: 0; }
.legend-card {
    -fx-background-color: -surface-color;
    -fx-border-color: rgba(128,128,128,0.25);
    -fx-border-radius: 8; -fx-background-radius: 8;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.05, 0, 2);
}
.legend-head { -fx-padding: 12 16; }
.legend-row { -fx-padding: 8 16; }
.legend-row:hover { -fx-background-color: -hover-bg; -fx-cursor: hand; }

/* 表格 */
.table-card {
    -fx-background-color: -surface-color;
    -fx-border-color: rgba(128,128,128,0.25);
    -fx-border-radius: 8; -fx-background-radius: 8;
    -fx-padding: 12 16 16 16;   /* 替代不存在的 -fx-margin */
}
.table-card .table-head { -fx-padding: 0 0 8 0; }
.num { -fx-alignment: CENTER-RIGHT; }
.up { -fx-text-fill: -color-up; }
.down { -fx-text-fill: -color-down; }
```

**深色兜底色板**：`.chart-pie` 深色 10 色（`#BB86FC, #03DAC6, #CF6679, ...`）写入
`darkTheme.css`（覆盖同一选择器），与 §4.3 色表一致——因为只有该文件在夜间被加载。
切片实际颜色由代码按 §4.3 规则设置，兜底色板仅在代码路径失效时可见。

### 7.3 数字格式约定

- **货币金额**：千分位分隔 + 2 位小数，如 `$117,523.42`
- **占比**：`#,##0.00%`（保留 2 位），< 0.01% 时显示 `<0.01%`
- **数量**：`#,##0.########`（最多 8 位小数，去尾零）
- **涨跌**：`+1.23%` / `−3.45%`（使用 `−` 而非 `-`）
- **时间**：`HH:mm:ss`（刷新时间，取自报价 `LAST_UPDATED`）、`yyyy-MM-dd`（24h 区间）

---

## 8. 交互与状态机

### 8.1 状态机

```
                ┌──────────────┐
                │   INITIAL    │ (empty)
                └──────┬───────┘
                       │ loadPortfolioData()
                       ▼
            ┌─────────────────────┐
            │     LOADING         │ ◀──── 刷新按钮 / 切换模式 / 偏好变化
            └──────┬──────────────┘
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
   ┌────────┐  ┌────────┐  ┌────────┐
   │ EMPTY  │  │ ERROR  │  │ LOADED │
   │(0数据) │  │(异常)  │  │(正常)  │
   └────┬───┘  └───┬────┘  └────┬───┘
        │  点击CTA │  点击重试   │
        └──────────┴────────────┘
                   │
                   ▼
              (回到 LOADING)
```

### 8.2 交互列表

| # | 触发者 | 行为 | 反馈 |
|---|----|----|----|
| 1 | 工具栏「现价」 | `viewModel.switchDisplayMode(CURRENT_PRICE)` | 饼图+图例+表格按现价重建（不重新查库） |
| 2 | 工具栏「成本价」 | `viewModel.switchDisplayMode(COST)` | 同上，按成本口径（§6.4） |
| 3 | 工具栏「隐藏小额」勾选 | 属性变化 → 写回 `PrefsHelper.NOTSMALLCOIN` + 重建 | 饼图+图例+表格立即重建，Spinner 同步启用/禁用 |
| 4 | Spinner 调节 | 属性变化 → 写回 `PrefsHelper.NOTSMALLCOINNUM` + 重建 | 300ms debounce 后生效 |
| 5 | 「刷新」按钮 | `viewModel.loadPortfolioData()` | overlay 显示 loader |
| 6 | 「导出 CSV」按钮 | 控制器弹 `FileChooser`，`viewModel.exportToCsv(file)` 用 `CSVHelper.writeCsv` 写数据 | 保存对话框 + 成功/失败 Toast |
| 7 | 饼图扇形 hover | 扇形放大 + 图例滚动到对应行（按 symbol 匹配） | 200ms ease 动画 |
| 8 | 饼图扇形点击 | 在图例中选中对应行 + 表格滚动到对应行 | 高亮 1.5s 后恢复 |
| 9 | 图例行 hover | 扇形放大 + 表格行高亮 | 200ms ease 动画 |
| 10 | 图例行点击 | 表格滚动到对应行 + 选中 | 持续高亮 |
| 11 | 表格行 hover | 对应扇形 + 图例行高亮 | 200ms ease 动画 |
| 12 | 表格表头点击 | 排序（占比/币种/价值/涨跌） | 升降序箭头 |
| 13 | 表格行双击 | 跳转 `PATableView` 对应币种（P3，不在本版） | 切换模块 |
| 14 | ~~单位切换 USD/CNY/EUR~~ | **不在本版**（无汇率数据源，见 §12.1） | — |
| 15 | 空状态「去交易信息」 | `workbench.openModule(tradeInfoViewModule)` | 切换模块 |
| 16 | 错误状态「重试」 | `viewModel.loadPortfolioData()` | 重新加载 |

### 8.3 性能与节流

- **数据重建策略（v1.1 统一口径）**：数据每次全量重建，用
  `pieData.setAll(...)` / `tableData.setAll(...)` 替换内容（一次 ListChange 事件），
  饼图 `setData(viewModel.getPieData())` 仅初始化时调用一次，此后共享同一列表。
  JavaFX 自身会按索引复用未变化的切片节点，**不做**"复用 Data 实例"的手工优化
  （v1.0 两条建议并存且互相矛盾，此处二选一）。
- **加载粒度**：`loadPortfolioData()` 负责查库（交易 + 报价）+ 计算 + 重建；
  `rebuildData()` 只按当前 `displayMode`/偏好用已取数据重建，供切换模式与偏好变化复用。
- **切片交互挂载**：切片节点创建时机不确定，一律通过 `data.nodeProperty()` 监听挂载，
  用 `WeakListener` 包装避免泄漏。
- **Spinner 节流**：值变化 300ms debounce 后再写回 + 重建。
- **表格**：`TableView` 虚拟化本身支持 50+ 行；不做分页。
- **API 调用节流**：本模块不直接调 CoinMarketCap（数据来自 H2），仅「刷新」按钮主动触发查询，
  无节流需求。

---

## 9. 代码改动清单

### 9.1 新增文件

| 路径 | 说明 |
|----|----|
| `modules/piechart/vo/PieTableVO.java` | 详细表 VO（数值型 `BigDecimal` + `Color`） |
| `modules/piechart/vo/PieLegendVO.java` | 图例行 VO（symbol 匹配键 + `Color`） |
| `modules/piechart/PieLegendCell.java` | 自定义 `ListCell` |
| `modules/piechart/ValuationMode.java` | 枚举：现价/成本价 |
| `src/main/resources/org/lifxue/wuzhu/modules/piechart/piechart.css` | 模块独立样式（FXML 根节点声明） |

> 不新增子 FXML、不新增子控制器；成本口径以 VM 内 `CostBasis` record +
> `calculateCostBasis` 静态方法承载，不新增服务类。

### 9.2 修改文件

| 路径 | 改动 |
|----|----|
| `modules/piechart/TypePieChartView.fxml` | 17 行重写为 §6.1 结构（4 个注释分区），移除 `layoutX/Y` 与硬编码色 |
| `modules/piechart/TypePieChartViewController.java` | 新增 @FXML 字段、绑定、状态机、三向联动、导出对话框、切片节点挂载 |
| `viewmodel/TypePieChartViewModel.java` | 新增属性与方法（§6.2）、成本口径计算（§6.4）、偏好读写闭环、`setAll` 重建 |
| `modules/piechart/TypePieChartViewModule.java` | （可选）模块名 `数据图例` → `资产分布`；图标 `MDI_CHART_PIE` → `MDI_CHART_DONUT` |
| `themes/customTheme.css` | `.root > *` 追加浅色变量（§7.1） |
| `themes/darkTheme.css` | `.root > *` 追加深色变量 + `.chart-pie` 深色兜底色板 |
| `modules/setting/PreferencesView.fxml` | （P3，可选）删除「品种比例图」区块 |
| `modules/setting/PreferencesViewController.java` | （P3，可选）移除 `notSmallCheck`/`numSpinner` 处理逻辑 |
| `util/PrefsHelper.java` | 不改（向后兼容） |

### 9.3 不动的文件

- `WuZhuApplication.java`、`JavaFxApplication.java`、`PrimaryStageInitializer.java`
- `Workbench` 注入链路、FxWeaver `@FxmlView` 注解机制
- 主题切换 `InterfaceTheme.java`（stylesheet 替换机制原样保留）
- 数据访问层（`PortfolioCalculationService`、`ITradeInfoService`、`ICMCQuotesLatestService` 保持原样）

---

## 10. 实施步骤与验收标准

### 10.1 实施步骤（4 个 Sprint）

#### Sprint 1：P0 修复（0.5 人·天）

| 任务 | 验收 |
|----|----|
| 删除 FXML 硬编码颜色 `textFill="#c91590"` | Label 跟随主题颜色切换 |
| 修复 `layoutX/Y` 错位，改为 `HBox.hgrow`/`VBox.vgrow` 锚定 | 缩放窗口饼图不漂移 |
| Tooltip 字号 20px → 13px | 与界面字号一致 |
| 主题文件追加 `.chart-pie` 色板（浅/深各一份） | 浅色 / 深色都呈现品牌色 |

#### Sprint 2：P1 信息架构（3 人·天，含成本口径）

| 任务 | 验收 |
|----|----|
| 重写 `TypePieChartView.fxml` 为 §6.1 单文件结构 + `piechart.css` 挂载 | 分区注释清晰，无 `layoutX/Y` |
| 实现 4 个 KPI 卡片（数据来自 ViewModel 扩展） | 数值正确，最大持仓卡含 24h 涨跌副标题 |
| 工具栏：现价/成本价、隐藏小额、Spinner、刷新、导出 | 切换现价/成本价饼图刷新；勾选/Spinner 写回偏好并即时重建 |
| **成本口径计算**（§6.4 `calculateCostBasis`） | 加权平均成本单测通过；成本价模式饼图/KPI/表格金额一致 |
| 自绘 ListView 图例，替换 JavaFX 默认 Legend | 色点 + 名称 + 占比 + 价值，hover 高亮 |
| 详细数据表，7 列完整（24h/7d 直接读 `percentChange24h/7d`，**无需改 DTO**） | 涨跌列红绿色（数值判定，非字符串前缀） |

#### Sprint 3：P2 状态机与体验（2 人·天）

| 任务 | 验收 |
|----|----|
| 加载/空/错误状态覆盖层 | 三种状态切换正常，文案友好 |
| 饼图扇形 ↔ 图例行 ↔ 表格行 三向联动（按 symbol 匹配） | hover 任一处，另两处高亮 |
| 切片颜色代码分配（§4.3 规则）+ `nodeProperty` 挂载 | BTC/USDT/其他 颜色固定，深浅色切换正确 |
| 更新时间标签（取自报价 `LAST_UPDATED`） | 「更新于 HH:mm:ss」显示 |
| HHI 集中度计算与提示文案（按聚合前明细） | <1500 低、1500-2500 中、>2500 高、>5000 极高 |
| Donut 镂空半径适配与视觉打磨（可选：占比列进度条背景） | 深浅色下中心文字不被扇形干扰，或按 §6.1 回退实心饼图 |

#### Sprint 4：测试与文档（1 人·天）

| 任务 | 验收 |
|----|----|
| `TypePieChartViewModelTest` 单元测试 | 覆盖 buildPieData、calculateCostBasis、calculateHHI、switchDisplayMode、偏好读写闭环 |
| `TypePieChartViewControllerTest` 集成测试 | 模拟 0/1/8/50 币种场景 |
| 主题切换回归测试 | 浅色 / 深色切换无残留色、无残留样式 |
| 更新 `packaging/DEVELOPMENT.md` | 记录新模块结构与扩展点 |
| 更新 `docs/piechart-mockup.html` | 与 v1.1 方案同步（移除单位切换等出入点） |

### 10.2 验收标准

**功能验收**：

- [ ] 0 数据时显示空状态 CTA
- [ ] 1 个币种时饼图为完整圆 + 中心显示总额
- [ ] 50+ 币种时图表不卡顿（<200ms 响应）
- [ ] 现价/成本价切换有数据时刷新饼图（成本口径见 §6.4）
- [ ] 隐藏小额 + 阈值调整联动，并写回 `PrefsHelper`（重启后保留）
- [ ] 饼图、图例、表格三向 hover 联动
- [ ] 浅色 / 深色主题切换无残留
- [ ] 导出 CSV 可用（`CSVHelper.writeCsv`，无新增依赖）

**视觉验收**：

- [ ] 4 个 KPI 卡片高度一致，左侧色条对应图标颜色
- [ ] 饼图中心镂空显示总额，字号与 KPI 一致
- [ ] 图例行 hover 与饼图扇形 hover 视觉一致
- [ ] 表格涨跌列用 `-color-up` / `-color-down`（浅 `#1A8754`/`#D93025`）
- [ ] 暗色模式下文本对比度 ≥ 4.5:1

**工程验收**：

- [ ] `mvn clean package` 通过（含现有 Spring 上下文测试）
- [ ] `TypePieChartViewModel` 单元测试覆盖率 ≥ 70%（**模块级**口径：buildPieData/成本/HHI/偏好闭环）
- [ ] 无新增 Maven 依赖；ikonli 仅用传递依赖
- [ ] `piechart.css` 作用域隔离：切换到其他模块后样式无残留

> v1.1 删除 v1.0 的「启动时间变化 < 100ms / 内存增量 < 20MB」硬指标——
> 桌面应用冷启动测量噪声大且本项目无基准采集，改为上述可操作工程指标。

---

## 11. 回滚与风险控制

### 11.1 风险矩阵

| 风险 | 概率 | 影响 | 缓解措施 |
|----|----|----|----|
| **成本口径分歧**：加权平均 vs FIFO 与用户预期不符 | 中 | 高 | 开工前与用户确认口径（默认加权平均，§6.4 已写明假设与局限）；单测锁定算例 |
| 切片节点创建时机不确定，hover/颜色挂载失败 | 中 | 中 | 统一走 `data.nodeProperty()` 监听挂载（§6.3），集成测试覆盖 8/50 币种 |
| ViewModel 属性↔`PrefsHelper` 双入口写竞争 | 低 | 中 | 单一写回路径（属性监听器）+ `Simple*Property` 等值不触发特性防环；P3 移除首选项区块（§12.4） |
| 深色主题对比度不达标 | 中 | 低 | 用 WebAIM Contrast Checker 验证每对前景/背景 |
| 样式残留污染其他模块 | 低 | 中 | `piechart.css` 由 FXML 根节点声明，作用域限于模块子树（§6.1） |
| Donut 镂空半径与图尺寸不匹配（缩放/主题切换后） | 中 | 低 | 半径绑定图尺寸动态调整；不理想则按 §6.1 回退实心饼图 |
| 50+ 币种卡顿 | 低 | 中 | `TableView` 虚拟化 + `setAll` 全量重建；远期切换 `Canvas`（§12.2） |
| 联动监听造成内存泄漏 | 低 | 中 | `WeakListener` + 模块失活时清理 |

### 11.2 回滚预案

1. **代码回滚**：`git revert` 对应 PR 即可
2. **配置回滚**：`PrefsHelper.NOTSMALLCOIN` / `NOTSMALLCOINNUM` 已存值不受影响
3. **数据回滚**：本改造不涉及数据层（成本口径为内存计算，不落库）
4. **样式回滚**：删除 `piechart.css`、清理两个主题文件追加项即可
5. **FXML 回滚**：`git checkout HEAD~1 -- modules/piechart/`

### 11.3 灰度发布

- **方案 B（推荐）**：直接替换，靠 `git revert` 快速回退。桌面单人应用，无线上灰度价值；
  且 v1.1 已删除子 FXML 拆分，替换面更小。
- **方案 A（备选）**：保留旧 `TypePieChartView.fxml` 备份为 `TypePieChartView.legacy.fxml`，
  用 `BuildProfile`（`dev`/`prod`）区分新旧版本（仅当出现短期无法修复的阻塞缺陷时启用）。

---

## 12. 未来扩展

### 12.1 短期（1-2 月）

- **多币种本币**：在 `PrefsHelper` 加 `DISPLAY_CURRENCY` 配置（USD/CNY/EUR）。
  **前置条件**：需要法币汇率数据源（CoinMarketCap 报价以 USD 计价，本项目当前无汇率数据），
  建议手工配置固定汇率或引入免费汇率 API，确定后恢复 §8.2 的 #14 交互。
- **饼图模式切换**：「环形图 ↔ 饼图 ↔ 树状图（treemap）」三选一
- **首选项区块移除**：删除 `PreferencesView` 中「品种比例图」区块，消除双入口（§12.4）

> v1.1 勘误：v1.0 的"24h 涨跌列接入需 `CMCQuotesLatest` 增加 `percent_change_24h` 字段"
> 不成立——该字段（及 `percent_change_7d`）已存在并已随报价落库（`CMCQuotesLatest.java:79-83`），
> 本版 Sprint 2 直接实现。

### 12.2 中期（3-6 月）

- **饼图替换为 `Canvas` 自绘**：解决 50+ 币种卡顿
- **历史快照**：用 Flyway 加 `portfolio_snapshot` 表，每天定时记录饼图数据，支持回放
- **风险指标**：除 HHI 外增加 VaR、夏普比率（依赖收益率数据）
- **成本口径进阶**：FIFO/平均成本法可配置（当前为固定加权平均，§6.4）

### 12.3 长期（6+ 月）

- **响应式设计**：窗口缩到 < 800px 时自动改为 1 栏（图表 + 图例堆叠）
- **Web 端预览**：将图表导出为 SVG/PNG 分享
- **AI 助手**：基于持仓自动给出分散建议

### 12.4 跨模块联动

- 「数据图例」→ 「盈亏分析」：双击饼图扇形跳转到该币种明细
- 「数据图例」→ 「资金流水」：通过饼图发现 USDT 占比过高时，跳到资金流水看入金记录
- 「首选项」→ 「数据图例」：P3 移除 `PreferencesView` 中已迁移的「品种比例图」区块，
  避免两处 UI 写同一偏好 key

---

## 13. 附录

### 13.1 HHI 计算说明

**Herfindahl-Hirschman Index（赫芬达尔指数）**：

- 每个币种占比平方后求和，再乘以 10000
- 值越大表示越集中
- 解释标准：
  - `< 1500`：低集中度，分散良好
  - `1500 ~ 2500`：中集中度
  - `> 2500`：高集中度
  - `> 5000`：极高集中度，建议分散

**计算口径（v1.1 明确）**：基于**聚合前明细**（`coinValues` 各币 + USDT 余额）
计算，不受饼图「其他」桶聚合影响。

**示例**：

- 单币种 100%：HHI = 10000（极高）
- BTC 96.22% + 7 个小币种：HHI ≈ 9258（极高）
- 5 币种各 20%：HHI = 5×400 = 2000（中）

### 13.2 货币与中文对照

| Symbol | 中文名 | 用途 |
|----|----|----|
| USDT | 泰达币 | 计价、稳定币 |
| BTC | 比特币 | 主流币 |
| ETH | 以太坊 | 主流币 |
| 其他 | 各类山寨币 | 视情况 |

### 13.3 参考资料

- **WorkbenchFX 官方文档**：https://github.com/dlsc-software-consulting-gmbh/WorkbenchFX
- **JavaFX 21 文档**：https://openjfx.io/javadoc/21/
- **Material Design 颜色规范**：https://m3.material.io/styles/color/the-color-system/key-colors-tones
- **HHI 解释**：https://en.wikipedia.org/wiki/Herfindahl%E2%80%93Hirschman_index
- **原型文件**：`docs/piechart-mockup.html`

### 13.4 文档维护

- **文档位置**：`docs/plans/piechart-redesign.md`（本文件）
- **更新触发**：每次 `modules/piechart/` 下文件改动时同步更新
- **评审频率**：每季度一次
- **负责人**：模块作者 + UI 负责人

### 13.5 变更记录

| 版本 | 日期 | 作者 | 摘要 |
|----|----|----|----|
| v1.0 | 2025-08-20 | AI Assistant | 初版方案文档 |
| v1.1 | 2025-08-20 | AI Assistant | 依据代码库核对结果修订：单 FXML 替代 4 子文件拆分；新增 §6.4 成本口径计算；勘误 24h/7d 字段已存在；主题改 stylesheet 替换机制；修正 `ToggleButtonGroup`/`-fx-margin`/effect 变量/`SetChangeListener` 等非法 API；切片联动改用 `nodeProperty` + symbol 匹配；单位切换移出版本范围；灰度默认方案 B；验收标准改为可操作指标 |

---

## 附录：HTML 原型使用说明

```bash
# 1. 本地浏览器打开
xdg-open docs/piechart-mockup.html

# 2. 远程评审（启动本地服务）
python3 -m http.server 8000 --directory docs
# 访问 http://[your-ip]:8000/piechart-mockup.html

# 3. 嵌入 README
# GitHub / GitLab 的 Markdown 支持直接渲染 HTML 链接
[查看原型](docs/piechart-mockup.html)
```

**原型功能**：

- 顶部控制条可切换：浅色 / 深色、显示 / 隐藏标注
- 4 个黄色便签标注了核心改造点（数字 1-4 对应本文档第 5.1 节）
- 移除标注后即可作为团队评审的最终视觉稿

**注意**：原型中使用了 FontAwesome CDN 图标，**仅用于 HTML 演示**。JavaFX 实现时改用
`org.kordamp.ikonli.javafx.FontIcon` + `MaterialDesign.MDI_*` 枚举，**不引入新依赖**。
原型与 v1.1 方案的出入点（单位切换、工具栏控件形态）以本文档为准，Sprint 4 同步。
