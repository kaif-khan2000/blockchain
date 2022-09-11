// package minibitcoin;

// import java.util.ArrayList;
// import java.util.Base64;
// import java.util.HashMap;
// import java.security.Security;
// import com.google.gson.GsonBuilder;

// import Block;
// import Transaction;
// import TransactionInput;
// import TransactionOutput;
// import Wallet;

// public class Minichain {

//     public static ArrayList<Block> blockchain = new ArrayList<Block>();
//     public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>(); // list of all
//                                                                                                        // unspent
//                                                                                                        // transactions.
//     public static float minimumTransaction = 0.1f;
//     public static int difficulty = 2;
//     public static Wallet walletA;
//     public static Wallet walletB;
//     public static Transaction genesisTransaction;

//     // function to check validity of hashes.
//     public static Boolean isChildValid() {
//         Block currentBlock;
//         Block prevBlock;
//         String hashTarget = new String(new char[difficulty]).replace('\0', '0');

//         // loop through blockchain to check hashes
//         for (int i = 1; i < blockchain.size(); i++) {
//             currentBlock = blockchain.get(i);
//             prevBlock = blockchain.get(i - 1);

//             // compare registered hash and calculated hash

//             if (!currentBlock.hash.equals(currentBlock.CalculateHash())) {
//                 System.out.println("current Hashes not equal");
//                 return false;
//             }

//             if (!prevBlock.hash.equals(currentBlock.prevHash)) {
//                 System.out.println("current Hashes not equal");
//                 return false;
//             }

//             // chech if hash is solved
//             if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
//                 System.out.println("This block hasn't mined.");
//                 return false;
//             }
//         }
//         return true;
//     }
 

//     public static Boolean isChainValid() {
//         Block currentBlock;
//         Block previousBlock;
//         String hashTarget = new String(new char[difficulty]).replace('\0', '0');
//         HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>(); // a temporary working
//                                                                                                  // list of unspent
//                                                                                                  // transactions at a
//                                                                                                  // given block state.
//         tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

//         // loop through blockchain to check hashes:
//         for (int i = 1; i < blockchain.size(); i++) {

//             currentBlock = blockchain.get(i);
//             previousBlock = blockchain.get(i - 1);
//             // compare registered hash and calculated hash:
//             if (!currentBlock.hash.equals(currentBlock.CalculateHash())) {
//                 System.out.println("#Current Hashes not equal");
//                 return false;
//             }
//             // compare previous hash and registered previous hash
//             if (!previousBlock.hash.equals(currentBlock.prevHash)) {
//                 System.out.println("#Previous Hashes not equal");
//                 return false;
//             }
//             // check if hash is solved
//             if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
//                 System.out.println("#This block hasn't been mined");
//                 return false;
//             }

//             // loop through blockchains transactions:
//             TransactionOutput tempOutput;
//             for (int t = 0; t < currentBlock.transactions.size(); t++) {
//                 Transaction currentTransaction = currentBlock.transactions.get(t);

//                 if (!currentTransaction.verifiySignature()) {
//                     System.out.println("#Signature on Transaction(" + t + ") is Invalid");
//                     return false;
//                 }
//                 if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
//                     System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
//                     return false;
//                 }

//                 for (TransactionInput input : currentTransaction.inputs) {
//                     tempOutput = tempUTXOs.get(input.transactionOutputId);

//                     if (tempOutput == null) {
//                         System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
//                         return false;
//                     }

//                     if (input.UTXO.value != tempOutput.value) {
//                         System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
//                         return false;
//                     }

//                     tempUTXOs.remove(input.transactionOutputId);
//                 }

//                 for (TransactionOutput output : currentTransaction.outputs) {
//                     tempUTXOs.put(output.id, output);
//                 }

//                 if (currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
//                     System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
//                     return false;
//                 }
//                 if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
//                     System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
//                     return false;
//                 }

//             }

//         }
//         System.out.println("Blockchain is valid");
//         return true;
//     }

//     public static void addBlock(Block newBlock) {
//         newBlock.mineBlock(difficulty);
//         blockchain.add(newBlock);
//     }

//     // blockchain.add(new Block("hi vikas", "0"));
//     // System.out.println("Trying to Mine block 1...");
//     // blockchain.get(0).mineBlock(difficulty);

//     // blockchain.add(new Block("hi kaif",
//     // blockchain.get(blockchain.size()-1).hash));
//     // System.out.println("Trying to Mine block 2...");
//     // blockchain.get(1).mineBlock(difficulty);

//     // blockchain.add(new Block("hi ravi",
//     // blockchain.get(blockchain.size()-1).hash));
//     // System.out.println("Trying to Mine block 3...");
//     // blockchain.get(2).mineBlock(difficulty);

//     // System.out.println("\nBlockchain is valid: " + isChildValid());

//     // String blockchainJson = new
//     // GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//     // System.out.println("\n The BlockChain :");
//     // System.out.println(blockchainJson);

//     // for(int i = 0 ; i< blockchain.size(); i++){
//     // System.out.println("previous hash of block " + (i+1) +" : "+
//     // blockchain.get(i).prevHash);
//     // System.out.println("hash of block " + (i+1) +" : " +blockchain.get(i).hash);
//     // System.out.println("Timestamp :" +blockchain.get(i).timestamp);
//     // System.out.println("Nonce :" +blockchain.get(i).nonce);
//     // }

//     public static void main(String[] args) {

//         // add our blocks to the blockchain ArrayList:
//         Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // Setup Bouncey castle as a
//                                                                                         // Security Provider

//         // Create wallets:
//         walletA = new Wallet();
//         walletB = new Wallet();
//         Wallet coinbase = new Wallet();

//         // create genesis transaction, which sends 100 NoobCoin to walletA:
//         genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
//         genesisTransaction.generateSignature(coinbase.privateKey); // manually sign the genesis transaction
//         genesisTransaction.transactionId = "0"; // manually set the transaction id
//         genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value,
//                 genesisTransaction.transactionId)); // manually add the Transactions Output
//         UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // its important to store
//                                                                                             // our first transaction in
//                                                                                             // the UTXOs list.

//         System.out.println("Creating and Mining Genesis block... ");
//         Block genesis = new Block("0");
//         genesis.addTransaction(genesisTransaction);
//         addBlock(genesis);

//         // testing
//         Block block1 = new Block(genesis.hash);
//         System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//         System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
//         block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
//         addBlock(block1);
//         System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//         System.out.println("WalletB's balance is: " + walletB.getBalance());

//         Block block2 = new Block(block1.hash);
//         System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
//         block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
//         addBlock(block2);
//         System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//         System.out.println("WalletB's balance is: " + walletB.getBalance());

//         Block block3 = new Block(block2.hash);
//         System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
//         block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
//         System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//         System.out.println("WalletB's balance is: " + walletB.getBalance());

//         isChainValid();
//     }

// }
