package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;

import java.util.List;

/**
 * @ClassName ICashService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/13 17:04
 * @Version 1.0
 */
public interface ICashService {

    List<TradeInfoVO> queryTradeInfoByBaseCoinId(Integer coinId);
}
