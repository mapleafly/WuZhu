package org.lifxue.wuzhu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lifxue.wuzhu.entity.CMCMap;
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;

import java.util.List;

/**
 * @version 1.0
 * @classname ITradeInfo
 * @description 交易信息服务接口
 * @auhthor lifxue
 * @date 2023/2/12 15:39
 */
public interface ITradeInfoService extends IService<TradeInfo> {

    public List<String> queryCurSymbol();

    List<TradeInfoVO> queryTradeInfo(String symbol);

    CMCMap queryCoinBySymbol(String symbol);
}
