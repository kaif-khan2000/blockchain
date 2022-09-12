import java.util.HashMap;


import java.net.Socket;
import java.util.*;

class MessageClassifier extends Thread{
    private Message message;
    private Socket client;
    public MessageClassifier(Message message, Socket client){
        this.message = message;
        this.client = client;
    }

    public static int fetchedIps = 0; //0 not fetched yet, -1 fetched but no ips, 1 fetched ip's
    public static int fetchedRemainingBlocks = 0; //0 not fetched yet ,1 fetched

    public void run() {
        if(message.mType == 0){
            // requesting ip addresses
            System.out.println("["+message.ip+"] requesting ip addresses");
            String ips = Server.getIps(client);
            Message newMessage = new Message(1,ips);
            System.out.println("messageSent:"+newMessage.toString());
            Server.sendMessage(client, newMessage);
            return;
        }

        if (message.mType == 1) {
            // we have received ip addresses
            System.out.println("type 1:"+message.data);
            if(message.data.equals(" ")){
                fetchedIps = -1;
                return;
            }
            String[] ips = message.data.split(",");
            int count = 0;
            for (String ip :ips){
                try{
                    Server.connectToServer(ip);
                    count++;
                    if(count > 2) break;
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (count>0)
                fetchedIps = 1;
            else
                fetchedIps = -1;
            return;
        }

        if (message.mType == 2) {
            // we have received a transaction
            System.out.println("type 2:"+message.data);
            Transaction transaction = new Transaction(message.data);
            
            //Server.broadcast(message);
            if (!transaction.verifyTransaction()){
                System.out.println("Transaction is not valid");
                return;
            }
            
            //check weather the transaction inputs are valid
		    float tempAmount = 0;
		    for(TransactionInput i : transaction.inputs){
			    float temp = sql.checkUTXOExist(i.transactionOutputId);
                if(temp == -1){
                    System.out.println("Transaction input is not valid - no utxo found" + i.transactionOutputId);
                    return;
                }
                tempAmount += temp;
            }
            if(tempAmount < transaction.value){
                System.out.println("Transaction input is not valid - not enough utxo");
                return;
            }

            //check weather the transaction output is valid
            for(TransactionOutput o : transaction.outputs){
                tempAmount -= o.value;
            }
            if(tempAmount != 0){
                System.out.println("Transaction output is not valid");
                return;
            }

            //store it in mempool and broadcast it
            synchronized(MessageHandler.mempool){
                MessageHandler.mempool.add(transaction);
            }
            return;
        }
        
        if(message.mType == 3) {
            //requesting for remaining blocks
            String hash = message.data;
            sql.sendRemainingHash(client,hash);
            return;
        }

        if(message.mType == 4){
            //getting a block from seed node.
            Block newBlock = new Block(message.data,1);
            if(newBlock.hash.equals(newBlock.CalculateHash()))
                sql.storeblock(newBlock);
            return;
        }

        if(message.mType == 5){
            fetchedRemainingBlocks = 1;
            return;
        }

        if(message.mType == 6){
            Block newBlock = new Block(message.data,1);
            if(newBlock.verifyBlock()){
                synchronized(MessageHandler.blockReceived){
                    MessageHandler.blockReceived = true;
                }
                sql.storeblock(newBlock);
                System.out.println("Block received and verified");
            }
            else{
                System.out.println("Block received but not verified");
            }
            synchronized(MessageHandler.blockReceived){
                MessageHandler.blockReceived = false;
            }
        }
    }
}

public class MessageHandler {
    /*
     * collect all messages statistically.
     * if the message is a transaction, add it to the mempool.
     * if the message is a block, add it to the blockpool.
     * if the message is a ipadresses then add it to the ipadresses pool.
     */

    public static Message[] messagepool = new Message[10];
    public static int messageCount = 0;
    public static List<Transaction> mempool = Collections.synchronizedList(new ArrayList<Transaction>());
    public static int mempoolCount = 0;
    public static Boolean blockReceived = false;
    public static HashMap<String, Long> msgLookUp = new HashMap<String, Long>();

    public static void addToMessagepool(Message message, Socket client) {
        long currentTime = new Date().getTime();
        // message of 2 minutes old.
        if (currentTime - message.timestamp > 120000) {
            System.out.println("Message is too old");
            return;
        }
        if (!msgLookUp.containsKey(message.mId)) {
            messagepool[messageCount] = message;
            messageCount++;
            msgLookUp.put(message.mId, message.timestamp);
            System.out.println("received message: "+ message.toString());
            MessageClassifier m = new MessageClassifier(message, client);
            m.start();
            
        }
    }
}
