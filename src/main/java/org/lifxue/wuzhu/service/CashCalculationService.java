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

import org.lifxue.wuzhu.dto.CashSummary;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 现金计算服务
 * 负责现金流相关计算
 *
 * @author lif
 */
@Service
public class CashCalculationService {

    /**
     * 计算现金流汇总
     *
     * @param cashRecords 现金交易记录列表
     * @return 现金流汇总结果
     */
    public CashSummary calculateSummary(List<TradeInfo> cashRecords) {
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalWithdrawal = BigDecimal.ZERO;

        for (TradeInfo record : cashRecords) {
            if ("入金".equals(record.getSaleOrBuy())) {
                totalDeposit = totalDeposit.add(record.getQuoteNum());
            } else if ("出金".equals(record.getSaleOrBuy())) {
                totalWithdrawal = totalWithdrawal.add(record.getQuoteNum());
            }
        }

        return CashSummary.builder()
            .totalDeposit(totalDeposit)
            .totalWithdrawal(totalWithdrawal)
            .netCashFlow(totalDeposit.subtract(totalWithdrawal))
            .build();
    }
}
