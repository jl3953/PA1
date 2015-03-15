import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Used by ClientObject to represent an unread message to a client.
 */
public class MailNode {

    private String sender; //send of message
    private String message; //message itself

    public MailNode(String sender, String message){
        this.sender = sender;
        this.message = message;
    }

    public String sender(){
        return this.sender;
    }

    public String message(){
        return this.message;
    }
}
