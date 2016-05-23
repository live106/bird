package com.live106.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by live106 on 2016/5/23.
 */
public class TimeoutHandler extends SimpleChannelInboundHandler<Object> {
    public long startTime;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    public void println(String s) {
        System.err.println("TimeoutHandler : " + s);
    }
}
