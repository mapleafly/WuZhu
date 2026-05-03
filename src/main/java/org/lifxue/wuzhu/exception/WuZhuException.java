package org.lifxue.wuzhu.exception;

/**
 * WuZhu 应用程序基础异常类
 * 所有业务异常都应该继承此类
 */
public class WuZhuException extends RuntimeException {

    public WuZhuException(String message) {
        super(message);
    }

    public WuZhuException(String message, Throwable cause) {
        super(message, cause);
    }
}
