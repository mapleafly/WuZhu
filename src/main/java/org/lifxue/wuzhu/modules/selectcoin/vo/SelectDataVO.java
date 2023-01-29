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
package org.lifxue.wuzhu.modules.selectcoin.vo;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author xuelf
 */
public class SelectDataVO {

    private final SimpleIntegerProperty id;
    private final SimpleBooleanProperty select;
    private final SimpleStringProperty name;
    private final SimpleStringProperty symbol;
    private final SimpleIntegerProperty rank;
    // private final SimpleStringProperty price;
    private final SimpleStringProperty date;

    public SelectDataVO() {
        this(0, false, null, null, 0, null);
    }

    public SelectDataVO(
        Integer id, boolean select, String name, String symbol, Integer rank, String date) {
        this.id = new SimpleIntegerProperty(id);
        this.select = new SimpleBooleanProperty(select);
        this.name = new SimpleStringProperty(name);
        this.symbol = new SimpleStringProperty(symbol);
        this.rank = new SimpleIntegerProperty(rank);
        // this.price = new SimpleStringProperty(price);
        this.date = new SimpleStringProperty(date);
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id.get();
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    /**
     * @return the name
     */
    public boolean getSelect() {
        return select.get();
    }

    /**
     * @param select 1
     * @Description: set select
     * @return: void
     * @author: mapleaf
     * @date: 2020/6/23 17:37
     */
    public void setSelect(boolean select) {
        this.select.set(select);
    }

    /**
     * @Description: set select
     * @return: javafx.beans.property.SimpleBooleanProperty
     * @author: mapleaf
     * @date: 2020/6/23 18:41
     */
    public SimpleBooleanProperty selectProperty() {
        return select;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name.get();
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
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

    /**
     * @return the rank
     */
    public Integer getRank() {
        return rank.get();
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(Integer rank) {
        this.rank.set(rank);
    }

    public SimpleIntegerProperty rankProperty() {
        return rank;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date.get();
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date.set(date);
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    @Override
    public String toString() {
        return "CoinType [id:"
            + getId()
            + ",select:"
            + getSelect()
            + ",name:"
            + getName()
            + ",symbol:"
            + getSymbol()
            + ",rank:"
            + getRank()
            + ",last updated:"
            + this.getDate()
            + "]";
    }
}
