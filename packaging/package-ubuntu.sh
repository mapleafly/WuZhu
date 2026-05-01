#!/bin/bash
set -e

echo "=== WuZhu Ubuntu 24.04 打包脚本 ==="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查环境
echo -e "${YELLOW}检查环境...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: Java 未安装${NC}"
    echo "请安装 OpenJDK 21: sudo apt install openjdk-21-jdk"
    exit 1
fi

if ! command -v jpackage &> /dev/null; then
    echo -e "${RED}错误: jpackage 未找到${NC}"
    echo "请确保使用的是 JDK（不是 JRE）"
    exit 1
fi

if ! command -v dpkg-deb &> /dev/null; then
    echo -e "${YELLOW}安装 dpkg-deb...${NC}"
    sudo apt update
    sudo apt install -y dpkg-dev fakeroot
fi

echo -e "${GREEN}✓ 环境检查通过${NC}"
echo ""

# 清理
echo -e "${YELLOW}清理旧文件...${NC}"
./mvnw clean -q

# 编译
echo -e "${YELLOW}编译项目...${NC}"
./mvnw package -DskipTests -q

# 复制依赖
echo -e "${YELLOW}复制依赖...${NC}"
./mvnw dependency:copy-dependencies -DincludeScope=compile -DexcludeGroupIds=org.openjfx -q
cp target/WuZhu-1.0.jar target/dependency/

# 创建自定义 JRE
echo -e "${YELLOW}创建自定义 JRE...${NC}"
if [ ! -d "target/custom-jre" ]; then
    jlink \
      --module-path $(java --print-module-path) \
      --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.controls,javafx.fxml,javafx.web,jdk.localedata \
      --output target/custom-jre \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=2
fi

# 创建 .deb
echo -e "${YELLOW}创建 .deb 安装包...${NC}"
jpackage \
  --type deb \
  --name WuZhu \
  --app-version 1.0.0 \
  --vendor "lifxue" \
  --description "WuZhu - 加密货币交易记录和分析工具" \
  --copyright "Copyright 2023-2025 lifxue" \
  --main-class org.springframework.boot.loader.launch.JarLauncher \
  --main-jar WuZhu-1.0.jar \
  --input target/dependency \
  --dest target/dist \
  --runtime-image target/custom-jre \
  --linux-package-name wuzhu \
  --linux-app-category Office \
  --linux-menu-group Office \
  --linux-shortcut \
  --icon src/main/resources/org/lifxue/wuzhu/images/logo.png \
  --java-options "-Dfile.encoding=UTF-8"

echo ""
echo -e "${GREEN}=== 打包完成 ===${NC}"
echo ""

# 显示结果
if [ -f "target/dist/wuzhu_1.0.0_amd64.deb" ]; then
    FILE_SIZE=$(du -h target/dist/wuzhu_1.0.0_amd64.deb | cut -f1)
    echo -e "${GREEN}✓ 安装包已生成${NC}"
    echo "  位置: target/dist/wuzhu_1.0.0_amd64.deb"
    echo "  大小: $FILE_SIZE"
    echo ""
    echo "安装命令:"
    echo "  sudo dpkg -i target/dist/wuzhu_1.0.0_amd64.deb"
else
    echo -e "${RED}✗ 打包失败${NC}"
    exit 1
fi
