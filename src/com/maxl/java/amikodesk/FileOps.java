/*
Copyright (c) 2014 Max Lungarella <cybrmx@gmail.com>

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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileOps {
	static public String readFromFile(String filename) {
		String file_str = "";		
        try {
        	FileInputStream fis = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                file_str += (line + "\n");
            }
            br.close();
        }
        catch (Exception e) {
        	System.err.println(">> Error in reading file");        	
        }
        
		return file_str;	
	}	
	
	static public void writeToFile(String string_to_write, String dir_name, String file_name, String encoding) 
			throws IOException {
	   	File wdir = new File(dir_name);
	   	if (!wdir.exists())
	   		wdir.mkdirs();
		File wfile = new File(dir_name+file_name);
		if (!wfile.exists())
			wfile.createNewFile();
		
		// FileWriter fw = new FileWriter(wfile.getAbsoluteFile());
		// Used to be UTF-8 --> does not work (@maxl: 08/Jun/2013)
	   	CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
	   	encoder.onMalformedInput(CodingErrorAction.REPORT);
	   	encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(wfile.getAbsoluteFile()), encoder);
		BufferedWriter bw = new BufferedWriter(osw);      			
		bw.write(string_to_write);
		bw.close();
	}		
	
	static public Map<String,String> readFromCsvToMap(String filename) {
		Map<String, String> map = new TreeMap<String, String>();
		try {
			File file = new File(filename);
			if (!file.exists()) 
				return null;
			FileInputStream fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				String token[] = line.split("\\|\\|");
				map.put(token[0] + "-" + token[1], token[2]);
			}
			br.close();
		} catch (Exception e) {
			System.err.println(">> Error in reading csv file");
		}
		
		return map;
	}
			
	static public byte[] readBytesFromFile(String path) {
		File file = new File(path);
		if (!file.exists())
			return null;
		byte[] buf = new byte[(int)file.length()];
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(buf);
			dis.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return buf;
	}
	
	static public void writeBytesToFile(String path, byte[] buf) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(buf);
			fos.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	static public byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();	// new byte array
			ObjectOutputStream sout = new ObjectOutputStream(bout);		// serialization stream header
			sout.writeObject(obj);							// write object to serialied stream
			return (bout.toByteArray());
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public Object deserialize(byte[] byteArray) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(byteArray);
			ObjectInputStream sin = new ObjectInputStream(bin);
			return sin.readObject();
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public JFileChooser getFileChooser(String title, String filt, String descr) {
		JFileChooser fc = new JFileChooser();
		final String filter = filt;
		final String description = descr;
		fc.setDialogTitle(title);
		if (fc!=null) {
			fc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return (f.getName().toLowerCase().endsWith(filter));
				}
				public String getDescription() {
					return description;
				}
			});
		}
		return fc;
	}
}
