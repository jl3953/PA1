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
        ConnectionHandler.sendOut(sender.username(), recipient, payload);
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
        String capitalizedSentence;

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
                } 
                // broadcast
                else if (message.action().equals("send") && message.field3().equals("ALL")){
                    for (Map.Entry<String, ClientObject> entry : mymap.entrySet()){
                        ClientObject temp = entry.getValue();
                        System.out.println("temp: "+temp.username() + " co: " + co.username());
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
                //checking to see if client has any unread messages
                while (co.hasMail()){
                    System.out.println("has mail");
                    System.out.println(co.IP() + ":" + co.port());
                    MailNode node = co.getMail();
                    ConnectionHandler.sendOut(node.sender(), co, node.message());
                }


                //output stream to client server
                //Socket out = new Socket(machine, port);
                //DataOutputStream outToClient = new DataOutputStream(
                //      out.getOutputStream());

                //processing the actual request
                /**capitalizedSentence = clientSentence.toUpperCase() + "\n";
                  outToClient.writeBytes(capitalizedSentence);
                  out.close();*/
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}




