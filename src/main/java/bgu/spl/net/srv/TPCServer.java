package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;

import java.nio.channels.SelectionKey;
import java.util.function.Supplier;

public class TPCServer<T> extends BaseServer<T> {
    private ConnectionsImpl connections = ConnectionsImpl.getInstance();
    private int HandlerID=0;

    public TPCServer(int port, Supplier<StompMessagingProtocol> protocolFactory, Supplier<MessageEncoderDecoder<T>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
    }

    @Override
    protected void execute(BlockingConnectionHandler<T> handler) {
        int CHID;
        while (connections.addHandler(CHID=takeNumber(), handler));
        handler.startProtocol(CHID, connections);
        new Thread(handler).start();
      //  clientChan.register(selector, SelectionKey.OP_READ, handler);
    }

    private int takeNumber() {
        HandlerID++;
        if(HandlerID>512) HandlerID= HandlerID-512;
        return HandlerID;
    }
}
