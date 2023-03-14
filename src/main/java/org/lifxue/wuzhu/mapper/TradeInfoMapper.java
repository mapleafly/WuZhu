package org.lifxue.wuzhu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.lifxue.wuzhu.entity.TradeInfo;

/**
 * @version 1.0
 * @classname TradeInfoMapper
 * @description 交易数据Mapper
 * @auhthor lifxue
 * @date 2023/2/12 15:59
 */
public interface TradeInfoMapper extends BaseMapper<TradeInfo> {

    @Update("truncate table trade_info")
    void truncate();
}
