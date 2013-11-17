package com.maxl.java.amikodesk;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPopupMenu;

public class SearchHistory {

	private int searchDepth;	
	private LinkedList<String> prevSearches;
	private JPopupMenu prevSearchMenu;
	
	public SearchHistory(int depth) {
		searchDepth = depth;
	}
	
	public void popMenu(int x, int y) {
		prevSearchMenu = new JPopupMenu();
		Iterator<String> it = prevSearches.iterator();
		while (it.hasNext()) {
			prevSearchMenu.add(it.next().toString());
		}
	}
}
