package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class TPCServer<T> extends BaseServer<T> {
    private ConnectionsImpl connections = ConnectionsImpl.getInstance();
    private int HandlerID=0;
    private ConcurrentHashMap<Integer, Thread> threads;

    public TPCServer(int port, Supplier<StompMessagingProtocol> protocolFactory, Supplier<MessageEncoderDecoder<T>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
        threads= new ConcurrentHashMap<>();
    }

    @Override
    protected void execute(BlockingConnectionHandler<T> handler) {
        int CHID;
        while (connections.addHandler(CHID=takeNumber(), handler));
        handler.startProtocol(CHID, connections);

        Thread t = new Thread(handler);
        threads.put(CHID,t);
        t.start();

      //  clientChan.register(selector, SelectionKey.OP_READ, handler);
    }

    private int takeNumber() {
        HandlerID++;
        if(HandlerID>512) HandlerID= HandlerID-512;
        return HandlerID;
    }
}
