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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        BigDecimal usdtNum = BigDecimal.ZERO;
        double otherAllPrice = 0;

        boolean notSmallCoin = shouldHideSmallCoins();
        BigDecimal threshold = getMinValueThreshold();

        for (CMCQuotesLatest coin : quotes) {
            Integer id = coin.getTid();
            String symbol = coin.getSymbol();
            BigDecimal price = coin.getPrice() == null ? BigDecimal.ZERO : coin.getPrice();

            BigDecimal buyNum = BigDecimal.ZERO;
            BigDecimal saleNum = BigDecimal.ZERO;

            for (TradeInfo trade : tradeInfos) {
                if (trade.getBaseId().intValue() == id.intValue()) {
                    if ("USDT".equals(symbol)) {
                        if ("买".equals(trade.getSaleOrBuy())) {
                            usdtNum = usdtNum.subtract(trade.getQuoteNum());
                        } else if ("卖".equals(trade.getSaleOrBuy())) {
                            usdtNum = usdtNum.add(trade.getQuoteNum());
                        }
                    } else {
                        if ("买".equals(trade.getSaleOrBuy())) {
                            buyNum = buyNum.add(trade.getBaseNum());
                            usdtNum = usdtNum.subtract(trade.getQuoteNum());
                        } else if ("卖".equals(trade.getSaleOrBuy())) {
                            saleNum = saleNum.add(trade.getBaseNum());
                            usdtNum = usdtNum.add(trade.getQuoteNum());
                        }
                    }
                }
            }

            BigDecimal holding = buyNum.subtract(saleNum);
            double value = holding.multiply(price)
                .setScale(AppConstants.DEFAULT_SCALE, AppConstants.DEFAULT_ROUNDING_MODE)
                .doubleValue();

            if (notSmallCoin && value <= threshold.doubleValue()) {
                otherAllPrice += value;
            } else {
                result.add(new PieChart.Data(symbol, value));
            }
        }

        if (otherAllPrice > 0) {
            result.add(new PieChart.Data("其他", otherAllPrice));
        }

        if (usdtNum.compareTo(BigDecimal.ZERO) != 0) {
            result.add(new PieChart.Data("USDT", usdtNum.doubleValue()));
        }

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
