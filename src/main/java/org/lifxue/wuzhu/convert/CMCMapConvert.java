package org.lifxue.wuzhu.convert;

import org.lifxue.wuzhu.dto.CMCMapDto;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @ClassName CMCMapConvert
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/7/2 18:12
 * @Version 1.0
 */
@Mapper
public interface CMCMapConvert {
    CMCMapConvert INSTANCE = Mappers.getMapper(CMCMapConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tid", source = "id")
    @Mapping(target = "platformId", source = "platform.id")
    @Mapping(target = "tokenAddress", source = "platform.token_address")
    @Mapping(target = "isSelected", ignore = true)
    CMCMap convert(CMCMapDto dto);
    List<CMCMap> convertList(List<CMCMapDto> list);

    //todo 1. 解决时间格式问题， 2. 解决list转换问题
}
