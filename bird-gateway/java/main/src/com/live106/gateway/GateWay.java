package com.live106.gateway;

import com.live106.message.Protocols;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by live106 on 2016/5/19.
 */
public final class GateWay {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup childGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("logging", new LoggingHandler(LogLevel.DEBUG))
                                    .addLast("httpServer", new HttpServerCodec())
                                    .addLast("httpObject", new HttpObjectAggregator(65536))
                                    .addLast("websocketCompression", new WebSocketServerCompressionHandler())
//                            .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(4096, 0, 4, 0, 4))
                                    .addLast("websocket", new WebSocketServerProtocolHandler("/bird", null, true))
                                    .addLast("websocketFrame", new WebSocketFrameHandler())
                            .addLast("protobufDecoder", new ProtobufDecoder(Protocols.Protocol.getDefaultInstance()))
                            .addLast("gatewayHandler", new GateWayHandler())
                            ;
                        }
                    })
                    .bind(Config.LOCAL_PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }

    private static class Config {
        public static int LOCAL_PORT = 8080;
    }
}
