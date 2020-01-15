package bgu.spl.net.impl.echo;

import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.impl.stomp.StompProtocol;
import bgu.spl.net.srv.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class EchoClient {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            args = new String[]{InetAddress.getLocalHost().getHostAddress(), "CONNECT\naccept-version:1.2\nhost:local\nlogin:bob\npasscode:alic\n\n\u0000",
                    "CONNECT\naccept-version:1.2\nhost:local\nlogin:bo\npasscode:alic\n\n\u0000",
            "SUBSCRIBE\ndestination:sci-fi\nid:78\nreceipt:45\n\n\u0000","SEND\ndestination:sci-fi\n\nbob wish to borrow dune\n\u0000",
            "DISCONNECT\nreceipt:48\n\n\u0000"};
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }

        //BufferedReader and BufferedWriter automatically using UTF-8 encoding
        try (Socket sock = new Socket(args[0], 7778);

             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
            for(int i =1; i<args.length;i++) {
                System.out.println(args[i]);
                out.write(args[i]);
                out.newLine();
                out.flush();

                System.out.println("awaiting response");
                String msg = in.readLine();
                String line = in.readLine();
                while ( !line.equals("\u0000")) {
                    msg = msg + "\n" + line;
                    if(in.ready())line = in.readLine();
                }
                System.out.println("message from server: \n" + msg);
                System.out.println("-----------------------");
            }
        }
    }
}
