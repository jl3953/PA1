import java.io.*;
import java.net.*;

public class Server{

    public static void main(String[] args) throws Exception{

        String clientSentence;
        String capitalizedSentence;

        ServerSocket welcomeSocket = new ServerSocket(6789);

        while(true){
            
            Socket connectionSocket = welcomeSocket.accept();

            System.out.println("accepted another connection");

            Runnable handler = new ConnectionHandler(connectionSocket);
            new Thread((Runnable) handler).start();

        }
    }

}
