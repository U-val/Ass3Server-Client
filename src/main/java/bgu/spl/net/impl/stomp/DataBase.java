package bgu.spl.net.impl.stomp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataBase {

    private Map<String,ClientInfo> users = new ConcurrentHashMap<>();
    private Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();
    private Map<String, Map<String,Integer>> GenreToUsers = new ConcurrentHashMap<>();
    private Map<Integer,String> activeUsersToCHID = new ConcurrentHashMap<>();

    public Boolean addUser(String name, String PW, int ID){
        if(users.get(name)!=null) return false;
        users.put(name, new ClientInfo(name, PW));
        locks.put(name, new ReentrantReadWriteLock());

        return true;
    }

    public boolean isLoggedIn(int CHID) {
        String name = getName(CHID);
        if(name==null) return false;
        boolean ans;
        locks.get(name).readLock().lock();
        try{
            ans = users.get(name).getConnected();
        }finally {
            locks.get(name).readLock().unlock();
        }
        return ans;
    }
    // returns if the details are valid, and if are- log in to D-B
    public String logIn(int CHID, String name, String passCode) {
        String ans="";
        if(users.get(name)==null) addUser(name,passCode,CHID);
        locks.get(name).writeLock().lock();

        try {
            if(activeUsersToCHID.get(CHID)!=null) {
                if(activeUsersToCHID.get(CHID).equals(name)) ans="already logged in"; else ans="wrong user name";}
            else if(users.get(name).getPassWord().equals(passCode)){
                users.get(name).connect();
                activeUsersToCHID.put(CHID,name);
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
    public void removeClient(int connectionId){
        String name = getName(connectionId);
        logOut(name); // logout

        ReentrantReadWriteLock lock = locks.get(name); //acquire user's lock

        lock.writeLock().lock();
        try{
            this.activeUsersToCHID.remove(connectionId); // delete user's connectionId

            for (String gen:GenreToUsers.keySet()){
                this.GenreToUsers.get(gen).remove(name); // remove user from all his genres
            }
        }
        finally {
            lock.writeLock().unlock();
        }
//        synchronized (this.locks){
//            this.locks.remove(name); // remove user's lock
//        }
    }
    public Map<Integer,String> getIdMap(){
        return this.activeUsersToCHID;
    }

    public String getName(int id){
        return this.activeUsersToCHID.get(id);
    }

    public int getID(String name){
        for (int id:activeUsersToCHID.keySet()) {
            locks.get(name).readLock().lock();
            try {
                if (activeUsersToCHID.get(id).equals(name))
                    return id;
            }
            finally {
                locks.get(name).readLock().unlock();
            }
        }
        return -1; // if not found
    }

    public Map<String,Integer> getGenreList(String genre){
        return this.GenreToUsers.get(genre);
    }

    public ReentrantReadWriteLock getLock(String name){
        return this.locks.get(name);
    }
    public void subscribe (int CHID, String genre, int id){
        this.GenreToUsers.computeIfAbsent(genre, k -> new ConcurrentHashMap<>()); // create new genre id needed
        this.GenreToUsers.get(genre).put(getName(CHID),id); // add user to this genre
    }
    public void unsubscribe (int CHID, int id) {
        String name = getName(CHID);
        if(name == null) return;
        locks.get(name).writeLock().lock();
        try{
            this.GenreToUsers.forEach((gen,map)->{
                if(map.get(id)!=null && map.get(id).equals(name))
                    map.remove(id);
            });
        }finally {
            locks.get(name).writeLock().unlock();
        }

    }

    public int getSubId(String des, int connectionID) {
        String name = getName(connectionID);
        if(name==null) return -1;
        int ans;
        locks.get(name).readLock().lock();
        try{
            ans=GenreToUsers.get(des).get(name);
        }finally {
            locks.get(name).readLock().unlock();
        }
        return ans;
    }


    // TODO subscribe / join
    // TODO unsubscribe
}
