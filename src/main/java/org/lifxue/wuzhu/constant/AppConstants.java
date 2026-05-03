package org.lifxue.wuzhu.constant;

import java.math.RoundingMode;

/**
 * 应用通用常量类
 */
public final class AppConstants {
    
    private AppConstants() {
        // 防止实例化
    }
    
    /**
     * 默认代理主机地址
     */
    public static final String DEFAULT_PROXY_HOST = "127.0.0.1";
    
    /**
     * 默认代理端口
     */
    public static final String DEFAULT_PROXY_PORT = "56908";
    
    /**
     * BigDecimal 默认精度（小数位数）
     */
    public static final int DEFAULT_SCALE = 12;
    
    /**
     * BigDecimal 默认舍入模式
     */
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
}
