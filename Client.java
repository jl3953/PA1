import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Message client.
 *
 * @author Jennifer Lam jl3953
 */
public class Client{

    public BufferedReader inFromUser;
    public Socket clientSocket;
    public DataOutputStream outToServer;
    public BufferedReader inFromServer;
    public boolean online;

    public Client() throws Exception{
        //openConnections();
    }

    public void openConnections() throws Exception{
        this.clientSocket = new Socket("localhost", 6789);
        this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
        this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
        this.inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
        this.online = false;
    }

    public void closeConnections() throws Exception{
        this.clientSocket.close();
    }

    public void authenticate() throws Exception{
        openConnections();
        //prompt user
        System.out.print("Username: ");
        System.out.flush();
        String username = this.inFromUser.readLine();
        String password, reply;

        while(true){
            System.out.print("Password: ");
            System.out.flush();
            password = this.inFromUser.readLine();
            this.sendToServer(username + " " + password);

            reply = this.readFromServer();

            System.out.println(reply);

            if (reply.equals("OK")){
                System.out.println("Welcome to simple chat server!");
                this.online = true;
                closeConnections();
                return;
            } else if (reply.equals("THIRD_TIME")){
                System.out.println("Invalid password. Your account has been blocked." +
                        " Please try again after sometime.");
                System.exit(0);
            } else if (reply.equals("BLOCKED")){
                System.out.println("Due to multiple login failures, your account " +
                        "has been blocked. Please try again after sometime.");
                System.exit(0);
            } else{
                System.out.println("Invalid password. Please try again");
            }
        } 
    }

    public void sendToServer(String string) throws Exception{
        this.outToServer.writeBytes(string + "\n");
    }

    public String readFromServer() throws Exception{
        return this.inFromServer.readLine();
    }



    public static void main(String[] args) throws Exception{

        Client client = new Client();
        client.authenticate();

        String command = client.inFromUser.readLine();
        





        /**String sentence;
          String modifiedSentence;

          BufferedReader inFromUser = new BufferedReader(
          new InputStreamReader(System.in));

          Socket clientSocket = new Socket("localhost", 6789);

          DataOutputStream outToServer = new DataOutputStream(
          clientSocket.getOutputStream());

          BufferedReader inFromServer = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream()));

          sentence = inFromUser.readLine();

          outToServer.writeBytes(sentence + "\n");

          modifiedSentence = inFromServer.readLine();

          System.out.println("FROM SERVER: " + modifiedSentence);

          clientSocket.close();
          */
    }
}

