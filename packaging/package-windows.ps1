#!/usr/bin/env pwsh
#Requires -Version 5.1
#Requires -RunAsAdministrator

<#
.SYNOPSIS
    WuZhu Windows 打包脚本
.DESCRIPTION
    将 WuZhu 应用打包为 Windows MSI 安装程序
.PARAMETER AppVersion
    应用版本号（tag 驱动）。未指定时从环境变量 APP_VERSION 或最近 git tag 推导，默认 1.0.0。
.NOTES
    需要以管理员权限运行
    需要安装 WiX Toolset 3.x
#>

param(
    [string]$AppVersion = ""
)

$ErrorActionPreference = "Stop"

# 颜色定义
$Green = "`e[32m"
$Yellow = "`e[33m"
$Red = "`e[31m"
$Reset = "`e[0m"

Write-Host "${Yellow}=== WuZhu Windows 11 打包脚本 ===${Reset}"
Write-Host ""

# 版本号（tag 驱动）：命令行参数 > 环境变量 APP_VERSION > 最近 git tag > 默认 1.0.0
# 用法示例: .\package-windows.ps1 -AppVersion 1.0.1  或  APP_VERSION=1.0.1 .\package-windows.ps1
if (-not $AppVersion) {
    $AppVersion = $env:APP_VERSION
}
if (-not $AppVersion) {
    $gitTag = & git describe --tags --abbrev=0 2>$null
    if ($LASTEXITCODE -eq 0 -and $gitTag) {
        $AppVersion = $gitTag
    }
}
# 去掉前缀 v/V（如 v1.0.1 -> 1.0.1）
$AppVersion = $AppVersion -replace '^[vV]', ''
if ($AppVersion -notmatch '^\d+\.\d+\.\d+$') {
    $AppVersion = "1.0.0"
}
Write-Host "${Green}版本号: $AppVersion${Reset}"
Write-Host ""

# 检查环境
Write-Host "${Yellow}检查环境...${Reset}"

# 检查 Java
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "Java 未安装，请安装 JDK 21"
    exit 1
}

$javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
Write-Host "  Java: $javaVersion"

# 检查 jpackage
if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "jpackage 未找到，请确保使用的是 JDK（不是 JRE）"
    exit 1
}
Write-Host "  jpackage: 已找到"

# 检查 WiX
if (-not (Get-Command candle -ErrorAction SilentlyContinue)) {
    Write-Warning "WiX 工具未找到，尝试查找..."
    
    $wixPaths = @(
        "C:\Program Files (x86)\WiX Toolset v3.11\bin",
        "C:\Program Files (x86)\WiX Toolset v3.14\bin",
        "C:\Program Files\WiX Toolset v3.11\bin",
        "C:\Program Files\WiX Toolset v3.14\bin"
    )
    
    $found = $false
    foreach ($path in $wixPaths) {
        if (Test-Path "$path\candle.exe") {
            $env:Path += ";$path"
            $found = $true
            Write-Host "  WiX: 找到于 $path"
            break
        }
    }
    
    if (-not $found) {
        Write-Error "WiX Toolset 未安装。请从 https://wixtoolset.org/releases/ 下载安装"
        exit 1
    }
} else {
    Write-Host "  WiX: 已找到"
}

Write-Host "${Green}✓ 环境检查通过${Reset}"
Write-Host ""

# 清理
Write-Host "${Yellow}清理旧文件...${Reset}"
& .\mvnw.cmd clean -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven 清理失败"
    exit 1
}

# 编译
Write-Host "${Yellow}编译项目...${Reset}"
& .\mvnw.cmd package "-DskipTests" -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven 编译失败"
    exit 1
}

