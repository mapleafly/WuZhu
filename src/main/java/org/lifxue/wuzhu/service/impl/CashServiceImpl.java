package org.lifxue.wuzhu.service.impl;

import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.TradeInfoJpa;
import org.lifxue.wuzhu.repository.TradeInfoRepository;
import org.lifxue.wuzhu.service.ICashService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName CashServiceImpl
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/13 17:05
 * @Version 1.0
 */
@Service
public class CashServiceImpl implements ICashService {

    private TradeInfoRepository tradeInfoRepository;

    @Autowired
    public void setTradeInfoRepository(TradeInfoRepository tradeInfoRepository) {
        this.tradeInfoRepository = tradeInfoRepository;
    }

    @Override
    public List<TradeInfoVO> queryTradeInfoByBaseCoinId(Integer coinId) {
        List<TradeInfoJpa> tradeInfoList = tradeInfoRepository.findByBaseIdOrderByIdDesc(coinId);
        return CopyUtil.copyTradeInfoVOListForCash(tradeInfoList);
    }
}
