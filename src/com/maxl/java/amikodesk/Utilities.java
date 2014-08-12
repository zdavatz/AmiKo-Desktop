package com.maxl.java.amikodesk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

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
}
