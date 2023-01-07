package org.lifxue.wuzhu.service.feignc;

import org.lifxue.wuzhu.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 使用FeignClient，远程获取json数据
 */
@FeignClient(name = "ICMCMapFeignClient", url = "${coin-market-cap.coinMarketCapIDMap}", configuration = FeignClientConfig.class)
public interface ICMCMapFeignClient {

    /**
     *
     * @param listing_status 状态； "active" ， "inactive" , "untracked"; 默认是"active";
     * @param start   默认是1
     * @param limit  1...5000
     * @param sort  排序："id" , "cmc_rank" 默认是"id"
     * @param aux  默认包括 "platform,first_historical_data,last_historical_data,is_active"
     * @return
     */
    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJson(
        @RequestParam("listing_status") String listing_status,
        @RequestParam("start") Integer start,
        @RequestParam("limit") Integer limit,
        @RequestParam("sort") String sort,
        @RequestParam("aux") String aux
    );

    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJson(
        @RequestParam("start") Integer start,
        @RequestParam("limit") Integer limit,
        @RequestParam("sort") String sort,
        @RequestParam("aux") String aux
    );

    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJson(
        @RequestParam("start") Integer start,
        @RequestParam("limit") Integer limit,
        @RequestParam("sort") String sort
    );

    /**
     *
     * @param limit
     * @param sort
     * @return
     */
    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJson(
        @RequestParam("limit") Integer limit,
        @RequestParam("sort") String sort
    );

    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJson(
         @RequestParam("limit") Integer limit
    );
}
