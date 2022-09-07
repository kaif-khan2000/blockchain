
import java.sql.*;
import java.util.ArrayList;

import minibitcoin.*;

public class sql {

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

            if (rs > 0)
                System.out.println("Successfully Inserted");
            else
                System.out.println("Insert Failed");

            if(newBlock.prevHash.equals("0")){
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

    public static float fetchBalance(){
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
