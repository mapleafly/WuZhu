package org.lifxue.wuzhu.modules.tradeinfo.vo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @version 1.0
 * @classname CoinChoiceBoxVO
 * @description 品种选择框数据类
 * @auhthor lifxue
 * @date 2023/3/26 14:28
 */
public class CoinChoiceBoxVO {
    private SimpleIntegerProperty coinId;
    private SimpleStringProperty symbol;

    public CoinChoiceBoxVO(String symbol,int coinId){
        //setSymbol(symbol);
        //setCoinId(coinId);
        //this.symbol.set(symbol);
        //this.coinId.set(coinId);
        this.symbol = new SimpleStringProperty(symbol);
        this.coinId = new SimpleIntegerProperty(coinId);
    }


    /**
     * @return the coinId
     */
    public Integer getCoinId() {
        return coinId.get();
    }

    /**
     * @param coinId the rank to set
     */
    public void setCoinId(Integer coinId) {
        this.coinId.set(coinId);
    }

    public SimpleIntegerProperty coinIdProperty() {
        return coinId;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol.get();
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(String symbol) {
        this.symbol.set(symbol);
    }

    public SimpleStringProperty symbolProperty() {
        return symbol;
    }
}
