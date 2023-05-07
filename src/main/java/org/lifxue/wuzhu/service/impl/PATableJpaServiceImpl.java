package org.lifxue.wuzhu.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.pojo.CMCQuotesLatestJpa;
import org.lifxue.wuzhu.pojo.TradeInfoJpa;
import org.lifxue.wuzhu.service.ICMCMapJpaService;
import org.lifxue.wuzhu.service.ICMCQuotesLatestJpaService;
import org.lifxue.wuzhu.service.IPATableJpaService;
import org.lifxue.wuzhu.service.ITradeInfoJpaService;
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
public class PATableJpaServiceImpl implements IPATableJpaService {
    private ITradeInfoJpaService iTradeInfoJpaService;
    private ICMCQuotesLatestJpaService icmcQuotesLatestJpaService;
    private ICMCMapJpaService icmcMapJpaService;

    public PATableJpaServiceImpl(
        ITradeInfoJpaService iTradeInfoJpaService,
        ICMCQuotesLatestJpaService icmcQuotesLatestJpaService,
        ICMCMapJpaService icmcMapJpaService) {
        this.iTradeInfoJpaService = iTradeInfoJpaService;
        this.icmcQuotesLatestJpaService = icmcQuotesLatestJpaService;
        this.icmcMapJpaService = icmcMapJpaService;
    }

    @Override
    public List<PATableVO> queryAllVos() {
        List<TradeInfoJpa> tradeInfoList = iTradeInfoJpaService.findOrderByTradeDate();

        List<CMCQuotesLatestJpa> cmcQuotesLatests = icmcQuotesLatestJpaService.queryLatest();

        List<PATableVO> paTableVOS = new ArrayList<PATableVO>();
        for (TradeInfoJpa tradeInfo : tradeInfoList) {
            PATableVO paTableVO = CopyUtil.copy(tradeInfo);
            for (CMCQuotesLatestJpa cmcQuotesLatest : cmcQuotesLatests) {
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
        List<TradeInfoJpa> tradeInfoList = iTradeInfoJpaService.findByTradeDateBetweenOrderByTradeDateDesc(strStartDate, strEndDate);
        if (strCoinSymbol != null && !strCoinSymbol.isBlank()) {
            tradeInfoList = tradeInfoList.stream().filter(tradeInfo -> tradeInfo.getBaseSymbol().equals(strCoinSymbol)).toList();
        }
        if (!tradeType.equals("全部")) {
            tradeInfoList = tradeInfoList.stream().filter(tradeInfo -> tradeInfo.getSaleOrBuy().equals(tradeType)).toList();
        }
        //查询被选品种
        //List<CMCMap> cmcMaps = icmcMapService.queryAll(1);
        List<CMCQuotesLatestJpa> cmcQuotesLatests = icmcQuotesLatestJpaService.queryLatest();
        List<PATableVO> paTableVOS = new ArrayList<PATableVO>();
        for (TradeInfoJpa tradeInfo : tradeInfoList) {
            PATableVO paTableVO = CopyUtil.copy(tradeInfo);
            for (CMCQuotesLatestJpa cmcQuotesLatest : cmcQuotesLatests) {
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
    public CMCQuotesLatestJpa queryBySymbol(String symbol) {
        List<CMCQuotesLatestJpa> cmcQuotesLatests = icmcQuotesLatestJpaService.queryLatest();
        CMCQuotesLatestJpa c1 = null;
        for (CMCQuotesLatestJpa cmcQuotesLatest : cmcQuotesLatests) {
            if (cmcQuotesLatest.getSymbol().equals(symbol)) {
                c1 = cmcQuotesLatest;
                break;
            }
        }
        return c1;
    }
}
