package org.lifxue.wuzhu.repository;

import org.lifxue.wuzhu.pojo.CMCMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName CMCMapRepository
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 11:00
 * @Version 1.0
 */
@Repository
public interface CMCMapRepository extends JpaRepository<CMCMap, Integer> {
    @Query(value = "SELECT symbol FROM cmc_map WHERE is_selected = 1 order by symbol ", nativeQuery=true)
    List<String> getSymbolList();
    @Query(value = "SELECT s FROM CMCMap s WHERE s.symbol = (:symbol) order by s.symbol ")
    CMCMap queryCoinBySymbo(String symbol);

    @Query(value = "SELECT tid FROM cmc_map WHERE is_selected = 1 order by symbol ", nativeQuery=true)
    List<Integer> getSelectedIDs();

    @Query(value = "SELECT * FROM cmc_map WHERE is_selected = 1 order by symbol ", nativeQuery=true)
    List<CMCMap> getSelecteds();
    @Query(value = "SELECT s FROM CMCMap s WHERE s.isSelected = (:is_selected) order by s.symbol ")
    List<CMCMap> list(Integer is_selected);

    List<CMCMap> findByTid(Integer tid);
    List<CMCMap> findByTidIn(List<Integer> tidList);

    List<CMCMap> findBySymbolLikeOrderByRankAsc(String symbol);
}
