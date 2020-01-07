package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.concurrent.atomic.AtomicBoolean;

public class StompProtocol implements StompMessagingProtocol {
    private AtomicBoolean terminate = new AtomicBoolean(false);
    private int connectionID;
    private Connections<String> connections;

    public void start(int connectionId, Connections<String> connections){
        this.connectionID= connectionId;
        this.connections = connections;
    }

    public void process(String msg) {
        String[] spilted = msg.split("\n", 2);
        switch(spilted[0]) {
            case "SEND": sendProcess(spilted[1]); break;
            case "SUBSCRIBE": subscribeProcess(spilted[1]); break;
            case "UNSUBSCRIBE" : unsubscribeProcess(spilted[1]); break;
            case "ACK": ackProcess(spilted[1]); break;
            case "NACK": nackProcess(spilted[1]); break;
            case "BEGIN": beginProcess(spilted[1]); break;
            case "COMMIT": commitProcess(spilted[1]); break;
            case "ABORT": abortProcess(spilted[1]); break;
            case "DISCONNECT": siconnectProcess(spilted[1]); break;
            default: ErrorProcess("unknown title");
        }
    }

    private void sendProcess(String s) {
        String[] splited = s.split("\n");
        if(splited.length != 3 || !splited[2].equals("^@")) ErrorProcess("excepted '^@' at the 4th line"); return;
        if(splited[0].length()<12 || !splited[0].substring(0,11).equals("destination:")) ErrorProcess("excepted 'destination:' at the second line"); return;

    }

    public boolean shouldTerminate(){
        return terminate.get();
    }


}
