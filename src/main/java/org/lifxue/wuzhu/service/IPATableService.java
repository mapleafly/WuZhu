package org.lifxue.wuzhu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lifxue.wuzhu.entity.CMCQuotesLatest;
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;

import java.util.List;

/**
 * @version 1.0
 * @classname IPATableService
 * @description 数据分析
 * @auhthor lifxue
 * @date 2023/3/14 14:33
 */
public interface IPATableService  extends IService<TradeInfo> {

    List<PATableVO> queryAllVos();
    List<PATableVO> queryVOBy(String strCoinSymbol, String strStartDate, String strEndDate, String tradeType);
    List<String> queryCurSymbol();

    CMCQuotesLatest queryBySymbol(String symbol);

}
