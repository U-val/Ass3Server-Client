package bgu.spl.net.impl.stomp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataBase {

    private Map<String,ClientInfo> users = new ConcurrentHashMap<>();
    private Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();
    private Map<String, List<String>> GenreToUsers = new ConcurrentHashMap<>();
    private Map<String,Integer> activeUsersToCHID = new ConcurrentHashMap<>();

    public Boolean addUser(String name, String PW, int ID){
        if(users.get(name)!=null) return false;
        users.put(name, new ClientInfo(name, PW));
        locks.put(name, new ReentrantReadWriteLock());
        activeUsersToCHID.put(name,ID);
        return true;
    }

    public void TransferBook(String from, String to,String genre, String book){
        ReentrantReadWriteLock first, second;
        if(from.compareTo(to)>0) {first = locks.get(from); second = locks.get(to);}
        else {first = locks.get(to); second =  locks.get(from);}
        first.writeLock().lock();
        try{
            second.writeLock().lock();
            try {
                if(users.get(from).takeBook(genre,book))
                    users.get(to).addBook(genre,book);
            }finally {
                second.writeLock().unlock();
            }
        }finally {
            first.writeLock().unlock();
        }
    }
    public void subscribe(String name, String genre){

    }
    public void addBook (String name,String genre, String book){
        locks.get(name).writeLock().lock();
        try{
            users.get(name).addBook(genre,book);
        }finally {
            locks.get(name).writeLock().unlock();
        }
    }

    public List<String> showBooks(String genre, String name){
        List<String> res;
        locks.get(name).readLock().lock();
        try{
            res = users.get(name).getBooks(genre);
        }finally {
            locks.get(name).readLock().unlock();
        }
        if(res==null) return new LinkedList<>();
        return res;
    }
    // returns if the details are valid, and if are- log in to D-B
    public String logIn(int CHID, String name, String passCode) {
        String ans="";
        if(users.get(name)==null) addUser(name,passCode,CHID);
        locks.get(name).writeLock().lock();
        try {
            if(activeUsersToCHID.get(name)!=null) ans="already logged in";
            else if(users.get(name).getPassWord().equals(passCode)){
                users.get(name).connect();
                activeUsersToCHID.put(name,CHID);
            } else ans= "wrong password";
        }finally {
            locks.get(name).writeLock().unlock();
        }
        return ans;
    }

    public void logOut(String name){
        locks.get(name).writeLock().lock();
        try{
            users.get(name).disconnect();
        } finally {
            locks.get(name).writeLock().unlock();
        }
    }


    // TODO subscribe / join
    // TODO unsubscribe
}
