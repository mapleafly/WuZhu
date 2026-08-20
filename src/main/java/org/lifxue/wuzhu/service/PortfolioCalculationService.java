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
package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.constant.CoinConstants;
import org.lifxue.wuzhu.dto.PortfolioValuation;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投资组合估值计算服务
 *
 * <p>统一计算各币种持仓、USDT 余额、各币种当前价值与组合总价值，
 * 供"数据图例"（饼图）与"数据分析"页面共享，保证两处"当前总价值"一致。</p>
 *
 * @author lif
 */
@Service
public class PortfolioCalculationService {

    /**
     * 计算投资组合当前估值
     *
     * <p>USDT 余额为当前可用余额：
     * 入金/出金（USDT 记录）+ 卖出币所得 - 买入币所花（非 USDT 交易），
     * 与"数据图例"饼图原有口径保持一致。</p>
     *
     * @param trades 全部交易记录（含 USDT 出入金记录）
     * @param quotes 最新行情数据（CMC_QUOTES_LATEST）
     * @return 组合估值结果
     */
    public PortfolioValuation calculate(List<TradeInfo> trades, List<CMCQuotesLatest> quotes) {
        // 步骤1: 计算各币种持仓数量和USDT余额
        Map<Integer, BigDecimal> holdings = new HashMap<>();
        BigDecimal usdtBalance = BigDecimal.ZERO;

        if (trades != null) {
            for (TradeInfo trade : trades) {
                if (CoinConstants.USDT_COIN_ID.equals(trade.getBaseId())) {
                    // USDT记录：入金/出金（入金存"卖"，出金存"买"）
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
        }

        // 步骤2: 计算各币种（不含USDT）当前价值和总额
        Map<Integer, BigDecimal> coinValues = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        if (quotes != null) {
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
        }

        // USDT 余额单独记录（不进入 coinValues，避免按市值重复计算）
        if (usdtBalance.compareTo(BigDecimal.ZERO) > 0) {
            totalValue = totalValue.add(usdtBalance);
        }

        return PortfolioValuation.builder()
            .holdings(holdings)
            .usdtBalance(usdtBalance)
            .coinValues(coinValues)
            .totalValue(totalValue)
            .build();
    }
}
