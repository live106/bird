package com.live106;

import com.live106.proxy.ProxyConfig;
import com.live106.proxy.ProxyInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by live106 on 2016/5/16.
 */
public class GateWay {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup childGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ProxyInitializer(ProxyConfig.REMOTE_HOST, ProxyConfig.REMOTE_PORT))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(ProxyConfig.LOCAL_PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }

    }
}
