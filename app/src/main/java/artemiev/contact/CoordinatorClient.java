package artemiev.contact;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import static artemiev.contact.WebSocketClient.connectionActive;
import static artemiev.contact.WebSocketClient.sendRequest;

public class CoordinatorClient extends CustomClient implements ClientInterface {
    public CoordinatorClient(Channel ch, MyEventListener el) {
        super(ch, el);
    }

    public void register(String nickName) {
        WebSocketFrame frame = new TextWebSocketFrame("registerPlayer\n" + Integer.toString(Player.ID) + '\n' + nickName + '\n');
        channel.writeAndFlush(frame);
    }

    public void fetchRoomList() {
        if (connectionActive(true)) {
            ChannelFuture fut = sendRequest("{\n" +
                    "  \"message\": {\n" +
                    "    \"request\" : \"roomList\",\n" +
                    "    \"id\": 0\n" +
                    "  }\n" +
                    "}");

            try {
                fut.sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!fut.isSuccess()) {
                eventListener.onEventFailed("Failed to send request");
            }

        } else {
            eventListener.onEventFailed("Failed to establish connection");
        }

    }

    public void fetchRoomData(int roomID) {
        sendRequest("getRoomData", roomID);
    }

    public void createRoom() {
        sendRequest("{\n" +
                "  \"message\": {\n" +
                "    \"request\" : \"newRoom\",\n" +
                "    \"roomName\" : \"firstroom\",\n" +
                "    \"playerLimit\":  8,\n" +
                "    \"id\" : 0\n" +
                "  }\n" +
                "}", Player.ID);
    }

    public void enterRoom(int roomID) {
        sendRequest("enterRoom", roomID ,Player.ID);
    }

    public void leaveRoom() {
        sendRequest("enterRoom", Player.ID);
    }
}
