/*
 * Copyright 2020 lif.
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
package org.lifxue.wuzhu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 饼图数据项DTO
 *
 * @author lif
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieChartItem {

    /**
     * 币种符号
     */
    private String symbol;

    /**
     * 市值
     */
    private BigDecimal value;

    /**
     * 百分比
     */
    private BigDecimal percent;

    public PieChartItem(String symbol, BigDecimal value) {
        this.symbol = symbol;
        this.value = value;
    }
}
