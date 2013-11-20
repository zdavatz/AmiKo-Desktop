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

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;

public class WindowSaver implements AWTEventListener {

	private final static String PropFileName = "amiko_config.props";
	private static WindowSaver saver;
	private static String mAppDataFolder;
	private static String mDbPath;
	private Map frameMap;
	
	private WindowSaver() {
		frameMap = new HashMap();
		File prop_file = new File(mAppDataFolder + "\\" + PropFileName);
		Properties settings = new Properties();
		if (!prop_file.exists()) {
			System.out.println("No configuration file found... creating one.");
			try {
				settings.store(new FileOutputStream(mAppDataFolder + "\\" + PropFileName), null);
			} catch (IOException e) {
				System.out.println(e);
			}
		} else {
			System.out.println("Configuration file found... loading it.");
		}
	}
	
	public static WindowSaver getInstance(String app_data_folder) {
		mAppDataFolder = app_data_folder;		
		if (saver==null) {
			saver = new WindowSaver();
		}
		return saver;
	}
	
	public static String getDbPath() {
		return mDbPath;
	}
	
	public static void setDbPath(String db_path) {
		mDbPath = db_path;
		try {
			saveSettings();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void eventDispatched(AWTEvent evt) {
		try {
			if (evt.getID()==WindowEvent.WINDOW_OPENED) {
				// Listen to WINDOW_OPEN events and retrieve reference to JFrame				
				ComponentEvent cev = (ComponentEvent)evt;
				if (cev.getComponent() instanceof JFrame) {
					JFrame frame = (JFrame)cev.getComponent();
					loadSettings(frame);
				}
			} else if (evt.getID()==WindowEvent.WINDOW_CLOSING) {
				// Listen to window close events ...
				ComponentEvent cev = (ComponentEvent)evt;
				if (cev.getComponent() instanceof JFrame) {
					saveSettings();
				}			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static int getInt(Properties props, String name, int value) {
		String v = props.getProperty(name);
		if (v==null) {
			return value;
		}
		return Integer.parseInt(v);
	}
	
	private static String getString(Properties props, String name, String value) {
		String v = props.getProperty(name);
		if (v==null) {
			return value;
		}
		return v;
	}
	
	public static void loadSettings(JFrame frame) throws IOException {
		File prop_file = new File(mAppDataFolder + "\\" + PropFileName);
		if (prop_file.exists()) {
			Properties settings = new Properties();			
			settings.load(new FileInputStream(mAppDataFolder + "\\" + PropFileName));
			String name = frame.getName();
	        // Default dbpath is ""
	        mDbPath = getString(settings, name + ".db", "");
	        saver.frameMap.put(name, mDbPath);
	        // Calculate default screen position...
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	        int x0 = (screen.width-1024)/2;
	        int y0 = (screen.height-768)/2;	        
			int x = getInt(settings, name + ".x", x0);
			int y = getInt(settings, name + ".y", y0);
			int w = getInt(settings, name + ".w", 1024);
			int h = getInt(settings, name + ".h", 768);
			frame.setLocation(x,y);
			frame.setSize(new Dimension(w,h));
			saver.frameMap.put(name, frame);
			frame.validate();
		}
	}
	
	public static void saveSettings() throws IOException {
		Properties settings = new Properties();
		File prop_file = new File(mAppDataFolder + "\\" + PropFileName);
		if (prop_file.exists())
			settings.load(new FileInputStream(mAppDataFolder + "\\" + PropFileName));		
		Iterator it = saver.frameMap.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
            JFrame frame = (JFrame)saver.frameMap.get(name);            
            settings.setProperty(name + ".db", mDbPath);            
            settings.setProperty(name + ".x", "" + frame.getX());    
            settings.setProperty(name + ".y", "" + frame.getY());    
            settings.setProperty(name + ".w", "" + frame.getWidth());    
            settings.setProperty(name + ".h", "" + frame.getHeight());
        } 
        settings.store(new FileOutputStream(mAppDataFolder + "\\" + PropFileName), null); 
	}
}
