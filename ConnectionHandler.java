import java.io.*;
import java.net.*;

/**
 * Called on by server to handle a new connection from accept().
 *
 * @author Jennifer Lam jl3953
 */
public class ConnectionHandler implements Runnable{

    private Socket connectionSocket;

    public ConnectionHandler(Socket connectionSocket){
        this.connectionSocket = connectionSocket;
    }

    public void run(){

        String clientSentence;
        String capitalizedSentence;

        try{
            //input stream from connecting client
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));

            clientSentence = inFromClient.readLine();

            //output stream to client server
            String[] temp = clientSentence.split(" ");
            String addressport = temp[3].split(":")[1];
            String machine = addressport.split("/")[0];
            int port = Integer.parseInt(addressport.split("/")[1]);

            Socket out = new Socket(machine, port);
            DataOutputStream outToClient = new DataOutputStream(
                    out.getOutputStream());

            if(clientSentence != null){
                capitalizedSentence = clientSentence.toUpperCase() + "\n";
                outToClient.writeBytes(capitalizedSentence);
            }
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}




