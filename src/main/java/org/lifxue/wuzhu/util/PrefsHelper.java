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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author lif
 */
@Slf4j
public class PrefsHelper {
    // 主题
    public static final String THEME = "theme";
    // 更新价格
    public static final String UPDATEPRICE = "updateprice";
    // 更新coin信息
    public static final String COINIDMAP = "coinidmap";
    // coin信息最后更新日期
    public static final String COINIDMAP_DATE = "coinidmaplastdate";
    // 忽略小额品种
    public static final String NOTSMALLCOIN = "notsmallcoin";
    public static final String NOTSMALLCOINNUM = "notsmallcoinnum";
    //代理设置
    public static final String PROXY = "proxy";
    public static final String HOST = "host";
    public static final String PORT = "port";

    //coin-market-cap网站的 apikey
    public static final String CMC_API_KEY = "cmcapikey";

    //信息保存的路径
    private static final Preferences preferences =
        Preferences.userRoot().node("/org/lifxue/wuzhu");

    /**
     * @param key   1
     * @param value 2
     * @Description: 更新Preferences的内容
     * @return: void
     * @author: mapleaf
     * @date: 2020/6/23 18:31
     */
    public static void updatePreferencesValue(String key, String value) {
        preferences.put(key, value);
    }

    /**
     * 将最新Preferences的值写入配置文件
     */
    public static void flushPreferences() {
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
    public static String getPreferencesValue(String key, String v) {
        return preferences.get(key, v);
    }

    public static void removePreferences(String k) {
        preferences.remove(k);
    }

    public static String getCmcApiKey() {
        return preferences.get(CMC_API_KEY, "");
    }
}
