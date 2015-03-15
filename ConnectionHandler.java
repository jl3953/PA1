import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Called on by server to handle a new connection from accept().
 *
 * @author Jennifer Lam jl3953
 */
public class ConnectionHandler implements Runnable{

    private Socket connectionSocket;
    private ConcurrentHashMap<String,ClientObject> mymap;

    public ConnectionHandler(Socket connectionSocket, ConcurrentHashMap<String,ClientObject> mymap){
        this.connectionSocket = connectionSocket;
        this.mymap = mymap;
    }

    public static void sendOut(ClientObject sender, ClientObject recipient, String payload){
        //check if recipient has blocked sender
        try{
            if (recipient.isBlocked(sender)){
                Socket conn = new Socket(sender.IP(), sender.port());
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes("Your message could not be delivered as the recipient has blocked you.\n");
                conn.close();
            } else {
                //open connection
                Socket conn = new Socket(recipient.IP(), recipient.port());
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                //send message
                out.writeBytes(sender.username() + ": " + payload + "\n");
                //close connection
                conn.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void sendOut(String sender, ClientObject recipient, String payload){
        try{
            //open connection
            Socket conn = new Socket(recipient.IP(), recipient.port());
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            //send message
            out.writeBytes(sender + ": " + payload + "\n");
            //close connection
            conn.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){

        String clientSentence;

        try{
            //input stream from connecting client
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));
            clientSentence = inFromClient.readLine();

            //Processing client request
            if (clientSentence != null){
                System.out.println(clientSentence);
                MessageObject message = new MessageObject(clientSentence);

                //Identifying client
                ClientObject co = mymap.get(message.sender());

                //checking to see if client has any unread messages

                //HEARTBEAT
                if (message.action().equals("send") && message.field3().equals("HEARTBEAT")){
                    Calendar cal = Calendar.getInstance();
                    co.setOnline(true);
                    co.setLastbeat(cal.getTimeInMillis() + HeartBeat.INTERVAL * 1000);
                    co.setIP(message.IP());
                    co.setPort(message.port());
                    //broadcast presence
                    if (co.firstTime()){
                        co.setFirstTime(false);
                        for (Map.Entry<String, ClientObject> entry : mymap.entrySet()){
                            ClientObject temp = entry.getValue();
                            if (temp.equals(co))
                                continue;
                            if (temp.isBlocked(co))
                                continue;
                            String presence = "User " + co.username() + " is online.";
                            if (temp.online()){
                                ConnectionHandler.sendOut(co,temp,presence);
                            }
                        }
                    }
                } 
                // broadcast
                else if (message.action().equals("send") && message.field3().equals("ALL")){
                    for (Map.Entry<String, ClientObject> entry : mymap.entrySet()){
                        ClientObject temp = entry.getValue();
                        if(temp.equals(co)){
                            continue;
                        }
                        if (temp.online() && !temp.equals(co)){
                            ConnectionHandler.sendOut(co, temp, message.field4());
                        } else {
                            temp.putInMailbox(co.username(), message.field4());
                        }
                    }
                }
                //message
                else if (message.action().equals("send")){
                    ClientObject recipient = mymap.get(message.field3());
                    //if recipient is online
                    if (recipient.online()){
                        ConnectionHandler.sendOut(co, recipient, message.field4());
                    }
                    //recipient offline
                    else {
                        recipient.putInMailbox(co.username(), message.field4());
                    }
                }
                //block
                else if (message.action().equals("serveraction") && message.field3().equals("block")){
                    ClientObject blockedUser = mymap.get(message.field4());
                    co.blockUser(blockedUser);
                    try{
                        Socket conn = new Socket(co.IP(), co.port());
                        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes("User "+blockedUser.username()+" has been blocked.\n");
                        conn.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } //unblock
                else if (message.action().equals("serveraction") && message.field3().equals("unblock")){
                    ClientObject blockedUser = mymap.get(message.field4());
                    co.unblockUser(blockedUser);
                    try {
                        Socket conn = new Socket(co.IP(), co.port());
                        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes("User "+blockedUser.username()+" is unblocked.\n");
                        conn.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }//online 
                else if(message.action().equals("serveraction") && message.field3().equals("online")){
                    String onlinePeeps = "";
                    for (Map.Entry<String,ClientObject> entry : mymap.entrySet()){
                        ClientObject temp = entry.getValue();
                        if (temp.equals(co)){
                            continue;
                        }
                        if (temp.online()){
                            onlinePeeps += temp.username() + "\n";
                        }
                    }
                    try{
                        Socket conn = new Socket(co.IP(), co.port());
                        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes(onlinePeeps);
                        conn.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                //getaddress
                else if(message.action().equals("serveraction") && message.field3().equals("getaddress")){
                    String address = "";
                    ClientObject target = mymap.get(message.field4());
                    if (target.online()){
                    address += "User " + target.username() + "'s address is " + target.IP() + ":" + target.port();
                    } else {
                        address += "User " + target.username() + " is not online.";
                    }
                    try{
                        Socket conn = new Socket(co.IP(), co.port());
                        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes(address + "\n");
                        conn.close();
                    } catch (Exception e){ 
                        e.printStackTrace();
                    }   
                } //logout
                else if (message.action().equals("serveraction") && message.field3().equals("logout")){
                    co.setOnline(false);
                    String loggedout = "User " + co.username() + " has logged out.";
                    for (Map.Entry<String, ClientObject> entry : mymap.entrySet()){
                        ClientObject temp = entry.getValue();
                        if (temp.online()){
                            ConnectionHandler.sendOut(co, temp, loggedout);
                        }
                    }
                } else if (message.action().equals("serveraction") && message.field3().equals("private")){
                    String address = "";
                    ClientObject target = mymap.get(message.field4());
                    if(target.online()){
                        address += target.IP() + ":" + target.port();
                        DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
                        out.writeBytes(address + "\n");
                    } else {
                        Socket conn = new Socket(co.IP(), co.port());
                        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes(address + "\n");
                        conn.close();
                    }
                }

                else {}

                //checking to see if client has any unread messages
                while (co.hasMail()){
                    MailNode node = co.getMail();
                    ConnectionHandler.sendOut(mymap.get(node.sender()), co, node.message());
                }

            }
            connectionSocket.close();
            System.out.println("ConnectionHandler terminated");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}




