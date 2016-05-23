package com.live106.proxy;

import com.live106.util.IOUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by live106 on 2016/5/16.
 */
public final class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {
    private final String remoteHost;
    private final int remotePort;
    private volatile Channel outboundChannel;

    public ProxyFrontendHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel inboundChannel = ctx.channel();
        // connect to the remote
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new IdleStateHandler(ProxyConfig.BACK_END_IDLE_TIMEOUT, 0, 0)
                                , new ProxyBackendHandler(inboundChannel)
                        );
                    }
                })
                .option(ChannelOption.AUTO_READ, false);

        ChannelFuture f = b.connect(remoteHost, remotePort);
        this.outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    inboundChannel.read();
                } else {
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (outboundChannel != null) {
            IOUtils.closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        IOUtils.closeOnFlush(ctx.channel());
    }

}
