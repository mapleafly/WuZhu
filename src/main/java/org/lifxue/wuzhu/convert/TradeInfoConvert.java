package org.lifxue.wuzhu.convert;

import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * TradeInfo MapStruct转换器
 */
@Mapper(componentModel = "spring")
public interface TradeInfoConvert {

    @Mapping(target = "symbolPairs", expression = "java(entity.getBaseSymbol() + \"/\" + entity.getQuoteSymbol())")
    @Mapping(target = "date", source = "tradeDate")
    @Mapping(target = "coinId", source = "baseId")
    @Mapping(target = "chg", ignore = true)
    TradeInfoVO toVO(TradeInfo entity);

    List<TradeInfoVO> toVOList(List<TradeInfo> entities);

    @Mapping(target = "symbolPairs", expression = "java(entity.getBaseSymbol() + \"/\" + entity.getQuoteSymbol())")
    @Mapping(target = "date", source = "tradeDate")
    @Mapping(target = "coinId", source = "baseId")
    @Mapping(target = "chg", ignore = true)
    PATableVO toPAVO(TradeInfo entity);

    List<PATableVO> toPAVOList(List<TradeInfo> entities);
}
