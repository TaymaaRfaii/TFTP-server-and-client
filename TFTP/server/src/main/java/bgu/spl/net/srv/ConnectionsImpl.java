package bgu.spl.net.srv;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.impl.Packets.Packet;
public class ConnectionsImpl implements Connections<Packet> {

    ConcurrentHashMap<Integer,ConnectionHandler<Packet>> clientToHandler;
    public ConnectionsImpl(){
        clientToHandler=new ConcurrentHashMap<>();
    }
    @Override
    //add a client connectionId to active client map.
    public void connect(int connectionId, ConnectionHandler<Packet> handler) {
            clientToHandler.put(connectionId, handler);
    }
    @Override
    public boolean send(int connectionId, Packet msg) {
        ConnectionHandler<Packet> handler = clientToHandler.get(connectionId);
        if (handler != null) {
            handler.send(msg);//Sends the message msg to the client using the send method of the ConnectionHandler<T> instance.
            return true;
        }
        return false; 
        }
    @Override
    public void disconnect ( int connectionId){
        if(clientToHandler.contains(connectionId)){
            try{
                clientToHandler.get(connectionId).close();
            }
            catch(IOException e){}
            clientToHandler.remove(connectionId);
        }
    }

}
