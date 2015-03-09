import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Message client.
 *
 * @author Jennifer Lam jl3953
 */
public class Client{

    public static void authenticate(){
        Scanner sc = new Scanner(System.in);
        System.out.print("Username: ");
        System.out.flush();
        String username = sc.nextLine();
        System.out.print("Password: ");
        System.out.flush();
        String password = sc.nextLine();
        sc.close();

        System.out.println("" + username + password);
    }
        


    public static void main(String[] args) throws Exception{

        authenticate();


        String sentence;
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
    }
}

