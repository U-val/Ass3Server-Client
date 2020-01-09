package bgu.spl.net.srv;

import bgu.spl.net.impl.stomp.DataBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionsImpl<T> implements Connections<T> {

    private Map<Integer, ConnectionHandler<T>> connectionMap = new HashMap<>() ;
    private DataBase dataBase = new DataBase();
    private ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();


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
        List<String> genreClients= this.dataBase.getGenreList(channel);
        for (String name: genreClients){
            this.dataBase.getLock(name).writeLock().lock();
            try {
                this.connectionMap.get(this.dataBase.getID(name)).send(msg);
            }
            finally {
                this.dataBase.getLock(name).writeLock().unlock();
            }
        }
    }

    public void disconnect(int connectionId) {
        this.dataBase.removeClient(connectionId); // remove client from database (not entirely)

        this.connectionMap.remove(connectionId);
    }

    public void connect(int connectionId, String clientName,ConnectionHandler<T> ch){
        this.connectionMap.put(connectionId, ch); // add ConnectionHandler connectionMap
        this.dataBase.connectUserToDataBase(connectionId,clientName);
    }

    public DataBase getDataBase(){
        return this.dataBase;
    }

}
