import java.io.*;
import java.util.*;
import java.net.*;

public class MailNode {

    private String sender;
    private String message;

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
