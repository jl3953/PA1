import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientObject{

    private String username;
    private String password;
    private long blocked;
    private int retries;
    private boolean online;
    private long lastbeat;
    private String IP;
    private int port;
    private LinkedList<MailNode> mailbox;
    private Set<ClientObject> blockList;

    public static final int RETRIES = 3;

    public ClientObject(String username, String password, long time){
        this.username = username;
        this.password = password;
        this.blocked = time;
        this.retries = RETRIES;
        this.online = false;
        this.lastbeat = time;
        this.IP = "";
        this.port = 0;
        this.mailbox = new LinkedList<MailNode>();
        this.blockList = Collections.newSetFromMap(new ConcurrentHashMap<ClientObject,Boolean>());
    }

    public void blockUser(ClientObject co){
        this.blockList.add(co);
    }

    public boolean isBlocked(ClientObject co){
        return this.blockList.contains(co);
    }

    public void unblockUser(ClientObject co){
        this.blockList.remove(co);
    }

    public String username(){
        return this.username;
    }

    public String password(){
        return this.password;
    }

    public long blocked(){
        return this.blocked;
    }

    public void setBlocked(long time){
        this.blocked = time;
    }

    public int retries(){
        return this.retries;
    }

    public void setRetries(int r){
        this.retries = r;
    }

    public boolean online(){
        return this.online;
    }

    public void setOnline(boolean o){
        this.online = o;
    }

    public long lastbeat(){
        return this.lastbeat;
    }

    public void setLastbeat(long beat){
        this.lastbeat = beat;
    }

    public String IP(){
        return this.IP;
    }

    public void setIP(String newIP){
        this.IP = newIP;
    }

    public int port(){
        return this.port;
    }

    public void setPort(int newPort){
        this.port = newPort;
    }

    public void putInMailbox(String sender, String message){
        this.mailbox.addLast(new MailNode(sender, message));
    }

    public MailNode getMail(){
        return this.mailbox.pop();
    }

    public boolean hasMail(){
        return !this.mailbox.isEmpty();
    }
}
