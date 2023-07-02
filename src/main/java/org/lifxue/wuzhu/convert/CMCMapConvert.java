package org.lifxue.wuzhu.convert;

import org.lifxue.wuzhu.dto.CMCMapDto;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @ClassName CMCMapConvert
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/7/2 18:12
 * @Version 1.0
 */
public interface CMCMapConvert {
    CMCMapConvert INSTANCE = Mappers.getMapper(CMCMapConvert.class);

    CMCMap convert(CMCMapDto dto);
    List<CMCMap> convertList(List<CMCMapDto> list);
}
