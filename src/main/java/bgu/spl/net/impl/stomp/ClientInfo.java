package bgu.spl.net.impl.stomp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInfo {
    private String name;
    private String passWard;
    private int ID;
    private boolean connected;
    private List<String> Topics;
    private Map<String,List<String>> Inventory;

    public ClientInfo(String name, String PW, int id){
        this.name=name;
        this.passWard=PW;
        this.ID= id;
        this.connected = false;
        this.Topics = new LinkedList<>();
        this.Inventory = new ConcurrentHashMap<>();
    }

    public boolean getConnected() {
        return connected;
    }
    public void connect() {
        this.connected = true;
    }
    public void disconnect(){
        this.connected = false;
    }

    public int getID() {
        return ID;
    }
    public String getName() {
        return name;
    }
    public String getPassWard() {
        return passWard;
    }

    public List<String> getBooks(String genre) {
        return Inventory.get(genre);
    }

    public List<String> getTopics() {
        return Topics;
    }

    public boolean addBook(String genre, String book){
        if(Inventory.get(genre).contains(book)) return false;
        Inventory.get(genre).add(book);
        return true;
    }

    public boolean addTopic(String topic){
        if(Topics.contains(topic)) return false;
        Topics.add(topic);
        return true;
    }

    public boolean takeBook(String genre, String book) {
        return Inventory.get(genre).remove(book);
    }
}
