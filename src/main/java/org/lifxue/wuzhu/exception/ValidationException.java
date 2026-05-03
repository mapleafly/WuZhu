package org.lifxue.wuzhu.exception;

/**
 * 数据验证异常
 */
public class ValidationException extends WuZhuException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
