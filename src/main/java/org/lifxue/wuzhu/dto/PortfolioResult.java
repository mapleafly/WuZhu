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

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 投资组合盈亏计算结果DTO
 *
 * @author lif
 */
@Data
@Builder
public class PortfolioResult {

    /**
     * 持仓数量
     */
    private BigDecimal quantity;

    /**
     * 平均成本
     */
    private BigDecimal averageCost;

    /**
     * 当前市值
     */
    private BigDecimal currentValue;

    /**
     * 盈亏金额
     */
    private BigDecimal profitLoss;

    /**
     * 盈亏百分比
     */
    private BigDecimal profitLossPercent;
}
