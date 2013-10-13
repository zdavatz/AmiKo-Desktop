package com.maxl.java.amikodesk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AppServer implements Runnable {

	private int mServerPort = 7777;
	private ServerSocket mServerSocket = null;
	private boolean mIsStopped = false;
	private String mInput = null;
	private Thread mRunningThread = null;

	private static String _mInput = null;	
	
	public AppServer(int port) {
		mServerPort = port;
	}

	public void run() {
		synchronized(this) {
			mRunningThread = Thread.currentThread();
		}		
		openServerSocket();
		while (!mIsStopped) {
			Socket clientSocket = null;
			try {
				clientSocket = mServerSocket.accept();
			} catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);				
			}
			new Thread(new ConnectionRequestHandler(clientSocket)).start();
		}
	}

    private synchronized boolean isStopped() {
        return mIsStopped;
    }

    public synchronized void stop() {
        mIsStopped = true;
        try {
            mServerSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            mServerSocket = new ServerSocket(this.mServerPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 7777", e);
        }
    }	
    
	public synchronized String getInput() {
		if (_mInput != mInput) {
			_mInput = mInput;
			return mInput;
		}
		else 
			return "";
	}
	
    public class ConnectionRequestHandler implements Runnable {
    	
		private Socket mClientSocket = null;
		private BufferedReader mBufferedReader = null;
		private PrintWriter mPrintWriter = null;
		
		public ConnectionRequestHandler(Socket socket) {
			mClientSocket = socket;
		}
		
		public void run() {
			System.out.println("Client connected to socket: " + mClientSocket.toString());
			
			try {
				mBufferedReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
				mPrintWriter = new PrintWriter(mClientSocket.getOutputStream(), true);
				
				String input, output;
				
				while ((input = mBufferedReader.readLine()) != null) {
					mInput = input;
					output = "?";
					if (output != null) {
						mPrintWriter.println(output);
					} 
				}				
			} catch (IOException e) {
				e.printStackTrace();
			} finally { 
				//In case anything goes wrong we need to close our I/O streams and sockets
				try {
					mBufferedReader.close();
					mPrintWriter.close();
					mServerSocket.close();
				} catch(Exception e) { 
					System.out.println("Couldn't close I/O streams");
				}
			}
		}
	}	    
}

/**
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
		
		if (listening) {
			Socket mClientSocket = null;
			try {
				mClientSocket = mServerSocket.accept();
				// new ConnectionRequestHandler(mClientSocket).run();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			new Thread(new ConnectionRequestHandler(mClientSocket)).start();
		}
	}
	
	public void stop() {
		try {
			mServerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getInput() {
		if (_mInput != mInput) {
			_mInput = mInput;
			return mInput;
		}
		else 
			return "";
	}
	
 */

