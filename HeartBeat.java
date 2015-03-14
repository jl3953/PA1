import java.util.*;
import java.net.*;
import java.io.*;

public class HeartBeat implements Runnable{

    private int serverport;
    private String servermachine;
    private String name;
    private int listeningport;
    public static final int INTERVAL = 5; //in seconds

    public HeartBeat(int serverport, String servermachine, String name, int listeningport){
        this.serverport = serverport;
        this.servermachine = servermachine;
        this.name = name;
        this.listeningport = listeningport;
    }

    public void run(){

        try{
            while(true){
                //connect to server
                Socket connection = new Socket(this.servermachine, this.serverport);

                //send an initial verification of online-ness to server
                DataOutputStream outToServer = new DataOutputStream(
                        connection.getOutputStream());
                String initialOK = this.name + " ";
                outToServer.writeBytes(initialOK + "\n");

                //send heartbeat message
                String heartbeat = "sender:" + this.name +
                    " action:send" +
                    " field3:HEARTBEAT" +
                    " field4:" + InetAddress.getLocalHost().getHostAddress() + "/" + this.listeningport;
                outToServer.writeBytes(heartbeat + "\n");
                
                //close connection
                connection.close();

                //sleep for 30 seconds
                Thread.sleep(INTERVAL * 1000);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
