package org.lifxue.wuzhu.exception;

/**
 * 数据访问异常
 */
public class DataAccessException extends WuZhuException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
