package org.lifxue.wuzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.entity.CMCMap;
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.mapper.TradeInfoMapper;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @version 1.0
 * @classname ITradeInfoServiceImpl
 * @description 交易数据实现类
 * @auhthor lifxue
 * @date 2023/2/12 15:58
 */

@Slf4j
@Component
public class ITradeInfoServiceImpl extends ServiceImpl<TradeInfoMapper, TradeInfo> implements ITradeInfoService {

    ICMCMapService icmcMapService;

    TradeInfoMapper tradeInfoMapper;

    @Autowired
    public void setIcmcMapService(ICMCMapService icmcMapService, TradeInfoMapper tradeInfoMapper) {
        this.icmcMapService = icmcMapService;
        this.tradeInfoMapper = tradeInfoMapper;
    }

    /***
     * @description 查询可用coin的symbo集合
     * @author lifxue
     * @date 2023/2/12 17:59
     * @param
     * @return java.util.List<java.lang.String>
     **/
    @Override
    public List<String> queryCurSymbol() {
       return icmcMapService.queryCurSymbol();
    }

    /***
     * @description 查询指定的币种信息并转变成TradeInfoVO列表格式
     * @author lifxue
     * @date 2023/2/12 18:38
     * @param symbol
     * @return java.util.List<org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO>
     **/
    @Override
    public List<TradeInfoVO> queryTradeInfo(String symbol) {
        QueryWrapper<TradeInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("base_symbol", symbol);
        wrapper.orderByDesc("id");
        List<TradeInfo> tradeInfoList = list(wrapper);
        return CopyUtil.copyTradeInfoVOList(tradeInfoList);
    }

    @Override
    public CMCMap queryCoinBySymbol(String symbol) {
        return icmcMapService.queryCoinBySymbo(symbol);
    }

    @Override
    public void truncate() {
        tradeInfoMapper.truncate();
    }

    @Override
    public boolean saveBatch(List<String[]> list) {
        List<TradeInfo> tradeInfoList = CopyUtil.copyTradeInfoList(list);
        return saveBatch(tradeInfoList);
    }

    @Override
    public List<TradeInfo> list(Wrapper<TradeInfo> queryWrapper) {
        return super.list(queryWrapper);
    }
}
