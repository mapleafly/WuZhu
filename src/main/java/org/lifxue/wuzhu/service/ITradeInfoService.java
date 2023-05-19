package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.lifxue.wuzhu.pojo.TradeInfo;

import java.util.List;

/**
 * @ClassName ITradeInfoJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 16:11
 * @Version 1.0
 */
public interface ITradeInfoService {
    public List<String> queryCurSymbol();
    List<CoinChoiceBoxVO> queryCurCoin();
    List<TradeInfoVO> queryTradeInfoByBaseSymbol(String symbol);
    List<TradeInfoVO> queryTradeInfoByBaseCoinId(Integer coinId);

    CMCMap queryCoinBySymbol(String symbol);

    void truncate();

    boolean saveBatch(List<String[]> list);

    List<TradeInfo> findOrderByTradeDate();
    List<TradeInfo> findByTradeDateBetweenOrderByTradeDateDesc(String startDate, String endDate);

    boolean save(TradeInfo tradeInfoJpa);

    TradeInfo findById(Integer id);

    boolean deleteById(Integer id);
    boolean delete();
}
