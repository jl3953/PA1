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

    /**public HashMap<String, String> authentication;
      public HashMap<String, Integer> retries;
      public HashMap<String, Boolean> blocked;
      public ServerSocket welcomeSocket;
      */
    public final static int RETRIES = 3;
    public final static int BLOCK_TIME = 60;//in seconds


    /**public Server(){
      this.authentication = new HashMap<String,String>();
      this.blocked = new HashMap<String, Boolean>();
      this.retries = new HashMap<String, Integer>();
      initMaps("credentials.txt");
      }*/
    public static void initMaps(String filename,
            HashMap<String,String> authentication,
            HashMap<String,Long> blocked,
            HashMap<String, Integer> retries){
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
     */                             
    public static boolean authenticate(Socket connectionSocket, HashMap<String,String> authentication,
            HashMap<String,Long> blocked, HashMap<String, Integer> retries) throws Exception{
        /** Input from client */
        BufferedReader inFromClient = new BufferedReader(
                new InputStreamReader(connectionSocket.getInputStream()));
        /** Output to client */
        DataOutputStream outToClient = new DataOutputStream(
                connectionSocket.getOutputStream());
        //read in client first
        String reply = inFromClient.readLine();
        if (reply.equals("ONLINE")){
            return true;
        }
        String[] temp = reply.split(" ");
        String username = temp[0];
        String password = temp[1];
        //retries.put(username, retries.get(username)-1);

        //check if user is blocked
        Calendar cal = Calendar.getInstance();
        if (blocked.get(username) > cal.getTimeInMillis()){
            System.out.println("block: "+blocked.get(username)
                    + " current: "+cal.getTimeInMillis());
            outToClient.writeBytes("BLOCKED\n");
            retries.put(username, RETRIES);
            System.out.println("retries: "+retries.get(username));
            return false;
        }
        //first two tries
        while (retries.get(username) > 0){
            System.out.println("enter the retries while");
            System.out.println("password: " + password);
            if (authentication.get(username).equals(password)){
                outToClient.writeBytes("OK\n");
                retries.put(username, RETRIES);
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
        System.out.println("retries: " + retries.get(username));
        return false;

    }


    public static void main(String[] args) throws Exception{


        ServerSocket welcomeSocket = new ServerSocket(6789);
        HashMap<String, String> authentication = new HashMap<String,String>();
        HashMap<String, Long> blocked = new HashMap<String, Long>();
        HashMap<String, Integer> retries = new HashMap<String, Integer>();
        initMaps("credentials.txt", authentication, blocked, retries);

        while(true){

            System.out.println("begin while loop");

            Socket connectionSocket = welcomeSocket.accept();
            if(authenticate(connectionSocket, authentication, blocked, retries)){
                System.out.println("accepted another connection");
                Runnable handler = new ConnectionHandler(connectionSocket);
                new Thread((Runnable) handler).start();
            }
            System.out.println("end while loop");

        }
    }

}
