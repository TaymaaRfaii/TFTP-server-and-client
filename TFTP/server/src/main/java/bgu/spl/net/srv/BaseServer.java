package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.Packets.Packet;
import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.impl.tftp.TftpProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<TftpProtocol> protocolFactory;
    private final Supplier<TftpEncoderDecoder> encdecFactory;
    private ServerSocket sock;
    private ConnectionsImpl connections;
    private int id;

    public BaseServer(
            int port,
            Supplier<TftpProtocol> protocolFactory,
            Supplier<TftpEncoderDecoder> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        this.sock = null;
        this.connections=new ConnectionsImpl();
        id =1;

    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
            System.out.println("Server started");
            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSock = serverSock.accept();
                BlockingConnectionHandler handler = new BlockingConnectionHandler(
                        clientSock,
                        encdecFactory.get(),
                        (TftpProtocol) protocolFactory.get());
                handler.getProtocol().start(id,connections);        
                ((ConnectionsImpl)connections).connect(id , (ConnectionHandler<Packet>) handler) ; 
                id++;   
                execute(handler);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler  handler);

}