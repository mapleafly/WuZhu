package org.lifxue.wuzhu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.lifxue.wuzhu.entity.CMCQuotesLatest;

import java.util.List;

public interface CMCQuotesLatestMapper extends BaseMapper<CMCQuotesLatest> {
    //@Select("SELECT * FROM CMC_QUOTES_LATEST a WHERE ( select count(1) from CMC_QUOTES_LATEST b where a.TID = b.TID and b.LAST_UPDATED >= a.LAST_UPDATED )")
    @Select("SELECT * FROM CMC_QUOTES_LATEST c WHERE NOT EXISTS ( SELECT * FROM CMC_QUOTES_LATEST WHERE CMC_QUOTES_LATEST.TID = c.TID AND CMC_QUOTES_LATEST.LAST_UPDATED > c.LAST_UPDATED)")
    List<CMCQuotesLatest> queryLatest();

}
