
import minibitcoin.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate the keypair
            keyGen.initialize(ecSpec, random); // used to get 256byte acceptable security level
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : Minichain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) { // if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id, UTXO); // add it to our list of unspent transactions.
                total += UTXO.value;
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey _recipient, float value) {
        if (getBalance() < value) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if (total > value)
                break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }

    public static void main(String[] args) {
        
        JFrame f = new JFrame("Wallet");
        
        JPanel sendBox = new JPanel();
        
        JButton send = new JButton("Send");

        JPanel addressLayout = new JPanel();
        addressLayout.setLayout(new BorderLayout());
        JPanel myAddressLayout = new JPanel();
        myAddressLayout.setLayout(new BorderLayout());
        JLabel myAddress_title = new JLabel("Your Address: ");
        JLabel myAddress = new JLabel();
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

        sendBox.add(addressLayout, BorderLayout.NORTH);
        sendBox.add(sendLayout, BorderLayout.SOUTH);

        //SEND
        send.addActionListener(l -> {
            String from = myAddress.getText();
            String toAddress = to.getText();
            float amountToSend = Float.parseFloat(amount.getText());
            ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
            inputs.add(new TransactionInput(from));
            PublicKey fromkey = StringUtil.getPublicKeyFromString(from);
            PublicKey tokey = StringUtil.getPublicKeyFromString(toAddress);
            Transaction tr = new Transaction(fromkey, tokey, amountToSend, inputs);
            Server.broadcast(new Message(2,tr.toString()));
        } );
        
        //setup GUI
        f.setSize(800, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new FlowLayout());       
        
        f.getContentPane().add(sendBox);
        
        f.setVisible(true);

    

    }
}
