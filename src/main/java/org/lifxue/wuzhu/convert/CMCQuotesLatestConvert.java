package org.lifxue.wuzhu.convert;

import org.lifxue.wuzhu.dto.CMCQuotesLatestDto;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * CMCQuotesLatest MapStruct转换器
 */
@Mapper(componentModel = "spring")
public interface CMCQuotesLatestConvert {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tid", source = "id")
    @Mapping(target = "lastUpdated", source = "last_updated", qualifiedByName = "convertDate")
    @Mapping(target = "dateAdded", source = "date_added", qualifiedByName = "convertDate")
    @Mapping(target = "numMarketPairs", source = "num_market_pairs")
    @Mapping(target = "maxSupply", source = "max_supply")
    @Mapping(target = "circulatingSupply", source = "circulating_supply")
    @Mapping(target = "totalSupply", source = "total_supply")
    @Mapping(target = "isActive", source = "is_active")
    @Mapping(target = "cmcRank", source = "cmc_rank")
    @Mapping(target = "platformId", source = "platform.id")
    @Mapping(target = "tokenAddress", source = "platform.token_address")
    @Mapping(target = "price", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getPrice()) : null)")
    @Mapping(target = "volume24h", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getVolume_24h()) : null)")
    @Mapping(target = "volumeChange24h", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getVolume_change_24h()) : null)")
    @Mapping(target = "percentChange1h", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getPercent_change_1h()) : null)")
    @Mapping(target = "percentChange24h", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getPercent_change_24h()) : null)")
    @Mapping(target = "percentChange7d", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getPercent_change_7d()) : null)")
    @Mapping(target = "percentChange30d", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getPercent_change_30d()) : null)")
    @Mapping(target = "percentChange60d", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getPercent_change_60d()) : null)")
    @Mapping(target = "percentChange90d", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getPercent_change_90d()) : null)")
    @Mapping(target = "marketCap", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getMarket_cap()) : null)")
    @Mapping(target = "marketCapDominance", expression = "java(dto.getQuote() != null ? String.valueOf(dto.getQuote().getMarket_cap_dominance()) : null)")
    CMCQuotesLatest convert(CMCQuotesLatestDto dto);

    List<CMCQuotesLatest> convertList(List<CMCQuotesLatestDto> dtos);

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
