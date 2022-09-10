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
			// Scanner sc = new Scanner(System.in);
			// System.out.print("Enter the username of mysql: ");
			// String name = sc.nextLine();
			// System.out.print("Enter the password of mysql: ");
			// String pass = sc.nextLine();

			
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bitcoin","ravi","password");
			

			// if (con != null)			
			// 	System.out.println("Connected");		
			// else		
			// 	System.out.println("Not Connected");
			
			// con.close();
			// sc.close();
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
