package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final StompMessagingProtocol protocol;
    private final MessageEncoderDecoder<String> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ConcurrentLinkedQueue<String> writeQ;


    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<String> reader, StompMessagingProtocol protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.writeQ = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            read = in.read();
            while (!protocol.shouldTerminate() && connected && (read >= 0) || !writeQ.isEmpty()) {
                if(read >= 0){
                    String nextMessage = encdec.decodeNextByte((byte) read);
                    if (nextMessage != null)
                        protocol.process(nextMessage);
                    read= in.read();
                }
                if(!writeQ.isEmpty()){
                    out.write(encdec.encode(writeQ.poll()));
                    out.flush();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        writeQ.add((String) msg);

    }

    public void startProtocol(int chid, ConnectionsImpl connections) {
        this.protocol.start(chid,connections);
    }
}
