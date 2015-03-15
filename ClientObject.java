import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Class to represent the state of a client when Server.java is trying to keep
 * track of states of multiple clients--methods are very short and self explanatory.
 */
public class ClientObject{

    private String username;
    private String password;
    private long blocked;//whether or not client is blocked for authentication
    private int retries; //how many times client can try again to authenticate
    private boolean online; //whether or not client is online
    private long lastbeat; //last timestamp of heartbeat
    private String IP; //ipaddress of its listening thread
    private int port; //port of client's listening thread
    private LinkedList<MailNode> mailbox; //linked list of all unread message
    private Set<ClientObject> blockList; //set of all other clients that it has blocked
    private boolean firstTime; //whether or not it's the client's first time logging in

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
        this.firstTime = false;
    }

    public boolean firstTime(){
        return this.firstTime;
    }

    public void setFirstTime(boolean bool){
        this.firstTime = bool;
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
