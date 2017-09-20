package cn.com.citycloud.live.mgc.mail.async;

public abstract interface TaskQueue<E> {
    public abstract E take();

    public abstract void put(E paramE);
}
