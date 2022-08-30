
import java.sql.*;
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

            for (Transaction transaction: newBlock.transactions){
                
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        db obj = new db();
        Connection conn = obj.con;
        try {
            Statement stmt = conn.createStatement();
            // boolean rset = stmt.execute("CREATE DATABASE bitcoin;");//Query
            boolean rset = stmt.execute("use bitcoin;");
            rset = stmt.execute("create table wallet (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "privatekey varchar(100)," +
                    "publickey varchar(100)," +
                    "PRIMARY KEY (id)" +
                    ");");

            rset = stmt.execute("create table block (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "hash varchar(100)," +
                    "prevhash varchar(100)," +
                    "merkletree varchar(100)," +
                    "timestamp varchar(100)," +
                    "nonce int," +
                    "PRIMARY KEY (id)," +
                    "UNIQUE (hash)" +
                    ");");
            
            rset = stmt.execute("create table transaction (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "transaction_id varchar(100)," +
                    "publickey_sender varchar(100)," +
                    "publickey_receiver varchar(100)," +
                    "value int," +
                    "signature varchar(100)," +
                    "sequence int," +
                    "block_id varchar(100)," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (block_id) REFERENCES block(hash)" +
                    ");");

            rset = stmt.execute("create table tran_input (" +
                    "tran_outputid varchar(100)," +
                    "value int," +
                    "transaction_id varchar(100)" +
                    ");");

            rset = stmt.execute("create table tran_output (" +
                    "id int NOT NULL AUTO_INCREMENT," +
                    "receiver varchar(100)," +
                    "value int," +
                    "transaction_id varchar(100)," +
                    "utxo bool," +
                    "PRIMARY KEY (id)" +
                    ");");


        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // if (obj.con != null)
        // System.out.println("Connected");
        // else
        // System.out.println("Not Connected");
    }
}
