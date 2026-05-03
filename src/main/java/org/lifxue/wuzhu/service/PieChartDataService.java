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

import org.lifxue.wuzhu.dto.PieChartItem;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 饼图数据服务
 * 负责构建投资组合饼图数据
 *
 * @author lif
 */
@Service
public class PieChartDataService {

    /**
     * 构建投资组合饼图数据
     *
     * @param trades 交易列表
     * @param currentPrices 当前价格映射（币种符号 -> 价格）
     * @param hideSmallCoins 是否隐藏小额币种
     * @param threshold 小额币种阈值
     * @return 饼图数据项列表
     */
    public List<PieChartItem> buildPortfolioData(
            List<TradeInfo> trades,
            Map<String, BigDecimal> currentPrices,
            boolean hideSmallCoins,
            BigDecimal threshold) {

        // 聚合各币种持仓
        Map<String, BigDecimal> portfolio = new HashMap<>();

        for (TradeInfo trade : trades) {
            String symbol = trade.getBaseSymbol();
            BigDecimal quantity = portfolio.getOrDefault(symbol, BigDecimal.ZERO);

            if ("买".equals(trade.getSaleOrBuy())) {
                quantity = quantity.add(trade.getBaseNum());
            } else {
                quantity = quantity.subtract(trade.getBaseNum());
            }
            portfolio.put(symbol, quantity);
        }

        // 计算市值并过滤小额币种
        List<PieChartItem> items = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : portfolio.entrySet()) {
            BigDecimal price = currentPrices.get(entry.getKey());
            if (price != null) {
                BigDecimal value = entry.getValue().multiply(price);
                if (!hideSmallCoins || value.compareTo(threshold) >= 0) {
                    items.add(new PieChartItem(entry.getKey(), value));
                    totalValue = totalValue.add(value);
                }
            }
        }

        // 计算百分比
        for (PieChartItem item : items) {
            item.setPercent(item.getValue().divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        }

        return items;
    }
}
