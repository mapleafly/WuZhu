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
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 交易信息模块ViewModel
 * 管理交易信息的UI状态和业务逻辑
 *
 * @author xuelf
 * @date 2025/05/03
 */
@Component
public class TradeInfoViewModel {

    private ITradeInfoService tradeInfoService;

    // 数据列表
    private final ObservableList<TradeInfoVO> tradeDataList = FXCollections.observableArrayList();
    private final ObservableList<CoinChoiceBoxVO> coinList = FXCollections.observableArrayList();

    // 选中的币种
    private final ObjectProperty<CoinChoiceBoxVO> selectedBaseCoin = new SimpleObjectProperty<>();

    // 错误消息
    private final StringProperty errorMessage = new SimpleStringProperty();

    // 表单字段绑定
    private final ObjectProperty<BigDecimal> price = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> baseNum = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quoteNum = new SimpleObjectProperty<>();
    private final StringProperty tradeType = new SimpleStringProperty("买");
    private final ObjectProperty<LocalDate> tradeDate = new SimpleObjectProperty<>(LocalDate.now());

    // 加载状态
    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    // 当前选中的交易记录（用于编辑）
    private final ObjectProperty<TradeInfoVO> selectedTrade = new SimpleObjectProperty<>();

    @Autowired
    public void setTradeInfoService(ITradeInfoService tradeInfoService) {
        this.tradeInfoService = tradeInfoService;
    }

    // ObservableList getters
    public ObservableList<TradeInfoVO> getTradeDataList() {
        return tradeDataList;
    }

    public ObservableList<CoinChoiceBoxVO> getCoinList() {
        return coinList;
    }

    // Property accessors
    public ObjectProperty<CoinChoiceBoxVO> selectedBaseCoinProperty() {
        return selectedBaseCoin;
    }

    public CoinChoiceBoxVO getSelectedBaseCoin() {
        return selectedBaseCoin.get();
    }

    public void setSelectedBaseCoin(CoinChoiceBoxVO coin) {
        this.selectedBaseCoin.set(coin);
    }

    public ObjectProperty<TradeInfoVO> selectedTradeProperty() {
        return selectedTrade;
    }

    public TradeInfoVO getSelectedTrade() {
        return selectedTrade.get();
    }

