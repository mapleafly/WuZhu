# Windows 11 打包指南

将 WuZhu 打包为 Windows 安装程序（.msi 或 .exe）。

## 打包方式概览

| 方式 | 输出格式 | 优点 | 缺点 |
|------|----------|------|------|
| **MSI** | .msi | 标准安装包，支持卸载/升级，企业部署友好 | 需要 WiX 工具 |
| **EXE** | .exe | 单文件，用户体验好 | 需要 WiX 工具 |
| 绿色版 | .zip | 无需安装，解压即用 | 无开始菜单/快捷方式 |

## 环境准备

### 必需软件

1. **JDK 21 with JavaFX**（推荐 BellSoft Liberica）
2. **WiX Toolset 3.x**（重要：必须是 3.x，不是 4.x）
3. **Maven 3.8+**

### 安装步骤

#### 1. 安装 JDK 21（含 JavaFX）

**方案 A：使用 BellSoft Liberica（推荐）**

```powershell
# 下载 Liberica JDK 21 Full（包含 JavaFX）
# https://bell-sw.com/pages/downloads/

# 或使用 Chocolatey
choco install liberica21jdkfull
```

**方案 B：使用其他 JDK + JavaFX SDK**

如果使用不带 JavaFX 的 JDK，需要额外下载 JavaFX SDK：

```powershell
# 下载 JavaFX SDK
# https://gluonhq.com/products/javafx/
```

#### 2. 安装 WiX Toolset 3.14

```powershell
# 方案 1：使用 Chocolatey（推荐）
choco install wixtoolset

# 方案 2：手动下载安装
# https://github.com/wixtoolset/wix3/releases
# 下载 wix314.exe 并安装
```

**验证 WiX 安装**：

```powershell
# 检查 candle.exe 和 light.exe 是否在 PATH 中
candle -?
light -?

# 如果没有，手动添加到 PATH
# 默认位置：C:\Program Files (x86)\WiX Toolset v3.14\bin
```

#### 3. 验证环境

```powershell
# 检查 Java
java -version
# openjdk version "21.0.x" 202x-xx-xx

# 检查 jpackage
jpackage --version
# 应显示版本号

# 检查 WiX
candle -?
# 应显示 WiX 帮助信息
```

## 方式一：使用 jpackage 创建 MSI 安装包（推荐）

### 步骤 1：编译项目

打开 PowerShell，进入项目目录：

```powershell
cd C:\path\to\WuZhu

# 清理并编译
.\mvnw.cmd clean package -DskipTests
```

### 步骤 2：复制依赖项

```powershell
# 复制所有依赖到 target/dependency
.\mvnw.cmd dependency:copy-dependencies -DincludeScope=compile -DexcludeGroupIds=org.openjfx

# 复制主 JAR
copy target\WuZhu-1.0.jar target\dependency\
```

### 步骤 3：使用 jpackage 创建 MSI

```powershell
# 生成 UUID（每个应用固定一个，用于升级检测）
# 在线生成：https://www.uuidgenerator.net/
# 示例：12345678-1234-1234-1234-123456789abc

jpackage `
  --type msi `
  --name WuZhu `
  --app-version 1.0.0 `
  --vendor "lifxue" `
  --description "WuZhu - 加密货币交易记录和分析工具" `
  --copyright "Copyright 2023-2025 lifxue" `
  --main-jar WuZhu-1.0.jar `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --input target\dependency `
  --dest target\dist `
  --icon src\main\resources\org\lifxue\wuzhu\images\wuzhu-96.ico `
  --win-menu `
  --win-menu-group WuZhu `
  --win-shortcut `
  --win-dir-chooser `
  --win-per-user-install `
  --win-upgrade-uuid "YOUR-UUID-HERE" `
  --java-options "-Dfile.encoding=UTF-8" `
  --java-options "-Dspring.backgroundpreinitializer.ignore=true"
```

### 步骤 4：验证安装包

```powershell
# 查看生成的 MSI
dir target\dist\*.msi

# 查看 MSI 信息（需要安装 WiX）
msiinfo.exe target\dist\WuZhu-1.0.0.msi
```

### 步骤 5：测试安装

```powershell
# 静默安装
msiexec /i target\dist\WuZhu-1.0.0.msi /qn

# 或双击运行安装向导
# 安装后在开始菜单搜索 "WuZhu"
```

### 卸载

```powershell
# 命令行卸载
msiexec /x WuZhu-1.0.0.msi /qn

# 或在 设置 > 应用 中卸载
```

## 方式二：创建 EXE 安装包

