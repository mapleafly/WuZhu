package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.pojo.CMCQuotesLatestJpa;

import java.util.List;

/**
 * @ClassName IPATableJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 17:07
 * @Version 1.0
 */
public interface IPATableJpaService {
    List<PATableVO> queryAllVos();

    List<PATableVO> queryVOBy(String strCoinSymbol, String strStartDate, String strEndDate, String tradeType);

    List<String> queryCurSymbol();

    CMCQuotesLatestJpa queryBySymbol(String symbol);

}
