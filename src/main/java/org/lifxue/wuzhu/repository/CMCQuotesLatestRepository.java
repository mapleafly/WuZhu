package org.lifxue.wuzhu.repository;

import org.lifxue.wuzhu.pojo.CMCQuotesLatestJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName CMCQuotesLatestRepository
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 14:38
 * @Version 1.0
 */
@Repository
public interface CMCQuotesLatestRepository extends JpaRepository<CMCQuotesLatestJpa, Integer> {
    //@Query(value = "SELECT * FROM CMC_QUOTES_LATEST_JPA c WHERE NOT EXISTS ( SELECT * FROM CMC_QUOTES_LATEST_JPA WHERE CMC_QUOTES_LATEST_JPA.TID = c.TID AND CMC_QUOTES_LATEST_JPA.LAST_UPDATED >= c.LAST_UPDATED)", nativeQuery=true)
    @Query(value = "SELECT * FROM CMC_QUOTES_LATEST_JPA a inner join (SELECT MAX(id) AS max_id FROM CMC_QUOTES_LATEST_JPA GROUP BY TID) as b on a.ID=b.max_id", nativeQuery=true)
    List<CMCQuotesLatestJpa> queryLatest();
}
