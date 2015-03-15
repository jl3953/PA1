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

    /**
     * Opens connections and all input/output buffers associated with the server.
     * @param serverip IP address of the server
     * @param serverport port of the server.
     */
    public void openConnections(String serverip, int serverport) throws Exception{
        this.clientSocket = new Socket(serverip, serverport);
        this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
        this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
        this.inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
        this.online = false;
        this.random = new Random();
    }

    /**
     * Closes all connections to maintain non-permanent TCP connection.
     */
    public void closeConnections() throws Exception{
        this.clientSocket.close();
    }

    /**
     * Returns the client's username.
     */
    public String name(){
        return this.name;
    }

    /**
     * Client authenticates itself with the server--protocol and print statements
     * make it obvious what is happening in this method.
     * @param serverIP server's IP address
     * @param serverport server's port
     */
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

            reply = this.readFromServer();

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

    /**
     * Sends a line to the server.
     * @param string the message
     */
    public void sendToServer(String string) throws Exception{
        this.outToServer.writeBytes(string + "\n");
    }

    /**
     * Reads a message from the server
     */
    public String readFromServer() throws Exception{
        return this.inFromServer.readLine();
    }

    /**
     * Formats a request to the server following the protocol (described in README),
     * this send is used when a message must be sent from one client to another.
     * @param recipient recipient of message
     * @param payload the actual message
     */
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

    /**
     * Formats a request, but only when client is communicating only to server,
     * not other clients.
     * @param command client's command
     * @param param parameters of that command
     */
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

    /**
     * I tried to fix the port binding problem by using this method, but the
     * serverSocket cannot be passed in and out as such.
     */
    public static int assignPort(ServerSocket serverSocket){
        boolean truth = false;
        while (truth){
            Random rand = new Random();
            int port = Math.abs(rand.nextInt() % 10000);
            try {
                serverSocket = new ServerSocket(port);
                return port;
            } catch (BindException b){
                truth = true;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return 0;
    }


    public static void main(String[] args){

        String serverip = args[0];
        int serverport = Integer.parseInt(args[1]);

        Client client = null;
        try{
            client = new Client();
            client.authenticate(serverip, serverport);
        } catch (Exception e){
            e.printStackTrace();
        }

        //You need three threads--one to listen for incoming connections, 
        //one to go on with its business, one for heartbeats

        //Server thread
        int port = Math.abs(client.random.nextInt() % 10000);
        Runnable listener = new ClientListener(port);
        Thread t = new Thread(listener);
        t.start();

        try{
            //heartbeat thread
            Runnable heartbeat = new HeartBeat(serverport, serverip, client.name, port);
            Thread t2 = new Thread(heartbeat);
            t2.start();

            //here's the thread where we process commands
            String command;
            while(true){
                //read in user's command
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}

