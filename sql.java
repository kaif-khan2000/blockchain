
import java.sql.*;

public class sql {
    public static void main(String[] args)
    {
        db obj = new db();
        Connection conn=obj.con;
        try {
            Statement stmt = conn.createStatement();
            // boolean rset = stmt.execute("CREATE DATABASE bitcoin;");//Query
            boolean rset = stmt.execute("use bitcoin;");
            rset = stmt.execute("create table wallet ("+
                "id int NOT NULL AUTO_INCREMENT,"+
                "privatekey varchar(1000),"+
                "publickey varchar(1000),"+
                "PRIMARY KEY (id)"+
            ");");

            rset = stmt.execute("create table transaction ("+
                "id int NOT NULL AUTO_INCREMENT,"+
                "transaction_id varchar(1000),"+
                "publickey_sender varchar(1000),"+
                "publickey_receiver varchar(1000),"+
                "value int,"+
                "signature varchar(1000),"+
                "sequence int,"+
                "PRIMARY KEY (id)"+
            ");");

            rset = stmt.execute("create table tran_input ("+
                "tran_outputid varchar(1000),"+
                "value int,"+
                "transaction_id varchar(1000)"+
            ");");

            rset = stmt.execute("create table tran_output ("+
                "id int NOT NULL AUTO_INCREMENT,"+
                "receiver varchar(1000),"+
                "value int,"+
                "transaction_id varchar(1000),"+
                "utxo bool,"+
                "PRIMARY KEY (id)"+
            ");");

            rset = stmt.execute("create table block ("+
                "id int NOT NULL AUTO_INCREMENT,"+
                "hash varchar(1000),"+
                "prevhash varchar(1000),"+
                "merkletree varchar(1000),"+
                "timestamp varchar(1000),"+
                "nonce int,"+
                "PRIMARY KEY (id)"+
            ");");
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // if (obj.con != null)			
        //     System.out.println("Connected");		
        // else		
        //     System.out.println("Not Connected");
    }
}


