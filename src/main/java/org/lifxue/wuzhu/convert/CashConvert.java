package org.lifxue.wuzhu.convert;

import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.TradeInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Cash模块MapStruct转换器
 */
@Mapper(componentModel = "spring")
public interface CashConvert {

    @Mapping(target = "symbolPairs", expression = "java(entity.getBaseSymbol() + \"/\" + entity.getQuoteSymbol())")
    @Mapping(target = "date", source = "tradeDate")
    @Mapping(target = "coinId", source = "baseId")
    @Mapping(target = "saleOrBuy", expression = "java(entity.getSaleOrBuy() != null && entity.getSaleOrBuy().equals(\"卖\") ? \"入金\" : \"出金\")")
    @Mapping(target = "chg", ignore = true)
    TradeInfoVO toVO(TradeInfo entity);

    List<TradeInfoVO> toVOList(List<TradeInfo> entities);
}
