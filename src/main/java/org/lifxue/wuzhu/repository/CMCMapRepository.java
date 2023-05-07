package org.lifxue.wuzhu.repository;

import org.apache.ibatis.annotations.Param;

import org.lifxue.wuzhu.pojo.CMCMapJpa;
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
public interface CMCMapRepository extends JpaRepository<CMCMapJpa, Integer> {
    @Query(value = "SELECT symbol FROM cmc_map_jpa WHERE is_selected = 1 order by symbol ", nativeQuery=true)
    List<String> getSymbolList();
    @Query(value = "SELECT s FROM CMCMapJpa s WHERE s.symbol = (:symbol) order by s.symbol ")
    CMCMapJpa queryCoinBySymbo(@Param("symbol") String symbol);

    @Query(value = "SELECT id FROM cmc_map_jpa WHERE is_selected = 1 order by symbol ", nativeQuery=true)
    List<Integer> getSelectedIDs();

    @Query(value = "SELECT * FROM cmc_map_jpa WHERE is_selected = 1 order by symbol ", nativeQuery=true)
    List<CMCMapJpa> getSelecteds();
    @Query(value = "SELECT s FROM CMCMapJpa s WHERE s.isSelected = (:is_selected) order by s.symbol ")
    List<CMCMapJpa> queryAll(@Param("is_selected") Integer is_selected);
}
