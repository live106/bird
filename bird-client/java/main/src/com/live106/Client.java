package com.live106;

import com.live106.handler.TimeoutHandler;
import com.live106.handler.WebSocketClientHandler;
import com.live106.message.Protocols;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;

import static io.netty.buffer.Unpooled.wrappedBuffer;

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
    private static String websocketURL = "ws://localhost:8080/bird";
    private static URI uri;
    //    private static WebSocketClientProtocolHandler webSocketClientProtocolHandler;
    private static WebSocketClientHandler webSocketClientHandler;

    public static void main(String[] args) throws Exception {
        uri = new URI(websocketURL);
        final WebSocketClientHandshaker handShaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
//        webSocketClientProtocolHandler = new WebSocketClientProtocolHandler(handShaker);
        webSocketClientHandler = new WebSocketClientHandler(handShaker);
        Channel channel = configureBootstrap(new Bootstrap()).connect().sync().channel();
        webSocketClientHandler.handshakeFuture().sync();

        Protocols.CLogin.Builder builder = Protocols.CLogin.newBuilder();
        builder.setUsername("admin");
        builder.setPassword("password");
        builder.setMid("1234");

        Protocols.Protocol.Builder b = Protocols.Protocol.newBuilder();
        b.setCLogin(builder.build());

        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(wrappedBuffer(b.build().toByteArray()));
        channel.writeAndFlush(frame);
//        webSocketClientProtocolHandler.handshaker().handshake(channel);
//        .addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (future.isSuccess()) {
//                    Protocols.CLogin.Builder builder = Protocols.CLogin.newBuilder();
//                    builder.setUsername("admin");
//                    builder.setPassword("password");
//                    builder.setMid("1234");
//
//                    Protocols.Protocol.Builder b = Protocols.Protocol.newBuilder();
//                    b.setCLogin(builder.build());
////                    future.channel().writeAndFlush(b.build());
//
//                    BinaryWebSocketFrame frame = new BinaryWebSocketFrame(wrappedBuffer(b.build().toByteArray()));
//                    future.channel().writeAndFlush(frame);
//                } else {
//                    System.err.println("error");
//                }
//            }
//        });
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
                        ch.pipeline()
                                .addLast("idle", new IdleStateHandler(READ_TIMEOUT, 0, 0))
                                .addLast("httpCodec", new HttpClientCodec())
                                .addLast("httpObject", new HttpObjectAggregator(8192))
                                .addLast("websocketCompression", WebSocketClientCompressionHandler.INSTANCE)
                                .addLast("websocketHandler", webSocketClientHandler)
//                                .addLast("websocketHandler", webSocketClientProtocolHandler)
//                                .addLast("frameEncoder", new LengthFieldPrepender(4))
//                                .addLast("protobuffEncoder", new ProtobufEncoder())
//                                .addLast("timeout", handler)
                        ;
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
