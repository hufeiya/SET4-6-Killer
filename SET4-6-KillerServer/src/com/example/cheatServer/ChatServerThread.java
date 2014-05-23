package com.example.cheatServer;

//import java.awt.List;
import java.util.ArrayList;
import java.io.*; 
import java.net.*; 
import java.util.*; 

import com.example.cheatServer.ChatActivityServer.ChatThread;

public class ChatServerThread extends Thread{
	public ArrayList<Socket> clients = new ArrayList<Socket>();
	Socket s;
	ServerSocket server;
	@Override
	public void run(){
		
		try {
			server = new ServerSocket(12345);
			while(true)
			{
				System.out.println("Waiting...");
				//4.0之后联网不能写入主线程内
				/*
				Runnable  getTouch = new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							s = server.accept();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				new Thread(getTouch).start();
				*/
				s = server.accept();
				System.out.println("Starting!");
				clients.add(s);
				new ClientThread(s).start();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	class ClientThread extends Thread
	{
		Socket s;
		BufferedReader in;
		public ClientThread(Socket client) 
		{
			s = client;
			try 
			{
				in = new BufferedReader(new InputStreamReader(s.getInputStream(),"utf-8"));
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			try {
				String sentence = null;
				while((sentence = in.readLine()) != null)
				{
					for(Socket s : clients)
					{
						try {
							s.getOutputStream().write((sentence + "\n").getBytes("utf-8"));
							
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
		//
	
}

