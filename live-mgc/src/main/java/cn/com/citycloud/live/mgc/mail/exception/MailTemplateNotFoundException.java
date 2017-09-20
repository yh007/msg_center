package cn.com.citycloud.live.mgc.mail.exception;

public class MailTemplateNotFoundException extends RuntimeException {
     
    private static final long serialVersionUID = 6977607824880010356L;

    public MailTemplateNotFoundException() {
    }

    public MailTemplateNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MailTemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailTemplateNotFoundException(String message) {
        super(message);
    }

    public MailTemplateNotFoundException(Throwable cause) {
        super(cause);
    }
}
