// reads from others and print it to console

import java.net.*;
import java.io.*;

class Read extends Thread {
	private BufferedReader in = null;
	private Socket user;
	public String ip;
	public Read(Socket server) {
		this.user = server;
		this.ip = server.getInetAddress().toString().replace("/","");
		System.out.println("Reading object initializing");
		try{
			this.in = new BufferedReader(new InputStreamReader(server.getInputStream()));
		}catch(IOException i){
			System.out.println(i);
		}
	}
	public void run(){
		try{
			while(true){
				String msg = in.readLine();
				if(msg.equals("close")){
					System.out.println("["+ip+"] has closed the connection");
					
					break;
				}
				Message message = new Message(msg);
				MessageHandler.addToMessagepool(message);
				System.out.println("Testing2");
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}
}
