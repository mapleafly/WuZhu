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
import org.lifxue.wuzhu.constant.AppConstants;
import org.lifxue.wuzhu.constant.CoinConstants;
import org.lifxue.wuzhu.enums.BooleanEnum;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.service.ITradeInfoService;
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

    private final ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
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

    public ObservableList<PieChart.Data> getPieData() {
        return pieData;
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

            List<TradeInfo> tradeInfos = tradeInfoService.findOrderByTradeDate();
            List<CMCQuotesLatest> quotes = quotesLatestService.queryLatest();

            List<PieChart.Data> dataList = buildPieData(tradeInfos, quotes);
            pieData.addAll(dataList);

            // 计算账户总额（所有币种当前价值的总和）
            BigDecimal total = dataList.stream()
                .map(d -> BigDecimal.valueOf(d.getPieValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalValue.set(total);

        } catch (Exception e) {
            errorMessage.set("加载数据失败: " + e.getMessage());
        } finally {
            loading.set(false);
        }
    }

    private List<PieChart.Data> buildPieData(List<TradeInfo> tradeInfos, List<CMCQuotesLatest> quotes) {
        List<PieChart.Data> result = new ArrayList<>();
        
        // 步骤1: 计算各币种持仓数量和USDT余额
        Map<Integer, BigDecimal> holdings = new HashMap<>();
        BigDecimal usdtBalance = BigDecimal.ZERO;
        
        for (TradeInfo trade : tradeInfos) {
            if (CoinConstants.USDT_COIN_ID.equals(trade.getBaseId())) {
                // USDT记录：入金/出金
                if ("卖".equals(trade.getSaleOrBuy())) {
                    // 入金：增加USDT余额
                    usdtBalance = usdtBalance.add(trade.getBaseNum());
                } else if ("买".equals(trade.getSaleOrBuy())) {
                    // 出金：减少USDT余额
                    usdtBalance = usdtBalance.subtract(trade.getBaseNum());
                }
            } else {
                // 非USDT交易：买入/卖出其他币种
                Integer coinId = trade.getBaseId();
                BigDecimal currentHolding = holdings.getOrDefault(coinId, BigDecimal.ZERO);
                
                if ("买".equals(trade.getSaleOrBuy())) {
                    // 买入：增加持仓，减少USDT余额
                    currentHolding = currentHolding.add(trade.getBaseNum());
                    usdtBalance = usdtBalance.subtract(trade.getQuoteNum());
                } else if ("卖".equals(trade.getSaleOrBuy())) {
                    // 卖出：减少持仓，增加USDT余额
                    currentHolding = currentHolding.subtract(trade.getBaseNum());
                    usdtBalance = usdtBalance.add(trade.getQuoteNum());
                }
                
                holdings.put(coinId, currentHolding);
            }
        }
        
        // 步骤2: 计算各币种（包括USDT）当前价值和总额
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<Integer, BigDecimal> coinValues = new HashMap<>();
        
        // 计算非USDT币种价值
        for (CMCQuotesLatest quote : quotes) {
            Integer coinId = quote.getTid();
            // 跳过USDT
            if (CoinConstants.USDT_COIN_ID.equals(coinId)) {
                continue;
            }
            
            BigDecimal holding = holdings.getOrDefault(coinId, BigDecimal.ZERO);
            if (holding.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal price = quote.getPrice() == null ? BigDecimal.ZERO : quote.getPrice();
                BigDecimal value = holding.multiply(price);
                coinValues.put(coinId, value);
                totalValue = totalValue.add(value);
            }
        }
        
        // USDT价值（余额×1）
        if (usdtBalance.compareTo(BigDecimal.ZERO) > 0) {
            coinValues.put(CoinConstants.USDT_COIN_ID, usdtBalance);
            totalValue = totalValue.add(usdtBalance);
        }
        
        // 步骤3: 构建饼图数据
        boolean hideSmall = shouldHideSmallCoins();
        BigDecimal threshold = getMinValueThreshold();
        BigDecimal otherValue = BigDecimal.ZERO;
        
        // 先处理非USDT币种
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
        }
        
        // 添加"其他"分类
        if (otherValue.compareTo(BigDecimal.ZERO) > 0) {
            double otherPercentage = otherValue.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
            result.add(new PieChart.Data(
                String.format("其他 (%.2f%%)", otherPercentage), 
                otherValue.doubleValue()
            ));
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
