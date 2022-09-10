package minibitcoin;

public class TransactionInput {
    
    public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
	public TransactionOutput UTXO; //Contains the Unspent transaction output
	
	private String delim = "@tip";
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}

	public TransactionInput(String transactionInput, float val,TransactionOutput UTXO) {
		String[] parts = transactionInput.split(delim);
		this.transactionOutputId = parts[0];
		this.UTXO = UTXO;
	}

	public String toString() {
		if (UTXO == null)
			return transactionOutputId + delim + "null";
		else
			return transactionOutputId + delim + UTXO.toString();
	}
}
