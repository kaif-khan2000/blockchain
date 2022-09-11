

class Mining extends Thread{
    private Transaction[] mempool;
    public Mining (Transaction[] mempool){
        this.mempool = mempool;
    }

    public void run() {
        System.out.println(mempool[0].toString());
    }
}

// class CollectTransactions extends Thread{
//     private Transaction[] mempool;
//     private boolean validateTransaction(Transaction t) {
//         return true;
//     }
//     public CollectTransactions (Transaction[] mempool){
//         this.mempool = mempool;
//     }
//     public void run() {
//         System.out.println(mempool[0].toString());

//     }
// }

public class Main {
    
    public static void main(String[] args){
        
        Server.build();
        
    }
}
