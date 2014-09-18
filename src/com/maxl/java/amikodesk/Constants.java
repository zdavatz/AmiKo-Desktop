package com.maxl.java.amikodesk;

public class Constants {
	public static final boolean DEBUG = true;
		
	public static String DB_LANGUAGE = "";	
	
	public static final String DEFAULT_AMIKO_DB_BASE = "amiko_db_full_idx_";
	public static final String DEFAULT_AMIKO_REPORT_BASE = "amiko_report_";
	public static final String DEFAULT_INTERACTION_DB_BASE = "drug_interactions_idx_";
	public static final String DEFAULT_INTERACTION_CSV_BASE = "drug_interactions_csv_";
	public static final String GLN_CODES_FILE = "gln_codes_csv.csv";
	
	// Important folders
	public static final String IMG_FOLDER = "./images/";	
	public static final String HTML_FILES = "./fis/fi_de_html/";
	public static final String CSS_SHEET = "./css/amiko_stylesheet.css";
	public static final String JS_FOLDER = "./jscripts/";
	public static final String INTERACTIONS_SHEET = "./css/interactions_css.css";	

	// App names
	public static final String AMIKO_NAME = "AmiKo Desktop";
	public static final String COMED_NAME = "CoMed Desktop";	
	public static final String AMIKO_DESITIN_NAME = "AmiKo Desktop Desitin";
	public static final String COMED_DESITIN_NAME = "CoMed Desktop Desitin";
	public static final String AMIKO_MEDDRUGS_NAME = "med-drugs desktop";
	public static final String COMED_MEDDRUGS_NAME = "med-drugs-fr desktop";
	public static final String AMIKO_ZURROSE_NAME = "AmiKo Desktop ZR";
	public static final String COMED_ZURROSE_NAME = "CoMed Desktop ZR";

	// App icons
	public static final String AMIKO_ICON = "./icons/amiko_icon.png";
	public static final String DESITIN_ICON = "./icons/desitin_icon.png";
	public static final String DESITIN_LOGO = "./images/desitin_logo.png";
	public static final String MEDDRUGS_ICON = "./icons/meddrugs_icon.png";
	public static final String MEDDRUGS_LOGO = "./images/meddrugs_logo.png";
	public static final String ZURROSE_ICON = "./icons/amiko_icon.png";
	
	// -->> Note: uncomment name of app to compile!
	public static final String APP_NAME = AMIKO_NAME;
	// public static final String APP_NAME = COMED_NAME;
	// public static final String APP_NAME = AMIKO_DESITIN_NAME;
	// public static final String APP_NAME = COMED_DESITIN_NAME;
	// public static final String APP_NAME = AMIKO_MEDDRUGS_NAME;
	// public static final String APP_NAME = COMED_MEDDRUGS_NAME;
	// public static final String APP_NAME = AMIKO_ZURROSE_NAME;
	// public static final String APP_NAME = COMED_ZURROSE_NAME;
	public static final String APP_VERSION = "1.2.6 (32-bit)";	
	public static final String GEN_DATE = "15.09.2014";	
}
