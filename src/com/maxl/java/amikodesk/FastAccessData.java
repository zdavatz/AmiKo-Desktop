package com.maxl.java.amikodesk;

import java.util.ArrayList;
import java.util.List;

public class FastAccessData {
	// List of navigable sets
	List<ArrayList<String>> list_of_lists;

	// Constructor
	public FastAccessData() {		
		list_of_lists = new ArrayList<ArrayList<String>>();
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
}