```powershell
jpackage `
  --type exe `
  --name WuZhu `
  --app-version 1.0.0 `
  --vendor "lifxue" `
  --description "WuZhu - 加密货币交易记录和分析工具" `
  --main-jar WuZhu-1.0.jar `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --input target\dependency `
  --dest target\dist `
  --icon src\main\resources\org\lifxue\wuzhu\images\wuzhu-96.ico `
  --win-menu `
  --win-shortcut `
  --win-dir-chooser `
  --win-per-user-install `
  --win-upgrade-uuid "YOUR-UUID-HERE" `
  --java-options "-Dfile.encoding=UTF-8"
```

## 方式三：创建绿色版（ZIP 压缩包）

### 步骤 1：创建精简 JRE

```powershell
# 创建自定义 JRE（包含 JavaFX）
jlink `
  --module-path "$env:JAVA_HOME\jmods" `
  --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing,jdk.localedata `
  --output target\WuZhu-windows\runtime `
  --strip-debug `
  --no-man-pages `
  --no-header-files `
  --compress=2

# 或使用模块化方式（更快）
jlink `
  --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,javafx.controls,javafx.fxml,javafx.web,jdk.localedata `
  --output target\WuZhu-windows\runtime `
  --strip-debug `
  --no-man-pages `
  --compress=2
```

### 步骤 2：复制应用文件

```powershell
# 创建应用目录结构
New-Item -ItemType Directory -Force -Path target\WuZhu-windows\app

# 复制 JAR
copy target\WuZhu-1.0.jar target\WuZhu-windows\app\

# 创建启动脚本（WuZhu.bat）
$batchContent = @'
@echo off
set SCRIPT_DIR=%~dp0
"%SCRIPT_DIR%runtime\bin\java.exe" -jar "%SCRIPT_DIR%app\WuZhu-1.0.jar" %*
'@

$batchContent | Out-File -FilePath target\WuZhu-windows\WuZhu.bat -Encoding ASCII
```

### 步骤 3：打包为 ZIP

```powershell
cd target
Compress-Archive -Path WuZhu-windows -DestinationPath WuZhu-1.0.0-windows.zip -Force
```

### 使用绿色版

1. 解压 `WuZhu-1.0.0-windows.zip`
2. 双击 `WuZhu.bat` 运行

## 完整打包脚本

创建 `package-windows.ps1`：

```powershell
#!/usr/bin/env pwsh
#Requires -Version 5.1

$ErrorActionPreference = "Stop"

Write-Host "=== WuZhu Windows 打包脚本 ===" -ForegroundColor Cyan

# 检查环境
Write-Host "检查环境..." -ForegroundColor Yellow

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "jpackage 未找到，请确保 JDK 21+ 已安装并在 PATH 中"
    exit 1
}

if (-not (Get-Command candle -ErrorAction SilentlyContinue)) {
    Write-Warning "WiX candle 未找到，MSI 打包将失败"
    Write-Host "请安装 WiX Toolset 3.x: https://wixtoolset.org/releases/" -ForegroundColor Red
}

# 清理
Write-Host "清理旧文件..." -ForegroundColor Yellow
.\mvnw.cmd clean

# 编译
Write-Host "编译项目..." -ForegroundColor Yellow
.\mvnw.cmd package -DskipTests

# 复制依赖
Write-Host "复制依赖..." -ForegroundColor Yellow
.\mvnw.cmd dependency:copy-dependencies -DincludeScope=compile -DexcludeGroupIds=org.openjfx
copy target\WuZhu-1.0.jar target\dependency\

# 创建 MSI
Write-Host "创建 MSI 安装包..." -ForegroundColor Yellow

$upgradeUuid = "12345678-1234-1234-1234-123456789abc"  # 替换为你的 UUID

jpackage `
  --type msi `
  --name WuZhu `
  --app-version 1.0.0 `
  --vendor "lifxue" `
  --description "WuZhu - 加密货币交易记录和分析工具" `
  --copyright "Copyright 2023-2025 lifxue" `
  --main-jar WuZhu-1.0.jar `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --input target\dependency `
  --dest target\dist `
  --icon src\main\resources\org\lifxue\wuzhu\images\wuzhu-96.ico `
  --win-menu `
  --win-menu-group WuZhu `
  --win-shortcut `
  --win-dir-chooser `
  --win-per-user-install `
  --win-upgrade-uuid $upgradeUuid `
  --java-options "-Dfile.encoding=UTF-8"

Write-Host "=== 打包完成 ===" -ForegroundColor Green
Write-Host "安装包位置: target\dist\WuZhu-1.0.0.msi" -ForegroundColor Green

# 显示文件信息
if (Test-Path target\dist\WuZhu-1.0.0.msi) {
    $fileInfo = Get-Item target\dist\WuZhu-1.0.0.msi
    Write-Host "文件大小: $([math]::Round($fileInfo.Length / 1MB, 2)) MB" -ForegroundColor Green
}
```

运行脚本：

