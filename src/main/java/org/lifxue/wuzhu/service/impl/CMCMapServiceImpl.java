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
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.mapper.CMCMapMapper;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.feignc.ICMCMapFeignClient;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
public class CMCMapServiceImpl extends ServiceImpl<CMCMapMapper, CMCMap> implements ICMCMapService {

    private final Integer LIMIT = 5000;
    private final String CMCMAP_AUX = "platform,first_historical_data,last_historical_data,is_active";
    private final ICMCMapFeignClient icmcMapFeignClient;

    @Resource
    private CMCMapMapper cmcMapMapper;

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

    /**
     * @param sort
     * @return java.util.List<org.lifxue.wuzhu.entity.CMCMap>
     * @description 获取全部CMCMap的网络数据
     * @author lifxue
     * @date 2023/2/8 12:56
     **/
    private List<CMCMap> getAllCmcMap(String sort) {
        int start = 1;
        List<CMCMap> listAll = new ArrayList<>();
        //每次获取上限是LIMIT个，如果每次获取足额LIMIT，就继续获取，直到获取的数量不足LIMIT，表示已经全部获取，这时退出循环
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
        return listAll;
    }

    /**
     * @param sort
     * @return boolean
     * @description 批量保存新数据，数据库中已有的数据保持不变
     * @author lifxue
     * @date 2023/2/8 12:53
     **/
    @Override
    @Transactional
    public boolean saveNewBatch(String sort) {
        //网络查询
        List<CMCMap> listAll = getAllCmcMap(sort);
        if (listAll.isEmpty()) {
            return false;
        }
        //数据库查询所得
        List<CMCMap> cmcMapList = list();
        if (cmcMapList == null || cmcMapList.isEmpty()) {
            return super.saveOrUpdateBatch(listAll);
        }
        //最后结果集
        List<CMCMap> resultList = new ArrayList<>();
        //中间存储
        HashSet<CMCMap> hashSet = new HashSet<>();
        cmcMapList.forEach(i -> {
            i.setIsSelected(0);
            hashSet.add(i);
        });
        listAll.forEach(i -> {
            if (!hashSet.contains(i)) {
                resultList.add(i);
            }
        });
        //如果没有新的币种，返回true，不操作数据库
        if (resultList.isEmpty()) {
            return true;
        }
        return super.saveOrUpdateBatch(resultList);
    }

    @Override
    @Transactional
    public boolean saveOrUpdateBatch(String sort) {
        List<CMCMap> listAll = getAllCmcMap(sort);
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

    @Override
    public List<String> queryCurSymbol() {
        return this.cmcMapMapper.getSymbolList();
    }

    /***
     * @description 获取所有背选中的coin
     * @author lifxue
     * @date 2023/2/12 22:35
     * @param
     * @return java.util.List<java.lang.Integer>
     **/
    @Override
    public List<Integer> getSelectedIDs() {
        return this.cmcMapMapper.getSelectedIDs();
    }

    @Override
    public CMCMap queryCoinBySymbo(String symbo) {
        return this.cmcMapMapper.queryCoinBySymbo(symbo);
    }

    @Override
    @Transactional
    public boolean updateSelectedBatch(List<Integer> selected) {
        log.info("selected size = {}", selected.size());
        List<CMCMap> cmcMapList = listByIds(selected);

        for(CMCMap cmcMap : cmcMapList) {
            cmcMap.setIsSelected(1);
        }

        for(CMCMap cmcMap : cmcMapList) {
            log.info(cmcMap.toString());
        }
        return updateBatchById(cmcMapList);
    }

    /***
     * @description 1-被选 0-未选
     * @author lifxue
     * @date 2023/3/14 15:25
     * @param isSelect
     * @return java.util.List<org.lifxue.wuzhu.entity.CMCMap>
     **/
    public List<CMCMap> queryAll(Integer isSelect) {
        return this.cmcMapMapper.queryAll(1);
    }
}
