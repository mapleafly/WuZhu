# WuZhu 开发运行指南

本文档说明如何在开发阶段运行 WuZhu 应用程序。

## 环境要求

### 必需软件

| 软件 | 最低版本 | 说明 |
|------|----------|------|
| Java JDK | 21 | 必须使用 JDK（不是 JRE） |
| Maven | 3.8+ | 使用 `./mvnw` 可免安装 |
| Git | 任意 | 克隆项目代码 |

### 验证环境

```bash
# 检查 Java 版本
java -version
# 应显示: openjdk version "21" 或更高

# 检查 Maven 版本
mvn -version
# 或
./mvnw -version
```

## 快速开始（跨平台通用）

### 1. 克隆项目

```bash
git clone <repository-url>
cd WuZhu
```

### 2. 编译项目

```bash
# 编译并打包（跳过测试）
./mvnw clean package -DskipTests

# 或完整构建（包含测试）
./mvnw clean package
```

### 3. 运行应用

**方式一：使用 Maven Spring Boot 插件（推荐开发使用）**

```bash
./mvnw spring-boot:run
```

**方式二：直接运行 JAR 文件**

```bash
java -jar target/WuZhu-1.0.jar
```

**方式三：使用 JavaFX Maven 插件（开发调试用）**

```bash
./mvnw javafx:run
```

## 开发工作流

### 日常开发命令

```bash
# 清理并编译（不打包）
./mvnw clean compile

# 仅运行测试
./mvnw test

# 打包（跳过测试，快速构建）
./mvnw clean package -DskipTests

# 完整构建流程
./mvnw clean compile test package
```

### 热重载开发

Spring Boot DevTools 已集成，支持代码修改后自动重启：

1. 以开发模式运行：
   ```bash
   ./mvnw spring-boot:run
   ```

2. 修改 Java 代码后，DevTools 会自动重新加载应用

3. 某些资源修改（如 FXML、CSS）可能需要手动重启

### 调试模式

```bash
# 启用调试（默认端口 5005）
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdlib=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

然后在 IDE 中配置 Remote Debug 连接到 localhost:5005

## 配置文件说明

### application.yml 位置

运行时配置文件优先级（从高到低）：
1. `./config/application.yml`（应用目录下的 config 文件夹）
2. `./application.yml`（应用目录）
3. `classpath:application.yml`（打包在 JAR 内）

### 开发环境配置建议

创建 `application-dev.yml`：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # 开发时每次重建数据库
    show-sql: true           # 显示 SQL 语句

logging:
  level:
    org.lifxue.wuzhu: DEBUG
```

使用开发配置运行：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 常见问题

### 1. Java 版本不匹配

**错误**: `Unsupported class file major version`

**解决**:
```bash
# 检查当前 Java 版本
java -version

# 如有多个 JDK，临时切换
export JAVA_HOME=/path/to/jdk21
export PATH=$JAVA_HOME/bin:$PATH
```

### 2. Maven 依赖下载失败

**解决**:
```bash
# 清理本地仓库后重试
./mvnw clean
rm -rf ~/.m2/repository/org/lifxue
./mvnw clean package
```

### 3. 端口冲突

如果 8080 端口被占用（虽然本应用不启动 Web 服务器），可以忽略。

### 4. 内存不足

```bash
# 增加 Maven 内存
export MAVEN_OPTS="-Xmx2g"
./mvnw clean package
```

## IDE 配置

### IntelliJ IDEA

1. 导入项目：`File -> Open` 选择 `pom.xml`
2. 启用注解处理：`Settings -> Build -> Annotation Processors -> Enable`
3. 设置 JDK：`Project Structure -> SDKs` 添加 JDK 21

### VS Code

推荐插件：
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support

### Eclipse

1. 导入 Maven 项目：`File -> Import -> Maven -> Existing Maven Projects`
2. 安装 Lombok 插件
3. 设置 Java Compiler 级别为 21

## 下一步

- [打包为安装文件（Ubuntu）](./PACKAGING_UBUNTU.md)
- [打包为安装文件（Windows）](./PACKAGING_WINDOWS.md)
