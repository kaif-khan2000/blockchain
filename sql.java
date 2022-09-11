
import java.net.Socket;
import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;


public class sql {

    public static float checkUTXOExist(String transactionOutputId) {
        try {
            Statement stmt = db.con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from tran_output where utxo = 1 and tranoutput_id = '" + transactionOutputId + "'");
            if (rs.next()) {
                return rs.getFloat("value");
            }
            stmt.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return -1;
    }

    public static void createTransaction (PublicKey fromkey, PublicKey tokey, float amount) {
        String key = StringUtil.getStringFromKey(fromkey);
        //fetch tran_output such that their utxo == 1 and their address == fromkey and their amount >= amount
        try{
            ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM tran_output WHERE utxo = 1 AND address = '"+key+"' order by value asc;");
            float tempAmount = 0;
            while(rs.next()){
                tempAmount += rs.getFloat("value");
                TransactionInput in = new TransactionInput(rs.getString("tranoutput_id"));
                inputs.add(in);
                if(tempAmount >= amount){
                    break;
                }
            }
            if (tempAmount < amount){
                System.out.println("Not enough funds to send transaction. Transaction Discarded.");
                return;
            }
            for (TransactionInput input : inputs) {
                st.executeUpdate("UPDATE tran_output SET utxo = 0 WHERE tranoutput_id = '"+input.transactionOutputId+"';");
            }

            Transaction tr = new Transaction(fromkey, tokey, amount, inputs, tempAmount);
            tr.generateSignature(Wallet.privateKey);
            tr.processTransaction();
            MessageHandler.mempool.add(tr);
            Message msg = new Message(2, tr.toString());
            Server.broadcast(msg);
        }  catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<TransactionInput> fetchInputs(String t_id){
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
        //write code here
        try {
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM tran_input where transaction_id = '" + t_id + "';");
            while (rs.next()) {
                String tran_output = rs.getString("tran_outputid");//TransactionInput table have reference to transactionoutput table
                //Searching for tranoutput_id in tran_output table to UTXO which is TransactionOutput object
                ResultSet rsin = st.executeQuery("SELECT * FROM tran_output where tranoutput_id = '" + tran_output + "';");
                if(rsin.next()) {
                    PublicKey address = StringUtil.getPublicKeyFromString(rsin.getString("address"));
                    Float value = Float.parseFloat(rsin.getString("value"));
                    String transaction_id = rsin.getString("transaction_id");
                    TransactionOutput to = new TransactionOutput(address, value, transaction_id);
                    TransactionInput tr = new TransactionInput(tran_output,value,to);
                    inputs.add(tr);
                }
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inputs;
    }
    public static ArrayList<TransactionOutput> fetchOutputs(String t_id){
        ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
        //write code here
        try {
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM tran_output where transaction_id = '" + t_id + "';");
            while (rs.next()) {
                PublicKey address = StringUtil.getPublicKeyFromString(rs.getString("address"));
                Float value = Float.parseFloat(rs.getString("value"));
                String transaction_id = rs.getString("transaction_id");
                TransactionOutput to = new TransactionOutput(address, value, transaction_id);
                outputs.add(to);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return outputs;
    } 
    public static ArrayList<Transaction> fetchTransactions(String block_id) {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        try {
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM transaction where block_id = '" + block_id + "';");
            while (rs.next()) {
                String transaction_id = rs.getString("transaction_id");
                String senderPk = rs.getString("publickey_sender");
                String receiverPk = rs.getString("publickey_receiver");
                String value = rs.getString("value");
                String signature = rs.getString("signature");
                Transaction tr = new Transaction();
                tr.sender = StringUtil.getPublicKeyFromString(senderPk);
                tr.reciepient = StringUtil.getPublicKeyFromString(receiverPk);
                tr.signature = signature.getBytes();
                tr.value = Float.parseFloat(value);
                tr.transactionId = transaction_id;
                
                tr.inputs = fetchInputs(transaction_id);
                tr.outputs = fetchOutputs(transaction_id);

                transactions.add(tr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public static Block createBlock(String hash) {
        Block block = new Block();
        try {
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM block WHERE hash = '" + hash + "';");
            if (rs.next()) {
                hash = rs.getString("hash");
                String prevHash = rs.getString("prevHash");
                String merkleRoot = rs.getString("merkletree");
                long timestamp = rs.getLong("timestamp");
                int nonce = rs.getInt("nonce");
                block.hash = hash;
                block.prevHash = prevHash;
                block.merkleRoot = merkleRoot;
                block.timestamp = timestamp;
                block.nonce = nonce;                
            }

            block.transactions = fetchTransactions(hash);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return block;
    }

    public static String getLastHash() {
        try {
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("select hash from lasthash order by id desc limit 1");
            while (rs.next()) {
                return rs.getString("hash");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void sendRemainingHash(Socket client,String hash){
        try{
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("Select hash from block where prevhash = '"+hash+"';"); 
            while(rs.next()){
                hash = rs.getString("hash");
                Block newBlock = createBlock(hash);
                Message msg = new Message(4,newBlock.toString());
                Server.sendMessage(client, msg);
                rs.close();
                rs = st.executeQuery("Select hash from block where prevhash = '"+hash+"';");
            }
            Message msg = new Message(5,"upto date.");
            Server.sendMessage(client, msg);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void storeblock(Block newBlock) {
        try {
            Connection conn = db.con;
            Statement st = conn.createStatement();
            int rs = st.executeUpdate("insert into block(hash,prevhash,merkletree,timestamp,nonce) values('"
                    + newBlock.hash + "','"
                    + newBlock.prevHash + "','"
                    + newBlock.merkleRoot + "','"
                    + String.valueOf(newBlock.timestamp) + "','"
                    + String.valueOf(newBlock.nonce) + "');");

            // update the lasthash table with the hash at id = 1
            rs = st.executeUpdate("update lasthash set hash = '" + newBlock.hash + "' where id = 1;");

            if (rs > 0)
                System.out.println("Successfully Inserted");
            else
                System.out.println("Insert Failed");

            if (newBlock.prevHash.equals("0")) {
                return;
            }
            for (Transaction transaction : newBlock.transactions) {
                MessageHandler.mempool.remove(transaction);
                rs = st.executeUpdate(
                        "insert into transaction(transaction_id, publickey_sender,publickey_receiver,value,signature,block_id) values "
                                +
                                // add transaction id here
                                "('" + transaction.transactionId +
                                "','" + StringUtil.getStringFromKey(transaction.sender) +
                                "','" + StringUtil.getStringFromKey(transaction.reciepient) +
                                "','" + transaction.value +
                                "','" + transaction.signature.toString() +
                                "','" + newBlock.hash + "');");
                if (transaction.inputs != null) {
                    for (TransactionInput input : transaction.inputs) {
                        rs = st.executeUpdate("insert into tran_input(transaction_id,transaction_output_id) values " +
                                "('" + transaction.transactionId.toString() +
                                "','" + input.transactionOutputId.toString() + "');");
                    }
                }
                for (TransactionOutput output : transaction.outputs) {
                    rs = st.executeUpdate("insert into tran_output(tranoutput_id,transaction_id, address, value, utxo) values " +
                            "('" + output.id +
                            "','" + transaction.transactionId +
                            "','" + StringUtil.getStringFromKey(output.reciepient) +
                            "','" + output.value +
                            "'," + "1" + "" +
                            ");");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static float fetchBalance() {
        try {
            Connection conn = db.con;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select sum(value) from tran_output where address = '"
                    + StringUtil.getStringFromKey(Wallet.publicKey) + "' and utxo = 1;");
            while (rs.next()) {
                return rs.getFloat(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static ArrayList<Transaction> fetchUTXO(int amount, String address) {
        ArrayList<Transaction> utxo = new ArrayList<Transaction>();
        try {
            Connection conn = db.con;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "select * from tran_output as o,transaction as p where t.transaction_id = o.transaction_id and address='"
                            + address + "' and utxo=1;");
            while (rs.next()) {
                Transaction transaction = new Transaction(new String("demo"));
                utxo.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utxo;
    }

    public static void dropDatabase() {
        Connection stmt = db.con;
        try {
            stmt.createStatement().execute("drop database if exists bitcoin");
            stmt.createStatement().execute("create database bitcoin");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        dropDatabase();
        Connection conn = db.con;
        try {
            Statement stmt = conn.createStatement();
            boolean rset;
            stmt.execute("use bitcoin;");
            rset = stmt.execute("create table lasthash(id int not null auto_increment,hash varchar(64),PRIMARY KEY (id),UNIQUE (hash));");
            rset = stmt.execute("insert into lasthash(hash) values('0');");
            rset = stmt.execute("create table wallet (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "privatekey varchar(500)," +
                    "publickey varchar(500)," +
                    "PRIMARY KEY (id)" +
                    ");");

            rset = stmt.execute("create table block (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "hash varchar(500)," +
                    "prevhash varchar(500)," +
                    "merkletree varchar(500)," +
                    "timestamp varchar(500)," +
                    "nonce int," +
                    "PRIMARY KEY (id)," +
                    "UNIQUE (hash)" +
                    ");");

            rset = stmt.execute("create table transaction (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "transaction_id varchar(500)," +
                    "publickey_sender varchar(500)," +
                    "publickey_receiver varchar(500)," +
                    "value int," +
                    "signature varchar(500)," +
                    "sequence int," +
                    "block_id varchar(500)," +
                    "PRIMARY KEY (id)," +
                    "UNIQUE(transaction_id)," +
                    "FOREIGN KEY (block_id) REFERENCES block(hash)" +
                    ");");

            rset = stmt.execute("create table tran_input (" +
                    "tran_outputid varchar(500)," +
                    "transaction_id varchar(500)," +
                    "FOREIGN KEY (transaction_id) REFERENCES transaction(transaction_id)" +
                    ");");

            rset = stmt.execute("create table tran_output (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "tranoutput_id varchar(500)," +
                    "transaction_id varchar(500)," +
                    "address varchar(500)," +
                    "value int," +
                    "utxo bool," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (transaction_id) REFERENCES transaction(transaction_id)" +
                    ");");
            System.out.println("Database created successfully..."+!rset);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