```powershell
# 设置执行策略（首次使用 PowerShell 脚本需要）
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 运行打包脚本
.\package-windows.ps1
```

## 高级配置

### 使用 Maven 自动打包

在 `pom.xml` 中添加 profiles 支持：

```xml
<profiles>
    <!-- Windows 打包配置 -->
    <profile>
        <id>windows</id>
        <activation>
            <os>
                <family>windows</family>
            </os>
        </activation>
        <build>
            <plugins>
                <!-- 复制依赖 -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-dependency-plugin</artifactId>
                    <version>3.6.1</version>
                    <executions>
                        <execution>
                            <id>copy-dependencies</id>
                            <phase>package</phase>
                            <goals>
                                <goal>copy-dependencies</goal>
                            </goals>
                            <configuration>
                                <includeScope>compile</includeScope>
                                <excludeGroupIds>org.openjfx</excludeGroupIds>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>

                <!-- jpackage 打包 -->
                <plugin>
                    <groupId>org.panteleyev</groupId>
                    <artifactId>jpackage-maven-plugin</artifactId>
                    <version>1.7.4</version>
                    <executions>
                        <execution>
                            <id>jpackage-msi</id>
                            <phase>package</phase>
                            <goals>
                                <goal>jpackage</goal>
                            </goals>
                            <configuration>
                                <name>WuZhu</name>
                                <appVersion>1.0.0</appVersion>
                                <vendor>lifxue</vendor>
                                <description>WuZhu - 加密货币交易记录和分析工具</description>
                                <mainJar>WuZhu-1.0.jar</mainJar>
                                <mainClass>org.springframework.boot.loader.launch.JarLauncher</mainClass>
                                <input>${project.build.directory}/dependency</input>
                                <destination>${project.build.directory}/dist</destination>
                                <type>MSI</type>
                                <icon>${project.basedir}/src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico</icon>
                                <winMenu>true</winMenu>
                                <winMenuGroup>WuZhu</winMenuGroup>
                                <winShortcut>true</winShortcut>
                                <winDirChooser>true</winDirChooser>
                                <winPerUserInstall>true</winPerUserInstall>
                                <winUpgradeUuid>YOUR-UUID-HERE</winUpgradeUuid>
                                <javaOptions>
                                    <option>-Dfile.encoding=UTF-8</option>
                                </javaOptions>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

使用 Maven 打包：

```powershell
# Windows 环境下自动激活 windows profile
.\mvnw.cmd clean package -Pwindows
```

## 签名安装包（可选）

对于生产发布，建议对安装包进行数字签名：

```powershell
# 使用 signtool（需要 Windows SDK）
signtool sign `
  /f "certificate.pfx" `
  /p "password" `
  /tr http://timestamp.digicert.com `
  /td sha256 `
  /fd sha256 `
  target\dist\WuZhu-1.0.0.msi
```

## 发布到 GitHub Releases

```powershell
# 使用 GitHub CLI
gh release create v1.0.0 `
  target\dist\WuZhu-1.0.0.msi `
  target\WuZhu-1.0.0-windows.zip `
  --title "WuZhu v1.0.0" `
  --notes "Windows 11 安装包"
```

## 常见问题

### 1. "WiX tools not found"

**解决**：
```powershell
# 检查 WiX 是否安装
Test-Path "C:\Program Files (x86)\WiX Toolset v3.14\bin\candle.exe"

# 添加到 PATH
$env:Path += ";C:\Program Files (x86)\WiX Toolset v3.14\bin"
[Environment]::SetEnvironmentVariable("Path", $env:Path, "User")
```

### 2. "jpackage 不是可识别的命令"

**解决**：
```powershell
# 确保使用 JDK（不是 JRE）
# 检查 JAVA_HOME
$env:JAVA_HOME

# 如果没有，手动设置
$env:JAVA_HOME = "C:\Program Files\BellSoft\LibericaJDK-21-Full"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### 3. 打包后应用无法启动

**解决**：
```powershell
# 检查是否缺少依赖
# 确保所有依赖都在 target/dependency 中
dir target\dependency\*.jar | Measure-Object

# 检查主类名是否正确
jar tf target\WuZhu-1.0.jar | findstr "META-INF/MANIFEST.MF"
```

### 4. 中文显示乱码

确保打包时添加：
```powershell
--java-options "-Dfile.encoding=UTF-8"
```

## 相关文档

- [开发运行指南](./DEVELOPMENT.md)
- [Ubuntu 打包指南](./PACKAGING_UBUNTU.md)
- [WiX Toolset 文档](https://wixtoolset.org/documentation/manual/v3/)
- [Oracle jpackage 文档](https://docs.oracle.com/en/java/javase/21/jpackage/packaging-tool-user-guide.pdf)
