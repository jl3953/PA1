import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Used by server as another thread to periodically check for heartbeats.
 */
public class HeartbeatChecker implements Runnable{

    private ConcurrentHashMap<String, ClientObject> mymap; //map of all client states from server
    public static final int INTERVAL = (int) (HeartBeat.INTERVAL * 1.25); //check interval--slightly over
                                                                        //30 seconds to avoid race condition

    public HeartbeatChecker(ConcurrentHashMap<String, ClientObject> mymap){
        this.mymap = mymap;
    }

    public void run(){

        //every so often, iterate through the ConcurrentHashMap and check if 
        //timestamp of last heartbeat is updated. if not, set offline.
        try{
            while (true){

                for (Map.Entry<String,ClientObject> entry : mymap.entrySet()){

                    Calendar cal = Calendar.getInstance();
                    ClientObject co = entry.getValue();
                    //timed out
                    if (co.lastbeat() < cal.getTimeInMillis()){
                        co.setOnline(false);
                    }
                }

                //sleep for specified interval
                Thread.sleep(INTERVAL * 1000);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

