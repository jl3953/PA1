import java.io.*;
import java.net.*;

public class ConnectionHandler implements Runnable{

    private Socket connectionSocket;

    public ConnectionHandler(Socket connectionSocket){
        this.connectionSocket = connectionSocket;
    }

    public void run(){

        String clientSentence;
        String capitalizedSentence;

        try{
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));

            DataOutputStream outToClient = new DataOutputStream(
                    connectionSocket.getOutputStream());

            clientSentence = inFromClient.readLine();

            capitalizedSentence = clientSentence.toUpperCase() + "\n";

            outToClient.writeBytes(capitalizedSentence);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}




