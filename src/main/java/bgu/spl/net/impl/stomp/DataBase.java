package bgu.spl.net.impl.stomp;

import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataBase {

    private Map<String,ClientInfo> users = new ConcurrentHashMap<>(); // Map<name,user>
    private Map<Integer,String> idMap = new ConcurrentHashMap<>();// Map<ID,name>
    private Map<String,List<String>> genre = new ConcurrentHashMap<>(); //Map<genre,List<user_name>>
    private final Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>(); // Map<name,Lock>

    public Boolean addUser(String name, String PW, int ID){
        if(users.get(name)!=null) return false;
        users.put(name, new ClientInfo(name, PW, ID));
        locks.put(name, new ReentrantReadWriteLock());
        idMap.put(ID,name);
        return true;
    }

    public Map<Integer,String> getIdMap(){
        return this.idMap;
    }

    public String getName(int id){
        return this.idMap.get(id);
    }

    public int getID(String name){
        for (int id:idMap.keySet()) {
            locks.get(name).readLock().lock();
            try {
                if (idMap.get(id).equals(name))
                    return id;
            }
            finally {
                locks.get(name).readLock().lock();
            }
        }
        return -1; // if not found
    }

    public List<String> getGenreList(String genre){
        return this.genre.get(genre);
    }

    public ReentrantReadWriteLock getLock(String name){
        return this.locks.get(name);
    }

    public void removeClient(int connectionId){
        String name = getName(connectionId);
        logOut(name); // logout
        ReentrantReadWriteLock lock = locks.get(name); //acquire user's lock
        lock.writeLock().lock();
        try{
            this.idMap.remove(connectionId); // delete user's connectionId

            for (String gen:genre.keySet()){
                this.genre.get(gen).remove(name); // remove user from all his genres
            }
        }
        finally {
            lock.writeLock().unlock();
        }
        synchronized (this.locks){
            this.locks.remove(name); // remove user's lock
        }
    }

    public void connectUserToDataBase(int connectionId, String clientName){
        logIn(clientName); // login
        this.idMap.put(connectionId,clientName); // save connection id
    }


    private void logIn(String name) {
        locks.get(name).writeLock().lock();
        try {
            users.get(name).connect();
        }finally {
            locks.get(name).writeLock().unlock();
        }
    }

    private void logOut(String name){
        locks.get(name).writeLock().lock();
        try{
            users.get(name).disconnect();
        } finally {
            locks.get(name).writeLock().unlock();
        }
    }

    public void registerGenere (String name, String genre){
        this.genre.computeIfAbsent(genre, k -> new LinkedList<>()); // create new genre id needed
        this.genre.get(genre).add(name); // add user to this genre
    }

    // TODO subscribe / join
    // TODO unsubscribe

    //    public void TransferBook(String from, String to,String genre, String book){
//        ReentrantReadWriteLock first, second;
//        if(from.compareTo(to)>0) {first = locks.get(from); second = locks.get(to);}
//        else {first = locks.get(to); second =  locks.get(from);}
//        first.writeLock().lock();
//        try{
//            second.writeLock().lock();
//            try {
//                if(users.get(from).takeBook(genre,book))
//                    users.get(to).addBook(genre,book);
//            }finally {
//                second.writeLock().unlock();
//            }
//        }finally {
//            first.writeLock().unlock();
//        }
//    }
//
//    public void addBook (String name,String genre, String book){
//        locks.get(name).writeLock().lock();
//        try{
//            users.get(name).addBook(genre,book);
//        }finally {
//            locks.get(name).writeLock().unlock();
//        }
//    }
//
//    public List<String> showBooks(String genre, String name){
//        List<String> res = null;
//        locks.get(name).readLock().lock();
//        try{
//            res = users.get(name).getBooks(genre);
//        }finally {
//            locks.get(name).readLock().unlock();
//        }
//        if(res==null) return new LinkedList<>();
//        return res;
//    }
}
