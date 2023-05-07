package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.CMCMapJpa;
import org.lifxue.wuzhu.pojo.TradeInfoJpa;

import java.util.List;

/**
 * @ClassName ITradeInfoJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 16:11
 * @Version 1.0
 */
public interface ITradeInfoJpaService {
    public List<String> queryCurSymbol();
    List<CoinChoiceBoxVO> queryCurCoin();
    List<TradeInfoVO> queryTradeInfoByBaseSymbol(String symbol);
    List<TradeInfoVO> queryTradeInfoByBaseCoinId(Integer coinId);

    CMCMapJpa queryCoinBySymbol(String symbol);

    void truncate();

    boolean saveBatch(List<String[]> list);

    List<TradeInfoJpa> findOrderByTradeDate();
    List<TradeInfoJpa> findByTradeDateBetweenOrderByTradeDateDesc(String startDate, String endDate);
}
