package artemiev.contact;

import java.util.ArrayList;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private MyEventListener EventListener;

    public void setEventListener(MyEventListener el) {
        EventListener = el;
    }

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            System.out.println("WebSocket Client received message: " + textFrame.text());

            tryParseTextResponse(textFrame.text());

        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    private void tryParseTextResponse(String response) {
        //roomList\n roomNumber \n roomId <space> roomName <space> playerCount <space> playerLimit \n

        String[] tokens = response.split("[\n]");
        switch (tokens[0]) {
            case "roomList": {
                ArrayList<Room> rooms = new ArrayList<>();
                int l = tokens.length;
                if (l < 2)
                    break;

                int roomNumber = Integer.getInteger(tokens[1]);
                System.out.println("WTFUUUUUUUUU");
                if (roomNumber == 0) {
                    //ToDo handle rooms not found
                    if (EventListener != null)
                        EventListener.onEventFailed("rooms not found");
                }
                if (roomNumber + 2 != l) {
                    //ToDO handle incorrect response
                    if (EventListener != null)
                        EventListener.onEventFailed("roomNumber + 2 != l");
                }

                for (int i = 0; i < roomNumber; i++) {
                    String[] roomParams = tokens[i+2].split("[ ]");

                    if (roomParams.length != 4) {
                        //ToDo handle incorrect response
                        if (EventListener != null)
                            EventListener.onEventFailed("roomParams fail");
                    }

                    int roomId = Integer.getInteger(roomParams[0]);
                    String roomName = roomParams[1];
                    int playerCount = Integer.getInteger(roomParams[2]);
                    int playerLimit = Integer.getInteger(roomParams[3]);

                    rooms.add(new Room(roomId, roomName, playerCount, playerLimit));
                    if (EventListener != null)
                        EventListener.onEventCompleted(rooms);
                }

                break;
            }
            case "roomData": {

                break;
            }
            default: {

            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}

