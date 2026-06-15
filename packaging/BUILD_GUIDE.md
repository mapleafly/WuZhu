# WuZhu Windows 打包指南

本文档记录 WuZhu 应用在 Windows 11 下的完整打包流程。

## 前置要求

- JDK 21 with JavaFX (推荐 BellSoft Liberica)
- WiX Toolset 3.x (用于生成 MSI/EXE 安装程序)
- Maven 3.8+

## 打包流程（两步法）

### 步骤 1：编译项目

```bash
# 清理并编译
./mvnw.cmd clean package -DskipTests
```

确认 `target/WuZhu-1.0.jar` 已生成，且能用以下命令运行：

```bash
java -jar target/WuZhu-1.0.jar
```

### 步骤 2：生成应用程序镜像 (app-image)

**重要**：使用干净的输入目录，只包含 fat jar

```bash
# 创建干净的输入目录
mkdir -p dist/input
cp target/WuZhu-1.0.jar dist/input/

# 生成 app-image
jpackage --type app-image \
  --name wuzhu \
  --input dist/input \
  --main-jar WuZhu-1.0.jar \
  --icon src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico \
  --dest dist
```

生成的 app-image 位于 `dist/wuzhu/`，包含：

- `wuzhu.exe` - 启动程序
- `app/WuZhu-1.0.jar` - Spring Boot fat jar
- `runtime/` - 内置 JRE

**验证**：测试 exe 是否能正常运行

```bash
cd dist/wuzhu
./wuzhu.exe
```

### 步骤 3：生成 MSI 安装程序

```bash
# 需要 WiX Toolset 在 PATH 中
export PATH="$PATH:/c/Program Files (x86)/WiX Toolset v3.14/bin"

jpackage --type msi \
  --win-dir-chooser \
  --win-menu \
  --win-per-user-install \
  --win-shortcut \
  --icon src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico \
  --name WuZhu \
  --app-version 1.0.0 \
  --vendor "lifxue" \
  --description "WuZhu - 加密货币交易记录和分析工具" \
  --copyright "Copyright 2023-2025 lifxue" \
  --app-image dist/wuzhu \
  --dest install
```

输出：`install/WuZhu-1.0.0.msi`

### 步骤 4（可选）：生成 EXE 安装程序

```bash
jpackage --type exe \
  --win-dir-chooser \
  --win-menu \
  --win-per-user-install \
  --win-shortcut \
  --icon src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico \
  --name WuZhu \
  --app-version 1.0.0 \
  --vendor "lifxue" \
  --description "WuZhu - 加密货币交易记录和分析工具" \
  --copyright "Copyright 2023-2025 lifxue" \
  --app-image dist/wuzhu \
  --dest install
```

输出：`install/WuZhu-1.0.0.exe`

## 完整一键打包脚本

使用项目自带的 PowerShell 脚本：

```powershell
# 以管理员身份运行 PowerShell
.\packaging\package-windows.ps1
```

或手动执行完整流程：

```bash
#!/bin/bash
# build-windows.sh - Windows 打包脚本

set -e

echo "=== 步骤 1: 编译项目 ==="
./mvnw.cmd clean package -DskipTests -q

echo "=== 步骤 2: 生成 app-image ==="
rm -rf dist
mkdir -p dist/input
cp target/WuZhu-1.0.jar dist/input/

jpackage --type app-image \
  --name wuzhu \
  --input dist/input \
  --main-jar WuZhu-1.0.jar \
  --icon src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico \
  --dest dist

echo "=== 步骤 3: 生成 MSI 安装程序 ==="
export PATH="$PATH:/c/Program Files (x86)/WiX Toolset v3.14/bin"
rm -rf install
mkdir install

jpackage --type msi \
  --win-dir-chooser --win-menu --win-per-user-install --win-shortcut \
  --icon src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico \
  --name WuZhu --app-version 1.0.0 \
  --vendor "lifxue" \
  --description "WuZhu - 加密货币交易记录和分析工具" \
  --copyright "Copyright 2023-2025 lifxue" \
  --app-image dist/wuzhu \
  --dest install

echo "=== 打包完成 ==="
ls -lh install/
```

## 常见问题

### 1. 安装后的 exe 无法运行

**原因**：直接使用 `--input target/dependency` 导致依赖分离，exe 无法正确加载。

**解决**：使用两步法，先生成 app-image，再用 app-image 生成安装程序。

### 2. "WiX tools not found"

**解决**：

```bash
# 添加到 PATH
export PATH="$PATH:/c/Program Files (x86)/WiX Toolset v3.14/bin"

# 验证
candle -?
```

### 3. 生成的安装包太大

- MSI/EXE: ~150MB（包含完整 JRE）
- 如需减小体积，考虑使用模块化 JRE（jlink）

### 4. 图标不显示

确保图标路径正确：

```bash
ls src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico
```

## 安装包信息

| 格式  | 文件                        | 大小     | 特点           |
| --- | ------------------------- | ------ | ------------ |
| MSI | `install/WuZhu-1.0.0.msi` | ~150MB | 标准安装包，企业部署友好 |
| EXE | `install/WuZhu-1.0.0.exe` | ~150MB | 单文件，用户体验好    |
| 便携版 | `dist/wuzhu/`             | ~150MB | 无需安装，解压即用    |

## 相关文档

- [Windows 打包详细指南](./PACKAGING_WINDOWS.md)
- [Ubuntu 打包指南](./PACKAGING_UBUNTU.md)
- [开发环境搭建](./DEVELOPMENT.md)
