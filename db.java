// Java program to illustrate
// Connecting to the Database

import java.sql.*;

public class db
{
	public static Connection con;
	public static int a=1;
	static {
		try
		{
			// Class.forName("oracle.jdbc.driver.OracleDriver");
			
			// Establishing Connection
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitcoin","kaif","ali@6666");

			// if (con != null)			
			// 	System.out.println("Connected");		
			// else		
			// 	System.out.println("Not Connected");
			
			// con.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public db()
	{
		
	}
}
