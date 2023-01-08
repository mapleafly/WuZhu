package org.lifxue.wuzhu.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.lifxue.wuzhu.dto.CMCQuotesLatestDto;
import org.lifxue.wuzhu.entity.CMCQuotesLatest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;


public interface ICMCQuotesLatestService extends IService<CMCQuotesLatest> {

    List<CMCQuotesLatest> getHttpJsonById(String id, String convert);
    List<CMCQuotesLatest> getHttpJsonById(String id, String convert, String aux);
    List<CMCQuotesLatest> getHttpJsonByIdAndConvertId(String id, String convert_id);
    List<CMCQuotesLatest> getHttpJsonByIdAndConvertId(String id, String convert_id, String aux);
    List<CMCQuotesLatest> getHttpJsonBySymbol(String symbol, String convert);
    List<CMCQuotesLatest> getHttpJsonBySymbol(String symbol, String convert, String aux);

    boolean saveBatch(List<CMCQuotesLatest> list);

}
