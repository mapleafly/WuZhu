package org.lifxue.wuzhu.service;
import org.lifxue.wuzhu.pojo.CMCMapJpa;

import java.util.List;

/**
 * @ClassName ICMCMapJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 11:07
 * @Version 1.0
 */
public interface ICMCMapJpaService {

    List<CMCMapJpa> getJson(String listing_status, Integer start, Integer limit, String sort, String aux);

    List<CMCMapJpa> getJson(Integer start, Integer limit, String sort, String aux);

    List<CMCMapJpa> getJson(Integer start, Integer limit, String sort);

    List<CMCMapJpa> getJson(Integer limit, String sort);

    List<CMCMapJpa> getJson(Integer limit);

    boolean saveOrUpdateBatch(String sort);

    boolean saveNewBatch(String sort);

    boolean saveOrUpdateBatch(Integer limit, String sort);

    boolean saveOrUpdateBatch(Integer start, Integer limit, String sort);

    boolean saveOrUpdateBatch(Integer limit, String sort, String aux);

    boolean saveOrUpdateBatch(Integer start, Integer limit, String sort, String aux);

    List<String> queryCurSymbol();
    List<Integer> getSelectedIDs();
    List<CMCMapJpa> getSelecteds();
    CMCMapJpa queryCoinBySymbo(String symbo);

    List<CMCMapJpa> findBySymbolLikeOrderByTid(String symbol);

    boolean updateSelectedBatch(List<Integer> selected);

    /***
     * @description 查询数据，参数为1 已选  参数为0 未选
     * @author lifxue
     * @date 2023/3/14 15:28
     * @param isSelect
     * @return java.util.List<org.lifxue.wuzhu.entity.CMCMap>
     **/
    List<CMCMapJpa> list(Integer isSelect);

    List<CMCMapJpa> list();

    List<CMCMapJpa> getById(Integer tid);

    boolean update(CMCMapJpa cmcMapJpa);

    boolean delete();
}
