import java.io.*;
import java.util.*;
import java.net.*;

public class MessageObject{

    private String sender;
    private String action;
    private String field3;
    private String field4;
    private String IP; //only for heartbeat messages
    private int port; //only for heartbeat messages

    public static final int SENDER = 1;
    public static final int ACTION = 2;
    public static final int FIELD3 = 3;
    public static final int FIELD4 = 4;

    public MessageObject(String clientSentence){
        parseSentence(clientSentence);
        if (this.action.equals("send") && this.field3.equals("HEARTBEAT")){
            extractAddress();
        } else {
            this.IP = "0";
            this.port = 0;
        }
    }

    private void parseSentence(String clientSentence){

        //Split into fields first
        String[] fields = clientSentence.split(":");

        this.sender = extract(fields[SENDER]);
        this.action = extract(fields[ACTION]);
        this.field3 = extract(fields[FIELD3]);
        this.field4 = fields[FIELD4];
        //message or broadcast
        /**if (fields.length > 4){
            for (int i = 4; i < fields.length; i++){
                this.field4 += " " + fields[i];
            }
        }**/
    }


    private String extract(String field){
        
        String[] temp = field.split(" ");
        return temp[0].trim();
    }

    private void extractAddress(){
        String[] temp = this.field4.split("/");
        this.IP = temp[0].trim();
        this.port = Integer.parseInt(temp[1].trim());
    }

    public String sender(){
        return this.sender;
    }

    public String action(){
        return this.action;
    }

    public String field3(){
        return this.field3;
    }

    public String field4(){
        return this.field4;
    }

    public String IP(){
        return this.IP;
    }

    public int port(){
        return this.port;
    }
}







