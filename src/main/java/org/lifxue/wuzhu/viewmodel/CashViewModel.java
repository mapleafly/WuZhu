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
import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.lifxue.wuzhu.service.ICashService;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CashViewModel {

    private ITradeInfoService tradeInfoService;
    private ICashService cashService;

    private final ObservableList<TradeInfoVO> cashDataList = FXCollections.observableArrayList();
    private final ObservableList<CoinChoiceBoxVO> coinList = FXCollections.observableArrayList();
    private final ObservableList<String> transactionTypes = FXCollections.observableArrayList("入金", "出金");

    private final ObjectProperty<CoinChoiceBoxVO> selectedCoin = new SimpleObjectProperty<>();
    private final StringProperty transactionType = new SimpleStringProperty("入金");
    private final ObjectProperty<LocalDate> transactionDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty();

    private final ObjectProperty<TradeInfoVO> selectedRecord = new SimpleObjectProperty<>();

    @Autowired
    public void setTradeInfoService(ITradeInfoService tradeInfoService) {
        this.tradeInfoService = tradeInfoService;
    }

    @Autowired
    public void setCashService(ICashService cashService) {
        this.cashService = cashService;
    }

    public ObservableList<TradeInfoVO> getCashDataList() {
        return cashDataList;
    }

    public ObservableList<CoinChoiceBoxVO> getCoinList() {
        return coinList;
    }

    public ObservableList<String> getTransactionTypes() {
        return transactionTypes;
    }

    public ObjectProperty<CoinChoiceBoxVO> selectedCoinProperty() {
        return selectedCoin;
    }

    public CoinChoiceBoxVO getSelectedCoin() {
        return selectedCoin.get();
    }

    public void setSelectedCoin(CoinChoiceBoxVO coin) {
        this.selectedCoin.set(coin);
    }

    public StringProperty transactionTypeProperty() {
        return transactionType;
    }

    public String getTransactionType() {
        return transactionType.get();
    }

    public void setTransactionType(String type) {
        this.transactionType.set(type);
    }

    public ObjectProperty<LocalDate> transactionDateProperty() {
        return transactionDate;
    }

    public LocalDate getTransactionDate() {
        return transactionDate.get();
    }

    public void setTransactionDate(LocalDate date) {
        this.transactionDate.set(date);
    }

    public ObjectProperty<BigDecimal> amountProperty() {
        return amount;
    }

    public BigDecimal getAmount() {
        return amount.get();
    }

    public void setAmount(BigDecimal amount) {
        this.amount.set(amount);
    }

    public ObjectProperty<TradeInfoVO> selectedRecordProperty() {
        return selectedRecord;
    }

    public TradeInfoVO getSelectedRecord() {
        return selectedRecord.get();
    }

    public void setSelectedRecord(TradeInfoVO record) {
        this.selectedRecord.set(record);
        if (record != null) {
            populateFormFromRecord(record);
        } else {
            resetForm();
        }
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage.get();
    }

    public void setErrorMessage(String message) {
        this.errorMessage.set(message);
    }

    public void initializeCoins() {
        coinList.clear();
        coinList.add(new CoinChoiceBoxVO(CoinConstants.USDT_SYMBOL, CoinConstants.USDT_COIN_ID));
    }

    public void loadCashData(Integer coinId) {
        if (cashService == null || coinId == null) {
            return;
        }
        cashDataList.clear();
        List<TradeInfoVO> list = cashService.queryTradeInfoByBaseCoinId(coinId);
        if (list != null) {
            cashDataList.addAll(list);
        }
    }

    public boolean saveCashRecord() {
        if (!validateInput()) {
            return false;
        }

        if (tradeInfoService == null) {
            setErrorMessage("服务未初始化");
            return false;
        }

        TradeInfo tradeInfo = new TradeInfo();
        populateTradeInfoFromForm(tradeInfo);

        boolean success = tradeInfoService.save(tradeInfo);
        if (success) {
            cashDataList.add(0, CopyUtil.copyforCash(tradeInfo));
            resetForm();
        }

        return success;
    }

    public boolean updateCashRecord(Integer recordId) {
        if (!validateInput()) {
            return false;
        }

        if (tradeInfoService == null || recordId == null) {
            return false;
        }

        TradeInfo tradeInfo = tradeInfoService.findById(recordId);
        if (tradeInfo == null) {
            setErrorMessage("未找到要更新的记录");
            return false;
        }

        populateTradeInfoFromForm(tradeInfo);

        boolean success = tradeInfoService.save(tradeInfo);
        if (success) {
            TradeInfoVO updatedVO = CopyUtil.copyforCash(tradeInfo);
            for (int i = 0; i < cashDataList.size(); i++) {
                if (cashDataList.get(i).getId().equals(recordId)) {
                    cashDataList.set(i, updatedVO);
                    break;
                }
            }
        }

        return success;
    }

    public boolean deleteCashRecord(Integer recordId) {
        if (tradeInfoService == null || recordId == null) {
            return false;
        }

        boolean success = tradeInfoService.deleteById(recordId);
        if (success) {
            cashDataList.removeIf(record -> record.getId().equals(recordId));
        }

        return success;
    }

    private boolean validateInput() {
        StringBuilder error = new StringBuilder();

        if (getSelectedCoin() == null) {
            error.append("无效的类别!\n");
        }

        if (getTransactionType() == null || getTransactionType().isEmpty()) {
            error.append("无效的出入金类型!\n");
        }

        if (getTransactionDate() == null) {
            error.append("无效的时间!\n");
        }

        if (getAmount() == null || getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            error.append("无效的数量!\n");
        }

        if (error.length() > 0) {
            setErrorMessage(error.toString());
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    private void populateTradeInfoFromForm(TradeInfo tradeInfo) {
        if (getSelectedCoin() != null) {
            tradeInfo.setBaseId(getSelectedCoin().getCoinId());
            tradeInfo.setBaseSymbol(getSelectedCoin().getSymbol());
        }

        tradeInfo.setQuoteId(CoinConstants.USDT_COIN_ID);
        tradeInfo.setQuoteSymbol(CoinConstants.USDT_SYMBOL);

        if ("入金".equals(getTransactionType())) {
            tradeInfo.setSaleOrBuy("卖");
        } else {
            tradeInfo.setSaleOrBuy("买");
        }

        tradeInfo.setPrice(BigDecimal.ONE);
        tradeInfo.setBaseNum(getAmount());
        tradeInfo.setQuoteNum(getAmount());
        tradeInfo.setTradeDate(getTransactionDate().toString());
    }

    private void populateFormFromRecord(TradeInfoVO record) {
        if (record == null) {
            return;
        }

        setSelectedCoin(new CoinChoiceBoxVO(CoinConstants.USDT_SYMBOL, CoinConstants.USDT_COIN_ID));
        setTransactionType("卖".equals(record.getSaleOrBuy()) ? "入金" : "出金");
        setAmount(parseBigDecimal(record.getBaseNum()));
        setTransactionDate(parseLocalDate(record.getDate()));
    }

    public void resetForm() {
        setTransactionType("入金");
        setAmount(null);
        setTransactionDate(LocalDate.now());
        setSelectedRecord(null);
        setErrorMessage(null);
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
