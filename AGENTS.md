# WuZhu 项目知识库

**生成时间:** 2025-05-01  
**提交:** `6fe8ce3`  
**分支:** master

## 项目概述

**WuZhu** 是一款加密货币工具，用于记录交易信息和分析加密货币数据。

这是一个 **Spring Boot + JavaFX** 混合架构的桌面应用程序，使用 H2 嵌入式数据库存储数据，通过 CoinMarketCap API 获取加密货币市场数据。

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 2.7.10 | 应用框架 |
| JavaFX | 21.0.2 | UI 框架 |
| WorkbenchFX | 11.3.1 | 工作台框架 |
| H2 Database | 2.2.220 | 嵌入式数据库 |
| OpenFeign | 2021.0.3 | HTTP 客户端 |
| Lombok | 1.18.30 | 代码生成 |
| MapStruct | 1.5.5 | 对象映射 |
| RichTextFX | 0.11.0 | 富文本编辑 |

## 项目结构

```
WuZhu/
├── src/main/java/org/lifxue/wuzhu/
│   ├── config/           # 配置类 (Feign、代理)
│   ├── convert/          # MapStruct 转换器
│   ├── dto/              # 数据传输对象
│   ├── enums/            # 枚举类
│   ├── modules/          # 功能模块 (8个业务模块)
│   │   ├── cash/         # 现金管理
│   │   ├── file/         # 导入导出
│   │   ├── note/         # 笔记 (富文本)
│   │   ├── piechart/     # 饼图
│   │   ├── selectcoin/   # 币种选择
│   │   ├── setting/      # 偏好设置
│   │   ├── statistics/   # 统计分析
│   │   └── tradeinfo/    # 交易信息
│   ├── pojo/             # JPA 实体类
│   ├── repository/       # 数据访问层
│   ├── service/          # 服务层
│   │   ├── feignc/       # Feign 客户端
│   │   └── impl/         # 服务实现
│   ├── springfx/         # Spring-JavaFX 集成
│   ├── themes/           # 主题配置
│   └── util/             # 工具类
└── src/main/resources/   # FXML、CSS、配置
```

## 快速导航

| 任务 | 位置 | 说明 |
|------|------|------|
| 启动入口 | `WuZhuApplication.java` | Spring Boot + JavaFX 启动类 |
| JavaFX 集成 | `springfx/` | Stage 初始化和事件处理 |
| 数据库实体 | `pojo/` | JPA 实体定义 |
| API 客户端 | `service/feignc/` | CoinMarketCap API 调用 |
| 界面控制器 | `modules/*/ViewController.java` | FXML 控制器 |

## 开发约定

### 1. 依赖顺序（重要）
```xml
<!-- lombok要放在mapstruct前面 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
```

### 2. 依赖注入
使用 **Setter 注入**（而非构造器注入）：
```java
@Autowired
public void setRepository(Repository repo) {
    this.repository = repo;
}
```

### 3. VO 类模式
视图对象使用 JavaFX Property 支持 UI 绑定：
```java
public class XxxVO {
    private final SimpleStringProperty name;
    public SimpleStringProperty nameProperty() { return name; }
}
```

### 4. 模块结构
每个模块必须包含：
- `XxxViewController.java` - FXML 控制器 (`@FxmlView`)
- `XxxViewModule.java` - 模块定义
- `vo/` - 视图对象

## 常用命令

```bash
# 构建项目
./mvnw clean package

# 跳过测试构建
./mvnw clean package -DskipTests

# 运行测试
./mvnw test

# 仅编译
./mvnw compile

# 清理
./mvnw clean

# 运行 (打包后)
java -jar target/WuZhu-1.0.jar
```

## 配置说明

### 数据库
- **类型**: H2 嵌入式
- **文件位置**: `~/.wuzhu/h2/wuzhudbjpa`
- **模式**: `ddl-auto: update`

### API 配置
需要在 `application.yml` 中配置 CoinMarketCap API Key：
```yaml
coin-market-cap:
  customHeader: X-CMC_PRO_API_KEY
```

### 代理设置
支持 HTTP 代理连接 CoinMarketCap：
```yaml
proxy:
  host: 127.0.0.1
  port: 53214
  domains: pro-api.coinmarketcap.com
```

## 注意事项

1. **非 Web 应用**: 配置中 `web-application-type: none`，这是纯桌面应用
2. **数据库文件**: H2 数据库存储在用户目录，重装应用数据不会丢失
3. **API Key**: 首次使用需要配置 CoinMarketCap API Key
4. **测试**: 当前测试覆盖率较低，仅有一个 Spring 上下文测试

## 启动流程

```
WuZhuApplication.main()
    ↓
Application.launch(JavaFxApplication.class)
    ↓
JavaFxApplication.init() → 初始化 Spring 上下文
    ↓
JavaFxApplication.start() → 发布 StageReadyEvent
    ↓
PrimaryStageInitializer → 初始化 WorkbenchFX + 加载所有模块
```

## 相关文件

- `pom.xml` - Maven 配置
- `src/main/resources/application.yml` - 应用配置
- `src/main/resources/org/lifxue/wuzhu/modules/*/*.fxml` - 界面定义
