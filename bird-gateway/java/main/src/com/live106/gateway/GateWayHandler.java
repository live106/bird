package com.live106.gateway;

import com.live106.dispatcher.ProtocolDispatcher;
import com.live106.message.Protocols;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * Created by live106 on 2016/5/19.
 */
public class GateWayHandler extends SimpleChannelInboundHandler<Protocols.Protocol> {

    private static DefaultEventExecutorGroup executors = new DefaultEventExecutorGroup(4);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocols.Protocol msg) throws Exception {
        executors.execute(() -> ProtocolDispatcher.dispatch(msg));
    }
}
