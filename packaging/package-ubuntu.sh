#!/bin/bash
set -e

echo "=== WuZhu Ubuntu 24.04 打包脚本 ==="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 版本号（tag 驱动）：命令行参数 > 环境变量 APP_VERSION > 最近 git tag > 默认 1.0.0
# 用法示例: ./package-ubuntu.sh 1.0.1  或  APP_VERSION=1.0.1 ./package-ubuntu.sh
APP_VERSION="${1:-${APP_VERSION:-}}"
if [ -z "$APP_VERSION" ]; then
    APP_VERSION="$(git describe --tags --abbrev=0 2>/dev/null || true)"
fi
# 去掉前缀 v/V（如 v1.0.1 -> 1.0.1）
APP_VERSION="${APP_VERSION#v}"
APP_VERSION="${APP_VERSION#V}"
case "$APP_VERSION" in
    [0-9]*\.[0-9]*\.[0-9]*) ;;  # 形如 1.0.1
    *) APP_VERSION="1.0.0" ;;
esac
echo -e "${GREEN}版本号: $APP_VERSION${NC}"
echo ""

# 检查环境
echo -e "${YELLOW}检查环境...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: Java 未安装${NC}"
    echo "请安装 BellSoft Liberica JDK 21 Full（含 JavaFX），详见 docs/plans/DEV_ENV_MISE.md"
    exit 1
fi

if ! command -v jpackage &> /dev/null; then
    echo -e "${RED}错误: jpackage 未找到${NC}"
    echo "请确保使用的是 JDK（不是 JRE）"
    exit 1
fi

# 检查 JavaFX 是否可用
if ! java --list-modules 2>/dev/null | grep -q "javafx"; then
    echo -e "${RED}错误: 当前 JDK 不包含 JavaFX 模块${NC}"
    echo ""
    echo "解决方案（推荐）:"
    echo "  使用 mise 安装 BellSoft Liberica JDK 21 Full:"
    echo "    mise use -g java@liberica-javafx-21"
    echo "  或从 https://bell-sw.com/pages/downloads 下载最新版 Full JDK 并安装"
    echo ""
    echo "2. 查看详细文档: cat packaging/PACKAGING_UBUNTU.md"
    exit 1
fi

# 解析 JAVA_HOME（若未设置则从 java 可执行文件推导）
if [ -z "${JAVA_HOME:-}" ]; then
    JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
fi

# 检查 jmods 目录（jlink 所需，含 JavaFX 模块）
if [ ! -d "$JAVA_HOME/jmods" ]; then
    echo -e "${RED}错误: 未找到 JDK 的 jmods 目录（$JAVA_HOME/jmods）${NC}"
    echo "请确保 JAVA_HOME 指向完整的 JDK（含 JavaFX 模块），例如 mise 安装的 Liberica Full"
    echo "参考文档: packaging/PACKAGING_UBUNTU.md"
    exit 1
fi

if ! command -v fakeroot &> /dev/null; then
    echo -e "${YELLOW}安装 fakeroot...${NC}"
    sudo apt update
    sudo apt install -y fakeroot
fi

# 注意：dpkg-deb 已包含在 Ubuntu 预装的 dpkg 包中，无需单独安装

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
# includeScope 用 runtime（compile + runtime）：h2 数据库驱动是 runtime scope，
# 只 copy compile 会漏掉，导致启动报 Cannot load driver class: org.h2.Driver。
# 排除 dev 工具（optional）：spring-boot-devtools / spring-boot-configuration-processor。
./mvnw dependency:copy-dependencies -DincludeScope=runtime -DexcludeGroupIds=org.openjfx -DexcludeArtifactIds=spring-boot-devtools,spring-boot-configuration-processor -q
# 主 jar 使用 Spring Boot 的"薄 jar"（.original = 未 repackage 前的原 jar，应用类在 jar 根部，
# 可被 jpackage 的 classpath 启动直接加载）。不能用 fat jar：其应用类藏在 BOOT-INF/，classpath 启动无法加载。
cp target/WuZhu-1.0.jar.original target/dependency/WuZhu-1.0.jar

# 创建自定义 JRE
echo -e "${YELLOW}创建自定义 JRE...${NC}"
if [ ! -d "target/custom-jre" ]; then
    jlink \
      --module-path "$JAVA_HOME/jmods" \
      --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.jgss,java.instrument,jdk.unsupported,javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing,jdk.localedata \
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
  --app-version "$APP_VERSION" \
  --vendor "lifxue" \
  --description "WuZhu - 加密货币交易记录和分析工具" \
  --copyright "Copyright 2023-2025 lifxue" \
  --main-class org.lifxue.wuzhu.WuZhuApplication \
  --main-jar WuZhu-1.0.jar \
  --input target/dependency \
  --dest target/dist \
  --runtime-image target/custom-jre \
  --linux-package-name wuzhu \
  --linux-app-category Office \
  --linux-menu-group Office \
  --linux-shortcut \
  --icon src/main/resources/org/lifxue/wuzhu/images/logo.png \
  --java-options "-Dfile.encoding=UTF-8" \
  --java-options "--add-modules" \
  --java-options "javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing"

echo ""
echo -e "${GREEN}=== 打包完成 ===${NC}"
echo ""

# 显示结果
DEB_FILE="target/dist/wuzhu_${APP_VERSION}_amd64.deb"
if [ -f "$DEB_FILE" ]; then
    FILE_SIZE=$(du -h "$DEB_FILE" | cut -f1)
    echo -e "${GREEN}✓ 安装包已生成${NC}"
    echo "  位置: $DEB_FILE"
    echo "  大小: $FILE_SIZE"
    echo ""
    echo "安装命令:"
    echo "  sudo dpkg -i $DEB_FILE"
else
    echo -e "${RED}✗ 打包失败${NC}"
    exit 1
fi
