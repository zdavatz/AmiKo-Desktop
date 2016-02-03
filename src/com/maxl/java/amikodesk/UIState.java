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

public class UIState {

	static String use_mode = "aips";
	static String prev_use_mode = "aips";
	static String database_used = "aips";
	static int query_type = 0;
	static int med_index = -1;
	static boolean seek_interactions = false;
	static boolean shopping_mode = false;
	static boolean compare_mode = false;
	
	public UIState(String use) {		
		use_mode = use;
		if (use_mode.equals("aips")) {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = false;			
		} else if (use_mode.equals("favorites")) {
			database_used = "favorites";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = false;			
		} else if (use_mode.equals("interactions")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = true;
			shopping_mode = false;
			compare_mode = false;		
		} else if (use_mode.equals("shopping") || use_mode.equals("loadcart") || use_mode.equals("assortlist")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = false;
			shopping_mode = true;
			compare_mode = false;				
		} else if (use_mode.equals("comparison")) {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = true;				
		} else {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = false;			
		}
	}

	public void setUseMode(String use) {
		prev_use_mode = use_mode;
		use_mode = use;
		if (use_mode.equals("aips")) {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = false;		
		} else if (use_mode.equals("favorites")) {
			database_used = "favorites";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = false;				
		} else if (use_mode.equals("interactions")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = true;
			shopping_mode = false;
			compare_mode = false;				
		} else if (use_mode.equals("shopping") || use_mode.equals("loadcart") || use_mode.equals("assortlist")) {
			database_used = "aips";		// Default DB choice
			seek_interactions = false;
			shopping_mode = true;
			compare_mode = false;				
		} else if (use_mode.equals("comparison")) {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = true;				
		} else {
			database_used = "aips";
			seek_interactions = false;
			shopping_mode = false;
			compare_mode = false;			
		}
	}
	
	public void restoreUseMode() {
		use_mode = prev_use_mode;
	}
	
	public String getPrevUseMode() {
		return prev_use_mode;
	}
	
	public String getUseMode() {
		return use_mode;
	}
	
	public boolean isSearchMode() {
		return (!seek_interactions && !shopping_mode && !compare_mode);
	}
	
	public boolean isInteractionsMode() {
		return seek_interactions;
	}
	
	public boolean isShoppingMode() {
		return shopping_mode;
	}
	
	public boolean isRoseShoppingMode() {
		return (shopping_mode && Utilities.isRoseShoppingApp());
	}
	
	public boolean isLoadCart() {
		return use_mode.equals("loadcart");
	}
	
	public boolean isAssortList() {
		return use_mode.equals("assortlist");
	}
	
	public boolean isComparisonMode() {
		return compare_mode;
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
	
	boolean isCustomerSearchMode() {
		return (query_type==5);
	}
	
	public void setMedIndex(int index) {
		med_index = index;
	}
}
