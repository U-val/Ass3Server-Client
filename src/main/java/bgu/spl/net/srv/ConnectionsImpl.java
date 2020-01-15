package bgu.spl.net.srv;

import bgu.spl.net.impl.stomp.DataBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionsImpl<T> implements Connections<T> {

    private Map<Integer, ConnectionHandler<T>> connectionMap = new ConcurrentHashMap<>() ;
    private DataBase dataBase = new DataBase();
    private ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();

    private static class holder<String> {
        private static ConnectionsImpl instance = new ConnectionsImpl<>();
    }
    /**
     * Retrieves the single instance of this class.
     */
    public static ConnectionsImpl getInstance() {
        return holder.instance;
    }
    //for server ONLYYY
    public boolean addHandler(int CHID, ConnectionHandler<T> handler) {
        return connectionMap.putIfAbsent(CHID,handler)!=null ;
    }

    public boolean send(int connectionId, T msg) {
        connectionLock.writeLock().lock();
        try {
            if (!this.connectionMap.containsKey(connectionId))
                return false;
            ConnectionHandler<T> ch = this.connectionMap.get(connectionId);
                ch.send(msg); // send message via connectionHandler
                return true; // need to change?
        }
        finally {
            connectionLock.writeLock().unlock();
        }
    }

    public void send(String channel, T msg) {
        Collection<String> genreClients= this.dataBase.getGenreList(channel).keySet();
        for (String name: genreClients){
            int id = this.dataBase.getID(name);
//            this.dataBase.getLock(name).writeLock().lock();
//            try {
                //this.connectionMap.get(id).send(msg);
                send(id,msg);
//            }
//            finally {
//                this.dataBase.getLock(name).writeLock().unlock();
//            }
        }
    }

    public void disconnect(int connectionId) {

        this.dataBase.removeClient(connectionId); // remove client from database (not entirely)

        this.connectionMap.remove(connectionId);
    }

//    public void connect(int connectionId, String clientName,ConnectionHandler<T> ch){
//        this.connectionMap.put(connectionId, ch); // add ConnectionHandler connectionMap
//        this.dataBase.connectUserToDataBase(connectionId,clientName);
//    }

    public DataBase getDataBase(){
        return this.dataBase;
    }

}
