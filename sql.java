
import java.sql.*;
import java.util.ArrayList;

import minibitcoin.*;

public class sql {
    public static ArrayList<TransactionInput> fetchInputs(String t_id){
        ArrayList<TransactionInput> inputs = null;
        //write code here
        return inputs;
    }
    public static ArrayList<TransactionOutput> fetchOutputs(String t_id){
        ArrayList<TransactionOutput> outputs = null;
        //write code here
        return outputs;
    } 
    public static ArrayList<Transaction> fetchTransactions(String block_id) {
        ArrayList<Transaction> transactions = null;
        try {
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM transaction where block_id = '" + block_id + "';");
            String result = "";
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
        Block block = null;
        String delim = "@block";
        try {
            Statement st = db.con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM blocks WHERE hash = '" + hash + "';");
            String result = "";
            if (rs.next()) {
                hash = rs.getString("hash");
                String prevHash = rs.getString("prevHash");
                String merkleRoot = rs.getString("merkleRoot");
                long timestamp = rs.getLong("timestamp");
                int nonce = rs.getInt("nonce");
                result = hash + delim + prevHash + delim + merkleRoot + delim + Long.toString(timestamp) + delim
                        + Integer.toString(nonce);
            }

            block = new Block(result, 0);
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
                    rs = st.executeUpdate("insert into tran_output(transaction_id, address, value, utxo) values " +
                            "('" + transaction.transactionId +
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
        db obj = new db();
        Connection conn = obj.con;
        try {
            Statement stmt = conn.createStatement();
            boolean rset;
            stmt.execute("use bitcoin;");
            rset = stmt.execute("create table lasthash(id int not null auto_increment,hash varchar(64));");
            rset = stmt.execute("insetr into lasthash values('0');");
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
                    "transaction_id varchar(500)," +
                    "address varchar(500)," +
                    "value int," +
                    "utxo bool," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (transaction_id) REFERENCES transaction(transaction_id)" +
                    ");");
            System.out.println("Database created successfully...");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
