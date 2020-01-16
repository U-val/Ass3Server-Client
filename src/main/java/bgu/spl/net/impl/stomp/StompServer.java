package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("invalid arguments! required - <port> <server type>");
        }
        int port = Integer.parseInt(args[0]);
        if(args[1].equals("tpc")) {
            Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    port, //port
                    StompProtocol::new, //protocol factory
                    EncDecImp::new //message encoder decoder factory
            ).serve();
        }
        else {
            Server.threadPerClient(
                    port, //port
                    StompProtocol::new, //protocol factory
                    EncDecImp::new //message encoder decoder factory
            ).serve();
        }
    }


}
