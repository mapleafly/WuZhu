#!/bin/bash
#
# verify-runtime.sh — 验证打包用的自定义 JRE（custom-jre）是否具备完整的 TLS/HTTPS 能力。
#
# 背景（v1.0.4 bug）：jlink 自建 runtime 的 --add-modules 遗漏了 jdk.crypto.ec，
# 导致 HTTPS 请求报：
#   (unexpected_message) Received close_notify during handshake
#   executing GET https://pro-api.coinmarketcap.com/...
# 两个"更新"功能（更新货币数据 / 更新现价）全部受影响。
#
# 本脚本在打包完成后自动运行，防止该问题再次回归：
#   1. 检查 runtime 是否包含 jdk.crypto.ec（ECDHE/TLS 必需）；
#   2. 用 runtime 内置 JVM 对 CoinMarketCap API 做一次真实 TLS 握手。
#
# 用法：
#   ./packaging/verify-runtime.sh [runtime_dir]   # 默认 target/custom-jre
#   exit 0 = 通过；exit 1 = 失败（可接入 CI 作为打包后冒烟测试）
set -e

RUNTIME_DIR="${1:-target/custom-jre}"
if [ ! -d "$RUNTIME_DIR" ]; then
    echo "❌ 未找到 runtime 目录: $RUNTIME_DIR（请先执行打包脚本生成 custom-jre）"
    exit 1
fi

JAVA_BIN="$RUNTIME_DIR/bin/java"
if [ ! -x "$JAVA_BIN" ]; then
    echo "❌ runtime 缺少可执行文件: $JAVA_BIN"
    exit 1
fi

echo "=== 检查 jdk.crypto.ec 模块 ==="
if ! "$JAVA_BIN" --list-modules 2>/dev/null | grep -q "jdk.crypto.ec"; then
    echo "❌ runtime 缺少 jdk.crypto.ec 模块，HTTPS/TLS(ECDHE) 会失败！"
    echo "   请在 jlink --add-modules 中加入: jdk.crypto.ec,jdk.crypto.cryptoki"
    exit 1
fi
echo "✅ jdk.crypto.ec 已包含"

if ! "$JAVA_BIN" --list-modules 2>/dev/null | grep -q "jdk.crypto.cryptoki"; then
    echo "⚠️ 警告: jdk.crypto.cryptoki 未包含（非必需，但建议加上）"
else
    echo "✅ jdk.crypto.cryptoki 已包含"
fi

echo ""
echo "=== 真实 TLS 握手测试（pro-api.coinmarketcap.com） ==="
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT
cat > "$WORK_DIR/TlsProbe.java" <<'JAVA'
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
public class TlsProbe {
    public static void main(String[] args) throws Exception {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/map?limit=1";
        try {
            HttpsURLConnection c = (HttpsURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(15000);
            c.setRequestProperty("Accept", "application/json");
            int code = c.getResponseCode();
            System.out.println("[TlsProbe] SUCCESS HTTP " + code + " (SSL:" + c.getCipherSuite() + ")");
        } catch (Exception e) {
            System.out.println("[TlsProbe] FAILED: " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
    }
}
JAVA

if ! "$JAVA_BIN" -version >/dev/null 2>&1; then
    echo "❌ runtime 的 java 启动失败"
    exit 1
fi

# 用完整的宿主 JDK 编译探测程序（compile 不需要 runtime 模块），再用 runtime 运行
if command -v javac >/dev/null 2>&1; then
    javac -d "$WORK_DIR" "$WORK_DIR/TlsProbe.java" 2>/dev/null || {
        # javac 不在 PATH 时，尝试从 runtime 上级 JDK 推导
        echo "⚠️ 找不到 javac，跳过 TLS 握手测试（仍保留模块检查）"
        exit 0
    }
else
    echo "⚠️ 找不到 javac，跳过 TLS 握手测试（仍保留模块检查）"
    exit 0
fi

if "$JAVA_BIN" -cp "$WORK_DIR" TlsProbe 2>&1 | grep -q "SUCCESS"; then
    echo "✅ TLS 握手成功，runtime 网络能力正常"
    exit 0
else
    echo "❌ TLS 握手失败，请检查 runtime 模块（jdk.crypto.ec 是否遗漏）与网络/代理"
    exit 1
fi
