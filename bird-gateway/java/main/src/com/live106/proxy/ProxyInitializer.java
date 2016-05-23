package com.live106.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by live106 on 2016/5/16.
 */
public class ProxyInitializer extends ChannelInitializer<SocketChannel> {
    private final String remoteHost;
    private final int remotePort;

    public ProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO), new ProxyFrontendHandler(remoteHost, remotePort));
    }
}
