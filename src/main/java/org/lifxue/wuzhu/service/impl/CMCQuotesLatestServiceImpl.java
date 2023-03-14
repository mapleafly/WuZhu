package org.lifxue.wuzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lifxue.wuzhu.dto.*;
import org.lifxue.wuzhu.entity.CMCMap;
import org.lifxue.wuzhu.entity.CMCQuotesLatest;
import org.lifxue.wuzhu.mapper.CMCQuotesLatestMapper;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.service.feignc.ICMCQuotesLatestFeignClient;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CMCQuotesLatestServiceImpl extends ServiceImpl<CMCQuotesLatestMapper, CMCQuotesLatest> implements ICMCQuotesLatestService {

    private final String ICMCQUOTESLATEST_AUX = "num_market_pairs,cmc_rank,date_added,tags,platform," +
        "max_supply,circulating_supply,total_supply,is_active,is_fiat";

    ICMCQuotesLatestFeignClient icmcQuotesLatestFeignClient;
    ICMCMapService icmcMapService;
    CMCQuotesLatestMapper cmcQuotesLatestMapper;

    @Autowired
    public void setIcmcQuotesLatestFeignClient(ICMCQuotesLatestFeignClient icmcQuotesLatestFeignClient) {
        this.icmcQuotesLatestFeignClient = icmcQuotesLatestFeignClient;
    }

    @Autowired
    public void setIcmcMapService(ICMCMapService icmcMapService) {
        this.icmcMapService = icmcMapService;
    }

    @Autowired
    public void setCmcQuotesLatestMapper(CMCQuotesLatestMapper cmcQuotesLatestMapper) {
        this.cmcQuotesLatestMapper = cmcQuotesLatestMapper;
    }

    @Nullable
    private List<CMCQuotesLatest> convertCmcQuotes(String ids, String convert, String strJson) {
        List<CMCQuotesLatestDto> listCMCQuotesLatestDto = new ArrayList<>();

        if (strJson == null || strJson.isEmpty()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(strJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Status status = getStatus(rootNode);
        if (status == null || status.getErrorCode() != 0) {
            return null;
        }

        JsonNode data = rootNode.path("data");
        for (String key : ids.split(",")) {
            JsonNode coin = data.path(key);
            if (coin.isArray()) {
                coin = coin.get(0);
            }
            CMCQuotesLatestDto cmcQuotesLatestDto = new CMCQuotesLatestDto();
            cmcQuotesLatestDto.setId(coin.get("id").asInt());
            if (coin.hasNonNull("name")) {
                cmcQuotesLatestDto.setName(coin.get("name").asText());
            }
            if (coin.hasNonNull("symbol")) {
                cmcQuotesLatestDto.setSymbol(coin.get("symbol").asText());
            }
            if (coin.hasNonNull("slug")) {
                cmcQuotesLatestDto.setSlug(coin.get("slug").asText());
            }
            if (coin.hasNonNull("num_market_pairs")) {
                cmcQuotesLatestDto.setNum_market_pairs(coin.get("num_market_pairs").asInt());
            }
            if (coin.hasNonNull("date_added")) {
                //String date = DateHelper.utcToLocal(coin.get("date_added").asText());
                cmcQuotesLatestDto.setDate_added(coin.get("date_added").asText());
            }
            if (coin.hasNonNull("max_supply")) {
                cmcQuotesLatestDto.setMax_supply(coin.get("max_supply").asText());
            }
            if (coin.hasNonNull("circulating_supply")) {
                cmcQuotesLatestDto.setCirculating_supply(coin.get("circulating_supply").asText());
            }
            if (coin.hasNonNull("total_supply")) {
                cmcQuotesLatestDto.setTotal_supply(coin.get("total_supply").asText());
            }
            if (coin.hasNonNull("is_active")) {
                cmcQuotesLatestDto.setIs_active(coin.get("is_active").asInt());
            }
            if (coin.hasNonNull("is_fiat")) {
                cmcQuotesLatestDto.setIs_fiat(coin.get("is_fiat").asInt());
            }
            if (coin.hasNonNull("cmc_rank")) {
                cmcQuotesLatestDto.setCmc_rank(coin.get("cmc_rank").asInt());
            }
            if (coin.hasNonNull("last_updated")) {
                //String date = DateHelper.utcToLocal(coin.get("last_updated").asText());
                cmcQuotesLatestDto.setLast_updated(coin.get("last_updated").asText());
            }
            if (coin.hasNonNull("self_reported_circulating_supply")) {
                cmcQuotesLatestDto.setSelf_reported_circulating_supply(coin.get("self_reported_circulating_supply").asText());
            }
            if (coin.hasNonNull("self_reported_market_cap")) {
                cmcQuotesLatestDto.setSelf_reported_market_cap(coin.get("self_reported_market_cap").asText());
            }

            if (coin.hasNonNull("platform")) {
                Platform p = mapper.convertValue(coin.path("platform"), Platform.class);
                cmcQuotesLatestDto.setPlatform(p);
            }

            Quote quoteDto = getQuote(coin, convert);
            if (quoteDto != null) {
                cmcQuotesLatestDto.setQuote(quoteDto);
            }

            List<Tag> listTags = getTags(coin);
            if (listTags != null) {
                cmcQuotesLatestDto.setTags(listTags);
            }

            listCMCQuotesLatestDto.add(cmcQuotesLatestDto);
        }

        return CopyUtil.copyListCMCQuotes(listCMCQuotesLatestDto);
    }

    private Status getStatus(@NotNull JsonNode rootNode) {
        Status status = null;
        if (rootNode.hasNonNull("status")) {
            JsonNode jsonStatus = rootNode.path("status");
            try {
                ObjectMapper mapper = new ObjectMapper();
                status = mapper.readValue(jsonStatus.toString(), Status.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return status;
    }

    private Quote getQuote(@NotNull JsonNode coin, String convert) {
        Quote quote = null;
        if (coin.hasNonNull("quote")) {
            JsonNode jsonQuote = coin.path("quote");
            if (jsonQuote.hasNonNull(convert)) {
                JsonNode quoteConvert = jsonQuote.path(convert);
                ObjectMapper mapper = new ObjectMapper();
                quote = mapper.convertValue(quoteConvert, Quote.class);
                if (quote != null) {
                    quote.setQuote(convert);
                }
            }
        }
        return quote;
    }

    private List<Tag> getTags(@NotNull JsonNode coin) {
        List<Tag> listTags = null;
        if (coin.hasNonNull("tags")) {
            JsonNode tags = coin.path("tags");
            try {
                listTags =
                    new ObjectMapper().readValue(tags.traverse(), new TypeReference<ArrayList<Tag>>() {
                    });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return listTags;
    }

    @Override
    public List<CMCQuotesLatest> getHttpJsonById(String id, String convert) {
        return getHttpJsonById(id, convert, ICMCQUOTESLATEST_AUX);
    }

    @Override
    public List<CMCQuotesLatest> getHttpJsonById(String id, String convert, String aux) {
        String strJson = icmcQuotesLatestFeignClient.getHttpJsonById(id, convert, aux);
        return convertCmcQuotes(id, convert, strJson);
    }

    @Override
    public List<CMCQuotesLatest> getHttpJsonByIdAndConvertId(String id, String convert_id) {
        return getHttpJsonByIdAndConvertId(id, convert_id, ICMCQUOTESLATEST_AUX);
    }

    @Override
    public List<CMCQuotesLatest> getHttpJsonByIdAndConvertId(String id, String convert_id, String aux) {
        String strJson = icmcQuotesLatestFeignClient.getHttpJsonByIdAndConvertId(id, convert_id, aux);
        return convertCmcQuotes(id, convert_id, strJson);
    }

    @Override
    public List<CMCQuotesLatest> getHttpJsonBySymbol(String symbol, String convert) {
        return getHttpJsonBySymbol(symbol, convert, ICMCQUOTESLATEST_AUX);
    }

    @Override
    public List<CMCQuotesLatest> getHttpJsonBySymbol(String symbol, String convert, String aux) {
        String strJson = icmcQuotesLatestFeignClient.getHttpJsonBySymbol(symbol, convert, aux);
        return convertCmcQuotes(symbol, convert, strJson);
    }

    @Override
    @Transactional
    public boolean saveBatch(List<CMCQuotesLatest> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return super.saveBatch(list);
    }

    /**
     * @return boolean
     * @description 获取并保存被选中的币种的当前价格数据
     * @author lifxue
     * @date 2023/2/8 17:12
     **/
    @Override
    @Transactional
    public boolean saveBatch() {
        QueryWrapper<CMCMap> wrapper = new QueryWrapper<>();
        wrapper.eq("is_Selected", 1);
        List<CMCMap> cmcMapList = icmcMapService.list(wrapper);
        if (cmcMapList == null || cmcMapList.isEmpty()) {
            return false;
        }
        StringBuilder ids = new StringBuilder();
        for (CMCMap cmcMap : cmcMapList) {
            ids.append(cmcMap.getId()).append(",");
        }
        ids = new StringBuilder(ids.substring(0, ids.length() - 1));
        List<CMCQuotesLatest> list = getHttpJsonById(ids.toString(), "USD");
        return saveBatch(list);
    }

    @Override
    public List<CMCQuotesLatest> queryLatest() {
        return cmcQuotesLatestMapper.queryLatest();
    }


}
