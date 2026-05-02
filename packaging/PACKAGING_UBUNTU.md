# Ubuntu 24.04 打包指南

将 WuZhu 打包为 Ubuntu 安装包（.deb）。

## 打包方式概览

| 方式 | 输出格式 | 适用场景 |
|------|----------|----------|
| **jpackage** | .deb | 推荐，原生 Ubuntu 安装包 |
| jpackage | .rpm | Fedora/openSUSE 等 |
| jlink + 脚本 | 目录 | 绿色版，解压即用 |

## 环境准备

### 必需软件

```bash
# 更新系统
sudo apt update

# 安装 OpenJDK 21（如未安装）
sudo apt install openjdk-21-jdk

# 安装 fakeroot（dpkg-deb 已包含在系统预装的 dpkg 中）
sudo apt install fakeroot

# 验证 jpackage 可用
jpackage --version
```

> **注意**：`dpkg-deb` 命令已包含在 Ubuntu 预装的 `dpkg` 包中，无需单独安装。如果遇到问题，可运行 `sudo apt install --reinstall dpkg` 修复。

### 验证环境

```bash
# 检查 Java
java -version
# openjdk version "21.0.x" 202x-xx-xx

# 检查 jpackage
which jpackage
# /usr/lib/jvm/java-21-openjdk-amd64/bin/jpackage
```

## 方式一：使用 jpackage 创建 .deb 包（推荐）

### 步骤 1：编译项目

```bash
# 进入项目目录
cd WuZhu

# 清理并编译
./mvnw clean package -DskipTests
```

### 步骤 2：创建自定义 JRE（使用 jlink）

**重要前提**：标准 OpenJDK 不包含 JavaFX 模块。你有两个选择：

#### 选项 A：安装 BellSoft Liberica JDK 21 Full（推荐，包含 JavaFX）

```bash
# 下载并安装 BellSoft Liberica JDK 21 Full（包含 JavaFX）
wget https://download.bell-sw.com/java/21.0.7+10/bellsoft-jdk21.0.7+10-linux-amd64-full.deb
sudo dpkg -i bellsoft-jdk21.0.7+10-linux-amd64-full.deb

# 设置为默认 Java
sudo update-alternatives --config java
# 选择 liberica 版本

# 验证 JavaFX 可用
java --list-modules | grep javafx
```

然后使用 jlink：

```bash
# 创建输出目录
mkdir -p target/dist

# 使用 jlink 创建精简 JRE（包含 JavaFX 模块）
# 注意：--print-module-path 只在 BellSoft/Temurin JDK 中可用
jlink \
  --module-path $(java --print-module-path) \
  --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing \
  --output target/custom-jre \
  --strip-debug \
  --no-man-pages \
  --no-header-files \
  --compress=2
```

#### 选项 B：使用标准 OpenJDK + 外部 JavaFX

如果你坚持使用标准 OpenJDK：

```bash
# 1. 下载 JavaFX jmods
wget https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-jmods.zip
unzip openjfx-21.0.2_linux-x64_bin-jmods.zip -d /tmp/javafx

# 2. 使用 jlink（手动指定模块路径）
JDK_PATH=/usr/lib/jvm/java-21-openjdk-amd64
jlink \
  --module-path "$JDK_PATH/jmods:/tmp/javafx/javafx-jmods-21.0.2" \
  --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing \
  --output target/custom-jre \
  --strip-debug \
  --no-man-pages \
  --no-header-files \
  --compress=2
```

### 步骤 3：使用 jpackage 创建 .deb

```bash
jpackage \
  --type deb \
  --name WuZhu \
  --app-version 1.0.0 \
  --vendor "lifxue" \
  --description "WuZhu - 加密货币交易记录和分析工具" \
  --copyright "Copyright 2023-2025 lifxue" \
  --main-class org.springframework.boot.loader.JarLauncher \
  --main-jar WuZhu-1.0.jar \
  --input target \
  --dest target/dist \
  --runtime-image target/custom-jre \
  --linux-package-name wuzhu \
  --linux-app-category Office \
  --linux-menu-group Office \
  --linux-shortcut \
  --icon src/main/resources/org/lifxue/wuzhu/images/logo.png \
  --java-options "-Dfile.encoding=UTF-8" \
  --java-options "-Dspring.backgroundpreinitializer.ignore=true"
```

### 步骤 4：验证安装包

```bash
# 查看生成的 .deb 文件
ls -lh target/dist/*.deb

# 查看包信息
dpkg-deb -I target/dist/wuzhu_1.0.0_amd64.deb

# 查看包内容
dpkg-deb -c target/dist/wuzhu_1.0.0_amd64.deb
```

