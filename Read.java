// reads from others and print it to console

import java.net.*;
import java.io.*;

class Read extends Thread {
	private BufferedReader in = null;
	private Socket user;
	public String ip;
	private int index;
	public Read(Socket server,int index) {
		this.index = index;
		this.user = server;
		this.ip = server.getInetAddress().toString().replace("/","");
		System.out.println("Reading object initializing");
		try{
			this.in = new BufferedReader(new InputStreamReader(server.getInputStream()));
		}catch(IOException i){
			i.printStackTrace();
		}
	}
	public void run(){
		try{
			while(true){
				String msg = in.readLine();
				
				if (msg == null) continue;
				if(msg.equals("close")){
					System.out.println("["+ip+"] has closed the connection");
					break;
				}
				if (msg.equals("connected")) {
					Server.ip[index] = ip;
					System.out.println("["+ip+"]"+" Connection was successfull");
					continue;
				}
				Message message = new Message(msg);
				MessageHandler.addToMessagepool(message);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
