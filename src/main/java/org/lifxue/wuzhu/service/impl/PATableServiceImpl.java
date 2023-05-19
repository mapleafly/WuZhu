package org.lifxue.wuzhu.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.service.IPATableService;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName PATableJpaServiceImpl
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 17:08
 * @Version 1.0
 */
@Slf4j
@Service
public class PATableServiceImpl implements IPATableService {
    private ITradeInfoService iTradeInfoJpaService;
    private ICMCQuotesLatestService icmcQuotesLatestJpaService;
    private ICMCMapService icmcMapJpaService;

    public PATableServiceImpl(
        ITradeInfoService iTradeInfoJpaService,
        ICMCQuotesLatestService icmcQuotesLatestJpaService,
        ICMCMapService icmcMapJpaService) {
        this.iTradeInfoJpaService = iTradeInfoJpaService;
        this.icmcQuotesLatestJpaService = icmcQuotesLatestJpaService;
        this.icmcMapJpaService = icmcMapJpaService;
    }

    @Override
    public List<PATableVO> queryAllVos() {
        List<TradeInfo> tradeInfoList = iTradeInfoJpaService.findOrderByTradeDate();

        List<CMCQuotesLatest> cmcQuotesLatests = icmcQuotesLatestJpaService.queryLatest();

        List<PATableVO> paTableVOS = new ArrayList<PATableVO>();
        for (TradeInfo tradeInfo : tradeInfoList) {
            PATableVO paTableVO = CopyUtil.copy(tradeInfo);
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
    public List<PATableVO> queryVOBy(String strCoinSymbol, String strStartDate, String strEndDate, String tradeType) {
        List<TradeInfo> tradeInfoList = iTradeInfoJpaService.findByTradeDateBetweenOrderByTradeDateDesc(strStartDate, strEndDate);
        if (strCoinSymbol != null && !strCoinSymbol.isBlank()) {
            tradeInfoList = tradeInfoList.stream().filter(tradeInfo -> tradeInfo.getBaseSymbol().equals(strCoinSymbol)).toList();
        }
        if (!tradeType.equals("全部")) {
            tradeInfoList = tradeInfoList.stream().filter(tradeInfo -> tradeInfo.getSaleOrBuy().equals(tradeType)).toList();
        }
        //查询被选品种
        //List<CMCMap> cmcMaps = icmcMapService.queryAll(1);
        List<CMCQuotesLatest> cmcQuotesLatests = icmcQuotesLatestJpaService.queryLatest();
        List<PATableVO> paTableVOS = new ArrayList<PATableVO>();
        for (TradeInfo tradeInfo : tradeInfoList) {
            PATableVO paTableVO = CopyUtil.copy(tradeInfo);
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
        return icmcMapJpaService.queryCurSymbol();
    }

    @Override
    public CMCQuotesLatest queryBySymbol(String symbol) {
        List<CMCQuotesLatest> cmcQuotesLatests = icmcQuotesLatestJpaService.queryLatest();
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
