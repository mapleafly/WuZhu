package org.lifxue.wuzhu.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @version 1.0
 * @classname TradeInfoDto
 * @description 交易数据类
 * @auhthor lifxue
 * @date 2023/2/12 15:40
 */


@Data
@Entity
@Table(name = "trade_info")
public class TradeInfo implements Serializable {
    //自增id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;
    //base coin id
    @Column(name = "BASE_ID")
    private Integer baseId;
    // base coin 简称
    @Column(name = "BASE_SYMBOL")
    private String baseSymbol;
    //quote coin id
    @Column(name = "QUOTE_ID")
    private Integer quoteId;
    // quote coin 简称
    @Column(name = "QUOTE_SYMBOL")
    private String quoteSymbol;
    //买或者卖
    @Column(name = "sale_or_buy")
    private String saleOrBuy;
    //买入或卖出价格
    private String price;
    //基准货币买入或卖出数量
    @Column(name = "BASE_NUM")
    private String baseNum;
    //计价货币数量
    @Column(name = "QUOTE_NUM")
    private String quoteNum;
    //交易时间
    @Column(name = "TRADE_DATE")
    private String tradeDate;
}
