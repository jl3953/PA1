import java.util.*;
import java.io.*;
import java.net.*;

public class CommandObject{

    private String action;
    private String param1;
    private String param2;

    public CommandObject(String command){
        parseCommand(command);
    }

    private void parseCommand(String command){

        String[] temp = command.split("\\s+");
        this.action = temp[0].trim();

        if(temp.length >= 2 && !this.action.equals("message")){
            this.param1 = temp[1].trim();
            for(int i = 2; i < temp.length; i++){
                this.param1 += " " + temp[i];
            }
        } else if (temp.length >= 3 && this.action.equals("message")){
            this.param2 = temp[2].trim();
            for (int i = 3; i < temp.length; i++){
                this.param2 += " " + temp[i].trim();
            }
        } else {
            this.param1 = "";
            this.param2 = "";
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
