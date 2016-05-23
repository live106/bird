package com.live106;

import com.live106.handler.TimeoutHandler;
import com.live106.message.Protocols;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by live106 on 2016/5/23.
 */
public class Client {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    // Sleep 5 seconds before a reconnection attempt.
    static final int RECONNECT_DELAY = Integer.parseInt(System.getProperty("reconnectDelay", "5"));
    // Reconnect when the server sends nothing for 10 seconds.
    static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

    private static final TimeoutHandler handler = new TimeoutHandler();

    public static void main(String[] args) throws Exception {
        configureBootstrap(new Bootstrap()).connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    Protocols.CLogin.Builder builder = Protocols.CLogin.newBuilder();
                    builder.setUsername("admin");
                    builder.setPassword("password");
                    builder.setMid("1234");

                    Protocols.Protocol.Builder b = Protocols.Protocol.newBuilder();
                    b.setCLogin(builder.build());
                    future.channel().writeAndFlush(b.build());
                }
            }
        });
    }

    private static Bootstrap configureBootstrap(Bootstrap b) {
        return configureBootstrap(b, new NioEventLoopGroup());
    }

    static Bootstrap configureBootstrap(Bootstrap b, EventLoopGroup g) {
        b.group(g)
                .channel(NioSocketChannel.class)
                .remoteAddress(HOST, PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(READ_TIMEOUT, 0, 0), new LengthFieldPrepender(4), new ProtobufEncoder(), handler);
                    }
                });
        return b;
    }

    static void connect(Bootstrap b) {
        b.connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.cause() != null) {
                    handler.startTime = -1;
                    handler.println("Failed to connect: " + future.cause());
                }
            }
        });
    }
}
