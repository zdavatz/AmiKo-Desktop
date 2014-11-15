package com.maxl.java.amikodesk;

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
	
	static public String prettyFormat(float value) {
		return String.format("%,.2f", value);
	}
}
