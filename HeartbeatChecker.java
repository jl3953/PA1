import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

public class HeartbeatChecker implements Runnable{

    private ConcurrentHashMap<String, ClientObject> mymap;
    public static final int INTERVAL = HeartBeat.INTERVAL * 2;

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

