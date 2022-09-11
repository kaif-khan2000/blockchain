
import java.util.Date;
import java.util.ArrayList;

public class Block{
   
    public String hash;
    public String prevHash;
    public String merkleRoot;
    public long timestamp; 
    public int nonce;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.

	private String delim = "@block";
	
	public Block() {}
    public Block (String prevHash){    
        this.prevHash = prevHash;
        this.timestamp = new Date().getTime();
        this.hash = CalculateHash();
    }

	public Block(String message, int val){
		String[] parts = message.split(delim);
		this.hash = parts[0];
		this.prevHash = parts[1];
		this.merkleRoot = parts[2];
		this.timestamp = Long.parseLong(parts[3]);
		this.nonce = Integer.parseInt(parts[4]);
		int n = Integer.parseInt(parts[5]);
		for(int i = 0; i < n; i++){
			this.transactions.add(new Transaction(parts[6+i]));
		}
		
	}

	public boolean verifyBlock(){
		if(!this.prevHash.equals(sql.getLastHash())){
			return false;
		}
		
		if(!this.hash.equals(this.CalculateHash())){
			return false;
		}

		String target = StringUtil.getDificultyString(Server.difficulty); //Create a string with difficulty * "0"
		if(!hash.substring( 0, Server.difficulty).equals(target)) {
			return false;
		}
		 		

		for(Transaction t : this.transactions){
			if(!t.verifyTransaction()){
				return false;
			}
		}
		return true;
	}
    public String CalculateHash(){
        String calculatedHash = StringUtil.applySha256(
            prevHash + 
            Long.toString(timestamp) + 
            Integer.toString(nonce) +
            merkleRoot
        );
        return calculatedHash;
    }

    public int mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0" 
		while(!hash.substring( 0, difficulty).equals(target)) {
			synchronized(MessageHandler.blockReceived){
				if(MessageHandler.blockReceived){
					return 0;
				}
			}
			nonce ++;
			hash = CalculateHash();
		}
		System.out.println("Block Mined!!! : " + hash);
		return 1;
	}
	
	//Add transactions to this block
	public boolean addTransaction(Transaction transaction) {
		//process transaction and check if valid, unless block is genesis block then ignore.
		if(transaction == null) return false;		
		if((prevHash != "0")) {
			if((transaction.verifyTransaction() != true)) {
				System.out.println(transaction.transactionId + " failed to verify");
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}

	public String toString() {
		String result = hash + delim + prevHash + delim + merkleRoot + delim + Long.toString(timestamp) + delim + Integer.toString(nonce);
		int n = transactions.size();
		result += (delim + n);
		for(Transaction transaction : transactions) {
			result += delim + transaction.toString();
		}

		return result;

	}

}


