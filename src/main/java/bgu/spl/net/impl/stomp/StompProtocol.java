package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.concurrent.atomic.AtomicBoolean;

public class StompProtocol implements StompMessagingProtocol {
    private AtomicBoolean terminate = new AtomicBoolean(false);
    private int connectionID;
    private Connections<String> connections;
    // for local use
    private int currSize; // the message size
    private String currVersion = "1.2";
    private String[] headers;
    private String[] body;

    public void start(int connectionId, Connections<String> connections){
        this.connectionID= connectionId;
        this.connections = connections;
    }
    //Main Process
    public void process(String msg) {
        currSize = msg.length();
        String[] splited = msg.split("\n", 2);
        String[] temp = splited[1].split("\n\n",2);
        if(temp.length!=2) {ErrorProcess("Invalid msg");return;}
        headers=temp[0].split("\n");
        body = temp[1].split("\n");
        switch(splited[0]) {
            case "CONNECT": connectProcess(); break;
            case "SEND": sendProcess(); break;
            case "SUBSCRIBE": subscribeProcess(); break;
            case "UNSUBSCRIBE" : unsubscribeProcess(); break;
//            case "ACK": ackProcess(); break;
//            case "NACK": nackProcess(); break;
//            case "BEGIN": beginProcess(); break;
//            case "COMMIT": commitProcess(); break;
//            case "ABORT": abortProcess(); break;
            case "DISCONNECT": disconnectProcess(); break;
            default: ErrorProcess("unknown title");
        }
    }
//********subProcess****************************

    private void unsubscribeProcess() {
        // headers check
        if(!checkHeaders(new String[]{"id"},true)) return;

        int ID = Integer.parseInt(headers[0].substring(3));
        //TODO - unsubscribe logic for (connection ID, subscription ID)
        connections.getDataBase().unsubscribe(ID);


    }



    private void subscribeProcess() {
        //headers check
        if(!checkHeaders(new String[]{"destination","id"},true)) return;

        String destination = headers[0].substring(12);
        int ID = Integer.parseInt(headers[1].substring(3));
        // TODO - subscribe in DataBase with Connections
    }

    private void connectProcess() {

        //headers check
        if(!checkHeaders(new String[]{"accept-version","host","login","passcode"},true)) return;

        String accept_version = headers[0].substring(15);
        String host = headers[1].substring(5);
        String login = headers[2].substring(6);
        String passWord = headers[3].substring(9);
        if(!accept_version.equals(currVersion)) {ErrorProcess("Invalid version! excepted: '"+currVersion+"'; provided: '"+accept_version+"';"); return;}
        String DataBaseRespond = connections.getDataBase().logIn(connectionID, login, passWord);
        if(DataBaseRespond.equals(""))
            connections.send(connectionID,"CONNECTED\nversion:"+currVersion+"\n\n^@");
        else
            ErrorProcess(DataBaseRespond);
    }

    private void disconnectProcess() {
        // headers check
        if(!checkHeaders(new String[]{"receipt"},true)) return;

        connections.send(connectionID, "RECEIPT\nreceipt-id:"+headers[0].substring(8)+"\n\n^@");
        connections.disconnect(connectionID);
    }


    private void sendProcess() {
        if(!checkHeaders(new String[]{"destination"},true)) return;

        String destination = headers[0].substring(12);
        //TODO - process text content - currMsg[1]
        //connections- do something correspond to the content
    }
    private void ErrorProcess(String outputString) {

        connections.send(connectionID,"ERROR" +
                "\nreceipt-id:"+extractReceiptID()+
                "\ncontent-type:"+ (body[0].equals("^@") ? "plain": "text")+
                "\ncontent-length:"+currSize+
                "\nthe message: \n ----- \n"+
                printLineByLine(headers) + "\n" + printLineByLine(body) +
                "\n-----\n" + outputString +"\n^@");
    }
    private void ReceiptProcess() {
        String recId= fetchHeader("receipt")
        connections.send(connectionID, "RECEIPT\nreceipt:"+recId+"\n\n^@");
    }

    public boolean shouldTerminate(){
        return terminate.get();
    }

    //************************************************helpers**********************************************************
    // check and send an error if necessarily
    private boolean checkHeaders(String[] bindings, boolean withError){
        boolean ans = bindings.length <= headers.length;
        if(withError && !ans)  ErrorProcess("missing headers in the frame");
        for(int i=0;ans && i<bindings.length; i++){
            int l = bindings[i].length();
            ans = headers[i].length()>=l && headers[i].substring(0,l).equals(bindings[i]+":");
            if(withError && !ans) ErrorProcess("the header: "+bindings[i]+" was excepted; instead provided: "+headers[i] );
        }
        return ans;
    }
    private String fetchHeader(String s){
        int l= s.length()+1;
        for(String str: headers)
            if(str.length()>=l && str.substring(0,l).equals(s+":"))
                return str.substring(l);
        return "";
    }

    private String printLineByLine(String[] array){
        StringBuilder res= new StringBuilder();
        for(String s: array){
            if(!s.equals("^@"))
                res.append(s).append("\n");
        }
        return res.toString();
    }

    private String extractReceiptID() {

        for (String s : headers) {
            if (s.length() > 8 && s.substring(0, 8).equals("receipt:")) return s.substring(8);
        }
        return "none";
    }


}
