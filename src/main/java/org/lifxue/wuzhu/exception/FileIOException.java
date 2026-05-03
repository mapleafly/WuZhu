package org.lifxue.wuzhu.exception;

/**
 * 文件操作异常
 */
public class FileIOException extends WuZhuException {

    public FileIOException(String message) {
        super(message);
    }

    public FileIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
