/*
 * Copyright 2020 lif.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifxue.wuzhu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @version 1.0
 * @classname PrefsHelper
 * @description 首选项助手
 * @auhthor lifxue
 * @date 2023/1/6 14:36
 */
@Slf4j
@Component
public class PrefsHelper {
    // 主题
    public final String THEME = "theme";
    // 更新价格
    public final String UPDATEPRICE = "updateprice";
    // 更新coin信息
    public final String COINIDMAP = "coinidmap";
    // coin信息最后更新日期
    public final String COINIDMAP_DATE = "coinidmaplastdate";
    // 忽略小额品种
    public final String NOTSMALLCOIN = "notsmallcoin";
    public final String NOTSMALLCOINNUM = "notsmallcoinnum";
    //代理设置
    public final String PROXY = "proxy";
    public final String HOST = "host";
    public final String PORT = "port";

    //coin-market-cap网站的 apikey
    public final String CMC_API_KEY = "cmcapikey";

    //信息保存的路径
    private final Preferences preferences = Preferences.userRoot().node("/org/lifxue/wuzhu");

    /**
     * @param key   1
     * @param value 2
     * @Description: 更新Preferences的内容
     * @return: void
     * @author: mapleaf
     * @date: 2020/6/23 18:31
     */
    public void updatePreferencesValue(String key, String value) {
        preferences.put(key, value);
    }

    /**
     * 将最新Preferences的值写入配置文件
     */
    public void flushPreferences() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            log.error(e.toString());
        }
    }

    /**
     * @param key 1
     * @param v   2
     * @Description: 根据key获取configProperties中对应的value
     * @return: java.lang.String
     * @author: mapleaf
     * @date: 2020/6/23 18:31
     */
    public String getPreferencesValue(String key, String v) {
        return preferences.get(key, v);
    }

    public void removePreferences(String k) {
        preferences.remove(k);
    }

    public String getCmcApiKey() {
        return preferences.get(CMC_API_KEY, "");
    }
}
