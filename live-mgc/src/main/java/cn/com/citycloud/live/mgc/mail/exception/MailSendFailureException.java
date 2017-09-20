package cn.com.citycloud.live.mgc.mail.exception;

public class MailSendFailureException extends RuntimeException {
    
    private static final long serialVersionUID = 1436266786574980153L;

    public MailSendFailureException() {
    }

    public MailSendFailureException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public MailSendFailureException(String arg0) {
        super(arg0);
    }

    public MailSendFailureException(Throwable arg0) {
        super(arg0);
    }
}
