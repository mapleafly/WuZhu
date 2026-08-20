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
import java.util.Map;

/**
 * 投资组合当前估值结果 DTO
 *
 * <p>用于在"数据图例"（饼图）与"数据分析"页面之间共享一致的持仓与估值计算，
 * 保证两处展示的"当前总价值"完全一致。</p>
 *
 * @author lif
 */
@Data
@Builder
public class PortfolioValuation {

    /**
     * 各币种持仓数量（coinId -> 数量），不含 USDT
     */
    private Map<Integer, BigDecimal> holdings;

    /**
     * USDT 余额（出入金净额）
     */
    private BigDecimal usdtBalance;

    /**
     * 各币种当前价值（coinId -> 当前市值），不含 USDT
     */
    private Map<Integer, BigDecimal> coinValues;

    /**
     * 组合当前总价值 = Σ(各币种持仓 × 现价) + USDT 余额
     */
    private BigDecimal totalValue;
}
