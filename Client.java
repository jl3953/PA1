import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Message client.
 *
 * @author Jennifer Lam jl3953
 */
public class Client{

    public BufferedReader inFromUser;
    public Socket clientSocket;
    public DataOutputStream outToServer;
    public BufferedReader inFromServer;
    public boolean online;
    public Random random;
    public String name;

    public Client() throws Exception{
        //openConnections();
        this.name = "";
    }

    public void openConnections(String serverip, int serverport) throws Exception{
        this.clientSocket = new Socket(serverip, serverport);
        this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
        this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
        this.inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
        this.online = false;
        this.random = new Random();
    }

    public void closeConnections() throws Exception{
        this.clientSocket.close();
    }

    public String name(){
        return this.name;
    }

    public void authenticate(String serverIP, int serverport) throws Exception{
        openConnections(serverIP, serverport);
        //prompt user
        System.out.print("Username: ");
        System.out.flush();
        String username = this.inFromUser.readLine();
        String password, reply;

        while(true){
            System.out.print("Password: ");
            System.out.flush();
            password = this.inFromUser.readLine();
            this.sendToServer(username + " " + password);

            System.out.println("Client: so far so good");
            reply = this.readFromServer();
            System.out.println("Client: " + reply);

            if (reply.equals("OK")){
                System.out.println("Welcome to simple chat server!");
                this.online = true;
                this.name = username;
                closeConnections();
                return;
            } else if (reply.equals("THIRD_TIME")){
                System.out.println("Invalid password. Your account has been blocked." +
                        " Please try again after sometime.");
                System.exit(0);
            } else if (reply.equals("BLOCKED")){
                System.out.println("Due to multiple login failures, your account " +
                        "has been blocked. Please try again after sometime.");
                System.exit(0);
            } else{
                System.out.println("Invalid password. Please try again");
            }
        } 
    }

    public void sendToServer(String string) throws Exception{
        this.outToServer.writeBytes(string + "\n");
    }

    public String readFromServer() throws Exception{
        return this.inFromServer.readLine();
    }

    public void sendMessage(String recipient, String payload){

        try{
            String initialOK = this.name;
            outToServer.writeBytes(initialOK + "\n");

            String message = "sender:" + this.name +
                " action:send" +
                " field3:" + recipient +
                " field4:" + payload;

            outToServer.writeBytes(message + "\n");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendCommand(String command, String param){
        try{
            String initialOK = this.name;
            outToServer.writeBytes(initialOK + "\n");

            String message = "sender:" + this.name +
                " action:serveraction" +
                " field3:" + command +
                " field4:" + param;

            outToServer.writeBytes(message + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception{

        String serverip = args[0];
        int serverport = Integer.parseInt(args[1]);

        Client client = new Client();
        client.authenticate(serverip, serverport);

        //You need three threads--one to listen for incoming connections, 
        //one to go on with its business, one for heartbeats

        //Server
        int port = Math.abs(client.random.nextInt() % 10000);
        Runnable listener = new ClientListener(port);
        Thread t = new Thread(listener);
        t.start();

        //heartbeat
        Runnable heartbeat = new HeartBeat(serverport, serverip, client.name, port);
        Thread t2 = new Thread(heartbeat);
        t2.start();

        //here's the one where we go about our own business
        String command;
        while(true){
            command = client.inFromUser.readLine();
            CommandObject comObj = new CommandObject(command);

            if (comObj.action().equals("message")){
                client.openConnections(serverip, serverport);
                client.sendMessage(comObj.param1(), comObj.param2());
                client.closeConnections();
            } else if (comObj.action().equals("broadcast")){
                client.openConnections(serverip, serverport);
                client.sendMessage("ALL", comObj.param1());
                client.closeConnections();
            } else if (comObj.action().equals("block")){
                client.openConnections(serverip, serverport);
                client.sendCommand(comObj.action(), comObj.param1());
                client.closeConnections();
            } else if (comObj.action().equals("unblock")){
                client.openConnections(serverip, serverport);
                client.sendCommand(comObj.action(), comObj.param1());
                client.closeConnections();
            } else if (comObj.action().equals("online")){
                client.openConnections(serverip, serverport);
                client.sendCommand(comObj.action(), comObj.param1());
                client.closeConnections();
            } else if (comObj.action().equals("getaddress")){
                client.openConnections(serverip, serverport);
                client.sendCommand(comObj.action(), comObj.param1());
                client.closeConnections();
            } else if (comObj.action().equals("logout")){
                client.openConnections(serverip, serverport);
                client.sendCommand(comObj.action(), comObj.param1());
                client.closeConnections();
                System.out.println("Goodbye!");
                System.exit(0);
            } else if (comObj.action().equals("private")){
                //getaddress
                try {
                    client.openConnections(serverip, serverport);
                    client.sendCommand("private", comObj.param1());
                    String address = client.inFromServer.readLine();
                    //parse address
                    String privateip = address.split(":")[0];
                    int privateport = Integer.parseInt(address.split(":")[1]);
                    client.closeConnections();
                    //send private message
                    client.openConnections(privateip, privateport);
                    client.outToServer.writeBytes(client.name + ": " + comObj.param2() + "\n");
                    client.closeConnections();
                }
                catch (NullPointerException e){
                    System.out.println("User " + comObj.param1() + " is no" +
                            " longer available at this address. You may send" +
                            " an offline message through the server.");
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            else{
            }

        }
    }

}

