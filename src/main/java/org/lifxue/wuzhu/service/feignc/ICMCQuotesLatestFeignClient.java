package org.lifxue.wuzhu.service.feignc;

import org.lifxue.wuzhu.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
  * @classname ICMCQuotesLatestFeignClient
  * @description 使用FeignClient，远程获取json数据
  * @auhthor lifxue
  * @date 2023/1/8 14:31
  * @version 1.0
*/
@FeignClient(
    name = "ICMCQuotesLatestFeignClient",
    url = "${coin-market-cap.quotesLatest}",
    configuration = FeignClientConfig.class)
public interface ICMCQuotesLatestFeignClient {

    /***
     * @description  id 指定coin id， 多个id用逗号分割， 如："1982,9444"; convert指定转换品种的符号，比如USD，BTC；
     * @author lifxue
     * @date 2023/1/8 14:32
     * @param id, convert
     * @return java.lang.String
     **/
    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJsonById(
        @RequestParam("id") String id,
        @RequestParam("convert") String convert
    );

    /***
     * @description  id 指定coin id， 多个id用逗号分割， 如："1982,9444"; convert指定转换品种的符号，比如USD，BTC；
     * @author lifxue
     * @date 2023/1/8 14:32
     * @param id, convert
     * @return java.lang.String
     **/
    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJsonByIdAndConvertId(
        @RequestParam("id") String id,
        @RequestParam("convert_id") String convert_id,
        @RequestParam("aux") String aux
    );

    /**
     *
     * @param id 指定coin id， 多个id用逗号分割， 如："1982,9444"
     * @param convert
     * @param aux  默认值是 "num_market_pairs,cmc_rank,date_added,tags,platform,max_supply,circulating_supply,
     *             total_supply,is_active,is_fiat"
     * @return
     */
    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJsonById(
        @RequestParam("id") String id,
        @RequestParam("convert") String convert,
        @RequestParam("aux") String aux
    );

    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJsonBySymbol(
        @RequestParam("symbol") String symbol,
        @RequestParam("convert") String convert,
        @RequestParam("aux") String aux
    );

    @GetMapping(headers = {"Accept=${coin-market-cap.httpHeader}"})
    public String getHttpJsonBySlug(
        @RequestParam("slug") String slug ,
        @RequestParam("convert") String convert,
        @RequestParam("aux") String aux
    );


}
