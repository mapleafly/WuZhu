/*
 * Copyright 2019 xuelf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifxue.wuzhu.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import org.lifxue.wuzhu.constant.CoinConstants;
import org.lifxue.wuzhu.dto.PortfolioValuation;
import org.lifxue.wuzhu.enums.BooleanEnum;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.service.PortfolioCalculationService;
import org.lifxue.wuzhu.util.PrefsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TypePieChartViewModel {

    private ITradeInfoService tradeInfoService;
    private ICMCQuotesLatestService quotesLatestService;
    private PortfolioCalculationService portfolioCalculationService;

    private final ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
    private final Map<PieChart.Data, BigDecimal> pieDataQuantities = new HashMap<>();
    private final ObjectProperty<BigDecimal> totalValue = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final BooleanProperty hideSmallCoins = new SimpleBooleanProperty(false);
    private final ObjectProperty<BigDecimal> minValueThreshold = new SimpleObjectProperty<>(new BigDecimal("100"));
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty();

    @Autowired
    public void setTradeInfoService(ITradeInfoService tradeInfoService) {
        this.tradeInfoService = tradeInfoService;
    }

    @Autowired
    public void setQuotesLatestService(ICMCQuotesLatestService quotesLatestService) {
        this.quotesLatestService = quotesLatestService;
    }

    @Autowired
    public void setPortfolioCalculationService(PortfolioCalculationService portfolioCalculationService) {
        this.portfolioCalculationService = portfolioCalculationService;
    }

    public ObservableList<PieChart.Data> getPieData() {
        return pieData;
    }

    /**
     * 获取指定饼图数据项对应的持仓数量
     *
     * @param data 饼图数据项
     * @return 持仓数量，未知数据项返回 {@link BigDecimal#ZERO}
     */
    public BigDecimal getPieDataQuantity(PieChart.Data data) {
        return pieDataQuantities.getOrDefault(data, BigDecimal.ZERO);
    }

    public ObjectProperty<BigDecimal> totalValueProperty() {
        return totalValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue.get();
    }

    public BooleanProperty hideSmallCoinsProperty() {
        return hideSmallCoins;
    }

    public boolean isHideSmallCoins() {
        return hideSmallCoins.get();
    }

    public void setHideSmallCoins(boolean hide) {
        this.hideSmallCoins.set(hide);
    }

    public ObjectProperty<BigDecimal> minValueThresholdProperty() {
        return minValueThreshold;
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public void loadPortfolioData() {
        if (tradeInfoService == null || quotesLatestService == null) {
            return;
        }

        loading.set(true);
        errorMessage.set(null);

        try {
            pieData.clear();
            pieDataQuantities.clear();

            List<TradeInfo> tradeInfos = tradeInfoService.findOrderByTradeDate();
            List<CMCQuotesLatest> quotes = quotesLatestService.queryLatest();

            PortfolioValuation valuation =
                portfolioCalculationService.calculate(tradeInfos, quotes);

            List<PieChart.Data> dataList = buildPieData(valuation, quotes);
            pieData.addAll(dataList);

            // 计算账户总额（所有币种当前价值的总和）
            totalValue.set(valuation.getTotalValue());

        } catch (Exception e) {
            errorMessage.set("加载数据失败: " + e.getMessage());
        } finally {
            loading.set(false);
        }
    }

    private List<PieChart.Data> buildPieData(PortfolioValuation valuation, List<CMCQuotesLatest> quotes) {
        List<PieChart.Data> result = new ArrayList<>();

        Map<Integer, BigDecimal> holdings = valuation.getHoldings();
        BigDecimal usdtBalance = valuation.getUsdtBalance();
        Map<Integer, BigDecimal> coinValues = valuation.getCoinValues();
        BigDecimal totalValue = valuation.getTotalValue();

        // 步骤1: 构建饼图数据
        boolean hideSmall = shouldHideSmallCoins();
        BigDecimal threshold = getMinValueThreshold();
        BigDecimal otherValue = BigDecimal.ZERO;
        BigDecimal otherQuantity = BigDecimal.ZERO;

        // 先处理非USDT币种
        if (quotes != null) {
            for (CMCQuotesLatest quote : quotes) {
                Integer coinId = quote.getTid();
                // 跳过USDT
                if (CoinConstants.USDT_COIN_ID.equals(coinId)) {
                    continue;
                }

                BigDecimal value = coinValues.getOrDefault(coinId, BigDecimal.ZERO);
                if (value.compareTo(BigDecimal.ZERO) > 0) {
                    if (hideSmall && value.compareTo(threshold) < 0) {
                        otherValue = otherValue.add(value);
                        otherQuantity = otherQuantity.add(holdings.getOrDefault(coinId, BigDecimal.ZERO));
                    } else {
                        String symbol = quote.getSymbol();
                        double percentage = value.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .doubleValue();
                        PieChart.Data data = new PieChart.Data(
                            String.format("%s (%.2f%%)", symbol, percentage),
                            value.doubleValue()
                        );
                        result.add(data);
                        pieDataQuantities.put(data, holdings.getOrDefault(coinId, BigDecimal.ZERO));
                    }
                }
            }
        }

        // 添加USDT到饼图
        if (usdtBalance.compareTo(BigDecimal.ZERO) > 0) {
            double usdtPercentage = usdtBalance.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
            PieChart.Data usdtData = new PieChart.Data(
                String.format("USDT (%.2f%%)", usdtPercentage),
                usdtBalance.doubleValue()
            );
            result.add(usdtData);
            pieDataQuantities.put(usdtData, usdtBalance);
        }

        // 添加"其他"分类
        if (otherValue.compareTo(BigDecimal.ZERO) > 0) {
            double otherPercentage = otherValue.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
            PieChart.Data otherData = new PieChart.Data(
                String.format("其他 (%.2f%%)", otherPercentage),
                otherValue.doubleValue()
            );
            result.add(otherData);
            pieDataQuantities.put(otherData, otherQuantity);
        }

        // 按价值降序排序
        result.sort(Comparator.comparingDouble(PieChart.Data::getPieValue).reversed());

        return result;
    }

    private boolean shouldHideSmallCoins() {
        String value = PrefsHelper.getPreferencesValue(PrefsHelper.NOTSMALLCOIN, BooleanEnum.NO.toString());
        return BooleanEnum.YES.equals(BooleanEnum.valueOf(value));
    }

    private BigDecimal getMinValueThreshold() {
        String value = PrefsHelper.getPreferencesValue(PrefsHelper.NOTSMALLCOINNUM, "100");
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return new BigDecimal("100");
        }
    }
}
