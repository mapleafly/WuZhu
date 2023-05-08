package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.pojo.CMCQuotesLatestJpa;

import java.util.List;

/**
 * @ClassName ICMCQuotesLatestJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 14:41
 * @Version 1.0
 */
public interface ICMCQuotesLatestJpaService {

    List<CMCQuotesLatestJpa> getHttpJsonById(String id, String convert);

    List<CMCQuotesLatestJpa> getHttpJsonById(String id, String convert, String aux);

    List<CMCQuotesLatestJpa> getHttpJsonByIdAndConvertId(String id, String convert_id);

    List<CMCQuotesLatestJpa> getHttpJsonByIdAndConvertId(String id, String convert_id, String aux);

    List<CMCQuotesLatestJpa> getHttpJsonBySymbol(String symbol, String convert);

    List<CMCQuotesLatestJpa> getHttpJsonBySymbol(String symbol, String convert, String aux);

    boolean saveBatch(List<CMCQuotesLatestJpa> list);

    boolean saveBatch();

    /***
     * @description 从数据库获取最新的数据
     * @author lifxue
     * @date 2023/3/14 15:52
     * @param
     * @return java.util.List<org.lifxue.wuzhu.entity.CMCQuotesLatest>
     **/
    List<CMCQuotesLatestJpa> queryLatest();

    boolean delete();

}
