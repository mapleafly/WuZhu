#!/usr/bin/env pwsh
#Requires -Version 5.1
#Requires -RunAsAdministrator

<#
.SYNOPSIS
    WuZhu Windows 打包脚本
.DESCRIPTION
    将 WuZhu 应用打包为 Windows MSI 安装程序
.NOTES
    需要以管理员权限运行
    需要安装 WiX Toolset 3.x
#>

$ErrorActionPreference = "Stop"

# 颜色定义
$Green = "`e[32m"
$Yellow = "`e[33m"
$Red = "`e[31m"
$Reset = "`e[0m"

Write-Host "${Yellow}=== WuZhu Windows 11 打包脚本 ===${Reset}"
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
& .\mvnw.cmd package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven 编译失败"
    exit 1
}

# 复制依赖
Write-Host "${Yellow}复制依赖...${Reset}"
& .\mvnw.cmd dependency:copy-dependencies -DincludeScope=compile -DexcludeGroupIds=org.openjfx -q
Copy-Item target\WuZhu-1.0.jar target\dependency\

# 创建 MSI
Write-Host "${Yellow}创建 MSI 安装包...${Reset}"

# UUID 用于升级检测（固定值，不要更改）
$upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"

$jpackageArgs = @(
    "--type", "msi",
    "--name", "WuZhu",
    "--app-version", "1.0.0",
    "--vendor", "lifxue",
    "--description", "WuZhu - 加密货币交易记录和分析工具",
    "--copyright", "Copyright 2023-2025 lifxue",
    "--main-jar", "WuZhu-1.0.jar",
    "--main-class", "org.springframework.boot.loader.launch.JarLauncher",
    "--input", "target\dependency",
    "--dest", "target\dist",
    "--icon", "src\main\resources\org\lifxue\wuzhu\images\wuzhu-96.ico",
    "--win-menu",
    "--win-menu-group", "WuZhu",
    "--win-shortcut",
    "--win-dir-chooser",
    "--win-per-user-install",
    "--win-upgrade-uuid", $upgradeUuid,
    "--java-options", "-Dfile.encoding=UTF-8"
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
$msiPath = "target\dist\WuZhu-1.0.0.msi"
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
