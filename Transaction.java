
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;


public class Transaction {
    public String transactionId; // this is also the hash of the transaction.
    public PublicKey sender; // senders address/public key.
    public PublicKey reciepient; // Recipients address/public key.
    public float value;
    public byte[] signature; // this is to prevent anybody else from spending funds in our wallet.
	public long timestamp;
    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    public static int sequence = 0; // a rough count of how many transactions have been generated. 
	public static String delim = " & ";
    private float tempValue;
	// Constructor: 
	public Transaction() {}

	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs, float tempValue) {
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
		this.tempValue = tempValue;
		this.timestamp = new Date().getTime();
    }

	public Transaction(String message) {
		String[] parts = message.split(delim);
		this.transactionId = parts[0];
		this.sender = StringUtil.getPublicKeyFromString(parts[1]);
		this.reciepient = StringUtil.getPublicKeyFromString(parts[2]);
		this.value = Float.parseFloat(parts[3]);
		int inputLength = Integer.parseInt(parts[4]);
		this.inputs = new ArrayList<TransactionInput>();
		for(int i = 0; i < inputLength; i++) {
			this.inputs.add(new TransactionInput(parts[5+i]));
		}
		this.outputs = new ArrayList<TransactionOutput>();
		
		int outputLength = Integer.parseInt(parts[5+inputLength].strip());
		for(int i = 0; i < outputLength; i++) {
			this.outputs.add(new TransactionOutput(parts[6+inputLength+i]));
		}
		this.signature = parts[6+inputLength+outputLength].getBytes();
		this.timestamp = Long.parseLong(parts[7+inputLength+outputLength]);
	}
	
	public void print() {
		System.out.println("Transaction: "+transactionId+" "+sender+" "+reciepient+" "+value+" "+inputs.toString()+" "+outputs.toString());
	}

    //This Calculates the transaction hash (which will be used as its Id)
    private String calculateHash() {
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(reciepient) +
                Float.toString(value) + timestamp
                );
    }

    //Signs all the data we dont wish to be tampered with.
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
        signature = StringUtil.applyECDSASig(privateKey,data);		
    }
    //Verifies the data we signed hasnt been tampered with
    public boolean verifiySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

	public boolean verifyTransaction() {
		//check if transaction is signed correctly
		if(!verifiySignature()) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
		
		// check weather the transaction id is valid
		if(!transactionId.equals(calculateHash())) {
			System.out.println("#Transaction id is invalid");
			return false;
		}
		
		return true;
	}

    public void processTransaction() {	
		//generate transaction outputs:
		float leftOver = tempValue - value; //get value of inputs then the left over change:
		transactionId = calculateHash();
		outputs.add(new TransactionOutput(this.reciepient, value,transactionId,1)); //send value to recipient
		outputs.add(new TransactionOutput(this.sender, leftOver,transactionId,1)); //send the left over 'change' back to sender	
	}
		

    //returns sum of outputs:
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}


	public String toString() {
		int inputLength = 0;
		String inputstring="";
		for(TransactionInput i : inputs) {
			if (i.toString() == null) continue;
			System.out.println(i.toString());
			inputstring += i.toString() + delim;
			inputLength++;
		}
		if(inputs.size() > 0)
			inputstring = inputstring.substring(0, inputstring.length()-3);
		String outputstring="";
		int outputLength = 0;
		for(TransactionOutput o : outputs) {
			if(o.toString() == null) continue;
			outputstring += o.toString() + delim;
			outputLength++;
		}
		if(outputs.size() > 0)
			outputstring = outputstring.substring(0, outputstring.length()-3);
		String senderkey = StringUtil.getStringFromKey(sender);
		String reciepientkey = StringUtil.getStringFromKey(reciepient);
		String result = transactionId + delim + senderkey + delim + reciepientkey + delim + value + delim + inputLength;
		if (inputLength > 0)
			result += delim + inputstring;
		result += delim + outputLength;
		if (outputLength > 0)
			result += delim + outputstring;
		result += delim + StringUtil.getStringFromSignature(signature) + delim + timestamp;
		return result;
	}

}
