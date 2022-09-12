import java.sql.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;
import javax.swing.*;
import java.security.*;

public class Wallet extends Thread {
    public static PrivateKey privateKey;
    public static PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        // generateKeyPair();
    }

    // public void generateKeyPair() {
    // try {
    // KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
    // SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    // ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
    // // Initialize the key generator and generate the keypair
    // keyGen.initialize(ecSpec, random); // used to get 256byte acceptable security
    // level
    // KeyPair keyPair = keyGen.generateKeyPair();
    // privateKey = keyPair.getPrivate();
    // publicKey = keyPair.getPublic();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }

    

    // public Transaction sendFunds(PublicKey _recipient, float value) {
    //     if (getBalance() < value) {
    //         System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
    //         return null;
    //     }
    //     ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

    //     float total = 0;
    //     for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
    //         TransactionOutput UTXO = item.getValue();
    //         total += UTXO.value;
    //         inputs.add(new TransactionInput(UTXO.id));
    //         if (total > value)
    //             break;
    //     }

    //     Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
    //     newTransaction.generateSignature(privateKey);

    //     for (TransactionInput input : inputs) {
    //         UTXOs.remove(input.transactionOutputId);
    //     }
    //     return newTransaction;
    // }

    public void generateKeyPair() {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Connection conn = db.con;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from wallet");
            if (!rs.isBeforeFirst()) {
                // no keys
                try {
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
                    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                    ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
                    // Initialize the key generator and generate the keypair
                    keyGen.initialize(ecSpec, random); // used to get 256byte acceptable security level
                    KeyPair keyPair = keyGen.generateKeyPair();
                    privateKey = keyPair.getPrivate();
                    publicKey = keyPair.getPublic();

                    String pubkey = StringUtil.getStringFromKey(publicKey);
                    String prvkey = StringUtil.getStringFromKey(privateKey);
                    ;
                    // db obj = new db();
                    // Connection conn=obj.con;
                    try {
                        Statement stmt = conn.createStatement();
                        System.out.println("insert into wallet(privatekey,publickey) values('" +
                                prvkey + "','" +
                                pubkey +
                                "');");
                        int rset = stmt.executeUpdate("insert into wallet(privatekey,publickey) values('" +
                                prvkey + "','" +
                                pubkey +
                                "');");
                        if (rset > 0)
                            System.out.println("Successfully Inserted");
                        else
                            System.out.println("Insert Failed");
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //create a genesis block
                    Block genesisBlock = new Block("0");
                    genesisBlock.timestamp = 1662529884211L;
                    genesisBlock.mineBlock(3);
                    System.out.println(genesisBlock.hash);
                    sql.storeblock(genesisBlock);

                    // //seed node creating a block with 100 coins
                    // To use the below code please comment processTransaction in addTransaction
                    // Block block = new Block(genesisBlock.hash);
                    // Transaction t = new Transaction(publicKey, publicKey, 100, null,100);
                    // ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
                    // outputs.add(new TransactionOutput(t.reciepient, t.value, t.transactionId,1));
                    // t.outputs = outputs;

                    // t.generateSignature(privateKey);
                    // block.addTransaction(t);
                    // block.mineBlock(3);
                    // sql.storeblock(block);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // keys in database
                if (rs.next()) {
                    publicKey = StringUtil.getPublicKeyFromString(rs.getString(3));
                    privateKey = StringUtil.getPrivateKeyFromString(rs.getString(2));
                    // System.out.println("pk gen"+publicKey);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        generateKeyPair();

        JFrame f = new JFrame("Wallet");

        JButton send = new JButton("Send");
        JLabel balance = new JLabel("Balance: "+sql.fetchBalance());
        JPanel addressLayout = new JPanel();
        addressLayout.setLayout(new BorderLayout());
        JPanel myAddressLayout = new JPanel();
        myAddressLayout.setLayout(new BorderLayout());
        JLabel myAddress_title = new JLabel("Your Address: ");
        JLabel myAddress = new JLabel(StringUtil.getStringFromKey(publicKey));
        myAddressLayout.add(myAddress_title, BorderLayout.WEST);
        myAddressLayout.add(myAddress, BorderLayout.EAST);

        JLabel to_title = new JLabel("To: ");
        JTextField to = new JTextField(45);
        JPanel toLayout = new JPanel();
        toLayout.setLayout(new BorderLayout());
        toLayout.add(to_title, BorderLayout.WEST);
        toLayout.add(to, BorderLayout.EAST);

        addressLayout.add(myAddressLayout, BorderLayout.NORTH);
        addressLayout.add(toLayout, BorderLayout.SOUTH);

        JPanel sendLayout = new JPanel();
        sendLayout.setLayout(new BorderLayout());
        JPanel amountLayout = new JPanel();
        amountLayout.setLayout(new BorderLayout());
        JLabel amount_title = new JLabel("Amount: ");
        JTextField amount = new JTextField(5);
        amountLayout.add(amount_title, BorderLayout.WEST);
        amountLayout.add(amount, BorderLayout.EAST);

        sendLayout.add(amountLayout, BorderLayout.NORTH);
        sendLayout.add(send, BorderLayout.SOUTH);

        // sendBox.add(addressLayout, BorderLayout.NORTH);
        // sendBox.add(sendLayout, BorderLayout.SOUTH);

        // SEND
        send.addActionListener(l -> {
            System.out.println("Updating balance");
            balance.setText("Balance: "+sql.fetchBalance());
            String toAddress = to.getText();
            if (toAddress.equals("")){
                return;
            }
            float amountToSend = Float.parseFloat(amount.getText());
            PublicKey fromkey = publicKey;
            PublicKey tokey = StringUtil.getPublicKeyFromString(toAddress);
            sql.createTransaction(fromkey, tokey, amountToSend);
        });

        // setup GUI
        f.setSize(800, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(addressLayout);
        f.getContentPane().add(sendLayout);
        f.getContentPane().add(balance);
        // f.getContentPane().add(sendBox,BorderLayout.NORTH);

        f.setVisible(true);

    }
}
