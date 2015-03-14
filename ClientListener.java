import java.util.*;
import java.io.*;
import java.net.*;

public class ClientListener implements Runnable{

    private int port;//port on which client is listening on

    public ClientListener(int port){
        this.port = port;
    }


    public void run(){
        try{

            ServerSocket listen = new ServerSocket(this.port);
            while (true){
                //accept a connection from the server
                Socket connectionSocket = listen.accept();
                //open a buffer for reading input from server
                BufferedReader inFromServer = new BufferedReader(
                        new InputStreamReader(connectionSocket.getInputStream()));

                //read input from server and print it out to screen
                String line = "";
                while ((line = inFromServer.readLine()) != null){
                    if (line.equals("EXIT_NOW")){
                        System.out.println("You have been logged out. Your username is" +
                            " logged in elsewhere.");
                        System.exit(0);
                    }
                    System.out.println(line);
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}



