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

public class Constants {
	public static final boolean DEBUG = true;
		
	public static String DB_LANGUAGE = "";	
	
	// German section title abbreviations
	public static final String[] SectionTitle_DE = { "Zusammensetzung",
			"Galenische Form", "Kontraindikationen", "Indikationen",
			"Dosierung/Anwendung", "Vorsichtsmassnahmen", "Interaktionen",
			"Schwangerschaft", "Fahrtüchtigkeit", "Unerwünschte Wirk.",
			"Überdosierung", "Eig./Wirkung", "Kinetik", "Präklinik",
			"Sonstige Hinweise", "Zulassungsnummer", "Packungen", "Inhaberin",
			"Stand der Information" };
	// French section title abbrevations
	public static final String[] SectionTitle_FR = { "Composition",
			"Forme galénique", "Contre-indications", "Indications",
			"Posologie", "Précautions", "Interactions", "Grossesse/All.",
			"Conduite", "Effets indésir.", "Surdosage", "Propriétés/Effets",
			"Cinétique", "Préclinique", "Remarques", "Numéro d'autorisation",
			"Présentation", "Titulaire", "Mise à jour" };
	
	public static final String DEFAULT_AMIKO_DB_BASE = "amiko_db_full_idx_";
	public static final String DEFAULT_AMIKO_REPORT_BASE = "amiko_report_";
	public static final String DEFAULT_INTERACTION_DB_BASE = "drug_interactions_idx_";
	public static final String DEFAULT_INTERACTION_CSV_BASE = "drug_interactions_csv_";
	public static final String AGBS_HTML = "agbs_";
	public static final String GLN_CODES_FILE = "gln_codes_csv.csv";
	public static final String DEFAULT_ROSE_DB = "rose_db_full.db";
	public static final String ROSE_DB_NEW = "rose_db_new_full.db";
	
	// Important folders and files
	public static final String IMG_FOLDER = "./images/";	
	public static final String JS_FOLDER = "./jscripts/";
	public static final String SHOP_FOLDER = "./modules/shop/";
	public static final String ROSE_FOLDER = "./modules/rose/";
	public static final String HTML_FILES = "./fis/fi_de_html/";
	public static final String CSS_SHEET = "./css/amiko_stylesheet.css";
	public static final String INTERACTIONS_SHEET = "./css/interactions_css.css";
	public static final String SHOPPING_SHEET = "./css/shopping_css.css";
	public static final String ROSE_SHEET = "./css/zurrose_css.css";

	// App names
	public static final String AMIKO_NAME = "AmiKo Desktop";
	public static final String COMED_NAME = "CoMed Desktop";	
	public static final String AMIKO_DESITIN_NAME = "AmiKo Desktop Desitin";
	public static final String COMED_DESITIN_NAME = "CoMed Desktop Desitin";
	public static final String AMIKO_MEDDRUGS_NAME = "med-drugs desktop";
	public static final String COMED_MEDDRUGS_NAME = "med-drugs-fr desktop";
	public static final String AMIKO_ZURROSE_NAME = "AmiKo Desktop ZR";
	public static final String COMED_ZURROSE_NAME = "CoMed Desktop ZR";
	public static final String AMIKO_IBSA_NAME = "AmiKo Desktop IBSA";
	public static final String COMED_IBSA_NAME = "CoMed Desktop IBSA";

	// App icons
	public static final String AMIKO_ICON = "./icons/amiko_icon.png";
	public static final String DESITIN_ICON = "./icons/desitin_icon.png";
	public static final String DESITIN_LOGO = "./images/desitin_logo.png";
	public static final String MEDDRUGS_ICON = "./icons/meddrugs_icon.png";
	public static final String MEDDRUGS_LOGO = "./images/meddrugs_logo.png";
	public static final String ZURROSE_ICON = "./icons/amiko_icon.png";
	public static final String IBSA_ICON = "./icons/ibsa_icon.png";
	public static final String IBSA_LOGO = "./icons/ibsa_logo.bmp";
	
	// -->> Note: uncomment name of app to compile!
	// public static final String APP_NAME = AMIKO_NAME;
	// public static final String APP_NAME = COMED_NAME;
	// public static final String APP_NAME = AMIKO_DESITIN_NAME;
	// public static final String APP_NAME = COMED_DESITIN_NAME;
	// public static final String APP_NAME = AMIKO_MEDDRUGS_NAME;
	// public static final String APP_NAME = COMED_MEDDRUGS_NAME;
	public static final String APP_NAME = AMIKO_ZURROSE_NAME;
	// public static final String APP_NAME = COMED_ZURROSE_NAME;
	// public static final String APP_NAME = AMIKO_IBSA_NAME;
	// public static final String APP_NAME = COMED_IBSA_NAME;
	public static final String APP_VERSION = "1.4.6 (32-bit)";	
	public static final String GEN_DATE = "25.01.2016";	
}
