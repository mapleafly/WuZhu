package org.lifxue.wuzhu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lifxue.wuzhu.entity.CMCQuotesLatest;

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
