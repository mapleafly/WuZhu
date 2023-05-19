package org.lifxue.wuzhu.repository;

import org.lifxue.wuzhu.pojo.TradeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName TradInfoPrpository
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 14:39
 * @Version 1.0
 */
@Repository
public interface TradeInfoRepository extends JpaRepository<TradeInfo, Integer> {
    List<TradeInfo> findByBaseSymbolOrderByIdDesc(String baseSymbol);
    List<TradeInfo> findByBaseIdOrderByIdDesc(Integer baseId);
    List<TradeInfo> findByTradeDateBetweenOrderByTradeDateDesc(String startDate, String endDate);

}
