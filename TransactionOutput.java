
import java.security.PublicKey;

public class TransactionOutput {
 
    public String id;
	public PublicKey reciepient; //also known as the new owner of these coins.
	public float value; //the amount of coins they own
	public String parentTransactionId; //the id of the transaction this output was created in
	public int utxo = 1;
	
	//Constructor
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId, int utxo) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
		this.utxo = utxo;
	}
	private String delim = "@top";
	public TransactionOutput(String message){
		if(message == null || message.equals("null")) {
			return;
		}
		String[] parts = message.split(delim);
		this.id = parts[0];
		try{
			this.reciepient = StringUtil.getPublicKeyFromString(parts[1]);
		} catch(Exception e){
			this.reciepient = null;
		}
		
		this.value = Float.parseFloat(parts[2]);
		this.parentTransactionId = parts[3];
		this.utxo = Integer.parseInt(parts[4]);
		
	}
	
	//Check if coin belongs to you
	public boolean isMine(PublicKey publicKey) {
		
		return (publicKey == reciepient);
	}

	public void print() {
		System.out.println("TransactionOutput: "+id+" "+reciepient+" "+value+" "+parentTransactionId);
	}

	public String toString() {
		
		String key = StringUtil.getStringFromKey(reciepient);
		return id+delim+key+delim+value+delim+parentTransactionId+delim+utxo;
	}
}
