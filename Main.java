import minibitcoin.*;


class Mining extends Thread{
    private Transaction[] mempool;
    public Mining (Transaction[] mempool){
        this.mempool = mempool;
    }

    public void run() {
        System.out.println(mempool[0].toString());
    }
}

class CollectTransactions extends Thread{
    private Transaction[] mempool;
    private boolean validateTransaction(Transaction t) {
        return true;
    }
    public CollectTransactions (Transaction[] mempool){
        this.mempool = mempool;
    }
    public void run() {
        System.out.println(mempool[0].toString());

    }
}

public class Main {
    public static void main(String[] args){


        Message[] messagepool = new Message[10];
        int messageCount = 0;
        Transaction[] mempool = new Transaction[10];
        //CollectTransactions collectTransactions = new CollectTransactions(mempool);
        
        Server.build(messagepool,messageCount);
        // Mining mining = new Mining(mempool);
        // mining.start();
        // collectTransactions.start();
        
        // try {
        //     mining.join();
        //     collectTransactions.join();
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        
    }
}
