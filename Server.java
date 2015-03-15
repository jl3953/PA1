import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import java.net.*;
import java.lang.*;

/**
 * Message server.
 *   
 * @author Jennifer Lam jl3953
 */
public class Server{

    public final static int RETRIES = 3;
    public final static int BLOCK_TIME = 60;//in seconds


    /**
     * Initializes all the data structures to keep track of states.
     * @param filename filename of credentials
     * @param mymap map of all client's current states
     */
    public static void initMaps(String filename, ConcurrentHashMap<String, ClientObject> mymap){
        try{
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            String username;
            String password;
            Calendar cal = Calendar.getInstance();
            while((line = br.readLine()) != null){
                username = line.split(" ")[0];
                password = line.split(" ")[1];
                ClientObject co = new ClientObject(username, password, cal.getTimeInMillis());
                mymap.put(username, co);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Authenticates a user.
     * @param connectionSocket the socket to which server is connected to client
     * @param mymap map of all client states
     */                             
    public static boolean authenticate(Socket connectionSocket,
            ConcurrentHashMap<String, ClientObject> mymap) throws Exception{
        //Input from client
        BufferedReader inFromClient = new BufferedReader(
                new InputStreamReader(connectionSocket.getInputStream()));
        //Output to client
        DataOutputStream outToClient = new DataOutputStream(
                connectionSocket.getOutputStream());
        //read in client
        String reply = inFromClient.readLine();
        String[] temp = reply.split("\\s+");
        String username = temp[0].trim();

        //check if client is already logged in
        ClientObject co = mymap.get(username);
        if (temp.length == 1){
            return true;
        }
        if (temp.length == 2 && co.port() != 0){
            //check to see if person is already online.
            Socket conn = new Socket(co.IP(), co.port());
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes("EXIT_NOW" + "\n");
            conn.close();
        }
        String password = temp[1].trim();

        //Check if user is blocked
        Calendar cal = Calendar.getInstance();
        if(co.blocked() > cal.getTimeInMillis()){
            outToClient.writeBytes("BLOCKED\n");
            co.setRetries(co.retries()-1);
            return false;
        }

        //first two tries
        while (co.retries() > 0){
            //If password and username match, return true and mark user online
            if(co.password().equals(password)){
                outToClient.writeBytes("OK\n");
                co.setRetries(RETRIES);
                co.setOnline(true);
                co.setFirstTime(true);
                return true;
            } else if (co.retries() == 1){
                co.setRetries(0);
            } else {
                outToClient.writeBytes("TRY_AGAIN\n");
                co.setRetries(co.retries()-1);
                temp = inFromClient.readLine().split(" ");
                username = temp[0];
                password = temp[1];
            }
        }
        //third try
        outToClient.writeBytes("THIRD_TIME\n");
        co.setBlocked(cal.getTimeInMillis() + BLOCK_TIME * 10000);
        co.setRetries(RETRIES);
        return false;
    }

    public static void main(String[] args) throws Exception{


        ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(args[0]));
        ConcurrentHashMap<String, ClientObject> mymap = new ConcurrentHashMap<String, ClientObject>();
        initMaps("credentials.txt", mymap);

        //Spawn another thread to take care of checking whether clients are still alive
        Runnable heartbeatChecker = new HeartbeatChecker(mymap);
        Thread t = new Thread(heartbeatChecker);
        t.start();


        //take care of all incoming connections
        while(true){
            Socket connectionSocket = welcomeSocket.accept();
            if(authenticate(connectionSocket, mymap)){
                Runnable handler = new ConnectionHandler(connectionSocket, mymap);
                new Thread((Runnable) handler).start();
            }

        }
    }

}
