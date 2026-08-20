package org.lifxue.wuzhu.service;

import org.junit.jupiter.api.Test;
import org.lifxue.wuzhu.constant.CoinConstants;
import org.lifxue.wuzhu.dto.PortfolioValuation;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.pojo.TradeInfo;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PortfolioCalculationService 单元测试（纯逻辑，无需 GUI / 网络 / 数据库）。
 *
 * 验证"数据图例"与"数据分析"共享的组合估值口径：
 *   1. 持仓数量聚合（买加卖减）
 *   2. USDT 余额（入金"卖"加、出金"买"减，且买卖币会增减 USDT）
 *   3. 各币种当前价值 = 持仓 × 现价
 *   4. 组合总价值 = Σ(持仓 × 现价) + USDT 余额
 */
class PortfolioCalculationServiceTest {

    private final PortfolioCalculationService service = new PortfolioCalculationService();

    private static final Integer BTC_ID = 1;
    private static final Integer ETH_ID = 1027;

    private static TradeInfo trade(Integer baseId, String saleOrBuy, String baseNum, String quoteNum) {
        TradeInfo t = new TradeInfo();
        t.setBaseId(baseId);
        t.setBaseSymbol("BTC".equals(saleOrBuy) ? "BTC" : "OTHER");
        t.setSaleOrBuy(saleOrBuy);
        t.setBaseNum(new BigDecimal(baseNum));
        t.setQuoteNum(new BigDecimal(quoteNum));
        return t;
    }

    private static TradeInfo usdtTrade(String saleOrBuy, String amount) {
        TradeInfo t = new TradeInfo();
        t.setBaseId(CoinConstants.USDT_COIN_ID);
        t.setBaseSymbol(CoinConstants.USDT_SYMBOL);
        t.setSaleOrBuy(saleOrBuy);
        t.setBaseNum(new BigDecimal(amount));
        t.setQuoteNum(new BigDecimal(amount));
        return t;
    }

    private static CMCQuotesLatest quote(Integer tid, String symbol, String price) {
        CMCQuotesLatest q = new CMCQuotesLatest();
        q.setTid(tid);
        q.setSymbol(symbol);
        q.setPrice(new BigDecimal(price));
        return q;
    }

    @Test
    void calculate_emptyData_returnsZero() {
        PortfolioValuation v = service.calculate(List.of(), List.of());
        assertNotNull(v);
        assertEquals(BigDecimal.ZERO, v.getTotalValue());
        assertEquals(BigDecimal.ZERO, v.getUsdtBalance());
        assertTrue(v.getHoldings().isEmpty());
        assertTrue(v.getCoinValues().isEmpty());
    }

    @Test
    void calculate_aggregatesHoldingsAndCurrentValue() {
        // 买入 2 BTC @60000，卖出 0.5 BTC @70000 → 持仓 1.5 BTC
        List<TradeInfo> trades = List.of(
            trade(BTC_ID, "买", "2", "120000"),
            trade(BTC_ID, "卖", "0.5", "35000")
        );
        // BTC 现价 80000
        List<CMCQuotesLatest> quotes = List.of(quote(BTC_ID, "BTC", "80000"));

        PortfolioValuation v = service.calculate(trades, quotes);

        assertEquals(0, v.getHoldings().get(BTC_ID).compareTo(new BigDecimal("1.5")), "持仓应为 2 - 0.5 = 1.5");
        assertEquals(0, v.getCoinValues().get(BTC_ID).compareTo(new BigDecimal("120000")), "价值应为 1.5 × 80000");
        // 买入花 120000，卖出得 35000 → USDT = -120000 + 35000 = -85000（不足为 0，不计入总价值）
        assertEquals(0, v.getUsdtBalance().compareTo(new BigDecimal("-85000")), "买卖币应增减 USDT 余额");
        // 总价值只计入正余额：USDT 为负，不计入
        assertEquals(0, v.getTotalValue().compareTo(new BigDecimal("120000")), "总价值 = BTC 市值 + 正 USDT 余额");
    }

    @Test
    void calculate_usdtDepositAndWithdrawal_netBalance() {
        // 入金 1000（存"卖"），出金 200（存"买"）
        List<TradeInfo> trades = List.of(
            usdtTrade("卖", "1000"),
            usdtTrade("买", "200")
        );

        PortfolioValuation v = service.calculate(trades, List.of());

        assertEquals(0, v.getUsdtBalance().compareTo(new BigDecimal("800")), "USDT 余额 = 1000 - 200 = 800");
        assertEquals(0, v.getTotalValue().compareTo(new BigDecimal("800")), "仅有 USDT 时总价值等于余额");
    }

    @Test
    void calculate_skipUsdtCoinInQuotes_notDoubleCounted() {
        // 入金 100
        List<TradeInfo> trades = List.of(usdtTrade("卖", "100"));
        // 报价里包含 USDT 本身（825），应被跳过，不按市值重复计算
        List<CMCQuotesLatest> quotes = List.of(
            quote(CoinConstants.USDT_COIN_ID, "USDT", "1"),
            quote(BTC_ID, "BTC", "100")
        );

        PortfolioValuation v = service.calculate(trades, quotes);

        assertFalse(v.getCoinValues().containsKey(CoinConstants.USDT_COIN_ID), "USDT 不应进入 coinValues 按市值计算");
        assertEquals(0, v.getTotalValue().compareTo(new BigDecimal("100")), "总价值 = USDT 余额 100");
    }

    @Test
    void calculate_positiveUsdtIncludedInTotal() {
        // 入金 100 + 买入 BTC 30（花 30 USDT）→ USDT = 100 - 30 = 70
        List<TradeInfo> trades = List.of(
            usdtTrade("卖", "100"),
            trade(BTC_ID, "买", "0.1", "30")
        );
        List<CMCQuotesLatest> quotes = List.of(quote(BTC_ID, "BTC", "400"));

        PortfolioValuation v = service.calculate(trades, quotes);

        assertEquals(0, v.getUsdtBalance().compareTo(new BigDecimal("70")));
        assertEquals(0, v.getCoinValues().get(BTC_ID).compareTo(new BigDecimal("40")), "0.1 × 400 = 40");
        assertEquals(0, v.getTotalValue().compareTo(new BigDecimal("110")), "总价值 = 40 + 70");
    }

    @Test
    void calculate_nullInputs_handledGracefully() {
        PortfolioValuation v = service.calculate(null, null);
        assertNotNull(v);
        assertEquals(BigDecimal.ZERO, v.getTotalValue());
    }
}
