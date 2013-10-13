/*
Copyright (c) 2013 Max Lungarella <cybrmx@gmail.com>

This file is part of AmiKoDesk for Windows.

AmiKoDesk is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

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
                if (isStopped()) {
                    System.out.println("Server stopped.") ;
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
            throw new RuntimeException("Cannot open port " + mServerPort, e);
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
					// The following line will have to be changed...
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
