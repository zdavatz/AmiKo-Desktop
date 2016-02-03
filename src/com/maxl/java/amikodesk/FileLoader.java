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
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	public TreeMap<String, TreeMap<String, Float>> loadDesitinConditions() {
		TreeMap<String, TreeMap<String, Float>> map_conditions = new TreeMap<String, TreeMap<String, Float>>();
		
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\desitin_conditions.ser");
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER + "desitin_conditions.ser");
			System.out.println("Loading desitin_conditions.ser from default folder... " + encrypted_msg.length);
		} else {
			System.out.println("Loading desitin_conditions.ser from app data folder..." + encrypted_msg.length);	
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			map_conditions = (TreeMap<String, TreeMap<String, Float>>)FileOps.deserialize(plain_msg);
			
			/** TEST */
			/*
			for (Map.Entry<String, TreeMap<String, Float>> entry : map_conditions.entrySet()) {
				// System.out.println("customer gln = " + entry.getKey());
				for (Map.Entry<String, Float> entry2 : entry.getValue().entrySet()) {
					System.out.println("article ean = " + entry2.getKey() + " -> rebate = " + entry2.getValue());
				}
				if (entry.getKey().equals("7601001297500")) {
					System.out.println("Found customer " + entry.getKey());
					for (Map.Entry<String, Float> entry2 : entry.getValue().entrySet()) {
						System.out.println("ean = " + entry2.getKey() + " -> rebate = " + entry2.getValue());
					}
				}
			}
			*/
		}	
		
		return map_conditions;
	}
		
	@SuppressWarnings("unchecked")
	public TreeMap<String, String> loadIbsaGlns() {
		TreeMap<String, String> map_glns = new TreeMap<String, String>();
		
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\ibsa_glns.ser");
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER + "ibsa_glns.ser");
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
	@SuppressWarnings("unchecked")
	public HashMap<String, User> loadGlnCodes(String ser_file_name) {
		HashMap<String, User> user_map = new HashMap<String, User>();
		
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\" + ser_file_name);	// "gln_codes.ser"
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER + ser_file_name);
			System.out.println("Loading " + ser_file_name + " from default folder... " + encrypted_msg.length);
		} else {
			System.out.println("Loading " + ser_file_name + " from app data folder... " + encrypted_msg.length);
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			user_map = (HashMap<String, User>)FileOps.deserialize(plain_msg);
			
			/*
			for (Map.Entry<String, User> entry : user_map.entrySet()) {
				String gln = entry.getKey();
				User user = entry.getValue();
				System.out.println(user.gln_code + " - " + user.category + ", " 
						+ user.title + ", " + user.first_name + ", " + user.last_name + ", " 
						+ user.name1 + ", " + user.name2 + ", " + user.name3 + ", "
						+ user.city + ", " + user.street);
			}
			*/
		}	
		
		return user_map;
	}

	/**
	 * Loads Rose users
	 * Format: gln code -> user
	 * @param csv_file_name
	 * @return user map
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, User> loadRoseUserMap(String ser_file_name) {
		HashMap<String, User> user_map = new HashMap<String, User>();
		
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\" + ser_file_name);	
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.ROSE_FOLDER + ser_file_name);
			System.out.println("Loading " + ser_file_name + " from default folder... " + encrypted_msg.length);
		} else {
			System.out.println("Loading " + ser_file_name + " from app data folder... " + encrypted_msg.length);
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			user_map = (HashMap<String, User>)FileOps.deserialize(plain_msg);
		
			/*
			// Test
			if (Constants.DEBUG) {
				HashMap<String, Float> reb_map = user_map.get("7601000600356").rebate_map;
				if (reb_map != null) {
					System.out.println("rebate map");
					for (Map.Entry<String, Float> m : reb_map.entrySet()) {
						System.out.println(m.getKey() + " -> " + m.getValue());
					}
				}
				HashMap<String, Float> expenses_map = user_map.get("7601000600356").expenses_map;
				if (expenses_map != null) {
					System.out.println("expenses map");
					for (Map.Entry<String, Float> m : expenses_map.entrySet()) {
						System.out.println(m.getKey() + " -> " + m.getValue());
					}
				}
			}
			*/
		}
		
		return user_map;
	}
	
	/**
	 * Loads Rose sales figures
	 * Format: pharma code -> sales figure
	 * @param csv_file_name
	 * @return sales figures map
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Float> loadRoseSalesFigures(String ser_file_name) {
		HashMap<String, Float> sales_figures_map = new HashMap<String, Float>();

		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\" + ser_file_name);	
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.ROSE_FOLDER + ser_file_name);
			System.out.println("Loading " + ser_file_name + " from default folder... " + encrypted_msg.length);
		} else {
			System.out.println("Loading " + ser_file_name + " from app data folder... " + encrypted_msg.length);
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			sales_figures_map = (HashMap<String, Float>)FileOps.deserialize(plain_msg);
		}
		
		return sales_figures_map;
	}
	
	/**
	 * 
	 * @param ser_file_name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> loadRoseAutoGenerika(String ser_file_name) {
		ArrayList<String> auto_generika_list = new ArrayList<String>();

		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\" + ser_file_name);	
		if (encrypted_msg==null) {
			encrypted_msg = FileOps.readBytesFromFile(Constants.ROSE_FOLDER + ser_file_name);
			System.out.println("Loading " + ser_file_name + " from default folder... " + encrypted_msg.length);
		} else {
			System.out.println("Loading " + ser_file_name + " from app data folder... " + encrypted_msg.length);
		}
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			auto_generika_list = (ArrayList<String>)FileOps.deserialize(plain_msg);
		}
		
		return auto_generika_list;
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
							// System.out.println(s[0] + " -> " + s[3]);
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
