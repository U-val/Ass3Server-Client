package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, //port
                StompProtocol::new, //protocol factory
                EncDecImp::new //message encoder decoder factory
        ).serve();
    }


}
