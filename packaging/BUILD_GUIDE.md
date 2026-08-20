# WuZhu Windows 打包指南

本文档记录 WuZhu 应用在 Windows 11 下的完整打包流程。

> ⚠️ **历史遗留文档已重写**：本文档早期描述“fat jar + app-image 两步法”，已被证明会导致 `failed to launch JVM`。
> **当前正确流程请以 [PACKAGING_WINDOWS.md](./PACKAGING_WINDOWS.md) 和仓库内的 `package-windows.ps1` 为准**，本页只做速览与排错。

## 正确做法（一句话）

用仓库自带的 PowerShell 脚本（与 GitHub Actions 完全一致），**不要**手动拼 jpackage 参数：

```powershell
# 以管理员身份运行 PowerShell，在仓库根目录
.\packaging\package-windows.ps1 -AppVersion 1.0.5
```

该脚本内部已完成所有“避免 `failed to launch JVM`”的关键处理：

| 关键点 | 做法 |
|---|---|
| 主 jar | 用 **薄 jar**（`target\WuZhu-1.0.jar.original`）复制为 `target\dependency\WuZhu-1.0.jar` |
| main-class | `org.lifxue.wuzhu.WuZhuApplication` |
| 内置 runtime | `jlink` 自建 `target\custom-jre`（含 `bin\java.exe`），`--runtime-image` 指定 |
| JavaFX 模块 | `--java-options "--add-modules" "javafx.base,...,javafx.swing"` |
| 依赖 scope | `-DincludeScope=runtime`（含 h2 驱动），排除 devtools/configuration-processor |

## 环境要求

- JDK 21 with JavaFX（推荐 BellSoft Liberica，CI 用 `java-package: jdk+fx`）
- WiX Toolset 3.x（`choco install wixtoolset -y`）
- Maven（`./mvnw`）

## 手动打包（不推荐，仅调试用）

> 以下命令仅用于**理解**打包参数，日常发布请用脚本或 GitHub Actions（见 [DEVELOPMENT_RELEASE.md](../docs/DEVELOPMENT_RELEASE.md) §2.2）。

```bash
# 1. 编译
./mvnw.cmd clean package -DskipTests -q

# 2. 复制依赖（-D 参数必须加引号！）
./mvnw.cmd dependency:copy-dependencies "-DincludeScope=runtime" "-DexcludeGroupIds=org.openjfx" "-DexcludeArtifactIds=spring-boot-devtools,spring-boot-configuration-processor" -q
Copy-Item target\WuZhu-1.0.jar.original target\dependency\WuZhu-1.0.jar

# 3. jlink 自建 runtime（含 java.exe 启动器）
# 注意：必须包含 jdk.crypto.ec（TLS/ECDHE 所需）。缺失时 HTTPS 请求会报
#   "(unexpected_message) Received close_notify during handshake"（v1.0.4 已踩坑）。
jlink --module-path "$env:JAVA_HOME\jmods" `
  --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing,jdk.localedata,jdk.crypto.ec,jdk.crypto.cryptoki `
  --output target\custom-jre --strip-debug --no-man-pages --no-header-files --compress=2

# 4. jpackage 打 MSI
jpackage --type msi --name WuZhu --app-version 1.0.5 --vendor "lifxue" `
  --main-jar WuZhu-1.0.jar --main-class org.lifxue.wuzhu.WuZhuApplication `
  --input target\dependency --dest target\dist --runtime-image target\custom-jre `
  --icon src\main\resources\org\lifxue\wuzhu\images\wuzhu-96.ico `
  --win-menu --win-menu-group WuZhu --win-shortcut --win-dir-chooser --win-per-user-install `
  --java-options "-Dfile.encoding=UTF-8" `
  --java-options "--add-modules" `
  --java-options "javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing"
```

输出：`target\dist\WuZhu-1.0.5.msi`

## 常见问题

### 1. 安装后的 WuZhu.exe 弹窗 `failed to launch JVM`

按历史排障，共 3 个根因（均已修复并固化在脚本中）：

| 根因 | 检查 | 对应修复 |
|---|---|---|
| 用了 fat jar | `app\WuZhu-1.0.jar` 有 `BOOT-INF/`，体积 >10MB | 用薄 jar `WuZhu-1.0.jar.original`（~5MB） |
| runtime 缺 `java.exe` | `runtime\bin\java.exe` 不存在 | 用 `jlink` 自建 runtime + `--runtime-image` |
| 缺 h2 驱动 | 启动后报 `Cannot load driver class: org.h2.Driver` | `-DincludeScope=runtime` 复制依赖 |

> 诊断技巧：直接调用内置 JVM 拿真实报错，而非只看弹窗（详见 [DEVELOPMENT_RELEASE.md](../docs/DEVELOPMENT_RELEASE.md) §4.1）。

### 2. `WiX tools not found` / `candle` 找不到

**解决**：安装 WiX Toolset 3.x 并加入 PATH，或直接用脚本（CI 中 `choco install wixtoolset -y`）。

### 3. PowerShell 调 mvnw.cmd 报 `Unknown lifecycle phase .openjfx`

**解决**：`-D` 参数必须加引号：`"-DexcludeGroupIds=org.openjfx"`（详见 [DEVELOPMENT_RELEASE.md](../docs/DEVELOPMENT_RELEASE.md) §3.2）。

### 4. 生成的安装包太大

- MSI 约 100+MB（包含 jlink 精简 runtime + JavaFX）。
- 已经用 jlink 精简过；如需进一步缩小可裁剪 `jdk.localedata`（不推荐，影响多语言）。

## 安装包信息

| 格式 | 文件 | 说明 |
|---|---|---|
| MSI | `target\dist\WuZhu-1.0.5.msi` | 标准安装包（per-user），CI 自动发布 |

## 相关文档

- [Windows 打包详细指南（权威）](./PACKAGING_WINDOWS.md)
- [Ubuntu 打包指南](./PACKAGING_UBUNTU.md)
- [开发与发布流程（权威，含发布步骤）](../docs/DEVELOPMENT_RELEASE.md)
- [开发环境搭建](./DEVELOPMENT.md)
