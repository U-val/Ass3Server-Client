package bgu.spl.net.impl.stomp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataBase {
    // data base fields
    private Map<String,ClientInfo> users = new ConcurrentHashMap<>();
    private Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();
    private Map<String, Map<String,Integer>> GenreToUsers = new ConcurrentHashMap<>();
    private Map<Integer,String> activeUsersToCHID = new ConcurrentHashMap<>();

    /**
     * add user to 'users'
     * @param name      login name
     * @param PW        correspond passWord
     * @param ID
     * @return true if succeed and false o.w.
     */
    public Boolean addUser(String name, String PW, int ID){
        if(users.get(name)!=null) return false;
        users.put(name, new ClientInfo(name, PW));
        locks.put(name, new ReentrantReadWriteLock());

        return true;
    }

    /**
     * isLoggedIn
     * @param CHID    connection handler id
     * @return if connection handler is connected
     */
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

    /**
     * returns "" if the details are valid, and if are- log in to D-B
     * @param CHID      connection handler id
     * @param name      login name
     * @param passCode  correspond passWord
     * @return "" if the details are valid, and if are- log in to D-B
     *          <msg></msg> if there is any problem
     */

    public String logIn(int CHID, String name, String passCode) {
        String ans="";
        if(users.get(name)==null) addUser(name,passCode,CHID);
        locks.get(name).writeLock().lock();
        try {
            if(activeUsersToCHID.get(CHID)!=null) {
                if( activeUsersToCHID.get(CHID).equals(name)) ans= "already logged in";
                else ans="wrong user name";}
            else if(users.get(name).getConnected()) ans= "already logged in";
            else if(users.get(name).getPassWord().equals(passCode)){
                users.get(name).connect();
                activeUsersToCHID.put(CHID,name);
            } else ans= "wrong password";
        }finally {
            locks.get(name).writeLock().unlock();
        }
        return ans;
    }

    /**
     * change the client 'connected' flag to false
     * @param name      login name
     */
    public void logOut(String name){
        locks.get(name).writeLock().lock();
        try{
            users.get(name).disconnect();
        } finally {
            locks.get(name).writeLock().unlock();
        }
    }

    /**
     * removes client from 'active users map' and all his genre records
     * @param connectionId
     */
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
    }

    // to remove ???????????????????????????????????????
    public Map<Integer,String> getIdMap(){
        return this.activeUsersToCHID;
    }
    // return the name as represented at the 'activeUsersToCHID' map from given id
    public String getName(int id){
        return this.activeUsersToCHID.get(id);
    }

    // return the id as represented at the 'activeUsersToCHID' map from given name
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
    // return the users how subscribed to the given genre
    public Map<String,Integer> getGenreList(String genre){
        return this.GenreToUsers.get(genre);
    }
    // to remove ???????????????????????????????????????
    public ReentrantReadWriteLock getLock(String name){
        return this.locks.get(name);
    }

    /**
     * add a user
     * @param CHID
     * @param genre
     * @param id
     */
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
