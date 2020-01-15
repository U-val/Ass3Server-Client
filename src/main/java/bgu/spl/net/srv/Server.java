package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;

import java.io.Closeable;
import java.util.function.Supplier;

public interface Server<String> extends Closeable {

    /**
     * The main loop of the server, Starts listening and handling new clients.
     */
    void serve();

    /**
     *This function returns a new instance of a thread per client pattern server
     * @param port The port for the server socket
     * @param protocolFactory A factory that creats new MessagingProtocols
     * @param encoderDecoderFactory A factory that creats new MessageEncoderDecoder
     * @param <String> The Message Object for the protocol
     * @return A new Thread per client server
     */
    public static <String> Server<String>  threadPerClient(
            int port,
            Supplier<StompMessagingProtocol > protocolFactory,
            Supplier<MessageEncoderDecoder<String> > encoderDecoderFactory) {

//        return new BaseServer<String>(port, protocolFactory, encoderDecoderFactory) {
//            @Override
//            protected void execute(BlockingConnectionHandler<String>  handler) {
//                new Thread(handler).start();
//            }
//        };
        return new TPCServer<>(port,protocolFactory,encoderDecoderFactory);


    }

    /**
     * This function returns a new instance of a reactor pattern server
     * @param <T> The Message Object for the protocol
     * @param nthreads Number of threads available for protocol processing
     * @param port The port for the server socket
     * @param protocolFactory A factory that creats new MessagingProtocols
     * @param encoderDecoderFactory A factory that creats new MessageEncoderDecoder
     * @return A new reactor server
     */
    public static <T> Reactor<T> reactor(
            int nthreads,
            int port,
            Supplier<StompMessagingProtocol> protocolFactory,   // changed from MessageProtocol<T>
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory) {
        return new Reactor<T>(nthreads, port, protocolFactory, encoderDecoderFactory);
    }

}
