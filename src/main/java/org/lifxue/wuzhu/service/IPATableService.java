package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;

import java.util.List;

/**
 * @ClassName IPATableJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 17:07
 * @Version 1.0
 */
public interface IPATableService {
    List<PATableVO> queryAllVos();

    List<PATableVO> queryVOBy(String strCoinSymbol, String strStartDate, String strEndDate, String tradeType);

    List<String> queryCurSymbol();

    CMCQuotesLatest queryBySymbol(String symbol);

}
