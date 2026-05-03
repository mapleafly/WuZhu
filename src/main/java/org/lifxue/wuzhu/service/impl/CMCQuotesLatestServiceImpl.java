package org.lifxue.wuzhu.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lifxue.wuzhu.dto.*;
import org.lifxue.wuzhu.exception.ApiCallException;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.repository.CMCQuotesLatestRepository;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.convert.CMCQuotesLatestConvert;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.service.feignc.ICMCQuotesLatestFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CMCQuotesLatestJpaServiceImpl
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 14:43
 * @Version 1.0
 */
@Slf4j
@Service
public class CMCQuotesLatestServiceImpl implements ICMCQuotesLatestService {

    private final String ICMCQUOTESLATEST_AUX = "num_market_pairs,cmc_rank,date_added,tags,platform," +
        "max_supply,circulating_supply,total_supply,is_active,is_fiat";

    private final ICMCQuotesLatestFeignClient icmcQuotesLatestFeignClient;
    private final ICMCMapService icmcMapJpaService;
    private final CMCQuotesLatestRepository cmcQuotesLatestRepository;
    private final CMCQuotesLatestConvert cmcQuotesLatestConvert;

    @Autowired
    public CMCQuotesLatestServiceImpl(ICMCQuotesLatestFeignClient icmcQuotesLatestFeignClient,
                                       ICMCMapService icmcMapJpaService,
                                       CMCQuotesLatestRepository cmcQuotesLatestRepository,
                                       CMCQuotesLatestConvert cmcQuotesLatestConvert) {
        this.icmcQuotesLatestFeignClient = icmcQuotesLatestFeignClient;
        this.icmcMapJpaService = icmcMapJpaService;
        this.cmcQuotesLatestRepository = cmcQuotesLatestRepository;
        this.cmcQuotesLatestConvert = cmcQuotesLatestConvert;
    }


    @Nullable
    private List<CMCQuotesLatest> convertCmcQuotes(String ids, String convert, String strJson) {
        List<CMCQuotesLatestDto> listCMCQuotesLatestDto = new ArrayList<>();

        log.info("[convertCmcQuotes] 开始解析API响应数据 - ids: {}, convert: {}, json长度: {}",
            ids != null ? ids.substring(0, Math.min(ids.length(), 100)) : "null",
            convert,
            strJson != null ? strJson.length() : 0);

        if (strJson == null || strJson.isEmpty()) {
            log.error("[convertCmcQuotes] API返回空响应 - ids: {}, convert: {}", ids, convert);
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(strJson);
        } catch (JsonProcessingException e) {
            log.error("[convertCmcQuotes] 解析JSON失败 - ids: {}, 错误: {}", ids, e.getMessage(), e);
            throw new ApiCallException("Failed to parse CMC Quotes API response", e);
        }

        Status status = getStatus(rootNode);
        if (status == null) {
            log.error("[convertCmcQuotes] API响应中没有status信息 - ids: {}", ids);
            return null;
        }

        log.info("[convertCmcQuotes] API响应状态 - error_code: {}, error_message: {}",
            status.getErrorCode(),
            status.getErrorMessage() != null ? status.getErrorMessage() : "无错误信息");

        if (status.getErrorCode() != 0) {
            log.error("[convertCmcQuotes] API调用失败 - ids: {}, error_code: {}, error_message: {}",
                ids, status.getErrorCode(), status.getErrorMessage());
            return null;
        }

        JsonNode data = rootNode.path("data");
        for (String key : ids.split(",")) {
            JsonNode coin = data.path(key);
            if (coin.isMissingNode() || coin.isNull()) {
                log.warn("[convertCmcQuotes] API响应中未找到币种数据 - key: {}", key);
                continue;
            }
            if (coin.isArray()) {
                coin = coin.get(0);
            }
            // 检查coin是否为空对象
            if (coin == null || coin.isMissingNode() || coin.isNull()) {
                log.warn("[convertCmcQuotes] 币种数据为空 - key: {}", key);
                continue;
            }
            CMCQuotesLatestDto cmcQuotesLatestDto = new CMCQuotesLatestDto();
            // 检查id字段是否存在
            if (coin.hasNonNull("id")) {
                cmcQuotesLatestDto.setId(coin.get("id").asInt());
            } else {
                log.warn("[convertCmcQuotes] 币种数据缺少id字段 - key: {}", key);
                continue;
            }
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

        List<CMCQuotesLatest> result = cmcQuotesLatestConvert.convertList(listCMCQuotesLatestDto);
        log.info("[convertCmcQuotes] 解析完成 - 原始币种数: {}, 解析后数据量: {}",
            ids.split(",").length, result != null ? result.size() : 0);

        return result;
    }

    private Status getStatus(@NotNull JsonNode rootNode) {
        Status status = null;
        if (rootNode.hasNonNull("status")) {
            JsonNode jsonStatus = rootNode.path("status");
            try {
                ObjectMapper mapper = new ObjectMapper();
                status = mapper.readValue(jsonStatus.toString(), Status.class);
            } catch (JsonProcessingException e) {
                throw new ApiCallException("Failed to parse status from CMC API response", e);
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
                throw new ApiCallException("Failed to parse tags from CMC API response", e);
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
        log.info("[getHttpJsonById] 开始请求API - id: {}, convert: {}, aux: {}",
            id != null ? id.substring(0, Math.min(id.length(), 100)) : "null",
            convert,
            aux != null ? aux.substring(0, Math.min(aux.length(), 50)) : "null");

        String strJson = icmcQuotesLatestFeignClient.getHttpJsonById(id, convert, aux);

        log.info("[getHttpJsonById] API请求完成 - id: {}, 响应长度: {}",
            id != null ? id.substring(0, Math.min(id.length(), 50)) : "null",
            strJson != null ? strJson.length() : 0);

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

        return cmcQuotesLatestRepository.saveAll(list) == null ? false : true;
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
        log.info("[saveBatch] 开始批量更新价格数据");

        List<CMCMap> cmcMapList = icmcMapJpaService.list(1);
        if (cmcMapList == null || cmcMapList.isEmpty()) {
            log.error("[saveBatch] 没有选中的币种，更新取消");
            return false;
        }

        log.info("[saveBatch] 选中币种数量: {}", cmcMapList.size());

        StringBuilder ids = new StringBuilder();
        for (CMCMap cmcMap : cmcMapList) {
            ids.append(cmcMap.getTid()).append(",");
        }
        ids = new StringBuilder(ids.substring(0, ids.length() - 1));

        String idString = ids.toString();
        log.info("[saveBatch] 构建的ID字符串长度: {}, 前100字符: {}",
            idString.length(),
            idString.substring(0, Math.min(idString.length(), 100)));

        List<CMCQuotesLatest> list = getHttpJsonById(idString, "USD");
        log.info("[saveBatch] API调用完成，获取数据量: {}", list != null ? list.size() : 0);

        boolean result = saveBatch(list);
        log.info("[saveBatch] 保存结果: {}", result);

        return result;
    }

    @Override
    public List<CMCQuotesLatest> queryLatest() {
        return cmcQuotesLatestRepository.queryLatest();
    }

    @Override
    public boolean delete() {
        cmcQuotesLatestRepository.deleteAll();
        return true;
    }


}
