package com.maxl.java.amikodesk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

public class ComparisonCart implements java.io.Serializable {

	private static ResourceBundle m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));

	private static Map<String, Article> m_comparison_basket = null;
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_str = null;	
	
	public ComparisonCart() {
		// Load javascripts
		m_jscripts_str = FileOps.readFromFile(Constants.JS_FOLDER + "shopping_callbacks.js");
		// Load shopping cart css style sheet
		m_css_str = "<style type=\"text/css\">" + FileOps.readFromFile(Constants.SHOPPING_SHEET) + "</style>";
		// 
		if (Utilities.appLanguage().equals("de"))
			m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));
		else if (Utilities.appLanguage().equals("fr"))
			m_rb = ResourceBundle.getBundle("amiko_fr_CH", new Locale("fr", "CH"));	
	}
	
	public String updateComparisonCartHtml(Map<String, Article> comparison_basket) {
		String atc_code = "";
		String atc_class = "";
		
		m_comparison_basket = comparison_basket;
		
		// Test sorting...
		sortCart(1);
		
		String basket_html_str = "<table id=\"Warenkorb\" width=\"99%25\">";
		if (m_comparison_basket!=null && m_comparison_basket.size()>0) {
			basket_html_str += "<tr style=\"background-color:lightgray;\">"
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"30%\"><b>Packung</b></td>"							
					+ "<td style=\"text-align:left;padding-bottom:8px;\"; width=\"30%\"><b>Lieferant</b></td>"			
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"10%\"><b>ATC Klasse</b></td>"					
					+ "<td style=\"text-align:right;padding-bottom:8px;\"; width=\"10%\"><b>Grösse</b></td>"
					+ "<td style=\"text-align:right;padding-bottom:8px;\"; width=\"10%\"><b>Stärke</b></td>"										
					+ "<td style=\"text-align:right;padding-bottom:8px;\"; width=\"10%\"><b>Lager</b></td>"															
					+ "<td style=\"text-align:right;padding-bottom:8px;\"; width=\"5%\"><b>RBP</b></td>"		
					+ "</tr>";

			int num_rows = 0;
			for (Map.Entry<String, Article> entry : m_comparison_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();			
				if (num_rows%2==0)
					basket_html_str += "<tr id=\"" + ean_code + "\" style=\"background-color:blanchedalmond;\">";
				else 
					basket_html_str += "<tr id=\"" + ean_code + "\" style=\"background-color:whitesmoke;\">";
				basket_html_str += "<td style=\"text-align:left;\">" + article.getPackTitle() + "</td>"
						+ "<td style=\"text-align:left;\">" + article.getSupplier() + "</td>"				
						+ "<td style=\"text-align:right;\">" + article.getAtcCode() + "</td>"						
						+ "<td style=\"text-align:right;\">" + article.getPackSize() + " " + article.getPackGalen() + "</td>"
						+ "<td style=\"text-align:right;\">" + article.getPackUnit() + "</td>"						
						+ "<td style=\"text-align:right;\">" + article.getItemsOnStock() + "</td>"												
						+ "<td style=\"text-align:right;\">" + String.format("%.2f",article.getExfactoryPriceAsFloat()) + "</td>";
				basket_html_str += "</tr>";
				if (atc_code.isEmpty())
					atc_code = article.getAtcCode();
				if (atc_class.isEmpty())
					atc_class = article.getAtcClass();
				num_rows++;
			}
		}

		String atc_html_str = "ATC Code: " + atc_code + " [" + atc_class + "]";				
		
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str+ "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_str + "</head>"
				+ "<body><div id=\"shopping\">"
				+ "<div id=\"buttons\">" + "</div>"
				+ "<div><p>" + atc_html_str + "</p></div>"
				+ "<div>" + basket_html_str + "<br /></div>"
				+ "</div></body></html>";		
		
		return m_html_str;
	}
	
	public void sortCart(int type) {
		Set<Entry<String, Article>> set = m_comparison_basket.entrySet();
		List<Entry<String, Article>> list = new ArrayList<Entry<String, Article>>(set);
		if (type==1) {
			Collections.sort(list, new Comparator<Entry<String, Article>>() {
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return Integer.valueOf(a1.getValue().getItemsOnStock())
							.compareTo(a2.getValue().getItemsOnStock());
				}
			});
		}
	}
}
