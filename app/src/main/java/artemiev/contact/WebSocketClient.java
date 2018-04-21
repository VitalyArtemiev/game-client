package artemiev.contact;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;

/**
 * 46   * This is an example of a WebSocket client.
 * 47   * <p>
 * 48   * In order to run this example you need a compatible WebSocket server.
 * 49   * Therefore you can either start the WebSocket server from the examples
 * 50   * by running
 * 51   * or connect to an existing WebSocket server such as
 * 52   * <a href="http://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 * 53   * <p>
 * 54   * The client will attempt to connect to the URI passed to it as the first argument.
 * 55   * You don't have to specify any arguments if you want to connect to the example WebSocket server,
 * 56   * as this is the default.
 * 57
 */
public final class WebSocketClient /*implements ClientInterface*/{
    static final String URL = System.getProperty("url", "ws://192.168.1.34:8080/websocket");
    private static MyEventListener EventListener;

    public static void setEventListener(MyEventListener el) {
        EventListener = el;
        handler.setEventListener(el);
    }

    private static EventLoopGroup group;
    private static WebSocketClientHandler handler;
    private static Channel ch;

    public static void startup() throws Exception {
        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "192.168.1.34" : uri.getHost();
        final int port;
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

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            System.err.println("Only WS(S) is supported.");
            return;
        }

        /*final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }*/

        group = new NioEventLoopGroup();
        try {
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            handler =
                    new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

            Bootstrap b = new Bootstrap();
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000); //Todo: tweak timeout
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            /*if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            }*/
                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(8192),
                                    WebSocketClientCompressionHandler.INSTANCE,
                                    handler);
                        }
                    });

            ChannelFuture future = b.connect(uri.getHost(), port);
            future.sync();  //Todo: possibly add listener for a successful connection instead of sync

            if (!future.isSuccess()) {
                throw new Exception(future.cause());
            }
            ch = future.channel();

            handler.handshakeFuture().sync();


        } catch (Exception e) {
            group.shutdownGracefully();
            throw e;
        }
    }

    public static void shutdown() {
        group.shutdownGracefully();
    }

    private static boolean connectionActive(boolean tryReconnect) { //ToDO: this is messy
        boolean result = ch != null;
        try {
            if (tryReconnect && !result)
                startup();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    public static void pingServer() {
        WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
        ch.writeAndFlush(frame);
    }

    public static void closeConnection() throws InterruptedException {
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
    }

    private static String formRequest() {//variable parameter type and number
        String result = "";

        //Todo: form correct request

        return result;
    }

    public static void register(String nickName) {
        WebSocketFrame frame = new TextWebSocketFrame("registerPlayer\n" + Integer.toString(Player.ID) + '\n' + nickName + '\n');
        ch.writeAndFlush(frame);
    }

    public static void fetchRoomList() {
        if (connectionActive(true)) {
            WebSocketFrame frame = new TextWebSocketFrame("getRoomList\n"); //Todo: use formRequest()

            ChannelFuture fut = ch.writeAndFlush(frame);

            try {
                fut.sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!fut.isSuccess()) {
                EventListener.onEventFailed("Failed to send request");
            }

        } else {
            EventListener.onEventFailed("Failed to establish connection");
        }

    }

    public static void fetchRoomData(int roomID) {
        WebSocketFrame frame = new TextWebSocketFrame("getRoomData\n" + Integer.toString(roomID) + '\n');
        ch.writeAndFlush(frame);
    }

    public static void createRoom() {
        WebSocketFrame frame = new TextWebSocketFrame("createRoom\n" + Integer.toString(Player.ID) + '\n');
        ch.writeAndFlush(frame);
    }

    public static void enterRoom(int roomID) {
        WebSocketFrame frame = new TextWebSocketFrame("enterRoom\n" + Integer.toString(roomID) + '\n' + Integer.toString(Player.ID) + '\n');
        ch.writeAndFlush(frame);
    }

    public static void leaveRoom() {
        WebSocketFrame frame = new TextWebSocketFrame("enterRoom\n" + Integer.toString(Player.ID) + '\n');
        ch.writeAndFlush(frame);
    }
}
