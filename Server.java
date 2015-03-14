import java.io.*;
import java.util.*;

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
     * @param authentication maps name to password
     * @param blocked the time that a user is unblocked
     * @param retries how many retries a user has left
     */
    public static void initMaps(String filename,
            HashMap<String,String> authentication,
            HashMap<String,Long> blocked,
            HashMap<String, Integer> retries,
            HashMap<String, Beat> online){
        try{
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String username;
            String password;
            Calendar cal = Calendar.getInstance();
            while ((username = br.readLine()) != null){
                password = br.readLine();
                authentication.put(username, password);
                retries.put(username, RETRIES);
                blocked.put(username, cal.getTimeInMillis());
                online.put(username, false);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Authenticates a user.
     * @param connectionSocket the socket to which server is connected to client
     * @param authentication hashmap storing username to password
     * @param blocked hashmap indicating whether a username is blocked
     * @param retries hashmap telling us how many retries the client has left
     * @param online hashmap telling us which users are online
     */                             
    public static boolean authenticate(Socket connectionSocket, 
            HashMap<String,String> authentication,
            HashMap<String,Long> blocked, 
            HashMap<String, Integer> retries,
            HashMap<String, Boolean> online) throws Exception{
        /** Input from client */
        BufferedReader inFromClient = new BufferedReader(
                new InputStreamReader(connectionSocket.getInputStream()));
        /** Output to client */
        DataOutputStream outToClient = new DataOutputStream(
                connectionSocket.getOutputStream());
        //read in client first
        String reply = inFromClient.readLine();
        String[] temp = reply.split(" ");
        String username = temp[0].trim();

        //check if client is already logged in
        if (online.get(username)){
            return true;
        }
        String password = temp[1];

        //check if user is blocked
        Calendar cal = Calendar.getInstance();
        if (blocked.get(username) > cal.getTimeInMillis()){
            outToClient.writeBytes("BLOCKED\n");
            retries.put(username, RETRIES);
            return false;
        }
        //first two tries
        while (retries.get(username) > 0){
            //If pass word and username match, return true and mark user online
            if (authentication.get(username).equals(password)){
                outToClient.writeBytes("OK\n");
                retries.put(username, RETRIES);
                online.put(username, true);
                return true;
            } else if (retries.get(username) == 1){
                retries.put(username, retries.get(username)-1);
            } else {
                outToClient.writeBytes("TRY_AGAIN\n");
                retries.put(username,retries.get(username)-1);
                temp = inFromClient.readLine().split(" ");
                username = temp[0];
                password = temp[1];
            }
        }
        //third try
        outToClient.writeBytes("THIRD_TIME\n");
        blocked.put(username, cal.getTimeInMillis() + BLOCK_TIME * 1000);
        retries.put(username, RETRIES);
        return false;

    }


    public static void main(String[] args) throws Exception{


        ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(args[0]));
        HashMap<String, String> authentication = new HashMap<String,String>();
        HashMap<String, Long> blocked = new HashMap<String, Long>();
        HashMap<String, Integer> retries = new HashMap<String, Integer>();
        HashMap<String, Boolean> online = new HashMap<String, Boolean>();
        initMaps("credentials.txt", authentication, blocked, retries, online);

        //Spawn another thread to take care of checking whether clients are still alive
        Runnable heartbeatChecker = new HeartBeatChecker(online);
        Thread


        while(true){
            Socket connectionSocket = welcomeSocket.accept();
            if(authenticate(connectionSocket, authentication, blocked, retries,online)){
                System.out.println("valid connection.");
                Runnable handler = new ConnectionHandler(connectionSocket);
                new Thread((Runnable) handler).start();
            }

        }
    }

}
