package org.lifxue.wuzhu.util;

import org.lifxue.wuzhu.dto.CMCMapDto;
import org.lifxue.wuzhu.dto.CMCQuotesLatestDto;
import org.lifxue.wuzhu.dto.Platform;
import org.lifxue.wuzhu.dto.Quote;
import org.lifxue.wuzhu.entity.CMCQuotesLatest;
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.entity.CMCMap;
import org.lifxue.wuzhu.pojo.CMCMapJpa;
import org.lifxue.wuzhu.pojo.CMCQuotesLatestJpa;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CopyUtil {

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
            throw new RuntimeException(e);
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

    public static CMCMapJpa copyjpa(CMCMapDto dto) {
        if (dto == null) {
            return null;
        }

        CMCMapJpa cmcMap = new CMCMapJpa();
        cmcMap.setTid(dto.getId());
        cmcMap.setName(dto.getName());
        cmcMap.setSymbol(dto.getSymbol());
        cmcMap.setSlug(dto.getSlug());
        cmcMap.setIsActive(dto.getIsActive());
        cmcMap.setRank(dto.getRank());

        String firstDateTime = dto.getFirstHistoricalData().replace("Z", " UTC");
        String lastDateTime = dto.getLastHistoricalData().replace("Z", " UTC");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cmcMap.setFirstHistoricalData(defaultFormat.format(format.parse(firstDateTime)));
            cmcMap.setLastHistoricalData(defaultFormat.format(format.parse(lastDateTime)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Platform platform = dto.getPlatform();
        if (platform != null) {
            cmcMap.setPlatformId(platform.getId());
            cmcMap.setTokenAddress(platform.getToken_address());
        }

        return cmcMap;
    }

    public static List<CMCMapJpa> copyListCMCMapjpa(List<CMCMapDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return null;
        }
        List<CMCMapJpa> list = new ArrayList<>();
        for (CMCMapDto dto : dtoList) {
            if (dto != null) {
                list.add(copyjpa(dto));
            }
        }
        return list;
    }

    public static CMCMap copy(CMCMapDto dto) {
        if (dto == null) {
            return null;
        }

        CMCMap cmcMap = new CMCMap();
        cmcMap.setId(dto.getId());
        cmcMap.setName(dto.getName());
        cmcMap.setSymbol(dto.getSymbol());
        cmcMap.setSlug(dto.getSlug());
        cmcMap.setIsActive(dto.getIsActive());
        cmcMap.setRank(dto.getRank());

        String firstDateTime = dto.getFirstHistoricalData().replace("Z", " UTC");
        String lastDateTime = dto.getLastHistoricalData().replace("Z", " UTC");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cmcMap.setFirstHistoricalData(defaultFormat.format(format.parse(firstDateTime)));
            cmcMap.setLastHistoricalData(defaultFormat.format(format.parse(lastDateTime)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Platform platform = dto.getPlatform();
        if (platform != null) {
            cmcMap.setPlatformId(platform.getId());
            cmcMap.setTokenAddress(platform.getToken_address());
        }

        return cmcMap;
    }

    public static List<CMCMap> copyListCMCMap(List<CMCMapDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return null;
        }
        List<CMCMap> list = new ArrayList<>();
        for (CMCMapDto dto : dtoList) {
            if (dto != null) {
                list.add(copy(dto));
            }
        }
        return list;
    }

    public static CMCQuotesLatest copy(CMCQuotesLatestDto dto) {
        if (dto == null) {
            return null;
        }
        CMCQuotesLatest CMCQuotesLatest = new CMCQuotesLatest();
        CMCQuotesLatest.setTid(dto.getId());
        CMCQuotesLatest.setName(dto.getName());
        CMCQuotesLatest.setSymbol(dto.getSymbol());
        CMCQuotesLatest.setSlug(dto.getSlug());

        String lastUpdated = dto.getLast_updated().replace("Z", " UTC");
        String dataAdded = dto.getDate_added().replace("Z", " UTC");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            CMCQuotesLatest.setLastUpdated(defaultFormat.format(format.parse(lastUpdated)));
            CMCQuotesLatest.setDateAdded(defaultFormat.format(format.parse(dataAdded)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        CMCQuotesLatest.setNumMarketPairs(dto.getNum_market_pairs());
        CMCQuotesLatest.setMaxSupply(dto.getMax_supply());
        CMCQuotesLatest.setCirculatingSupply(dto.getCirculating_supply());
        CMCQuotesLatest.setTotalSupply(dto.getTotal_supply());
        CMCQuotesLatest.setIsActive(dto.getIs_active());
        CMCQuotesLatest.setCmcRank(dto.getCmc_rank());

        Platform platform = dto.getPlatform();
        if (platform != null) {
            CMCQuotesLatest.setPlatformId(platform.getId());
            CMCQuotesLatest.setTokenAddress(platform.getToken_address());
        }

        Quote quote = dto.getQuote();
        if (quote != null) {
            CMCQuotesLatest.setPrice(String.valueOf(quote.getPrice()));
            CMCQuotesLatest.setVolume24h(String.valueOf(quote.getVolume_24h()));
            CMCQuotesLatest.setVolumeChange24h(String.valueOf(quote.getVolume_change_24h()));
            CMCQuotesLatest.setPercentChange1h(String.valueOf(quote.getPercent_change_1h()));
            CMCQuotesLatest.setPercentChange24h(String.valueOf(quote.getPercent_change_24h()));
            CMCQuotesLatest.setPercentChange7d(String.valueOf(quote.getPercent_change_7d()));
            CMCQuotesLatest.setPercentChange30d(String.valueOf(quote.getPercent_change_30d()));
            CMCQuotesLatest.setPercentChange60d(String.valueOf(quote.getPercent_change_60d()));
            CMCQuotesLatest.setPercentChange90d(String.valueOf(quote.getPercent_change_90d()));
            CMCQuotesLatest.setMarketCap(String.valueOf(quote.getMarket_cap()));
            CMCQuotesLatest.setMarketCapDominance(String.valueOf(quote.getMarket_cap_dominance()));
        }

        return CMCQuotesLatest;
    }

    public static List<CMCQuotesLatest> copyListCMCQuotes(List<CMCQuotesLatestDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return null;
        }
        List<CMCQuotesLatest> list = new ArrayList<>();
        for (CMCQuotesLatestDto dto : dtoList) {
            if (dto != null) {
                list.add(copy(dto));
            }
        }
        return list;
    }

    public static CMCQuotesLatestJpa copyjpa(CMCQuotesLatestDto dto) {
        if (dto == null) {
            return null;
        }
        CMCQuotesLatestJpa cmcQuotesLatestJpa = new CMCQuotesLatestJpa();
        cmcQuotesLatestJpa.setTid(dto.getId());
        cmcQuotesLatestJpa.setName(dto.getName());
        cmcQuotesLatestJpa.setSymbol(dto.getSymbol());
        cmcQuotesLatestJpa.setSlug(dto.getSlug());

        String lastUpdated = dto.getLast_updated().replace("Z", " UTC");
        String dataAdded = dto.getDate_added().replace("Z", " UTC");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cmcQuotesLatestJpa.setLastUpdated(defaultFormat.format(format.parse(lastUpdated)));
            cmcQuotesLatestJpa.setDateAdded(defaultFormat.format(format.parse(dataAdded)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        cmcQuotesLatestJpa.setNumMarketPairs(dto.getNum_market_pairs());
        cmcQuotesLatestJpa.setMaxSupply(dto.getMax_supply());
        cmcQuotesLatestJpa.setCirculatingSupply(dto.getCirculating_supply());
        cmcQuotesLatestJpa.setTotalSupply(dto.getTotal_supply());
        cmcQuotesLatestJpa.setIsActive(dto.getIs_active());
        cmcQuotesLatestJpa.setCmcRank(dto.getCmc_rank());

        Platform platform = dto.getPlatform();
        if (platform != null) {
            cmcQuotesLatestJpa.setPlatformId(platform.getId());
            cmcQuotesLatestJpa.setTokenAddress(platform.getToken_address());
        }

        Quote quote = dto.getQuote();
        if (quote != null) {
            cmcQuotesLatestJpa.setPrice(String.valueOf(quote.getPrice()));
            cmcQuotesLatestJpa.setVolume24h(String.valueOf(quote.getVolume_24h()));
            cmcQuotesLatestJpa.setVolumeChange24h(String.valueOf(quote.getVolume_change_24h()));
            cmcQuotesLatestJpa.setPercentChange1h(String.valueOf(quote.getPercent_change_1h()));
            cmcQuotesLatestJpa.setPercentChange24h(String.valueOf(quote.getPercent_change_24h()));
            cmcQuotesLatestJpa.setPercentChange7d(String.valueOf(quote.getPercent_change_7d()));
            cmcQuotesLatestJpa.setPercentChange30d(String.valueOf(quote.getPercent_change_30d()));
            cmcQuotesLatestJpa.setPercentChange60d(String.valueOf(quote.getPercent_change_60d()));
            cmcQuotesLatestJpa.setPercentChange90d(String.valueOf(quote.getPercent_change_90d()));
            cmcQuotesLatestJpa.setMarketCap(String.valueOf(quote.getMarket_cap()));
            cmcQuotesLatestJpa.setMarketCapDominance(String.valueOf(quote.getMarket_cap_dominance()));
        }

        return cmcQuotesLatestJpa;
    }

    public static List<CMCQuotesLatestJpa> copyListCMCQuotesJap(List<CMCQuotesLatestDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return null;
        }
        List<CMCQuotesLatestJpa> list = new ArrayList<>();
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
        selectDataVO.setId(cmcMap.getId());
        //数据库中IsSelected为0，表示未选，1表示选中
        selectDataVO.setSelect(cmcMap.getIsSelected().equals(1));
        selectDataVO.setName(cmcMap.getName());
        selectDataVO.setSymbol(cmcMap.getSymbol());
        selectDataVO.setRank(cmcMap.getRank());
        selectDataVO.setDate(cmcMap.getLastHistoricalData());
        return selectDataVO;
    }

    public static List<SelectDataVO> copySelectDataList(List<CMCMap>  cmcMaps){
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

    public static TradeInfoVO copy(TradeInfo tradeInfo){
        if(tradeInfo == null){
            return null;
        }
        TradeInfoVO tradeInfoVO = new TradeInfoVO();
        tradeInfoVO.setId(tradeInfo.getId());
        tradeInfoVO.setDate(tradeInfo.getTradeDate());
        tradeInfoVO.setPrice(tradeInfo.getPrice());
        tradeInfoVO.setCoinId(tradeInfo.getBaseId());
        tradeInfoVO.setSaleOrBuy(tradeInfo.getSaleOrBuy());
        tradeInfoVO.setSymbolPairs(tradeInfo.getBaseSymbol() + "/" + tradeInfo.getQuoteSymbol());
        tradeInfoVO.setBaseNum(tradeInfo.getBaseNum());
        tradeInfoVO.setQuoteNum(tradeInfo.getQuoteNum());

        return tradeInfoVO;
    }

    public static List<TradeInfoVO> copyTradeInfoVOList(List<TradeInfo>  tradeInfos){
        if(tradeInfos == null || tradeInfos.isEmpty()){
            return null;
        }

        List<TradeInfoVO> tradeInfoVOS = new ArrayList<>();
        for (TradeInfo tradeInfo : tradeInfos){
            if (tradeInfo != null){
                tradeInfoVOS.add(copy(tradeInfo));
            }
        }
        return tradeInfoVOS;
    }

    public static List<TradeInfo> copyTradeInfoList(List<String[]> list){
        List<TradeInfo> tradeInfoList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String[] bean = list.get(i);
            TradeInfo tradeInfo = new TradeInfo();
            tradeInfo.setBaseId(Integer.valueOf(bean[1]));
            tradeInfo.setBaseSymbol(bean[2]);
            tradeInfo.setQuoteId(Integer.valueOf(bean[3]));
            tradeInfo.setQuoteSymbol(bean[4]);
            tradeInfo.setSaleOrBuy(bean[5]);
            tradeInfo.setPrice(bean[6]);
            tradeInfo.setBaseNum(bean[7]);
            tradeInfo.setQuoteNum(bean[8]);
            tradeInfo.setTradeDate(bean[9]);

            tradeInfoList.add(tradeInfo);
        }

        return tradeInfoList;
    }

    public static PATableVO copy2(TradeInfo tradeInfo){
        if(tradeInfo == null){
            return null;
        }
        PATableVO paTableVO = new PATableVO();
        paTableVO.setId(tradeInfo.getId());
        paTableVO.setCoinId(tradeInfo.getBaseId());
        paTableVO.setSaleOrBuy(tradeInfo.getSaleOrBuy());
        paTableVO.setPrice(tradeInfo.getPrice());
        paTableVO.setBaseNum(tradeInfo.getBaseNum());
        paTableVO.setQuoteNum(tradeInfo.getQuoteNum());
        paTableVO.setDate(tradeInfo.getTradeDate());
        paTableVO.setSymbolPairs(tradeInfo.getBaseSymbol() + "/" + tradeInfo.getQuoteSymbol());

        return paTableVO;
    }

    public static List<PATableVO> copyPATableVOList(List<TradeInfo>  tradeInfos){
        if(tradeInfos == null || tradeInfos.isEmpty()){
            return null;
        }

        List<PATableVO> paTableVOS = new ArrayList<>();
        for (TradeInfo tradeInfo : tradeInfos){
            if (tradeInfo != null){
                paTableVOS.add(copy2(tradeInfo));
            }
        }
        return paTableVOS;
    }

    public static CoinChoiceBoxVO copyCoinChoiceBoxVO(CMCMap cmcMap){
        if(cmcMap == null){
            return null;
        }
        CoinChoiceBoxVO coinChoiceBoxVO = new CoinChoiceBoxVO("", 0);
        coinChoiceBoxVO.setCoinId(cmcMap.getId());
        coinChoiceBoxVO.setSymbol(cmcMap.getSymbol());

        return coinChoiceBoxVO;
    }

    public static List<CoinChoiceBoxVO> copyCoinChoiceBoxVOList(List<CMCMap> list){
        List<CoinChoiceBoxVO> coinChoiceBoxVOS = new ArrayList<>();
        for (CMCMap cmcMap : list) {
            coinChoiceBoxVOS.add(copyCoinChoiceBoxVO(cmcMap));
        }

        return coinChoiceBoxVOS;
    }
}
