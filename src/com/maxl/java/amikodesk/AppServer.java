package com.maxl.java.amikodesk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class AppServer {

	private int mPort = 7777;
	private ServerSocket mServerSocket = null;
	
	public AppServer() {
	}
	
	/**
	 * Starts the AppServer on a given port
	 * @param port
	 */
	public void start(int port) {
		boolean listening = true;
		
		try {
			mPort = port;
			mServerSocket = new ServerSocket(port);
			System.out.println("Server ready on port: " + mPort);
		} catch (IOException e) {
			System.err.println("Could not listen on port " + mPort);
			System.exit(1);
		}
		
		while (listening) {
			try {
				new ConnectionRequestHandler(mServerSocket.accept()).run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops and closes the AppServer
	 */
	public void stop() {
		try {
			mServerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Class to handle all requests sent to server
	 * @author Max
	 *
	 */
	public class ConnectionRequestHandler implements Runnable {
		private Socket mSocket = null;
		private BufferedReader mInput = null;
		
		public ConnectionRequestHandler(Socket socket) {
			mSocket = socket;
		}
		
		public void run() {
			System.out.println("Client connected to socket: " + mSocket.toString());
			
			try {
				mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			} finally { 
				//In case anything goes wrong we need to close our I/O streams and sockets
				try {
					mInput.close();
					mSocket.close();
				} catch(Exception e) { 
					System.out.println("Couldn't close I/O streams");
				}
			}
		}
	}	
}

