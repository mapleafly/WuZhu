package org.lifxue.wuzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.entity.CMCQuotesLatest;
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.mapper.TradeInfoMapper;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.service.IPATableService;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @classname PATableServiceImpl
 * @description
 * @auhthor lifxue
 * @date 2023/3/14 14:38
 */
@Slf4j
@Component
public class PATableServiceImpl extends ServiceImpl<TradeInfoMapper, TradeInfo> implements IPATableService {

    TradeInfoMapper tradeInfoMapper;
    ITradeInfoService iTradeInfoService;
    ICMCQuotesLatestService iCMCQuotesLatestService;
    ICMCMapService icmcMapService;

    public PATableServiceImpl(TradeInfoMapper tradeInfoMapper,
                              ITradeInfoService iTradeInfoService,
                              ICMCQuotesLatestService iCMCQuotesLatestService,
                              ICMCMapService icmcMapService) {
        this.tradeInfoMapper = tradeInfoMapper;
        this.iTradeInfoService = iTradeInfoService;
        this.iCMCQuotesLatestService = iCMCQuotesLatestService;
        this.icmcMapService = icmcMapService;
    }

    @Override
    public List<PATableVO> queryAllVos() {
        QueryWrapper<TradeInfo> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("trade_date");
        List<TradeInfo> tradeInfoList = list(wrapper);
        List<CMCQuotesLatest> cmcQuotesLatests = iCMCQuotesLatestService.queryLatest();
        /*log.info("queryAllVos cmcQuotesLatests={}", cmcQuotesLatests.size());
        for(CMCQuotesLatest cmcQuotesLatest : cmcQuotesLatests){
            log.info("queryAllVos cmcQuotesLatests={}", cmcQuotesLatest.toString());
        }*/

        List<PATableVO> paTableVOS = new ArrayList<PATableVO>();
        for (TradeInfo tradeInfo : tradeInfoList) {
            PATableVO paTableVO = CopyUtil.copy2(tradeInfo);
            for (CMCQuotesLatest cmcQuotesLatest : cmcQuotesLatests) {
                if (paTableVO.getCoinId().equals(cmcQuotesLatest.getTid())) {
                    BigDecimal curPrice = new BigDecimal(cmcQuotesLatest.getPrice());
                    BigDecimal payPrice = new BigDecimal(paTableVO.getPrice());
                    log.info("curPrice: " + curPrice);
                    log.info("payPrice: " + payPrice);

                    String chg = curPrice.subtract(payPrice)
                        .divide(payPrice, 5, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();
                    paTableVO.setChg(chg + "%");
                    log.info("chg: " + chg);

                    break;
                }
            }
            paTableVOS.add(paTableVO);
        }

        return paTableVOS;
    }

    @Override
    public List<PATableVO> queryVOBy(String strCoinSymbol, String strStartDate, String strEndDate, String tradeType) {
        QueryWrapper<TradeInfo> wrapper = new QueryWrapper<>();
        if (strCoinSymbol != null && !strCoinSymbol.isBlank()) {
            wrapper.eq("base_symbol", strCoinSymbol);
        }
        if (!tradeType.equals("全部")) {
            wrapper.eq("sale_or_buy", tradeType);
        }
        wrapper.between("trade_date", strStartDate, strEndDate);
        wrapper.orderByDesc("trade_date");
        List<TradeInfo> tradeInfoList = list(wrapper);
        //查询被选品种
        //List<CMCMap> cmcMaps = icmcMapService.queryAll(1);
        List<CMCQuotesLatest> cmcQuotesLatests = iCMCQuotesLatestService.queryLatest();
        List<PATableVO> paTableVOS = new ArrayList<PATableVO>();
        for (TradeInfo tradeInfo : tradeInfoList) {
            PATableVO paTableVO = CopyUtil.copy2(tradeInfo);
            for (CMCQuotesLatest cmcQuotesLatest : cmcQuotesLatests) {
                if (paTableVO.getCoinId().equals(cmcQuotesLatest.getTid())) {
                    BigDecimal curPrice = new BigDecimal(cmcQuotesLatest.getPrice());
                    BigDecimal payPrice = new BigDecimal(paTableVO.getPrice());
                    String chg = curPrice.subtract(payPrice)
                        .divide(payPrice, 5, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();
                    paTableVO.setChg(chg + "%");
                    break;
                }
            }
            paTableVOS.add(paTableVO);
        }

        return paTableVOS;
    }

    @Override
    public List<String> queryCurSymbol() {
        return iTradeInfoService.queryCurSymbol();
    }

    @Override
    public CMCQuotesLatest queryBySymbol(String symbol) {
        List<CMCQuotesLatest> cmcQuotesLatests = iCMCQuotesLatestService.queryLatest();
        CMCQuotesLatest c1 = null;
        for (CMCQuotesLatest cmcQuotesLatest : cmcQuotesLatests) {
            if (cmcQuotesLatest.getSymbol().equals(symbol)) {
                c1 = cmcQuotesLatest;
                break;
            }
        }
        return c1;
    }
}
