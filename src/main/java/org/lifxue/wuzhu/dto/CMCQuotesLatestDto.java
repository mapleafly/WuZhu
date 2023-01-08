package org.lifxue.wuzhu.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CMCQuotesLatestDto {
    private Integer id;
    private String name;
    private String symbol;
    private String slug;
    private Integer is_active;
    private Integer is_fiat;
    private String circulating_supply;
    private String total_supply;
    private String max_supply;
    private String date_added;
    private Integer num_market_pairs;
    private Integer cmc_rank;
    private String last_updated;
    private List<Tag> tags;
    private Platform platform;
    private String self_reported_circulating_supply;
    private String self_reported_market_cap;
    private Quote quote;
}
