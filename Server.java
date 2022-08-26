import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {
    public static String seed = "192.168.134.190";
    private static ServerSocket server;
    boolean isServer = false;
    public static int n = 5;
    public Socket client1;
    public Socket server1 = null;
    public static Server servers[] = new Server[n];
    public static Server client[] = new Server[n];
    public static final int port = 8080;
    public static String clientIps;
    
    static {
        try {
            server = new ServerSocket(8080);
            for (int i = 0; i < n; i++) {
                servers[i] = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message[] messagepool;
    public static int mCount;
    public static String ip[] = new String[100];
    
    public static String getIps() {
        String ips = "";
        for (int i = 0; i < 2*n; i++) {
            if (ip[i] != null && !ip[i].equals("")) { 
                ips += ip[i] + ",";
            }
        }
        return ips;
    }

    public static void connectToServer(String ip1) {
        
        if(ip1.equals(Message.tempIp)){
            return;
        }
        if (ip1.equals("")) {
            System.out.println("IP field is empty.");
            return;
        }
        if (fetchIndex(ip1) != -1) {
            System.out.println("IP is already connected.");
            return;
        }
        int index = fetchNullIndex();
        if (index == -1) {
            System.out.println("No space left to connect.");
            return;
        }
        ip[n + index] = ip1;
        servers[index] = new Server(false);
        servers[index].setName("ClientThread-" + index);
        servers[index].start();
        try{TimeUnit.SECONDS.sleep(20);}catch(InterruptedException e){e.printStackTrace();}
    }
    
    public static void disconnectFromServer(String ip1) {
        int index = fetchIndex(ip1);
        if (index == -1) {
            System.out.println("disc:IP is not connected.");
            return;
        }
        index -= n;
        try {
            sendMessage(servers[index].server1.getInetAddress().toString().replace("/", ""), "close");
            servers[index].server1.close();
            servers[index] = null;
            ip[n + index] = null;
            System.out.println("Disconnected from " + ip1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int fetchNullIndex() {
        for (int i = 0; i < n; i++) {
            if (servers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public static int fetchIndex(String ipAddress) {
        for (int i = 0; i < 2*n; i++) {
            if (ip[i] != null && ip[i] != "") {
                try{
                    if (ip[i].equals(ipAddress)) {
                        return i;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                
            }
        }
        return -1;
    }

    public static void sendMessage(String ip, Message message) {
        if(ip.equals(Message.tempIp)){
            return;
        }
        int index = fetchIndex(ip);
        if (index == -1) {
            System.out.println("sendMess: IP is not connected.");
            return;
        }
        PrintWriter out = null;
        try {
            if (index < n) {
                out = new PrintWriter(client[index].client1.getOutputStream(), true);
            } else {
                try{
                    out = new PrintWriter(servers[index - n].server1.getOutputStream(), true);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException i) {
            i.printStackTrace();
        }
        System.out.println("\nsending message to " + ip);
        out.println(message.toString());
        System.out.println("\nmessage sent to " + ip + " "+ message.toString() + "\n");
        
    }

    public static void sendMessage(String ip, String message) {
        int index = fetchIndex(ip);
        if (index == -1) {
            System.out.println("sendMess(string):IP is not connected.");
            return;
        }
        PrintWriter out = null;
        try {
            if (index < n) {
                out = new PrintWriter(client[index].client1.getOutputStream(), true);
            } else {
                try{
                    out = new PrintWriter(servers[index - n].server1.getOutputStream(), true);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException i) {
            i.printStackTrace();
        }
        System.out.println("\nsending message to " + ip);
        out.println(message);
        System.out.println("\nmessage sent to " + ip + " "+ message.toString() + "\n");
        
    }

    public static void broadcast(Message message) {
        for(int i=0;i<2*n;i++){
            if(ip[i]!=null && ip[i]!=""){
                sendMessage(ip[i], message);
            }
        }
    }

    public Server(boolean isServer) {
        this.isServer = isServer;
        
    }

    public void serverFunction() {
        int index = -1;
        try {
            index = Integer.parseInt(Thread.currentThread().getName().replace("ServerThread-", ""));
            while (true) {

                System.out.println(Thread.currentThread().getName() + " Waiting for client to join");
                client1 = server.accept();
                System.out.println("Client joined");
                String ip1 = client1.getInetAddress().toString().replace("/", "");
				ip[index] = ip1;
                sendMessage(ip[index], "connected");
                Read thread = new Read(client1,index);
                thread.start();
                thread.join();
                client1.close();
                ip[index] = "";
            }
        } catch (IOException i) {
            i.printStackTrace();
            try {client1.close();}
            catch(Exception e){e.printStackTrace();}
            ip[index] = "";
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void clientFunction() {
        int index = Integer.parseInt(Thread.currentThread().getName().replace("ClientThread-", ""));
        try {
            index = n + index;
            server1 = new Socket(ip[index], port);
            System.out.println("Connected to server " + ip[index]);
            Read thread = new Read(server1,index);
            thread.start();
            thread.join();
            server1.close();
            ip[index] = "";
            servers[index - n] = null;

        } catch (IOException i) {
            i.printStackTrace();
            ip[index] = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (isServer) {
            serverFunction();
        } else {
            clientFunction();
        }
    }

    public static void build(Message[] messagepool1, int messageCount) {
        
        String[] ip = {"192.168.134.152"};
        messagepool = messagepool1;
        mCount = messageCount;
        for (int i = 0; i < n; i++) {
            client[i] = new Server(true);
            client[i].setName("ServerThread-" + i);
            client[i].start();
        }
        new Wallet().start();
        try{
            //System.out.println(Message.tempIp);
            if(!Message.tempIp.equals(seed)){
                System.out.println("\nEstablishing connection with seed " + seed+"\n");
                connectToServer(seed);
                Message message = new Message(0,"giveMeAddress");
                sendMessage(seed, message);
                //disconnectFromServer(seed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        // for (int i= 0;i<ip.length;i++){
        //     try{
        //         connectToServer(ip[i]);
        //     } catch (Exception e){
        //         System.out.println("UserError:" + e);
        //     }
        // }
    }
}
