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
            System.out.println("while loop start");
            //listen for server to send it messages

            ServerSocket listen = new ServerSocket(this.port);
            while (true){
                System.out.println("waiting for new connection");
                //accept a connection from the server
                Socket connectionSocket = listen.accept();
                //open a buffer for reading input from server
                BufferedReader inFromServer = new BufferedReader(
                        new InputStreamReader(connectionSocket.getInputStream()));

                //read input from server and print it out to screen
                String line = "";
                while ((line = inFromServer.readLine()) != null){
                    System.out.println(line);
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}



