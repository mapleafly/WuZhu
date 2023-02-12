package org.lifxue.wuzhu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @version 1.0
 * @classname TradeInfoDto
 * @description 交易数据类
 * @auhthor lifxue
 * @date 2023/2/12 15:40
 */

@Data
@TableName("trade_info")
public class TradeInfo implements Serializable {
    //自增id
    @TableId
    private Integer id;
    //base coin id
    private Integer baseId;
    // base coin 简称
    private String baseSymbol;
    //quote coin id
    private Integer quoteId;
    // quote coin 简称
    private String quoteSymbol;
    //买或者卖
    @TableField("sale_or_buy")
    private String saleOrBuy;
    //买入或卖出价格
    private String price;
    //基准货币买入或卖出数量
    private String baseNum;
    //计价货币数量
    private String quoteNum;
    //交易时间
    private String tradeDate;
}
