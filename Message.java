import java.util.*;
import java.net.*;

public class Message {
    public String mId;
    public int mType;  //mtype 0->requesting ip's, 1->response of ips, 2->block, 3->blockchain, 4->response
    public String ip;
    public long timestamp;
    public String data;

    private String delim = "@msg";
    public static String tempIp = "";

    static {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface
                    .getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    String tempIp2 = addr.getHostAddress();
                    if (tempIp2.contains("192") || tempIp2.contains("172")) {
                        tempIp = tempIp2;
                        break;
                    }

                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    
    
    public Message(int type, String message) {
        this.mId = UUID.randomUUID().toString();
        this.mType = type;
        this.data = message;
        this.ip = tempIp;
        this.timestamp = new Date().getTime();
    }

    public Message(String messageString){
        String[] message = messageString.split(delim);
        this.mId = message[0];
        this.mType = Integer.parseInt(message[1]);
        this.ip = message[2];
        this.timestamp = Long.parseLong(message[3]);
        try{this.data = message[4];} catch(Exception e){System.out.println(messageString);e.printStackTrace();}
    }

    public void print() {
        System.out.println("Message ID: " + mId);
        System.out.println("Message Type: " + mType);
        System.out.println("Message IP: " + ip);
        System.out.println("Message Data: " + data);
    }

    public String toString() {
        return this.mId + delim + this.mType + delim + this.ip + delim + this.timestamp + delim + this.data;
    }
    
    public static void main(String[] args){

        Message m = new Message(0, "Hello");
        System.out.println(m.toString());
        Message m2 = new Message(m.toString());
        m2.print();
    }
}