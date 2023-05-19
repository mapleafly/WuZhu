package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.pojo.CMCQuotesLatest;

import java.util.List;

/**
 * @ClassName ICMCQuotesLatestJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 14:41
 * @Version 1.0
 */
public interface ICMCQuotesLatestService {

    List<CMCQuotesLatest> getHttpJsonById(String id, String convert);

    List<CMCQuotesLatest> getHttpJsonById(String id, String convert, String aux);

    List<CMCQuotesLatest> getHttpJsonByIdAndConvertId(String id, String convert_id);

    List<CMCQuotesLatest> getHttpJsonByIdAndConvertId(String id, String convert_id, String aux);

    List<CMCQuotesLatest> getHttpJsonBySymbol(String symbol, String convert);

    List<CMCQuotesLatest> getHttpJsonBySymbol(String symbol, String convert, String aux);

    boolean saveBatch(List<CMCQuotesLatest> list);

    boolean saveBatch();

    /***
     * @description 从数据库获取最新的数据
     * @author lifxue
     * @date 2023/3/14 15:52
     * @param
     * @return java.util.List<org.lifxue.wuzhu.entity.CMCQuotesLatest>
     **/
    List<CMCQuotesLatest> queryLatest();

    boolean delete();

}
