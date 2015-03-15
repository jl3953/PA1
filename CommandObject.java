import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Used by Client.java to represent a user's command in an object for easier
 * manipulation.
 */
public class CommandObject{

    private String action; //what the command is, ie block, message
    private String param1; //first parameter of command
    private String param2; //second parameter of command

    public CommandObject(String command){
        parseCommand(command);
    }

    /**
     * Parses a user's command.
     * @param command
     */
    private void parseCommand(String command){

        //command delimited by spaces
        String[] temp = command.split("\\s+");

        this.action = temp[0].trim();
        if (this.action.equals("message") || this.action.equals("private")){
            this.param1 = temp[1].trim();
            this.param2 = temp[2].trim();
            for (int i=3; i < temp.length; i++){
                this.param2 += " " + temp[i];
            }
        } else if (this.action.equals("broadcast")){
            this.param1 = temp[1].trim();
            for (int i=2; i < temp.length; i++){
                this.param1 += " " + temp[i];
            }
        } else if (temp.length == 2){
            this.param1 = temp[1].trim();
            this.param2 = "nope";
        } else {
            this.param1 = "nope";
            this.param2 = "nope";
        }
    }

    public String action(){
        return this.action;
    }

    public String param1(){
        return this.param1;
    }

    public String param2(){
        return this.param2;
    }
}
