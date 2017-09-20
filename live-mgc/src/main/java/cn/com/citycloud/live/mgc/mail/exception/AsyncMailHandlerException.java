package cn.com.citycloud.live.mgc.mail.exception;

public class AsyncMailHandlerException extends RuntimeException {
    /**
     */
    private static final long serialVersionUID = 3749946700499655637L;

    public AsyncMailHandlerException() {
    }

    public AsyncMailHandlerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AsyncMailHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncMailHandlerException(String message) {
        super(message);
    }

    public AsyncMailHandlerException(Throwable cause) {
        super(cause);
    }
}
