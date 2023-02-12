package org.lifxue.wuzhu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lifxue.wuzhu.entity.CMCMap;

import java.util.List;

/**
  * @classname CMCMapMapper
  * @description
  * @auhthor lifxue
  * @date 2023/2/12 16:02
  * @version 1.0
*/
public interface CMCMapMapper extends BaseMapper<CMCMap> {

    @Select("SELECT symbol FROM cmc_map WHERE is_selected = 1 order by symbol ")
    List<String> getSymbolList();

    @Select("SELECT * FROM cmc_map WHERE symbol = #{symbol} order by symbol ")
    CMCMap queryCoinBySymbo(@Param("symbol") String symbol);

    @Select("SELECT id FROM cmc_map WHERE is_selected = 1 order by symbol ")
    List<Integer> getSelectedIDs();
}
