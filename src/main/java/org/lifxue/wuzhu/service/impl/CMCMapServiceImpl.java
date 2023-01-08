package org.lifxue.wuzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.dto.CMCMapDto;
import org.lifxue.wuzhu.dto.Quote;
import org.lifxue.wuzhu.dto.Status;
import org.lifxue.wuzhu.entity.CMCMap;
import org.lifxue.wuzhu.mapper.CMCMapMapper;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.feignc.ICMCMapFeignClient;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CMCMapServiceImpl extends ServiceImpl<CMCMapMapper, CMCMap> implements ICMCMapService {

    private final Integer LIMIT = 5000;
    private final String CMCMAP_AUX = "platform,first_historical_data,last_historical_data,is_active";
    private ICMCMapFeignClient icmcMapFeignClient;

    @Autowired
    public CMCMapServiceImpl(ICMCMapFeignClient icmcMapFeignClient) {
        this.icmcMapFeignClient = icmcMapFeignClient;
    }

    @Override
    public List<CMCMap> getJson(String listing_status, Integer start, Integer limit, String sort, String aux) {
        String jsonMap = icmcMapFeignClient.getHttpJson(listing_status, start, limit, sort, aux);
        return CopyUtil.copyListCMCMap(jsonToDto(jsonMap));
    }

    @Override
    public List<CMCMap> getJson(Integer start, Integer limit, String sort, String aux) {
        String jsonMap = icmcMapFeignClient.getHttpJson(start, limit, sort, aux);
        return CopyUtil.copyListCMCMap(jsonToDto(jsonMap));
    }

    @Override
    public List<CMCMap> getJson(Integer start, Integer limit, String sort) {
        String jsonMap = icmcMapFeignClient.getHttpJson(start, limit, sort);
        return CopyUtil.copyListCMCMap(jsonToDto(jsonMap));
    }

    @Override
    public List<CMCMap> getJson(Integer limit, String sort) {
        String jsonMap = icmcMapFeignClient.getHttpJson(limit, sort);
        return CopyUtil.copyListCMCMap(jsonToDto(jsonMap));
    }

    @Override
    public List<CMCMap> getJson(Integer limit) {
        String jsonMap = icmcMapFeignClient.getHttpJson(limit);
        return CopyUtil.copyListCMCMap(jsonToDto(jsonMap));
    }

    private List<CMCMapDto> jsonToDto(String jsonMap) {
        if (jsonMap == null || jsonMap.isEmpty()) {
            return null;
        }
        List<CMCMapDto> list;
        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(jsonMap);
            JsonNode data = rootNode.path("data");
            // 排除json字符串中实体类没有的字段
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            list = mapper.readValue(data.traverse(), new TypeReference<ArrayList<CMCMapDto>>() {
            });

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    private Status getStatus(JsonNode rootNode) {
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

    private Quote getQuote(JsonNode coin, String convert) {
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

    private List<String> getTags(JsonNode coin) {
        List<String> listTags = null;
        if (coin.hasNonNull("tags")) {
            JsonNode tags = coin.path("tags");
            try {
                listTags =
                    new ObjectMapper().readValue(tags.traverse(), new TypeReference<ArrayList<String>>() {
                    });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return listTags;
    }

    @Override
    @Transactional
    public boolean saveOrUpdateBatch(String sort) {
        int start = 1;
        List<CMCMap> listAll = new ArrayList<>();
        while (true) {
            List<CMCMap> list = getJson(start, LIMIT, sort);
            if (list == null || list.isEmpty()) {
                break;
            }
            listAll.addAll(list);
            if (list.size() == LIMIT) {
                start += LIMIT;
            } else if (list.size() < LIMIT) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (listAll.isEmpty()) {
            return false;
        }
        return super.saveOrUpdateBatch(listAll);
    }

    @Override
    public boolean saveOrUpdateBatch(Integer limit, String sort) {
        return saveOrUpdateBatch(1, limit, sort);
    }


    @Override
    @Transactional
    public boolean saveOrUpdateBatch(Integer start, Integer limit, String sort) {
        List<CMCMap> list = getJson(start, limit, sort);
        if (list == null || list.isEmpty()) {
            return false;
        }
        return super.saveOrUpdateBatch(list);
    }

    @Override
    public boolean saveOrUpdateBatch(Integer limit, String sort, String aux) {
        return saveOrUpdateBatch(1, limit, sort, aux);
    }

    @Override
    @Transactional
    public boolean saveOrUpdateBatch(Integer start, Integer limit, String sort, String aux) {
        List<CMCMap> list = getJson(start, limit, sort, aux);
        if (list == null || list.isEmpty()) {
            return false;
        }
        return super.saveOrUpdateBatch(list);
    }
}
