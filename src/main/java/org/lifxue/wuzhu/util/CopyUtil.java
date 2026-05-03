package org.lifxue.wuzhu.util;

import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.dto.CMCMapDto;
import org.lifxue.wuzhu.dto.CMCQuotesLatestDto;
import org.lifxue.wuzhu.dto.Platform;
import org.lifxue.wuzhu.dto.Quote;
import org.lifxue.wuzhu.exception.DataAccessException;
import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CopyUtil {

    /**
     * 将String转换为BigDecimal，处理null和空字符串
     * @param value 字符串值
     * @return BigDecimal值，如果输入为null或空则返回BigDecimal.ZERO
     */
    public static BigDecimal toBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("[CopyUtil] 无法将字符串转换为BigDecimal: {}", value);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 将BigDecimal转换为String，保留完整精度
     * @param value BigDecimal值
     * @return 字符串表示，如果输入为null则返回null
     */
    public static String toString(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    /**
     * 将double转换为BigDecimal，避免精度问题
     * @param value double值
     * @return BigDecimal值
     */
    public static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }

    public static <T> T copy(Object source, Class<T> c) {
        if (source == null) {
            return null;
        }
        return copy(source, c, (String) null);
    }

    public static <T> T copy(Object source, Class<T> c, @Nullable String... ignoreProperties) {
        if (source == null) {
            return null;
        }
        try {
            T instance = c.getDeclaredConstructor().newInstance();
            if (ignoreProperties == null) {
                BeanUtils.copyProperties(source, instance);
            } else {
                BeanUtils.copyProperties(source, instance, ignoreProperties);
            }
            return instance;
        } catch (InvocationTargetException | InstantiationException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new DataAccessException("Failed to copy object of type: " + c.getSimpleName(), e);
        }
    }

    public static <E, T> List<T> copyList(List<E> sources, Class<T> c) {
        if (CollectionUtils.isEmpty(sources)) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>();
        for (E source : sources) {
            list.add(copy(source, c));
        }
        return list;
    }

    public static CMCMap copyjpa(CMCMapDto dto) {
        if (dto == null) {
            return null;
        }

        CMCMap cmcMap = new CMCMap();
        cmcMap.setTid(dto.getId());
        cmcMap.setName(dto.getName());
        cmcMap.setSymbol(dto.getSymbol());
        cmcMap.setSlug(dto.getSlug());
        cmcMap.setIsActive(dto.getIsActive());
        cmcMap.setRank(dto.getRank());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(dto.getFirstHistoricalData() != null && !dto.getFirstHistoricalData().isEmpty()){
                String firstDateTime = dto.getFirstHistoricalData().replace("Z", " UTC");
                cmcMap.setFirstHistoricalData(defaultFormat.format(format.parse(firstDateTime)));
            }
            if(dto.getLastHistoricalData() != null && !dto.getLastHistoricalData().isEmpty()) {
                String lastDateTime = dto.getLastHistoricalData().replace("Z", " UTC");
                cmcMap.setLastHistoricalData(defaultFormat.format(format.parse(lastDateTime)));
            }
        } catch (Exception e) {
            log.error("CopyUtil- copyjpa(CMCMapDto dto)" + "格式错误");
        }

        Platform platform = dto.getPlatform();
        if (platform != null) {
            cmcMap.setPlatformId(platform.getId());
            cmcMap.setTokenAddress(platform.getToken_address());
        }
        return cmcMap;
    }

    public static List<CMCMap> copyListCMCMapjpa(List<CMCMapDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return null;
        }
        List<CMCMap> list = new ArrayList<>();
        for (CMCMapDto dto : dtoList) {
            if (dto != null) {
                list.add(copyjpa(dto));
            }
        }
        return list;
    }



    public static CMCQuotesLatest copyjpa(CMCQuotesLatestDto dto) {
        if (dto == null) {
            log.warn("[CopyUtil] DTO为null，跳过转换");
            return null;
        }
        CMCQuotesLatest cmcQuotesLatestJpa = new CMCQuotesLatest();
        cmcQuotesLatestJpa.setTid(dto.getId());
        cmcQuotesLatestJpa.setName(dto.getName());
        cmcQuotesLatestJpa.setSymbol(dto.getSymbol());
        cmcQuotesLatestJpa.setSlug(dto.getSlug());

        // 处理日期字段，添加null检查
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (dto.getLast_updated() != null && !dto.getLast_updated().isEmpty()) {
                String lastUpdated = dto.getLast_updated().replace("Z", " UTC");
                cmcQuotesLatestJpa.setLastUpdated(defaultFormat.format(format.parse(lastUpdated)));
            }
            if (dto.getDate_added() != null && !dto.getDate_added().isEmpty()) {
                String dataAdded = dto.getDate_added().replace("Z", " UTC");
                cmcQuotesLatestJpa.setDateAdded(defaultFormat.format(format.parse(dataAdded)));
            }
        } catch (Exception e) {
            log.error("[CopyUtil] 日期格式转换失败 - coinId: {}, 错误: {}", dto.getId(), e.getMessage());
        }

        cmcQuotesLatestJpa.setNumMarketPairs(dto.getNum_market_pairs());
        cmcQuotesLatestJpa.setMaxSupply(toBigDecimal(dto.getMax_supply()));
        cmcQuotesLatestJpa.setCirculatingSupply(toBigDecimal(dto.getCirculating_supply()));
        cmcQuotesLatestJpa.setTotalSupply(toBigDecimal(dto.getTotal_supply()));
        cmcQuotesLatestJpa.setIsActive(dto.getIs_active());
        cmcQuotesLatestJpa.setCmcRank(dto.getCmc_rank());

        Platform platform = dto.getPlatform();
        if (platform != null) {
            cmcQuotesLatestJpa.setPlatformId(platform.getId());
            cmcQuotesLatestJpa.setTokenAddress(platform.getToken_address());
        }

        Quote quote = dto.getQuote();
        if (quote != null) {
            cmcQuotesLatestJpa.setPrice(toBigDecimal(quote.getPrice()));
            cmcQuotesLatestJpa.setVolume24h(toBigDecimal(quote.getVolume_24h()));
            cmcQuotesLatestJpa.setVolumeChange24h(toBigDecimal(quote.getVolume_change_24h()));
            cmcQuotesLatestJpa.setPercentChange1h(toBigDecimal(quote.getPercent_change_1h()));
            cmcQuotesLatestJpa.setPercentChange24h(toBigDecimal(quote.getPercent_change_24h()));
            cmcQuotesLatestJpa.setPercentChange7d(toBigDecimal(quote.getPercent_change_7d()));
            cmcQuotesLatestJpa.setPercentChange30d(toBigDecimal(quote.getPercent_change_30d()));
            cmcQuotesLatestJpa.setPercentChange60d(toBigDecimal(quote.getPercent_change_60d()));
            cmcQuotesLatestJpa.setPercentChange90d(toBigDecimal(quote.getPercent_change_90d()));
            cmcQuotesLatestJpa.setMarketCap(toBigDecimal(quote.getMarket_cap()));
            cmcQuotesLatestJpa.setMarketCapDominance(toBigDecimal(quote.getMarket_cap_dominance()));
        }

        return cmcQuotesLatestJpa;
    }

    public static List<CMCQuotesLatest> copyListCMCQuotesJap(List<CMCQuotesLatestDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return null;
        }
        List<CMCQuotesLatest> list = new ArrayList<>();
        for (CMCQuotesLatestDto dto : dtoList) {
            if (dto != null) {
                list.add(copyjpa(dto));
            }
        }
        return list;
    }


    public static SelectDataVO copy(CMCMap cmcMap){
        if(cmcMap == null){
            return null;
        }
        SelectDataVO selectDataVO = new SelectDataVO();
        selectDataVO.setId(cmcMap.getTid());
        //数据库中IsSelected为0，表示未选，1表示选中
        selectDataVO.setSelect(cmcMap.getIsSelected().equals(1));
        selectDataVO.setName(cmcMap.getName());
        selectDataVO.setSymbol(cmcMap.getSymbol());
        selectDataVO.setRank(cmcMap.getRank());
        selectDataVO.setDate(cmcMap.getLastHistoricalData());
        return selectDataVO;
    }

    public static List<SelectDataVO> copySelectDataListJpa(List<CMCMap>  cmcMaps){
        if(cmcMaps == null || cmcMaps.isEmpty()){
            return null;
        }

        List<SelectDataVO> selectDataVOS = new ArrayList<>();
        for (CMCMap cmcMap : cmcMaps){
            SelectDataVO selectDataVO = new SelectDataVO();
            if (cmcMap != null){
                selectDataVOS.add(copy(cmcMap));
            }
        }
        return selectDataVOS;
    }


    public static TradeInfoVO copyjpa(TradeInfo tradeInfo){
        if(tradeInfo == null){
            return null;
        }
        TradeInfoVO tradeInfoVO = new TradeInfoVO();
        tradeInfoVO.setId(tradeInfo.getId());
        tradeInfoVO.setDate(tradeInfo.getTradeDate());
        tradeInfoVO.setPrice(toString(tradeInfo.getPrice()));
        tradeInfoVO.setCoinId(tradeInfo.getBaseId());
        tradeInfoVO.setSaleOrBuy(tradeInfo.getSaleOrBuy());
        tradeInfoVO.setSymbolPairs(tradeInfo.getBaseSymbol() + "/" + tradeInfo.getQuoteSymbol());
        tradeInfoVO.setBaseNum(toString(tradeInfo.getBaseNum()));
        tradeInfoVO.setQuoteNum(toString(tradeInfo.getQuoteNum()));

        return tradeInfoVO;
    }

    public static List<TradeInfoVO> copyTradeInfoVOListJpa(List<TradeInfo>  tradeInfos){
        if(tradeInfos == null || tradeInfos.isEmpty()){
            return null;
        }

        List<TradeInfoVO> tradeInfoVOS = new ArrayList<>();
        for (TradeInfo tradeInfo : tradeInfos){
            if (tradeInfo != null){
                tradeInfoVOS.add(copyjpa(tradeInfo));
            }
        }
        return tradeInfoVOS;
    }

    public static TradeInfoVO copyforCash(TradeInfo tradeInfo){
        if(tradeInfo == null){
            return null;
        }
        TradeInfoVO tradeInfoVO = new TradeInfoVO();
        tradeInfoVO.setId(tradeInfo.getId());
        tradeInfoVO.setDate(tradeInfo.getTradeDate());
        tradeInfoVO.setPrice(toString(tradeInfo.getPrice()));
        tradeInfoVO.setCoinId(tradeInfo.getBaseId());
        tradeInfoVO.setSaleOrBuy(tradeInfo.getSaleOrBuy().equals("卖") ? "入金" : "出金");
        tradeInfoVO.setSymbolPairs(tradeInfo.getBaseSymbol() + "/" + tradeInfo.getQuoteSymbol());
        tradeInfoVO.setBaseNum(toString(tradeInfo.getBaseNum()));
        tradeInfoVO.setQuoteNum(toString(tradeInfo.getQuoteNum()));

        return tradeInfoVO;
    }

    public static List<TradeInfoVO> copyTradeInfoVOListForCash(List<TradeInfo>  tradeInfos){
        if(tradeInfos == null || tradeInfos.isEmpty()){
            return null;
        }

        List<TradeInfoVO> tradeInfoVOS = new ArrayList<>();
        for (TradeInfo tradeInfo : tradeInfos){
            if (tradeInfo != null){
                tradeInfoVOS.add(copyforCash(tradeInfo));
            }
        }
        return tradeInfoVOS;
    }


    public static List<TradeInfo> copyTradeInfoListJpa(List<String[]> list){
        List<TradeInfo> tradeInfoList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String[] bean = list.get(i);
            TradeInfo tradeInfo = new TradeInfo();
            tradeInfo.setBaseId(Integer.valueOf(bean[1]));
            tradeInfo.setBaseSymbol(bean[2]);
            tradeInfo.setQuoteId(Integer.valueOf(bean[3]));
            tradeInfo.setQuoteSymbol(bean[4]);
            tradeInfo.setSaleOrBuy(bean[5]);
            // 从CSV导入的字符串转换为BigDecimal
            tradeInfo.setPrice(toBigDecimal(bean[6]));
            tradeInfo.setBaseNum(toBigDecimal(bean[7]));
            tradeInfo.setQuoteNum(toBigDecimal(bean[8]));
            tradeInfo.setTradeDate(bean[9]);

            tradeInfoList.add(tradeInfo);
        }

        return tradeInfoList;
    }


    public static PATableVO copy(TradeInfo tradeInfo){
        if(tradeInfo == null){
            return null;
        }
        PATableVO paTableVO = new PATableVO();
        paTableVO.setId(tradeInfo.getId());
        paTableVO.setCoinId(tradeInfo.getBaseId());
        paTableVO.setSaleOrBuy(tradeInfo.getSaleOrBuy());
        paTableVO.setPrice(toString(tradeInfo.getPrice()));
        paTableVO.setBaseNum(toString(tradeInfo.getBaseNum()));
        paTableVO.setQuoteNum(toString(tradeInfo.getQuoteNum()));
        paTableVO.setDate(tradeInfo.getTradeDate());
        paTableVO.setSymbolPairs(tradeInfo.getBaseSymbol() + "/" + tradeInfo.getQuoteSymbol());

        return paTableVO;
    }


    public static CoinChoiceBoxVO copyCoinChoiceBoxVO(CMCMap cmcMap){
        if(cmcMap == null){
            return null;
        }
        CoinChoiceBoxVO coinChoiceBoxVO = new CoinChoiceBoxVO("", 0);
        coinChoiceBoxVO.setCoinId(cmcMap.getTid());
        coinChoiceBoxVO.setSymbol(cmcMap.getSymbol());

        return coinChoiceBoxVO;
    }

    public static List<CoinChoiceBoxVO> copyCoinChoiceBoxVOListJpa(List<CMCMap> list){
        List<CoinChoiceBoxVO> coinChoiceBoxVOS = new ArrayList<>();
        for (CMCMap cmcMap : list) {
            coinChoiceBoxVOS.add(copyCoinChoiceBoxVO(cmcMap));
        }

        return coinChoiceBoxVOS;
    }
}
