package com.live106.dispatcher;

/**
 * Created by live106 on 2016/5/20.
 */
public abstract class ProtocolHandler<T> {

    private final T message;

    public ProtocolHandler(T message) {
        this.message = message;
    }
    public abstract boolean handle();

    public void doIt() {
        handle();
    }
}
