# WuZhu 打包和部署文档

本目录包含 WuZhu 应用的开发、打包和部署指南。

## 文档索引

| 文档 | 目标读者 | 内容概述 |
|------|----------|----------|
| [DEVELOPMENT.md](./DEVELOPMENT.md) | 开发者 | 开发环境搭建、日常开发命令、IDE 配置、调试技巧 |
| [PACKAGING_UBUNTU.md](./PACKAGING_UBUNTU.md) | 发布者 | Ubuntu 24.04 打包为 .deb 安装包 |
| [PACKAGING_WINDOWS.md](./PACKAGING_WINDOWS.md) | 发布者 | Windows 11 打包为 .msi/.exe 安装包 |

## 快速导航

### 我是开发者

→ 先看 [DEVELOPMENT.md](./DEVELOPMENT.md)

快速开始：
```bash
./mvnw clean package -DskipTests
java -jar target/WuZhu-1.0.jar
```

### 我要发布 Ubuntu 版本

→ 阅读 [PACKAGING_UBUNTU.md](./PACKAGING_UBUNTU.md)

一键打包：
```bash
./packaging/package-ubuntu.sh
# 输出: target/dist/wuzhu_1.0.0_amd64.deb
```

### 我要发布 Windows 版本

→ 阅读 [PACKAGING_WINDOWS.md](./PACKAGING_WINDOWS.md)

一键打包：
```powershell
.\packaging\package-windows.ps1
# 输出: target\dist\WuZhu-1.0.0.msi
```

## 打包方式对比

| 平台 | 格式 | 文件大小 | 用户体验 | 适用场景 |
|------|------|----------|----------|----------|
| Ubuntu | .deb | ~80-120MB | 标准安装 | 推荐 |
| Ubuntu | .tar.gz | ~60-100MB | 绿色版 | 便携使用 |
| Windows | .msi | ~100-150MB | 标准安装 | 推荐 |
| Windows | .exe | ~100-150MB | 单文件安装 | 简单分发 |
| Windows | .zip | ~80-120MB | 绿色版 | 便携使用 |

## 自动化 CI/CD

推荐使用 GitHub Actions 自动化多平台构建：

```yaml
# .github/workflows/build.yml
name: Build Installers

on: [push, pull_request]

jobs:
  build-ubuntu:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'liberica'
          java-version: '21'
          javafx: true
      - run: ./packaging/package-ubuntu.sh
      - uses: actions/upload-artifact@v4
        with:
          name: ubuntu-installer
          path: target/dist/*.deb

  build-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'liberica'
          java-version: '21'
          javafx: true
      - run: choco install wixtoolset
      - run: .\packaging\package-windows.ps1
      - uses: actions/upload-artifact@v4
        with:
          name: windows-installer
          path: target/dist/*.msi
```

## 技术栈说明

- **构建工具**: Maven 3.8+
- **JDK**: BellSoft Liberica JDK 21 Full（包含 JavaFX）
- **打包工具**: JDK jpackage（内置）
- **Windows 安装器**: WiX Toolset 3.x
- **Linux 安装器**: dpkg-deb

## 获取帮助

- 开发问题 → 查看 [DEVELOPMENT.md](./DEVELOPMENT.md) 的"常见问题"章节
- Ubuntu 打包问题 → 查看 [PACKAGING_UBUNTU.md](./PACKAGING_UBUNTU.md) 的"常见问题"章节
- Windows 打包问题 → 查看 [PACKAGING_WINDOWS.md](./PACKAGING_WINDOWS.md) 的"常见问题"章节
