package org.lifxue.wuzhu.exception;

/**
 * API调用异常
 */
public class ApiCallException extends WuZhuException {

    public ApiCallException(String message) {
        super(message);
    }

    public ApiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
