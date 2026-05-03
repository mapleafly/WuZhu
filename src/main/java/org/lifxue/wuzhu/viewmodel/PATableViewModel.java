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
import org.lifxue.wuzhu.constant.CoinConstants;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.service.CashCalculationService;
import org.lifxue.wuzhu.service.IPATableService;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PATableViewModel {

    private IPATableService paTableService;
    private CashCalculationService cashCalculationService;
    private ITradeInfoService tradeInfoService;

    private final ObservableList<PATableVO> paDataList = FXCollections.observableArrayList();
    private final ObservableList<String> availableSymbols = FXCollections.observableArrayList();
    private final ObservableList<String> tradeTypes = FXCollections.observableArrayList("全部", "买", "卖");

    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(LocalDate.now());
    private final StringProperty selectedSymbol = new SimpleStringProperty("全部品种");
    private final StringProperty selectedTradeType = new SimpleStringProperty("全部");

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty();

    private final StringProperty curCHG = new SimpleStringProperty();
    private final StringProperty nowPriceTotal = new SimpleStringProperty();
    private final StringProperty paPrice = new SimpleStringProperty();
    private final StringProperty numTotal = new SimpleStringProperty();
    private final StringProperty nowPrice = new SimpleStringProperty();
    private final StringProperty paPriceTotal = new SimpleStringProperty();

    @Autowired
    public void setPATableService(IPATableService paTableService) {
        this.paTableService = paTableService;
    }

    @Autowired
    public void setCashCalculationService(CashCalculationService cashCalculationService) {
        this.cashCalculationService = cashCalculationService;
    }

    @Autowired
    public void setTradeInfoService(ITradeInfoService tradeInfoService) {
        this.tradeInfoService = tradeInfoService;
    }

    public ObservableList<PATableVO> getPaDataList() {
        return paDataList;
    }

    public ObservableList<String> getAvailableSymbols() {
        return availableSymbols;
    }

    public ObservableList<String> getTradeTypes() {
        return tradeTypes;
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public void setStartDate(LocalDate date) {
        this.startDate.set(date);
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public LocalDate getEndDate() {
        return endDate.get();
    }

    public void setEndDate(LocalDate date) {
        this.endDate.set(date);
    }

    public StringProperty selectedSymbolProperty() {
        return selectedSymbol;
    }

    public String getSelectedSymbol() {
        return selectedSymbol.get();
    }

    public void setSelectedSymbol(String symbol) {
        this.selectedSymbol.set(symbol);
    }

    public StringProperty selectedTradeTypeProperty() {
        return selectedTradeType;
    }

    public String getSelectedTradeType() {
        return selectedTradeType.get();
    }

    public void setSelectedTradeType(String type) {
        this.selectedTradeType.set(type);
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public StringProperty curCHGProperty() {
        return curCHG;
    }

    public StringProperty nowPriceTotalProperty() {
        return nowPriceTotal;
    }

    public StringProperty paPriceProperty() {
        return paPrice;
    }

    public StringProperty numTotalProperty() {
        return numTotal;
    }

    public StringProperty nowPriceProperty() {
        return nowPrice;
    }

    public StringProperty paPriceTotalProperty() {
        return paPriceTotal;
    }

    public void loadAvailableSymbols() {
        if (paTableService == null) {
            return;
        }
        availableSymbols.clear();
        availableSymbols.add("全部品种");
        List<String> symbols = paTableService.queryCurSymbol();
        if (symbols != null) {
            availableSymbols.addAll(symbols);
        }
    }

    public void loadPAData() {
        if (paTableService == null) {
            return;
        }

        if (!validateInput()) {
            return;
        }

        loading.set(true);
        errorMessage.set(null);

        try {
            String symbol = getSelectedSymbol();
            String startDateStr = getStartDate() != null ? getStartDate().toString() : null;
            String endDateStr = getEndDate() != null ? getEndDate().toString() : null;
            String tradeType = getSelectedTradeType();

            if ("全部品种".equals(symbol)) {
                symbol = "";
            }

            paDataList.clear();
            List<PATableVO> list = paTableService.queryVOBy(symbol, startDateStr, endDateStr, tradeType);
            if (list != null) {
                paDataList.addAll(list);
            }

            updateStatistics(list, symbol);
        } catch (Exception e) {
            errorMessage.set("加载数据失败: " + e.getMessage());
        } finally {
            loading.set(false);
        }
    }

    private void updateStatistics(List<PATableVO> list, String symbol) {
        Map<String, String> stats;
        if (symbol != null && !symbol.isEmpty()) {
            stats = calculateSingleSymbolStats(symbol, list);
        } else {
            stats = calculateAllSymbolsStats(list);
        }

        curCHG.set(stats.getOrDefault("curCHG", ""));
        nowPriceTotal.set(stats.getOrDefault("nowPriceTotal", ""));
        paPrice.set(stats.getOrDefault("paPrice", ""));
        numTotal.set(stats.getOrDefault("numTotal", ""));
        nowPrice.set(stats.getOrDefault("nowPrice", ""));
        paPriceTotal.set(stats.getOrDefault("paPriceTotal", ""));
    }

    private Map<String, String> calculateSingleSymbolStats(String symbol, List<PATableVO> list) {
        Map<String, String> map = new HashMap<>();

        BigDecimal sale = BigDecimal.ZERO;
        BigDecimal numTotal = BigDecimal.ZERO;
        BigDecimal buy = BigDecimal.ZERO;

        if (list != null) {
            for (PATableVO td : list) {
                if ("买".equals(td.getSaleOrBuy())) {
                    numTotal = numTotal.add(new BigDecimal(td.getBaseNum()));
                    buy = buy.add(new BigDecimal(td.getQuoteNum()));
                } else if ("卖".equals(td.getSaleOrBuy())) {
                    numTotal = numTotal.subtract(new BigDecimal(td.getBaseNum()));
                    sale = sale.add(new BigDecimal(td.getQuoteNum()));
                }
            }
        }

        BigDecimal curPrice = BigDecimal.ZERO;
        if (paTableService != null) {
            var quote = paTableService.queryBySymbol(symbol);
            if (quote != null && quote.getPrice() != null) {
                curPrice = quote.getPrice();
            }
        }

        BigDecimal paPriceTotal = buy.subtract(sale);
        BigDecimal paPrice = numTotal.compareTo(BigDecimal.ZERO) > 0
            ? paPriceTotal.divide(numTotal, 8, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;

        map.put("numTotal", numTotal.setScale(8, BigDecimal.ROUND_HALF_UP).toPlainString());
        map.put("nowPriceTotal", numTotal.multiply(curPrice).setScale(8, BigDecimal.ROUND_HALF_UP).toPlainString());
        map.put("nowPrice", curPrice.setScale(8, BigDecimal.ROUND_HALF_UP).toPlainString());
        map.put("paPriceTotal", paPriceTotal.setScale(8, BigDecimal.ROUND_HALF_UP).toPlainString());
        map.put("paPrice", paPrice.toPlainString());

        String chg = "∞";
        if (paPrice.compareTo(BigDecimal.ZERO) <= 0) {
            chg = "+∞";
        } else {
            chg = curPrice.subtract(paPrice)
                .divide(paPrice, 5, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString();
        }
        map.put("curCHG", chg + "%");

        return map;
    }

    private Map<String, String> calculateAllSymbolsStats(List<PATableVO> list) {
        Map<String, String> map = new HashMap<>();

        BigDecimal totalBuy = BigDecimal.ZERO;
        BigDecimal totalSale = BigDecimal.ZERO;
        int transactionCount = 0;

        if (list != null) {
            transactionCount = list.size();
            for (PATableVO td : list) {
                if ("买".equals(td.getSaleOrBuy())) {
                    totalBuy = totalBuy.add(new BigDecimal(td.getQuoteNum()));
                } else if ("卖".equals(td.getSaleOrBuy())) {
                    totalSale = totalSale.add(new BigDecimal(td.getQuoteNum()));
                }
            }
        }

        // 计算出入金余额
        BigDecimal cashBalance = getCashBalance();

        // 总成本 = 买入总额 - 卖出总额
        BigDecimal totalInvestment = totalBuy.subtract(totalSale);
        // 当前总价值 = 交易盈亏 + 出入金余额
        BigDecimal nowTotalValue = totalInvestment.add(cashBalance);

        String chg = "0.00%";
        // 以总成本为基数计算收益率
        BigDecimal totalCost = totalBuy.compareTo(BigDecimal.ZERO) > 0 ? totalBuy : BigDecimal.ONE;
        BigDecimal profitPercent = nowTotalValue
            .divide(totalCost, 5, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, BigDecimal.ROUND_HALF_UP);
        chg = profitPercent.toPlainString() + "%";

        map.put("numTotal", transactionCount + " 笔交易");
        map.put("nowPriceTotal", nowTotalValue.setScale(8, BigDecimal.ROUND_HALF_UP).toPlainString());
        map.put("paPriceTotal", totalBuy.setScale(8, BigDecimal.ROUND_HALF_UP).toPlainString());
        map.put("curCHG", chg);
        map.put("paPrice", cashBalance.setScale(8, BigDecimal.ROUND_HALF_UP).toPlainString() + " (USDT余额)");

        return map;
    }

    /**
     * 获取当前出入金余额
     *
     * @return 余额
     */
    private BigDecimal getCashBalance() {
        if (cashCalculationService == null || tradeInfoService == null) {
            return BigDecimal.ZERO;
        }
        try {
            // 使用queryTradeInfoByBaseCoinId查询USDT记录
            List<TradeInfoVO> usdtRecords = tradeInfoService.queryTradeInfoByBaseCoinId(CoinConstants.USDT_COIN_ID);
            // 将TradeInfoVO转换为TradeInfo以兼容CashCalculationService
            BigDecimal totalDeposit = BigDecimal.ZERO;
            BigDecimal totalWithdrawal = BigDecimal.ZERO;
            for (TradeInfoVO record : usdtRecords) {
                if ("入金".equals(record.getSaleOrBuy())) {
                    totalDeposit = totalDeposit.add(new BigDecimal(record.getQuoteNum()));
                } else if ("出金".equals(record.getSaleOrBuy())) {
                    totalWithdrawal = totalWithdrawal.add(new BigDecimal(record.getQuoteNum()));
                }
            }
            return totalDeposit.subtract(totalWithdrawal);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean validateInput() {
        if (getStartDate() == null) {
            errorMessage.set("无效的起始时间!");
            return false;
        }
        if (getEndDate() == null) {
            errorMessage.set("无效的结束时间!");
            return false;
        }
        if (getStartDate().isAfter(getEndDate())) {
            errorMessage.set("起始时间不能晚于结束时间!");
            return false;
        }
        return true;
    }

    public void resetFilters() {
        setStartDate(LocalDate.of(2009, 1, 3));
        setEndDate(LocalDate.now());
        setSelectedSymbol("全部品种");
        setSelectedTradeType("全部");
    }
}
