package org.lifxue.wuzhu.convert;

import org.lifxue.wuzhu.dto.CMCMapDto;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * CMCMap MapStruct转换器
 */
@Mapper(componentModel = "spring")
public interface CMCMapConvert {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tid", source = "id")
    @Mapping(target = "firstHistoricalData", source = "firstHistoricalData", qualifiedByName = "convertDate")
    @Mapping(target = "lastHistoricalData", source = "lastHistoricalData", qualifiedByName = "convertDate")
    @Mapping(target = "platformId", source = "platform.id")
    @Mapping(target = "tokenAddress", source = "platform.token_address")
    @Mapping(target = "isSelected", ignore = true)
    CMCMap convert(CMCMapDto dto);

    List<CMCMap> convertList(List<CMCMapDto> list);

    @Named("convertDate")
    default String convertDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
            SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = isoDate.replace("Z", " UTC");
            return defaultFormat.format(format.parse(dateTime));
        } catch (Exception e) {
            return isoDate;
        }
    }
}
