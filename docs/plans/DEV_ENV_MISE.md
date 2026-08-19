# WuZhu 开发环境配置方案（WSL2 + Ubuntu 24.04 + mise）

> 目标：在 WSL2 + Ubuntu 24.04 下，用 **mise** 统一管理 JDK（含 JavaFX）与 Maven，支撑本 JavaFX 项目的日常开发、构建与打包。
> 生成时间：2025-08-19

---

## 一、背景与现状

### 1.1 当前环境（已验证）

| 项 | 现状 |
|----|------|
| 操作系统 | WSL2 + Ubuntu 24.04.4 LTS（kernel 6.6.87.2-microsoft-standard-WSL2） |
| mise | ✅ 已安装 `/home/lifxu/.local/bin/mise`，版本 2026.7.13 |
| mise 全局配置 | `~/.config/mise/config.toml`（含 gh/node/python/pnpm 等） |
| JDK | ❌ **未安装**（`java` 不在 PATH） |
| Maven | ❌ 未安装（有 `./mvnw` wrapper，但 wrapper 需要 JDK 才能启动） |
| WSLg | ✅ 已启用（`/mnt/wslg` 存在，DISPLAY=:0）→ **GUI 应用（JavaFX）可在 WSL2 中直接显示窗口** |
| gh CLI | ✅ 由 mise 管理，已登录 `mapleafly`（具 repo/workflow 权限） |
| 图形库 | ✅ libgtk-3 / libgl1 / mesa 已安装（JavaFX 运行所需） |

> 结论：本机已具备 WSLg 图形能力，缺的只是 JDK 与 Maven。

### 1.2 项目技术栈对 JDK 的要求

| 需求 | 说明 |
|------|------|
| Java 21 | `pom.xml` `<java.version>21</java.version>` |
| **JavaFX 模块** | 运行与打包（jlink/jpackage）都需要 javafx 模块，普通 OpenJDK **不含** JavaFX |
| 完整 JDK（非 JRE） | 需要 `jpackage` / `jlink` / `javac` |
| Maven 3.8+ | 项目自带 wrapper（3.8.6），但推荐直接用 mise 管理 Maven |

---

## 二、核心结论：mise 可以安装 BellSoft Liberica JDK 21 Full ✅

经查证（mise 官方 java 核心插件源码 `src/plugins/core/java.rs` 及数据源 `mise-java.jdx.dev` 元数据）：

- mise 的 java 插件支持 **Liberica** 厂商，版本名为 `liberica-21.0.x`。
- 同一版本号下存在**多个构建变体**（`features` 字段区分），mise 会用特征后缀来区分版本名：
  - `liberica-21.0.7+9` → 标准版（`bellsoft-jdk21.0.7+9-linux-amd64.tar.gz`）
  - **`liberica-javafx-21.0.7+9` → Full 版（含 JavaFX，`bellsoft-jdk21.0.7+9-linux-amd64-full.tar.gz`）** ← **本项目需要的就是这个**
  - `liberica-lite-21.0.7+9` → Lite 版
  - `liberica-nik-*` → Liberica NIK（Native Image Kit，本项目用不到）

**实测证据**：
```bash
# 该版本确实存在于 mise 远程版本列表
mise ls-remote java | grep -E "^liberica-javafx-21"
# 输出：
liberica-javafx-21.0.7+9
liberica-javafx-21.0.8+12
liberica-javafx-21.0.9+11
liberica-javafx-21.0.10+10
liberica-javafx-21.0.11+11
liberica-javafx-21.0.12+10   # ← 当前最新
```

**元数据 URL 证据**（`https://mise-java.jdx.dev/jvm/ga/linux/x86_64.json`）：
```json
{ "version": "21.0.7+9", "features": ["javafx","libericafx","minimal-vm"],
  "url": "https://github.com/bell-sw/Liberica/releases/download/21.0.7%2B9/bellsoft-jdk21.0.7%2B9-linux-amd64-full.tar.gz" }
```

> ✅ **结论：`java@liberica-javafx-21` 即 BellSoft Liberica JDK 21 Full（内置 JavaFX 模块），mise 完全支持安装与管理。** 安装后 `java --list-modules` 应能看到 `javafx.controls`、`javafx.fxml`、`javafx.web`、`javafx.media`、`javafx.swing` 等模块。

---

## 三、推荐方案：项目级 `.mise.toml` + 全局 maven

### 3.1 设计思路

| 工具 | 放置层级 | 理由 |
|------|---------|------|
| JDK (`liberica-javafx-21`) | **项目级** `.mise.toml` | 锁定到本项目所需版本，避免影响其他项目 |
| Maven | **全局** `~/.config/mise/config.toml` | 通用构建工具，全局一份即可 |
| gh | 已有（全局） | 用于发布流程 |

### 3.2 需要创建/修改的文件

**① 项目根目录新建 `.mise.toml`**（待你确认后创建，本方案仅给出内容）：

```toml
[tools]
java = "liberica-javafx-21.0.12+10"
```

> 若希望跟随最新 21 补丁：`java = "liberica-javafx-21"`（mise 会解析为该主版本最新版）。

**② 全局 `~/.config/mise/config.toml` 追加**（已有该文件，追加 maven 即可）：

```toml
[tools]
maven = "3.9.16"
```

### 3.3 需要执行的命令（一次性）

