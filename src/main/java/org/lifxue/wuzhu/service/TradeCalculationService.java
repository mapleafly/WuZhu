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

import org.lifxue.wuzhu.constant.AppConstants;
import org.lifxue.wuzhu.dto.PortfolioResult;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 交易计算服务
 * 负责投资组合盈亏相关计算
 *
 * @author lif
 */
@Service
public class TradeCalculationService {

    /**
     * 计算投资组合盈亏
     *
     * @param trades 交易列表
     * @param currentPrice 当前价格
     * @return 投资组合计算结果
     */
    public PortfolioResult calculatePortfolio(List<TradeInfo> trades, BigDecimal currentPrice) {
        BigDecimal totalBuy = BigDecimal.ZERO;
        BigDecimal totalSale = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;

        for (TradeInfo trade : trades) {
            if ("买".equals(trade.getSaleOrBuy())) {
                quantity = quantity.add(trade.getBaseNum());
                totalBuy = totalBuy.add(trade.getQuoteNum());
            } else {
                quantity = quantity.subtract(trade.getBaseNum());
                totalSale = totalSale.add(trade.getQuoteNum());
            }
        }

        BigDecimal averageCost = quantity.compareTo(BigDecimal.ZERO) > 0
            ? totalBuy.subtract(totalSale).divide(quantity, AppConstants.DEFAULT_SCALE, AppConstants.DEFAULT_ROUNDING_MODE)
            : BigDecimal.ZERO;

        BigDecimal currentValue = quantity.multiply(currentPrice);
        BigDecimal profitLoss = currentValue.subtract(totalBuy).add(totalSale);
        BigDecimal profitLossPercent = totalBuy.compareTo(BigDecimal.ZERO) > 0
            ? profitLoss.divide(totalBuy, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;

        return PortfolioResult.builder()
            .quantity(quantity)
            .averageCost(averageCost)
            .currentValue(currentValue)
            .profitLoss(profitLoss)
            .profitLossPercent(profitLossPercent)
            .build();
    }
}
