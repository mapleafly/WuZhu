package org.lifxue.wuzhu.config;

import feign.okhttp.OkHttpClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient.Builder;
import org.lifxue.wuzhu.constant.AppConstants;
import org.lifxue.wuzhu.enums.BooleanEnum;
import org.lifxue.wuzhu.util.PrefsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @classname FeignClientConfig
 * @description 使用代理连接网络
 * @auhthor lifxue
 * @date 2023/1/6 22:52
 */
@Slf4j
@Configuration
public class FeignClientConfig {
    @Value("#{'${proxy.domains}'.split(',')}")
    private Set<String> domainList;

    /**
     * @return org.lifxue.wuzhu.config.FeignAuthRequestInterceptor
     * @description Feign拦截器
     * @author lifxue
     * @date 2023/1/6 22:54
     **/
    @Bean(name = "feignAuthRequestInterceptor")
    public FeignAuthRequestInterceptor feignAuthRequestInterceptor() {
        return new FeignAuthRequestInterceptor();
    }

    /**
     * @return feign.okhttp.OkHttpClient
     * @description Feign代理设置 - Spring Cloud 2023兼容配置
     * @author lifxue
     * @date 2023/1/6 22:56
     **/
    @Bean
    public OkHttpClient feignClient() {
        Builder builder = new Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS);

        BooleanEnum proxyEnum =
            BooleanEnum.valueOf(PrefsHelper.getPreferencesValue(PrefsHelper.PROXY, BooleanEnum.NO.toString()));
        String proxyHost = PrefsHelper.getPreferencesValue(PrefsHelper.HOST, AppConstants.DEFAULT_PROXY_HOST);
        Integer proxyPort = Integer.valueOf(PrefsHelper.getPreferencesValue(PrefsHelper.PORT, AppConstants.DEFAULT_PROXY_PORT));

        if (proxyEnum.equals(BooleanEnum.YES) && domainList != null && !domainList.isEmpty()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            List<Proxy> proxyList = new ArrayList<>(1);
            proxyList.add(proxy);

            builder.proxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    if (proxyEnum.equals(BooleanEnum.NO)) {
                        return Collections.singletonList(Proxy.NO_PROXY);
                    }
                    if (uri == null || !domainList.contains(uri.getHost())) {
                        return Collections.singletonList(Proxy.NO_PROXY);
                    }
                    return proxyList;
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                    log.warn("Proxy connection failed for {}: {}", uri, ioe.getMessage());
                }
            });
        }

        return new OkHttpClient(builder.build());
    }
}