    public void setSelectedTrade(TradeInfoVO trade) {
        this.selectedTrade.set(trade);
        if (trade != null) {
            populateFormFromTrade(trade);
        } else {
            resetForm();
        }
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

    public ObjectProperty<BigDecimal> priceProperty() {
        return price;
    }

    public BigDecimal getPrice() {
        return price.get();
    }

    public void setPrice(BigDecimal price) {
        this.price.set(price);
    }

    public ObjectProperty<BigDecimal> baseNumProperty() {
        return baseNum;
    }

    public BigDecimal getBaseNum() {
        return baseNum.get();
    }

    public void setBaseNum(BigDecimal baseNum) {
        this.baseNum.set(baseNum);
    }

    public ObjectProperty<BigDecimal> quoteNumProperty() {
        return quoteNum;
    }

    public BigDecimal getQuoteNum() {
        return quoteNum.get();
    }

    public void setQuoteNum(BigDecimal quoteNum) {
        this.quoteNum.set(quoteNum);
    }

    public StringProperty tradeTypeProperty() {
        return tradeType;
    }

    public String getTradeType() {
        return tradeType.get();
    }

    public void setTradeType(String tradeType) {
        this.tradeType.set(tradeType);
    }

    public ObjectProperty<LocalDate> tradeDateProperty() {
        return tradeDate;
    }

    public LocalDate getTradeDate() {
        return tradeDate.get();
    }

    public void setTradeDate(LocalDate date) {
        this.tradeDate.set(date);
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public boolean isLoading() {
        return loading.get();
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    /**
     * 加载币种列表
     */
    public void loadCoins() {
        if (tradeInfoService == null) {
            return;
        }
        coinList.clear();
        List<CoinChoiceBoxVO> coins = tradeInfoService.queryCurCoin();
        if (coins != null) {
            coinList.addAll(coins);
        }
    }

    /**
     * 根据币种ID加载交易数据
     *
     * @param coinId 币种ID
     */
    public void loadTradeData(Integer coinId) {
        if (tradeInfoService == null || coinId == null) {
            return;
        }
        tradeDataList.clear();
        List<TradeInfoVO> trades = tradeInfoService.queryTradeInfoByBaseCoinId(coinId);
        if (trades != null) {
            tradeDataList.addAll(trades);
        }
    }

    /**
     * 重新加载当前选中币种的交易数据
     */
    public void reloadCurrentTradeData() {
        if (getSelectedBaseCoin() != null) {
            loadTradeData(getSelectedBaseCoin().getCoinId());
        }
    }

    /**
     * 验证输入数据
     *
     * @return 验证结果
     */
    public boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (getSelectedBaseCoin() == null) {
            errorMessage.append("无效的类别!\n");
        }

        if (getTradeType() == null || getTradeType().isEmpty()) {
            errorMessage.append("无效的买/卖!\n");
        }

        if (getTradeDate() == null) {
            errorMessage.append("无效的时间!\n");
        }

        if (getPrice() == null || getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errorMessage.append("无效的单价!\n");
        }

        if (getBaseNum() == null || getBaseNum().compareTo(BigDecimal.ZERO) <= 0) {
            errorMessage.append("无效的数量!\n");
        }

        if (getQuoteNum() == null || getQuoteNum().compareTo(BigDecimal.ZERO) <= 0) {
            errorMessage.append("无效的总价!\n");
        }

        if (errorMessage.length() > 0) {
            setErrorMessage(errorMessage.toString());
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    /**
     * 保存交易记录（新增）
     *
     * @return 是否保存成功
     */
    public boolean saveTrade() {
        if (!validateInput()) {
            return false;
        }

        if (tradeInfoService == null) {
            setErrorMessage("服务未初始化");
            return false;
        }

        TradeInfo tradeInfo = createTradeInfoFromForm();
        boolean success = tradeInfoService.save(tradeInfo);

        if (success) {
            tradeDataList.add(0, CopyUtil.copyjpa(tradeInfo));
            resetForm();
        }

        return success;
    }

    /**
     * 更新交易记录
     *
     * @param tradeId 交易记录ID
     * @return 是否更新成功
     */
    public boolean updateTrade(Integer tradeId) {
        if (!validateInput()) {
            return false;
        }

        if (tradeInfoService == null || tradeId == null) {
            return false;
        }

        TradeInfo tradeInfo = tradeInfoService.findById(tradeId);
        if (tradeInfo == null) {
            setErrorMessage("未找到要更新的记录");
            return false;
        }

        populateTradeInfoFromForm(tradeInfo);

        boolean success = tradeInfoService.save(tradeInfo);
        if (success) {
            // 更新列表中的记录
            TradeInfoVO updatedVO = CopyUtil.copyjpa(tradeInfo);
            for (int i = 0; i < tradeDataList.size(); i++) {
                if (tradeDataList.get(i).getId().equals(tradeId)) {
                    tradeDataList.set(i, updatedVO);
                    break;
                }
            }
        }

        return success;
    }

    /**
     * 删除交易记录
     *
     * @param tradeId 交易记录ID
     * @return 是否删除成功
     */
    public boolean deleteTrade(Integer tradeId) {
        if (tradeInfoService == null || tradeId == null) {
            return false;
        }

        boolean success = tradeInfoService.deleteById(tradeId);
        if (success) {
            tradeDataList.removeIf(trade -> trade.getId().equals(tradeId));
        }

        return success;
    }

    /**
     * 计算总价（价格×数量）
     */
    public void calculateQuoteNum() {
        BigDecimal currentPrice = getPrice();
        BigDecimal currentBaseNum = getBaseNum();

        if (currentPrice != null && currentBaseNum != null) {
            setQuoteNum(currentPrice.multiply(currentBaseNum));
        }
    }

    /**
     * 重置表单
     */
    public void resetForm() {
        setPrice(null);
        setBaseNum(null);
        setQuoteNum(null);
        setTradeType("买");
        setTradeDate(LocalDate.now());
        setSelectedTrade(null);
        setErrorMessage(null);
    }

    /**
     * 从交易记录填充表单
     *
     * @param trade 交易记录
     */
    private void populateFormFromTrade(TradeInfoVO trade) {
        if (trade == null) {
            return;
        }

        setPrice(parseBigDecimal(trade.getPrice()));
        setBaseNum(parseBigDecimal(trade.getBaseNum()));
        setQuoteNum(parseBigDecimal(trade.getQuoteNum()));
        setTradeType(trade.getSaleOrBuy());
        setTradeDate(parseLocalDate(trade.getDate()));
    }

    /**
     * 创建TradeInfo实体从表单数据
     *
     * @return TradeInfo实体
     */
    private TradeInfo createTradeInfoFromForm() {
        TradeInfo tradeInfo = new TradeInfo();
        populateTradeInfoFromForm(tradeInfo);
        return tradeInfo;
    }

    /**
     * 填充TradeInfo实体从表单数据
     *
     * @param tradeInfo 要填充的实体
     */
    private void populateTradeInfoFromForm(TradeInfo tradeInfo) {
        if (getSelectedBaseCoin() != null) {
            tradeInfo.setBaseId(getSelectedBaseCoin().getCoinId());
            tradeInfo.setBaseSymbol(getSelectedBaseCoin().getSymbol());
        }

        tradeInfo.setQuoteId(CoinConstants.USDT_COIN_ID);
        tradeInfo.setQuoteSymbol(CoinConstants.USDT_SYMBOL);
        tradeInfo.setSaleOrBuy(getTradeType());
        tradeInfo.setPrice(getPrice());
        tradeInfo.setBaseNum(getBaseNum());
        tradeInfo.setQuoteNum(getQuoteNum() != null ? getQuoteNum() : calculateQuoteNumValue());
        tradeInfo.setTradeDate(getTradeDate() != null ? getTradeDate().toString() : LocalDate.now().toString());
    }

    /**
     * 计算总价数值
     *
     * @return 总价
     */
    private BigDecimal calculateQuoteNumValue() {
        BigDecimal currentPrice = getPrice();
        BigDecimal currentBaseNum = getBaseNum();

        if (currentPrice != null && currentBaseNum != null) {
            return currentPrice.multiply(currentBaseNum);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 解析BigDecimal
     *
     * @param value 字符串值
     * @return BigDecimal
     */
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

    /**
     * 解析LocalDate
     *
     * @param value 日期字符串
     * @return LocalDate
     */
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
