package com.maxl.java.amikodesk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxl.java.shared.Conditions;
import com.maxl.java.shared.User;

public class FileLoader {

	FileLoader() {
		//
	}
	
	public TreeMap<String, Conditions> loadIbsaConditions() {
		TreeMap<String, Conditions> map_conditions = new TreeMap<String, Conditions>();
		
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\ibsa_conditions.ser");
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER + "ibsa_conditions.ser");
			System.out.println("Loading ibsa_conditions.ser from default folder... " + encrypted_msg.length);
		} else {
			System.out.println("Loading ibsa_conditions.ser from app data folder..." + encrypted_msg.length);	
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			map_conditions = (TreeMap<String, Conditions>)FileOps.deserialize(plain_msg);
		}	
		
		return map_conditions;
	}
	
	public TreeMap<String, String> loadIbsaGlns() {
		TreeMap<String, String> map_glns = new TreeMap<String, String>();
		
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\ibsa_glns.ser");
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER+"ibsa_glns.ser");
			System.out.println("Loading ibsa_glns.ser from default folder...");
		} else {
			System.out.println("Loading ibsa_glns.ser from app data folder...");
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			map_glns = (TreeMap<String, String>)FileOps.deserialize(plain_msg);
		}		

		return map_glns;
	}
	
	
	/**
	 * Load gln to user map
	 * @return HashMap
	 */
	public HashMap<String, User> loadGlnCodes() {
		HashMap<String, User> user_map = new HashMap<String, User>();
		
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\gln_codes.ser");
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER + "gln_codes.ser");
			System.out.println("Loading gln_codes.ser from default folder...");
		} else {
			System.out.println("Loading gln_codes.ser from app data folder...");
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			user_map = (HashMap<String, User>)FileOps.deserialize(plain_msg);
		}	
		
		return user_map;
	}

	/**
	 * Fill list of authors / med owners
	 */
	public List<Author> loadAuthors() {		
		List<Author> list_of_authors = new ArrayList<Author>();		
		try {
			// Load encrypted files
			byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\authors.ami.ser");
			if (encrypted_msg!=null) {
				System.out.println("Loading authors.ami.ser from app data folder...");
			} else {
				encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER	+ "authors.ami.ser");
				System.out.println("Loading authors.ami.ser from default folder...");
			}
			// Decrypt and deserialize
			if (encrypted_msg != null) {
				Crypto crypto = new Crypto();
				byte[] serialized_bytes = crypto.decrypt(encrypted_msg);
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
				Map<String, Object> authorData = mapper.readValue(serialized_bytes, typeRef);
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, String>> authorList = (ArrayList<HashMap<String, String>>) authorData.get("authors");
				list_of_authors.clear();
				for (HashMap<String, String> al : authorList) {
					Author auth = new Author();
					auth.setName(al.get("name"));
					auth.setCompany(al.get("company"));
					auth.setEmail(al.get("email"));
					auth.setEmailCC(al.get("emailcc"));
					auth.setSalutation(al.get("salutation"));
					if (al.get("server") != null) {
						String s[] = al.get("server").split(";");
						if (s.length == 4) {
							auth.setS(s[0]);
							auth.setL(s[1]);
							auth.setP(s[2]);
							auth.setO(s[3]);
						}
					}
					list_of_authors.add(auth);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list_of_authors;
	}
}
