package org.lifxue.wuzhu.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.util.PrefsHelper;
import org.springframework.beans.factory.annotation.Value;

/**
 * Feign拦截器，动态设置header中的apikey参数
 */
@Slf4j
public class FeignAuthRequestInterceptor implements RequestInterceptor {

    @Value("${coin-market-cap.customHeader}")
    private String customHeader;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(customHeader, PrefsHelper.getPreferencesValue(PrefsHelper.CMC_API_KEY, ""));
    }

}
