package com.maxl.java.amikodesk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class Utilities {

	static public String appDataFolder() {
		return (System.getenv("APPDATA") + "\\Ywesee\\" + Constants.APP_NAME);
	}
	
	static public String appLanguage() {
		if (Constants.DB_LANGUAGE.equals("DE"))
			return "de";
		else if (Constants.DB_LANGUAGE.equals("FR"))
			return "fr";
		else if (Constants.APP_NAME.equals(Constants.AMIKO_NAME) || Constants.APP_NAME.equals(Constants.AMIKO_DESITIN_NAME) 
				|| Constants.APP_NAME.equals(Constants.AMIKO_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.AMIKO_ZURROSE_NAME)) {
			return "de";
		} else if (Constants.APP_NAME.equals(Constants.COMED_NAME) || Constants.APP_NAME.equals(Constants.COMED_DESITIN_NAME) 
				|| Constants.APP_NAME.equals(Constants.COMED_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.COMED_ZURROSE_NAME)) {
			return "fr";
		}
		return "";
	}

	static public String appCustomization() {		
		if (Constants.APP_NAME.equals(Constants.AMIKO_NAME) || Constants.APP_NAME.equals(Constants.COMED_NAME)) {
			return "ywesee";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_DESITIN_NAME) || Constants.APP_NAME.equals(Constants.COMED_DESITIN_NAME)) {
			return "desitin";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.COMED_MEDDRUGS_NAME)) {
			return "meddrugs";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_ZURROSE_NAME) || Constants.APP_NAME.equals(Constants.COMED_ZURROSE_NAME)) {
			return "zurrose";
		}
		return "";
	}
	
	static public String readFromFile(String filename) {
		String file_str = "";		
        try {
        	FileInputStream fis = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
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

	static public void writeToFile(String string_to_write, String dir_name, String file_name) 
			throws IOException {
	   	File wdir = new File(dir_name);
	   	if (!wdir.exists())
	   		wdir.mkdirs();
		File wfile = new File(dir_name+file_name);
		if (!wfile.exists())
			wfile.createNewFile();
		// FileWriter fw = new FileWriter(wfile.getAbsoluteFile());
		// Used to be UTF-8 --> does not work (@maxl: 08/Jun/2013)
	   	CharsetEncoder encoder = Charset.forName("UTF-16").newEncoder();
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
