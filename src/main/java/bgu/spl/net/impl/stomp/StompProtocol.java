package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.concurrent.atomic.AtomicBoolean;

public class StompProtocol implements StompMessagingProtocol {
    private AtomicBoolean terminate = new AtomicBoolean(false);
    private int connectionID;
    private Connections<String> connections;
    private String[] currMsg; //0-headers, 1-text
    private int currSize; // the message size

    public void start(int connectionId, Connections<String> connections){
        this.connectionID= connectionId;
        this.connections = connections;
    }

    public void process(String msg) {
        currSize = msg.length();
        String[] splited = msg.split("\n", 2);
        currMsg = splited[1].split("\n\n",2);
        switch(splited[0]) {
            case "CONNECT": connectProcess(); break;
            case "SEND": sendProcess(); break;
            case "SUBSCRIBE": subscribeProcess(); break;
            case "UNSUBSCRIBE" : unsubscribeProcess(); break;
            case "ACK": ackProcess(); break;
            case "NACK": nackProcess(); break;
            case "BEGIN": beginProcess(); break;
            case "COMMIT": commitProcess(); break;
            case "ABORT": abortProcess(); break;
            case "DISCONNECT": disconnectProcess(); break;
            default: ErrorProcess("unknown title");
        }
    }

    private void disconnectProcess() {
        String[] headers = currMsg[0].split("\n");
        if(headers.length<1 || headers[0].length()<8 || !headers[0].substring(0,7).equals("receipt:")) {ErrorProcess("excepted the header field : 'receipt'"); return;}
        connections.send(connectionID, "msg(eith receipt)");
        connections.disconnect(connectionID);
    }

    private void ErrorProcess(String outputString) {


        connections.send(connectionID,"ERROR" +
              "\nreceipt-id:"+extractReceiptID()+
                "\ncontent-type:"+ (currMsg[1].equals("^@") ? "plain": "text")+
                "\ncontent-length:"+currSize+
                "\nthe message: \n ----- \n"+
                currMsg[0] + "\n" + currMsg[1] +
        "\n-----\n" + outputString +"\n^@");
    }

    private String extractReceiptID() {
        String[] headers = currMsg[0].split("\n");
        for (String s : headers) {
            if (s.length() > 8 && s.substring(0, 7).equals("receipt:")) return s.substring(8);
        }
        return "none";
    }

    private void unsubscribeProcess() {
        String[] headers = currMsg[0].split("\n");
        if(headers.length<3 || !headers[2].equals("^@")) {ErrorProcess("excepted '^@' at 3rd line");return;}
        if(headers[0].length()<3 || !headers.equals("id:")) {ErrorProcess("excepted 'id:' at the 2nd line"); return;}

        int ID = Integer.parseInt(headers[0].substring(3));
        //TODO - unsubscribe logic for (connection ID, subscription ID)
    }

    private void subscribeProcess() {
        String[] headers = currMsg[0].split("\n");
        if(headers.length<5 || !headers[4].equals("^@")) {ErrorProcess("excepted '^@' at the 5th line"); return;} //check needed - if with receipt
        if(headers[0].length()<12 || !headers[0].substring(0,11).equals("destination:")){ ErrorProcess("excepted 'destination:' at the 2nd line"); return;}
        if(headers[1].length()<3 || !headers[1].substring(0,2).equals("id:")) {ErrorProcess("excepted 'id:' at the 3rd line"); return;}

        String destination = headers[0].substring(12);
        int ID = Integer.parseInt(headers[1].substring(3));
        // TODO - subscribe in DataBase with Connections


    }

    private void connectProcess() {
        String[] headers = currMsg[0].split("\n");
        //headers check
        if(headers.length<6 || !headers[5].equals("^@")){ ErrorProcess("excepted '^@' at the 6th line"); return;}
        if(headers[0].length()<15 || !headers[0].substring(0,14).equals("accept-version:")) {ErrorProcess("bad argument at the 2nd line"); return;}
        if(headers[1].length()<5 || !headers[1].substring(0,4).equals("host:")) {ErrorProcess("bad argument at the 3rd line"); return;}
        if(headers[2].length()<6 || !headers[2].substring(0,5).equals("login:")) {ErrorProcess("bad argument at the 4th line"); return;}
        if(headers[3].length()<9 || !headers[3].substring(0,8).equals("passcode:")) {ErrorProcess("bad argument at the 5th line"); return;}

        String accept_version = headers[0].substring(15);
        String host = headers[1].substring(5);
        String login = headers[2].substring(6);
        String passWard = headers[3].substring(9);

        connections.login(connectionID, login, passWard, accept_version);


    }

    private void sendProcess() {
        String[] headers = currMsg[0].split("\n");
        if(headers.length != 4 || !headers[3].equals("^@")) {ErrorProcess("excepted '^@' at the 4th line"); return;}
        if(headers[0].length()<12 || !headers[0].substring(0,11).equals("destination:")) {ErrorProcess("excepted 'destination:' at the second line"); return;}
        String destination = headers[0].substring(12);
        //TODO - process text content - currMsg[1]
        //connections- do something correspond to the content
    }

    public boolean shouldTerminate(){
        return terminate.get();
    }


}
