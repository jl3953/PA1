import java.io.*;
import java.net.*;
import java.util.*;

public class ClientObject{

    private String username;
    private String password;
    private long blocked;
    private int retries;
    private boolean online;
    private long lastbeat;

    public static final int RETRIES = 3;

    public ClientObject(String username, String password, long time){
        this.username = username;
        this.password = password;
        this.blocked = time;
        this.retries = RETRIES;
        this.online = false;
        this.lastbeat = time;
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
}
