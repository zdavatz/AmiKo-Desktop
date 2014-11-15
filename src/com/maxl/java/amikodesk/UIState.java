package com.maxl.java.amikodesk;

public class UIState {

	static String use_mode = "aips";
	static String prev_use_mode = "aips";
	static String database_used = "aips";
	static int query_type = 0;
	static int med_index = -1;
	static boolean seek_interactions = false;
	static boolean shopping_mode = false;
	
	public UIState(String use) {		
		use_mode = use;
		if (use_mode.equals("aips")) {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
		} else if (use_mode.equals("favorites")) {
			database_used = "favorites";
			seek_interactions = false;
			shopping_mode = false;
		} else if (use_mode.equals("interactions")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = true;
			shopping_mode = false;
		} else if (use_mode.equals("shopping") || use_mode.equals("loadcart")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = false;
			shopping_mode = true;	
		} else {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
		}
	}

	public void setUseMode(String use) {
		prev_use_mode = use_mode;
		use_mode = use;
		if (use_mode.equals("aips")) {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
		} else if (use_mode.equals("favorites")) {
			database_used = "favorites";
			seek_interactions = false;
			shopping_mode = false;
		} else if (use_mode.equals("interactions")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = true;
			shopping_mode = false;
		} else if (use_mode.equals("shopping") || use_mode.equals("loadcart")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = false;
			shopping_mode = true;
		} else {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
		}
	}
	
	public void restoreUseMode() {
		use_mode = prev_use_mode;
	}
	
	public String getUseMode() {
		return use_mode;
	}
	
	public boolean isSearchMode() {
		return (!seek_interactions && !shopping_mode);
	}
	
	public boolean isInteractionsMode() {
		return seek_interactions;
	}
	
	public boolean isShoppingMode() {
		return shopping_mode;
	}
	
	public boolean isLoadCart() {
		return use_mode.equals("loadcart");
	}
	
	public void setDatabaseUsed(String database) {
		database_used = database;
	}
	
	public String databaseUsed() {
		return database_used;
	}
	
	public void setQueryType(int type) {
		query_type = type;
	}
	
	public int getQueryType() {
		return query_type;
	}
	
	public void setMedIndex(int index) {
		med_index = index;
	}
}
