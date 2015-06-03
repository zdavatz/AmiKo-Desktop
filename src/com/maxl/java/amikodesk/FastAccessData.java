package com.maxl.java.amikodesk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.maxl.java.shared.User;

public class FastAccessData {
	// List of navigable sets
	List<ArrayList<String>> list_of_lists;
	// List of users
	ArrayList<User> list_of_users;
	
	// Constructor
	public FastAccessData() {		
		list_of_lists = new ArrayList<ArrayList<String>>();
	}
	
	public void addUsers(ArrayList<User> list_of_users) {
		this.list_of_users = list_of_users;
	}
	
	public List<User> searchUser(String key) {
		String keys[] = key.split(",");
		ArrayList<User> ret_list = new ArrayList<User>();
		for (User u : list_of_users) {
			String s1 = u.first_name.toLowerCase();
			String s2 = u.last_name.toLowerCase();
			String s3 = u.name1.toLowerCase();
			String s4 = u.name2.toLowerCase();
			String s5 = u.zip.toLowerCase();
			String s6 = u.city.toLowerCase();
			String s7 = u.gln_code.toLowerCase();
			String s8 = u.ideale_id.toLowerCase();
			boolean found = true;
			for (int i=0; i<keys.length; ++i) {
				String k = keys[i].trim().toLowerCase();
				if (!s2.isEmpty() || !s3.isEmpty()) {
					if (s1.startsWith(k) || s2.startsWith(k) || s3.startsWith(k) || s4.startsWith(k)
							|| s5.startsWith(k) || s6.startsWith(k) || s7.startsWith(k) || s8.startsWith(k)) {
						found &= found;
					} else
						found = false;
				} else
					found = false;
			}
			if (found==true)
				ret_list.add(u);
		}
		return ret_list;
	}
	
	public void addList(ArrayList<String> list) {
		list_of_lists.add(list);
	}
	
	public List<String> search(String key) {
		ArrayList<String> ret_list = new ArrayList<String>();
		for (ArrayList<String> list : list_of_lists) {
			for (String s : list) {
				if (s.startsWith(key)) {
					ret_list.add(s);
				}
			}
		}
		return ret_list;
	}
	
	public List<String> betterSearch(String key) {
		Set<Integer> ret_index = new HashSet<Integer>();
		for (ArrayList<String> list : list_of_lists) {
			int index = 0;
			for (String s : list) {
				if (s.startsWith(key)) {
					ret_index.add(index);
				}
				index++;
			}
		}
		List<String> ret_list = new ArrayList<String>();
		for (Integer i : ret_index) {
			ret_list.add(formattedInfoAtIndex(i));
		}
		
		return ret_list;
	}
	
	public String formattedInfoAtIndex(int index) {
		return list_of_lists.get(0).get(index);
	}
}
