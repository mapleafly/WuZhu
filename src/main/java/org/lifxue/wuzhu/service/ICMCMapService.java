package org.lifxue.wuzhu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lifxue.wuzhu.entity.CMCMap;

import java.util.List;

public interface ICMCMapService extends IService<CMCMap> {

    List<CMCMap> getJson(String listing_status, Integer start, Integer limit, String sort, String aux);

    List<CMCMap> getJson(Integer start, Integer limit, String sort, String aux);

    List<CMCMap> getJson(Integer start, Integer limit, String sort);

    List<CMCMap> getJson(Integer limit, String sort);

    List<CMCMap> getJson(Integer limit);

    boolean saveOrUpdateBatch(String sort);

    boolean saveNewBatch(String sort);

    boolean saveOrUpdateBatch(Integer limit, String sort);

    boolean saveOrUpdateBatch(Integer start, Integer limit, String sort);

    boolean saveOrUpdateBatch(Integer limit, String sort, String aux);

    boolean saveOrUpdateBatch(Integer start, Integer limit, String sort, String aux);


}
