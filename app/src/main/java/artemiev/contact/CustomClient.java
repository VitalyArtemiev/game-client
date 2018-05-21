package artemiev.contact;

import io.netty.channel.Channel;

public class CustomClient {
    protected Channel channel;
    protected MyEventListener eventListener;

    public CustomClient(Channel ch, MyEventListener el) {
        channel = ch;
        eventListener = el;
    }
}
