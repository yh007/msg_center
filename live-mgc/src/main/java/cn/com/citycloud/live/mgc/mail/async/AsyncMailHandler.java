package cn.com.citycloud.live.mgc.mail.async;

import cn.com.citycloud.live.mgc.mail.exception.AsyncMailHandlerException;

public abstract interface AsyncMailHandler {
    public abstract void startAsyncHandle() throws AsyncMailHandlerException;

    public abstract void stopAsyncHandle() throws AsyncMailHandlerException;
}
