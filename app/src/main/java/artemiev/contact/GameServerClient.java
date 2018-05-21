package artemiev.contact;

import io.netty.channel.Channel;

public class GameServerClient extends CustomClient {
    public GameServerClient(Channel ch, MyEventListener el) {
        super(ch, el);
    }
}
