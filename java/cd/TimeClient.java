import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.lang.Thread;

public class TimeClient {

    private static int port;
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static URI uri;

    public static void main(String[] args) throws Exception {

        // (1) Set up URL and Port
        String URL = System.getProperty("url", "wss://ws-feed.gdax.com");
        uri = new URI(URL);
        String scheme = uri.getScheme() == null? "ws" : uri.getScheme();
        final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();

        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(uri.getHost(), port).sync(); // (5)
            f.await();
            Channel ch = f.channel();

            // Make Subscriptions
            String msg = "{\"type\": \"subscribe\",\"product_ids\": [\"ETH-USD\",\"ETH-EUR\"],\"channels\": [\"level2\",\"heartbeat\",{\"name\": \"ticker\",\"product_ids\": [\"ETH-BTC\",\"ETH-USD\"]}]}";
            ChannelFuture writeFuture = ch.writeAndFlush(msg);
            writeFuture.await();

            msg = "subscribe to feed 2";
            writeFuture = ch.writeAndFlush(msg);
            writeFuture.await();

            msg = "subscribe to feed 3";
            writeFuture = ch.writeAndFlush(msg);
            writeFuture.await();

            while(true){Thread.sleep(250);}

            // Wait until the connection is closed.
            // f.channel().closeFuture().sync();
            // ch.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}