# 复制依赖
# 注意：PowerShell 调用 mvnw.cmd 时，-D 参数必须加引号，
# 否则 -DexcludeGroupIds=org.openjfx 会被 cmd.exe 拆成 ".openjfx" 导致
# Maven 报 "Unknown lifecycle phase .openjfx"。
Write-Host "${Yellow}复制依赖...${Reset}"
# 说明：.
#   - includeScope 用 runtime（Maven 默认 runtime = compile + runtime）：
#     不能只 copy compile，h2 数据库驱动是 runtime scope，只 copy compile 会漏掉它，
#     导致启动报 Cannot load driver class: org.h2.Driver（见 v1.0.3 修复）。
#   - 排除 dev 工具（optional）：spring-boot-devtools / spring-boot-configuration-processor
#     不进生产包，与 Spring Boot fat jar 依赖集合一致。
#   - openjfx 不复制进 classpath（排除），改为依赖内置 runtime 自带的 JavaFX 模块，
#     通过 --add-modules 在启动时加载（见下方 jpackage 参数），避免平台分类器/原生库冲突。
#   - 主 jar 使用 Spring Boot 的"薄 jar"（WuZhu-1.0.jar.original = 未 re-package 前的原 jar，
#     应用类在 jar 根部直接可被 classpath 加载），重命名为 WuZhu-1.0.jar 作为 --main-jar。
#     不能用 fat jar：fat jar 的应用类藏在 BOOT-INF/ 里，只能靠 java -jar + JarLauncher 加载，
#     jpackage 的 classpath 启动方式无法直接加载，会报 failed to launch JVM。
& .\mvnw.cmd dependency:copy-dependencies "-DincludeScope=runtime" "-DexcludeGroupIds=org.openjfx" "-DexcludeArtifactIds=spring-boot-devtools,spring-boot-configuration-processor" -q
Copy-Item target\WuZhu-1.0.jar.original target\dependency\WuZhu-1.0.jar

# 创建自定义 JRE（jlink）—— 与 package-ubuntu.sh 一致，保证内置 runtime 完整可用
# 必须用 jlink 自建 runtime（含 bin\java.exe 启动器），不能依赖 jpackage 自动生成的 runtime：
# 自动生成的 runtime 缺少 JVM 启动器（bin\java.exe），导致 WuZhu.exe 报 failed to launch JVM。
Write-Host "${Yellow}创建自定义 JRE...${Reset}"
if (-not (Test-Path "target\custom-jre")) {
    & jlink `
        --module-path "$env:JAVA_HOME\jmods" `
        --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing,jdk.localedata `
        --output target\custom-jre `
        --strip-debug `
        --no-man-pages `
        --no-header-files `
        --compress=2
    if ($LASTEXITCODE -ne 0) {
        Write-Error "jlink 创建自定义 JRE 失败"
        exit 1
    }
}

# 创建 MSI
Write-Host "${Yellow}创建 MSI 安装包...${Reset}"

# UUID 用于升级检测（固定值，不要更改）
$upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"

$jpackageArgs = @(
    "--type", "msi",
    "--name", "WuZhu",
    "--app-version", $AppVersion,
    "--vendor", "lifxue",
    "--description", "WuZhu - 加密货币交易记录和分析工具",
    "--copyright", "Copyright 2023-2025 lifxue",
    "--main-jar", "WuZhu-1.0.jar",
    "--main-class", "org.lifxue.wuzhu.WuZhuApplication",
    "--input", "target\dependency",
    "--dest", "target\dist",
    "--runtime-image", "target\custom-jre",
    "--icon", "src\main\resources\org\lifxue\wuzhu\images\wuzhu-96.ico",
    "--win-menu",
    "--win-menu-group", "WuZhu",
    "--win-shortcut",
    "--win-dir-chooser",
    "--win-per-user-install",
    "--win-upgrade-uuid", $upgradeUuid,
    "--java-options", "-Dfile.encoding=UTF-8",
    # 非模块化 classpath 应用不能直接看到内置 runtime 里的 JavaFX 模块，
    # 必须显式 --add-modules 把它们加载进模块图，否则启动即失败。
    "--java-options", "--add-modules",
    "--java-options", "javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing"
)

& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "jpackage 打包失败"
    exit 1
}

Write-Host ""
Write-Host "${Green}=== 打包完成 ===${Reset}"
Write-Host ""

# 显示结果
$msiPath = "target\dist\WuZhu-$AppVersion.msi"
if (Test-Path $msiPath) {
    $fileInfo = Get-Item $msiPath
    $fileSizeMB = [math]::Round($fileInfo.Length / 1MB, 2)
    
    Write-Host "${Green}✓ 安装包已生成${Reset}"
    Write-Host "  位置: $msiPath"
    Write-Host "  大小: $fileSizeMB MB"
    Write-Host ""
    Write-Host "安装命令:"
    Write-Host "  msiexec /i $msiPath /qn"
    Write-Host ""
    Write-Host "或在文件资源管理器中双击安装"
} else {
    Write-Error "${Red}✗ 打包失败${Reset}"
    exit 1
}