### 步骤 5：测试安装

```bash
# 安装
sudo dpkg -i target/dist/wuzhu_1.0.0_amd64.deb

# 如遇到依赖问题，修复
sudo apt-get install -f

# 运行应用
wuzhu

# 或在应用菜单中搜索 "WuZhu"
```

### 卸载

```bash
sudo dpkg -r wuzhu
```

## 方式二：创建绿色版（无需安装）

### 步骤 1：创建精简 JRE

```bash
./mvnw clean package -DskipTests

# 获取 JDK 路径（用于指定 module-path）
JDK_HOME=$(dirname $(dirname $(readlink -f $(which java))))
echo "JDK 路径: $JDK_HOME"

# 使用 jlink 创建精简 JRE
jlink \
  --module-path "$JDK_HOME/jmods" \
  --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing \
  --output target/WuZhu-linux/runtime \
  --strip-debug \
  --no-man-pages \
  --no-header-files \
  --compress=2
```

> **注意**：`--print-module-path` 在标准 OpenJDK 和某些 JDK 发行版中不可用。使用上述方式手动指定 JDK 的 jmods 目录路径。

### 步骤 2：复制应用文件

```bash
# 创建应用目录
mkdir -p target/WuZhu-linux/app

# 复制 JAR
cp target/WuZhu-1.0.jar target/WuZhu-linux/app/

# 创建启动脚本
cat > target/WuZhu-linux/WuZhu.sh << 'EOF'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"$SCRIPT_DIR/runtime/bin/java" -jar "$SCRIPT_DIR/app/WuZhu-1.0.jar" "$@"
EOF

chmod +x target/WuZhu-linux/WuZhu.sh
```

### 步骤 3：打包为压缩文件

```bash
cd target
tar -czvf WuZhu-1.0.0-linux.tar.gz WuZhu-linux/
```

### 使用绿色版

```bash
# 解压
tar -xzvf WuZhu-1.0.0-linux.tar.gz
cd WuZhu-linux

# 运行
./WuZhu.sh
```

## 完整打包脚本

创建 `package-ubuntu.sh`：

```bash
#!/bin/bash
set -e

echo "=== WuZhu Ubuntu 打包脚本 ==="

# 清理
./mvnw clean

# 编译
./mvnw package -DskipTests

# 创建 JRE
if [ ! -d "target/custom-jre" ]; then
    echo "创建自定义 JRE..."
    jlink \
      --module-path $(java --print-module-path) \
      --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.controls,javafx.fxml \
      --output target/custom-jre \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=2
fi

# 创建 .deb
echo "创建 .deb 安装包..."
jpackage \
  --type deb \
  --name WuZhu \
  --app-version 1.0.0 \
  --vendor "lifxue" \
  --description "WuZhu - 加密货币交易记录和分析工具" \
  --main-class org.springframework.boot.loader.JarLauncher \
  --main-jar WuZhu-1.0.jar \
  --input target \
  --dest target/dist \
  --runtime-image target/custom-jre \
  --linux-package-name wuzhu \
  --linux-app-category Office \
  --linux-menu-group Office \
  --linux-shortcut \
  --icon src/main/resources/org/lifxue/wuzhu/images/logo.png \
  --java-options "-Dfile.encoding=UTF-8"

echo "=== 打包完成 ==="
echo "安装包位置: target/dist/wuzhu_1.0.0_amd64.deb"
ls -lh target/dist/*.deb
```

赋予执行权限并运行：

```bash
chmod +x package-ubuntu.sh
./package-ubuntu.sh
```

## 发布到 GitHub Releases

```bash
# 创建 Release（需要 gh CLI）
gh release create v1.0.0 \
  target/dist/wuzhu_1.0.0_amd64.deb \
  target/WuZhu-1.0.0-linux.tar.gz \
  --title "WuZhu v1.0.0" \
  --notes "Ubuntu 24.04 安装包"
```

## 常见问题

### 1. jpackage 报错 "module not found"

确保使用的 JDK 包含 JavaFX 模块，或使用 Liberica/Temurin Full JDK。

### 2. 缺少 libffi.so.7

```bash
sudo apt install libffi7
```

### 3. 应用启动后界面异常

确保系统安装了 JavaFX 所需的图形库：

```bash
sudo apt install libgtk-3-0 libx11-xcb1 libgl1-mesa-glx
```

### 4. 中文字体显示问题

```bash
sudo apt install fonts-noto-cjk
```

## 相关文档

- [开发运行指南](./DEVELOPMENT.md)
- [Windows 打包指南](./PACKAGING_WINDOWS.md)