```bash
# 1. 在项目目录安装并激活项目 JDK（会自动写入 .mise.toml）
cd /home/lifxu/src/WuZhu
mise install          # 读取 .mise.toml 安装所需工具
mise use -g maven@3.9.16   # 全局安装 Maven（若选择全局方案）
mise use java@liberica-javafx-21.0.12+10  # 写入项目级 .mise.toml

# 2. 验证
java -version                 # 应显示 21.0.x
java --list-modules | grep javafx   # 应列出 javafx.* 模块
mvn -version                  # 应显示 Apache Maven 3.9.x
jpackage --version            # 应显示 jpackage 版本（JDK 自带）
```

### 3.4 关键：mise activate（让 JAVA_HOME 自动生效）

- mise **shims 不会设置 `JAVA_HOME` 等环境变量**，必须在 shell 启动文件中启用 `mise activate`。
- 检查 `~/.bashrc` 是否已有以下行；若没有，需要追加（待你确认后执行）：

```bash
# ~/.bashrc 或 ~/.zshrc
eval "$(mise activate bash)"    # bash
# 或 zsh: eval "$(mise activate zsh)"
```

- 启用后，进入项目目录时 mise 会自动把 `JAVA_HOME` 指向 `~/.local/share/mise/installs/java/liberica-javafx-21.0.12+10`，并把 `java`/`jpackage`/`javac` 加入 PATH。
- **IDE（IDEA/VS Code）**：如果 IDE 在启动时读取 `JAVA_HOME`，切换版本后需重启 IDE。也可以直接在 IDE 的 JDK 配置里指向 mise 的安装路径。

### 3.5 版本选择说明

| 版本 | 说明 |
|------|------|
| `liberica-javafx-21.0.12+10` | 当前（元数据）最新 Liberica 21 Full |
| `liberica-javafx-21.0.7+9` | 与文档中写死的 21.0.7 接近，但非最新 |
| `21`（不带 vendor 前缀） | mise 默认走 OpenJDK，**不含 JavaFX**，本项目不适用 |
| `liberica-21`（不带 javafx） | 标准版，**不含 JavaFX**，运行/打包会缺模块 |

> ⚠️ 不要用 `java@21` 或 `java@liberica-21`（都不含 JavaFX）。**必须带 `-javafx` 特征后缀。**

---

## 四、JavaFX 运行/打包的注意事项

### 4.1 本地运行

```bash
# 方式一：Spring Boot 插件（开发推荐）
./mvnw spring-boot:run

# 方式二：JavaFX 插件
./mvnw javafx:run

# 方式三：打包后运行
./mvnw clean package -DskipTests
java -jar target/WuZhu-1.0.jar
```

> WSLg 已就绪，JavaFX 窗口可直接显示。若遇到图形异常，可确认 `libgtk-3-0`、`libgl1-mesa-glx`、`libx11-xcb1` 已安装（本机已具备）。

### 4.2 使用 mise JDK 时的 Maven 编译

mise 提供 java 后，`./mvnw` 会自动使用 `JAVA_HOME`（来自 mise activate）。也可以用：

```bash
mise exec -- ./mvnw clean package -DskipTests
# 或
mise run <自定义task>   # 若在 .mise.toml 中配置 [tasks]
```

### 4.3 GitHub Actions 里的等价配置（供 CI 参考）

`actions/setup-java@v4` **没有 `javafx: true` 参数**（项目 packaging/README.md 中写错了），正确的 JavaFX 方式是 `java-package: jdk+fx`：

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: liberica
    java-version: '21'
    java-package: jdk+fx   # ← 等价于 Liberica Full（含 JavaFX）
    cache: maven
```

---

## 五、落地步骤清单（待确认后执行）

| # | 动作 | 类型 |
|---|------|------|
| 1 | `~/.bashrc` 追加 `eval "$(mise activate bash)"` | 环境（一次性） |
| 2 | 项目根新建 `.mise.toml`，写入 `[tools] java = "liberica-javafx-21.0.12+10"` | 项目文件（新建） |
| 3 | `mise use -g maven@3.9.16` | 环境（一次性） |
| 4 | 运行 `mise install` + 验证命令（§3.3） | 环境（一次性） |
| 5 | （可选）把 `.mise.toml` 提交进仓库，团队统一 JDK | 项目文件 |
| 6 | （可选）配置 IDE 使用 mise 安装的 JDK | IDE 配置 |

> 说明：本方案**不修改**现有 pom.xml、脚本等任何项目代码；只涉及"新建 .mise.toml / 改 shell 配置 / 装工具"这三类动作，均待你确认后再执行。

---

## 六、常见问题（FAQ）

| 问题 | 说明 |
|------|------|
| `mise ls-remote java` 很慢 | 首次会拉取元数据，之后有缓存；是正常的 |
| 缓存目录只读报错 | 本环境 `~/.cache/mise/java` 出现过 read-only 警告，属沙箱限制，不影响功能 |
| 为什么不用 `sudo apt install openjdk-21-jdk` | Ubuntu 24.04 官方源不一定有 openjdk-21，且标准 OpenJDK 不含 JavaFX，还需要额外装 JavaFX jmods，绕远了 |
| 为什么不用 sdkman | 可以，但既然已在用 mise 管理其它工具，统一用 mise 更一致 |
| JAR 打包体积大 | 与 JDK 无关，属 jpackage 捆绑运行时的问题，见 `WINDOWS_PACKAGING_WSL2.md` |
