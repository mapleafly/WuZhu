package org.lifxue.wuzhu.pojo;

import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @version 1.0
 * @classname CMCQuotesLatest
 * @description 指定品种的最新价格表
 * @auhthor lifxue
 * @date 2023/1/8 14:06
 */
@Data
@Entity
@Table(name = "cmc_quotes_latest")
public class CMCQuotesLatest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;
    //coin or token id
    private Integer tid;
    //coin名称
    private String name;
    //简称(符号)
    private String symbol;
    //标称  The web URL friendly shorthand version of this cryptocurrency name
    private String slug;
    //最后更新时间
    @Column(name = "LAST_UPDATED" )
    private String lastUpdated;

    //aux
    //coin在市场中的交易对数量
    @Column(name = "NUM_MARKET_PAIRS" )
    private Integer numMarketPairs;
    //首次加入CoinMarketCap的时间
    @Column(name = "DATE_ADDED" )
    private String dateAdded;
    //最终的数量
    @Column(name = "MAX_SUPPLY", precision = 30, scale = 8)
    private BigDecimal maxSupply;
    //当前正在流通的硬币的大概数量
    @Column(name = "CIRCULATING_SUPPLY", precision = 30, scale = 8)
    private BigDecimal circulatingSupply;
    //当前大约存在的硬币总数（减去已被可验证燃烧的所有硬币）
    @Column(name = "TOTAL_SUPPLY", precision = 30, scale = 8)
    private BigDecimal totalSupply;
    //是否激活，1-yes  0=no
    @Column(name = "IS_ACTIVE" )
    private Integer isActive;
    //平台id，如果是基于某个平台的token，比如基于eth的很多coin和token
    @Column(name = "PLATFORM_ID" )
    private Integer platformId;
    //在平台中的创建地址,如果platform_id 为null，这里也是null
    @Column(name = "TOKEN_ADDRESS" )
    private String tokenAddress;
    //The cryptocurrency's CoinMarketCap rank by market cap.
    @Column(name = "CMC_RANK" )
    private Integer cmcRank;

    //quote
    //整个市场的最新平均交易价格
    @Column(name = "price", precision = 25, scale = 12)
    private BigDecimal price;
    //24小时交易量
    @Column(name = "volume_24h", precision = 30, scale = 2)
    private BigDecimal volume24h;
    @Column(name = "volume_change_24h", precision = 30, scale = 8)
    private BigDecimal volumeChange24h;
    //每种货币1小时的交易价格百分比变化
    @Column(name = "percent_change_1h", precision = 25, scale = 8)
    private BigDecimal percentChange1h;
    //每种货币的24小时交易价格百分比变化
    @Column(name = "percent_change_24h", precision = 25, scale = 8)
    private BigDecimal percentChange24h;
    //每种货币7天交易价格的百分比变化
    @Column(name = "percent_change_7d", precision = 25, scale = 8)
    private BigDecimal percentChange7d;
    @Column(name = "percent_change_30d", precision = 25, scale = 8)
    private BigDecimal percentChange30d;
    @Column(name = "percent_change_60d", precision = 25, scale = 8)
    private BigDecimal percentChange60d;
    @Column(name = "percent_change_90d", precision = 25, scale = 8)
    private BigDecimal percentChange90d;
    //CoinMarketCap计算的市值
    @Column(name = "market_cap", precision = 30, scale = 2)
    private BigDecimal marketCap;
    //市值占总市值百分比
    @Column(name = "market_cap_dominance", precision = 19, scale = 8)
    private BigDecimal marketCapDominance;

}
