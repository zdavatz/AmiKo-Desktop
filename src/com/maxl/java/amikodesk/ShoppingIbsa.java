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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import com.maxl.java.shared.Conditions;

public class ShoppingIbsa extends ShoppingCart implements java.io.Serializable {
	
	private static TreeMap<String, Conditions> m_map_ibsa_conditions = null;
	private static Map<String, String> m_map_ibsa_glns = null;
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_shopping_cart_str = null;		
	private static String m_images_dir = "";
	
	private static char m_user_class;
	
	private static Preferences m_prefs;
	
    private boolean isMuster(Article article, float cr) {
    	return (article.getBuyingPrice(cr)<=0.0f 
    			&& (article.getSellingPrice()>0.0f || article.getPublicPriceAsFloat()>0.0f));
    }
	
	private boolean checkForArticle(String n) {
		String name = n.toLowerCase();
		return (name.contains("festimon") ||
			name.contains("merional") ||
			name.contains("choriomon") ||
			name.contains("prolutex"));
	}
	
	private boolean checkIfSponsor(String a) {
		String author = a.toLowerCase();
		return (author.contains("ibsa") || 
				author.contains("desitin"));
	}
    
    private void updateAssortedCart() {
    	// Loop through the complete list
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			String ean_code = article.getEanCode();
			if (m_map_ibsa_conditions.containsKey(ean_code)) {
				// Get assort list for this particular ean_code
				List<String> assort_list = getAssortList(ean_code);
				int assort_qty = 0;
				if (assort_list!=null) {
					for (String al : assort_list) {
						// Check if ean codes is kosher!
						if (al.matches("[\\d]{13}")) {
							if (m_shopping_basket.containsKey(al)) {
								assort_qty += m_shopping_basket.get(al).getQuantity();
							}
						}
					}
				}
				article.setAssortedQuantity(assort_qty);
			}
		}
    }
	
    private int totQuantity() {
		int qty = 0;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			qty += article.getQuantity();
		}
		return qty;
	}	

	private int getDraufgabe(Article article) {
		if (m_map_ibsa_conditions!=null) {
			String ean_code = article.getEanCode();
			// First make sure ean code is in conditions map
			if (m_map_ibsa_conditions.containsKey(ean_code)) {
				// Get rebate map
				NavigableMap<Integer, Float> rebate = getRebateMap(ean_code);
				if (rebate!=null && rebate.size()>0) {					
					int qty = article.getQuantity();
					int assorted_qty = qty + article.getAssortedQuantity();
					// If rebate map contains assorted quantity
					// then get bonus (draufgabe) 
					// otherwise get closest possible value using "floorKey"
					if (rebate.containsKey(assorted_qty)) {
						if (rebate.get(assorted_qty)>0)
							return (int)(qty*rebate.get(assorted_qty)/100.0f);
					} else {
						int floor_units = rebate.floorKey(assorted_qty);		
						return (int)(qty*rebate.get(floor_units)/100.0f);
					}
				}
			}
		}
		return 0;
	}
        
	private int totDraufgabe() {
		int draufgabe = 0;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			draufgabe += getDraufgabe(article);
		}
		return draufgabe;
	}
	
	/**
	 * This function returns either 
	 * - a cash rebate (which is represented as a negative number in the conditions file)
	 * - or the cash rebate closest to the chosen quantity 
	 */
	private float getCashRebate(Article article) {
		if (m_map_ibsa_conditions!=null) {
			String ean_code = article.getEanCode();
			// First make sure ean code is in conditions map
			if (m_map_ibsa_conditions.containsKey(ean_code)) {
				NavigableMap<Integer, Float> rebate = getRebateMap(ean_code);
				if (rebate!=null && rebate.size()>0) {
					int qty = article.getQuantity();
					int assorted_qty = qty + article.getAssortedQuantity();
					// If rebate map contains key 
					// then get bonus (draufgabe) 
					// otherwise get closest possible value using "floorKey"
					if (rebate.containsKey(assorted_qty)) {
						if (rebate.get(assorted_qty)<=0)
							return -rebate.get(assorted_qty);	// returns a quantity >=0.0
					} else {
						int floor_units = rebate.floorKey(assorted_qty);	
						if (rebate.get(floor_units)<=0)
							return -rebate.get(floor_units);
					}
				}
			}
		}
		// All other cases return 0.0
		return 0.0f;
	}
	
	private float getGrandTotalCashRebate() {
		// float sum_weighted_draufgabe = 0.0f;
		float sum_weighted_tot_quantity = 0.0f;
		float sum_weighted_cash_rebate = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			// sum_weighted_draufgabe += article.getBuyingPrice()*article.getDraufgabe();
			sum_weighted_tot_quantity += article.getBuyingPrice(0.0f)*(article.getDraufgabe()+article.getQuantity());		
			if (article.getDraufgabe()>0)
				sum_weighted_cash_rebate += article.getBuyingPrice(0.0f)*article.getQuantity();
			else {
				float cr = article.getCashRebate();
				sum_weighted_cash_rebate += article.getBuyingPrice(cr)*article.getQuantity();
			}
		}				
		if (sum_weighted_tot_quantity>0.0f)
			return (100.0f*(sum_weighted_tot_quantity-sum_weighted_cash_rebate)/sum_weighted_tot_quantity);
		else 
			return 0.0f;
	}
	
	private float totBuyingPrice() {
		float tot_buying = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			tot_buying += article.getQuantity()*article.getBuyingPrice(0.0f);
		}
		return tot_buying;
	}
	
    private float totSellingPrice() {
		float tot_selling = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			tot_selling += article.getQuantity()*article.getSellingPrice();
		}
		return tot_selling;
    }
	
	private TreeMap<Integer, Float> getRebateMap(String ean_code) {
		TreeMap<Integer, Float> rebate_map = null;
		String gln_code = m_customer_gln_code.isEmpty() ? m_prefs.get("glncode", "7610000000000") : m_customer_gln_code;

		if (m_map_ibsa_glns.containsKey(gln_code)) {
			// Extract user class (group in case of pharmacies)
			String uc[] = m_map_ibsa_glns.get(gln_code).split(";");
			// This is the default class
			char user_class = uc[0].charAt(0);			
			m_user_class = user_class;
			if (uc.length>1)
				m_user_class = uc[1].charAt(0);
			// Is the user human or corporate?
			String user_type = m_prefs.get("type", "arzt");
			// System.out.println("Category for GLN " + gln_code + ": " + user_class + "-" + user_type + " / " + m_map_ibsa_glns.get(gln_code));
			// Get rebate conditions
			Conditions c = m_map_ibsa_conditions.get(ean_code);
			// Extract rebate conditions for particular doctor/pharmacy	
			if (user_type.equals("arzt")) {
				rebate_map = c.getDiscountDoctor(user_class);
			} else {
				if (user_type.equals("spital")) {
					rebate_map = c.getDiscountHospital(user_class);
				} else if (user_type.equals("apotheke")) {
					boolean is_promo_time = c.isPromoTime("pharmacy", user_class);
					rebate_map = c.getDiscountPharmacy(user_class, is_promo_time);
					if (rebate_map==null && uc.length>1) {	// Sanity check can't hurt...
						// This is the fall back class
						user_class = uc[1].charAt(0);	// Fallback!
						is_promo_time = c.isPromoTime("pharmacy", user_class);
						rebate_map = c.getDiscountPharmacy(user_class, is_promo_time);						
					}
				} else if (user_type.equals("drogerie")) {
					boolean is_promo_time = c.isPromoTime("drugstore", user_class);
					rebate_map = c.getDiscountDrugstore(user_class, is_promo_time);
				} else if (user_type.equals("grossist")) {
					rebate_map = c.getDiscountHospital(user_class);
				} else if (user_type.equals("wissenschaft") || user_type.equals("behörde")) {
					rebate_map = c.getDiscountHospital(user_class);
				}
			}
		}
		
		return rebate_map;
	}
    
	public ShoppingIbsa() {
		// Load javascripts
		m_jscripts_str = FileOps.readFromFile(Constants.JS_FOLDER + "shopping_callbacks.js");
		// Load shopping cart css style sheet
		m_css_shopping_cart_str = "<style type=\"text/css\">" + FileOps.readFromFile(Constants.SHOPPING_SHEET) + "</style>";
		// Preferences
		m_prefs = Preferences.userRoot().node(SettingsPage.class.getName());
		// Application data folder
		m_application_data_folder = Utilities.appDataFolder();
		//
		m_images_dir = System.getProperty("user.dir") + "/images/";	
		// 
		if (Utilities.appLanguage().equals("de"))
			m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));
		else if (Utilities.appLanguage().equals("fr"))
			m_rb = ResourceBundle.getBundle("amiko_fr_CH", new Locale("fr", "CH"));			
	}
	
	public void setMaps(Map<String, String> glns, TreeMap<String, Conditions> conditions) {
		m_map_ibsa_glns = glns;
		m_map_ibsa_conditions = conditions;
		
		/** Test
		for (Map.Entry<String, Conditions> entry : m_map_ibsa_conditions.entrySet()) {
			String gln = entry.getKey();
			Conditions c = entry.getValue();
			TreeMap<Integer, Float> reb = c.getDiscountDrugstore('B', false);
			if (reb!=null) {
				for (Map.Entry<Integer, Float> e2 : reb.entrySet()) {
					System.out.println(gln + " / " + e2.getKey() + " -> " + e2.getValue());
				}
			} else {
				// System.out.println(gln + " -> " + c.name);
			}
		}		
		*/
	}

	public List<String> getAssortList(String ean_code) {
		if (m_map_ibsa_conditions.containsKey(ean_code)) {
			List<String> assort_list = null;
			String gln_code = m_customer_gln_code.isEmpty() ? m_prefs.get("glncode", "7610000000000") : m_customer_gln_code;

			if (m_map_ibsa_glns.containsKey(gln_code)) {
				// Extract user class (group in case of pharmacies)
				String uc[] = m_map_ibsa_glns.get(gln_code).split(";");
				// This is the default class
				char user_class = uc[0].charAt(0);	
				// Is the user human or corporate?			
				String user_type = m_prefs.get("type", "arzt");
				// System.out.println("Category for GLN " + gln_code + ": " + user_class + "-" + user_type);
				// Get rebate conditions
				Conditions c = m_map_ibsa_conditions.get(ean_code);			
				if (user_type.equals("arzt")) {
					assort_list = c.getAssort("doctor");
				} else if (user_type.equals("apotheke")) {
					// For pharmacies use fallback category...
					if (uc.length>1)
						user_class = uc[1].charAt(0);
					boolean is_promo_time = c.isPromoTime("pharmacy", user_class);
					if (!is_promo_time)
						assort_list = c.getAssort("pharmacy");
					else 
						assort_list = c.getAssort("pharmacy-promo");
				} else if (user_type.equals("drogerie")) {
					boolean is_promo_time = c.isPromoTime("drugstore", user_class);
					if (!is_promo_time)
						assort_list = c.getAssort("drugstore");
					else 
						assort_list = c.getAssort("drugstore-promo");
				} else if (user_type.equals("spital")) {
					assort_list = c.getAssort("hospital");
				}
			}
			return assort_list;
		}
		return null;
	}
	
	public Map<String, String> getAssortedArticles(String ean_code) {
		Map<String, String> assortedArticles = new TreeMap<String, String>();		
		List<String> assortList = getAssortList(ean_code);
		// Loop through all ean codes
		if (assortList!=null) {
			for (String ass_ean : assortList) {
				// Get name associated with it
				Conditions c = m_map_ibsa_conditions.get(ass_ean);
				if (c!=null) {
					String name = c.name;
					assortedArticles.put(ass_ean, name);
				}
			}
		}
		return assortedArticles;
	}
        
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<table id=\"Warenkorb\" width=\"99%25\">";
		String bar_charts_str = "";
		
		String load_order_str = "";
		String fast_order_str[] = {"", "", "", "", ""};
	
		String delete_all_button_str = "";
		String generate_pdf_str = "";
		String generate_csv_str = "";
		String checkout_str = "";
		
		String delete_all_text = "";	
		String generate_pdf_text = "";
		String generate_csv_text = "";
		String checkout_text = "";
		
		float subtotal_buying_CHF = 0.0f;
		float subtotal_selling_CHF = 0.0f;		
		
		m_shopping_basket = shopping_basket;
		
		// Is the user human or corporate?
		String user_type = m_prefs.get("type", "arzt");		
		
		if (m_shopping_basket!=null && m_shopping_basket.size()>0) {
			int index = 1;						
			basket_html_str += "<tr>"
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"11%\"><b>" + m_rb.getString("ean") + "</b></td>"			// 12
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"7%\"><b>" + m_rb.getString("category") + "</b></td>"			// 20
					+ "<td style=\"text-align:center; padding-bottom:8px;\"; width=\"3%\"></td>"			// 20				
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"40%\"><b>" + m_rb.getString("article") + "</b></td>"		// 66	
					+ "<td style=\"text-align:center; padding-bottom:8px;\"; width=\"6%\"><b>Assort</b></td>"		// 73						
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"7%\"><b>" + m_rb.getString("quantity") + "</b></td>"			// 80
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"7%\"><b>" + m_rb.getString("bonus") + "</b></td>"			// 50		
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"12%\"><b>" + m_rb.getString("expense") + "</b></td>"		// 71
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"12%\"><b>" + m_rb.getString("proceeds") + "</b></td>"		// 80				
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"12%\"><b>" + m_rb.getString("profit") + "</b></td>"		// 89
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"5%\"><b>" + m_rb.getString("rebate") + "</b></td>"		// 96			
					+ "<td style=\"text-align:center; padding-bottom:8px;\"; width=\"3%\"></td>"					// 100
					+ "</tr>";
			
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();
				int draufgabe = 0;
				// Check if ean code is in conditions map (special treatment)
				if (m_map_ibsa_conditions.containsKey(ean_code)) {
					// Get rebate conditions
					Conditions c = m_map_ibsa_conditions.get(ean_code);
					float factory_price = c.fep_chf;
					// Diese Kundengruppen haben spezielle Preise
					if (user_type.equals("spital") || user_type.equals("grossist"))
						factory_price = c.fap_chf;					
					// System.out.println(index + ": " + ean_code + " - " + c.name);
					// Extract rebate conditions for particular doctor/pharmacy
					TreeMap<Integer, Float> rebate_map = getRebateMap(ean_code);
					// --> These medis have a drop-down menu with the given "Naturalrabatt"
					if (rebate_map!=null && rebate_map.size()>0) {
						// Initialize draufgabe
						int qty = rebate_map.firstEntry().getKey();
						float reb = rebate_map.firstEntry().getValue(); // [%]
						if (reb>0) {	
							draufgabe = (int)(qty*reb/100.0f);		
							article.setDraufgabe(draufgabe);
						} else {
							article.setDraufgabe(0);					
							article.setCashRebate(-reb);
						}
						// Loop through all possible packages (units -> rebate mapping)	
						boolean qty_found = false;
						String value_str = "";					
						for (Map.Entry<Integer, Float> e : rebate_map.entrySet()) {
							qty = e.getKey();
							reb = e.getValue();
							// Chose option that has been selected before
							if (qty==article.getQuantity()) {
								value_str += "<option selected=\"selected\" value=\"" + qty + "\">" + qty + "</option>";
								if (reb>0) {
									draufgabe = (int)(qty*reb/100.0f);
									article.setDraufgabe(draufgabe);
								} else {		
									article.setDraufgabe(0);
									article.setCashRebate(-reb);
								}
								qty_found = true;
							} else {
								value_str += "<option value=\"" + qty + "\">" + qty + "</option>";	
							}
						}

						if (!qty_found)
							article.setQuantity(rebate_map.firstEntry().getKey());		
						article.setMargin(m_margin_percent/100.0f);		
						article.setBuyingPrice(factory_price);
						article.setDropDownStr(value_str);						
					} else {
						// --> These medis are like any other medis, the prices, however, come from the IBSA Excel file	
						article.setBuyingPrice(factory_price);
					}
				}
				index++;				
			}

			updateAssortedCart();
			
			index = 1;
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				
				String category = article.getCategories();				
				String ean_code = article.getEanCode();
				// Check if ean code is in conditions map (special treatment) 
				// --> This is all IBSA stuff
				
				if (m_map_ibsa_conditions.containsKey(ean_code)) {	
					article.setCode("ibsa");
			
					String assort_articles_str = "";
					String assortierbar = "<img src=\"" + m_images_dir + "checkmark_icon.png\" />";
					List<String> assort_list = getAssortList(ean_code);
					int num_assorts = 0;
					if (assort_list!=null) {
						for (String al : assort_list) {
							if (al.matches("[\\d]{13}")) {
								Conditions cond_a = m_map_ibsa_conditions.get(al);
								if (cond_a!=null) {
									assort_articles_str += cond_a.name + "<br>";
									num_assorts++;
								}
							}
						}
					}
					
					if (assort_articles_str.isEmpty()) {
						assort_articles_str = "<div>" + m_rb.getString("negassort") + "</div>";
						assortierbar = "";
					} else {
						if (num_assorts>12)
							assort_articles_str = "<div style=\"right:0; top:10%; width:110%;\">" + assort_articles_str + "</div>";
						else
							assort_articles_str = "<div style=\"width:110%;\">" + assort_articles_str + "</div>";
					}
						
					String muster = "";
					
					// Extract rebate conditions for particular doctor/pharmacy
					TreeMap<Integer, Float> rebate_map = getRebateMap(ean_code);
					// --> These medis have a drop-down menu with the given "Naturalrabatt"
					if (rebate_map!=null && rebate_map.size()>0) {
						// Update draufgabe 	
						String bonus = "";
						int dg = getDraufgabe(article); 
						if (dg>=0) {	// Used to be dg>0! ... DOUBLE-CHECK!
							article.setDraufgabe(dg);
							if (dg>0)
								bonus = String.format("+ %d", dg);							
						}
						// Update cash rebate
						String cash_rebate_percent = "0%";
						float cr = getCashRebate(article);
						if (cr>=0.0f)
							article.setCashRebate(cr);
						cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
						
						cr = 0.0f;
						if (dg<=0)
							cr = getCashRebate(article);

						subtotal_buying_CHF += (article.getTotBuyingPrice(cr));
						
						if (article.isSpecial())	// article is in SL Liste
							subtotal_selling_CHF += article.getTotPublicPrice();
						else	// article is OTC (over-the-counter)
							subtotal_selling_CHF += article.getTotSellingPrice();					

						String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice(cr));
						String tot_selling_price_CHF = Utilities.prettyFormat(article.getTotSellingPrice());
						String profit_CHF = Utilities.prettyFormat(article.getTotSellingPrice()-article.getTotBuyingPrice(cr));
						if (article.isSpecial()) {
							tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
							profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotBuyingPrice(cr));
						}
						
						if (isMuster(article, cr)) {
							muster = "<b>" + m_rb.getString("musterAbbr") + "</b>";
						}						
						
						basket_html_str += "<tr id=\"" + ean_code + "\">";
						basket_html_str += "<td>" + ean_code + "</td>"
								+ "<td>" + category + "</td>"			
								+ "<td style=\"color:'#999999'\">" + muster + "</td>"
								+ "<td onmouseover=\"this.bgColor='#eeeed0';\" onmouseout=\"this.bgColor='#f0f0f0';\" onclick=\"assortList('Warenkorb',this);\">" 
									+ "<a href=\"#\" class=\"tooltip\">" + article.getPackTitle() + assort_articles_str + "</a></td>"	
								+ "<td style=\"text-align:center;\">" + assortierbar + "</td>"	
								+ "<td style=\"text-align:right;\">" + "<select id=\"selected" + index + "\" style=\"width:56px; direction:rtl; text-align:right;\" onchange=\"onSelect('Warenkorb',this," + index + ")\"" +
									" tabindex=\"" + index + "\">" + article.getDropDownStr()+ "</select></td>"
								+ "<td style=\"text-align:right;\">" + bonus + "</td>"						
								+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
								+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
								+ "<td style=\"text-align:right; color:green\">" + profit_CHF + "</td>"
								+ "<td style=\"text-align:right; color:green\">" + cash_rebate_percent /* profit_percent */ + "</td>"							
								+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
									+ m_images_dir + "trash_icon.png\" /></button>" + "</td>";
						basket_html_str += "</tr>";	
					} else {
						// --> These medis have NO drop-down menu
						int quantity = article.getQuantity();	
						
						subtotal_buying_CHF += article.getTotBuyingPrice(0.0f);
						
						if (article.isSpecial())	// article is in SL Liste
							subtotal_selling_CHF += article.getTotPublicPrice();
						else	// article is OTC (over-the-counter)
							subtotal_selling_CHF += article.getTotSellingPrice();	
						
						String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice(0.0f));
						String tot_selling_price_CHF = Utilities.prettyFormat(article.getTotSellingPrice());
						String profit_CHF = Utilities.prettyFormat(article.getTotSellingPrice()-article.getTotBuyingPrice(0.0f));
						String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
						
						if (article.isSpecial()) {
							tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
							profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotBuyingPrice(0.0f));
						}
						
						basket_html_str += "<tr id=\"" + ean_code + "\">";
						basket_html_str += "<td>" + ean_code + "</td>"
								+ "<td>" + category + "</td>"		
								+ "<td style=\"color:'#999999'\">" + muster + "</td>"
								+ "<td onmouseover=\"this.bgColor='#eeeed0';\" onmouseout=\"this.bgColor='#f0f0f0';\" onclick=\"assortList('Warenkorb',this);\">" 
									+ "<a href=\"#\" class=\"tooltip\">" + article.getPackTitle() + assort_articles_str + "</a></td>"	
								+ "<td style=\"text-align:center;\">" + assortierbar + "</td>"	
								+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:56px; text-align:right;\"" +
									" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this,5)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"
								+ "<td style=\"text-align:right;\"></td>"					
								+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
								+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
								+ "<td style=\"text-align:right; color:green\">" + profit_CHF + "</td>"
								+ "<td style=\"text-align:right; color:green\">" + cash_rebate_percent /* profit_percent */  + "</td>"							
								+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
									+ m_images_dir + "trash_icon.png\" /></button>" + "</td>";
						basket_html_str += "</tr>";	
					}
				} else {
					// --> Non-IBSA stuff
					int quantity = article.getQuantity();
					subtotal_buying_CHF += article.getTotExfactoryPrice();
					subtotal_selling_CHF += article.getTotPublicPrice();
					
					String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotExfactoryPrice());
					String tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
					String profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotExfactoryPrice());
					String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
					
					String assort_hover = "<td>" + article.getPackTitle() + "</td>";
					// Falls "ibsa" Produkt, erwähne explizit, dass es "nicht assortierbar" ist
					if (article.getAuthor().toLowerCase().contains("ibsa")) {
						assort_hover = "<td onMouseOver=\"this.bgColor='#eeeed0'\" onMouseOut=\"this.bgColor='#f0f0f0'\">" 
								+ "<a href=\"#\" class=\"tooltip\">" + article.getPackTitle() + "<div>" + m_rb.getString("negassort") + "</div></a></td>";	
					}
					
					basket_html_str += "<tr id=\"" + ean_code + "\">";
					basket_html_str += "<td>" + ean_code + "</td>"
							+ "<td>" + category + "</td>"
							+ "<td>" + "" + "</td>"
							+ assort_hover
							+ "<td>" + "" + "</td>"															
							+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:56px; text-align:right;\"" +
								" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this,5)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"
							+ "<td style=\"text-align:right;\"></td>"						
							+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
							+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
							+ "<td style=\"text-align:right; color:green\">" + profit_CHF + "</td>"
							+ "<td style=\"text-align:right; color:green\">" + cash_rebate_percent /* profit_percent */  + "</td>"							
							+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
								+ m_images_dir + "trash_icon.png\" /></button>" + "</td>";
					basket_html_str += "</tr>";			
				}
				index++;
			}
			/*
			 * Note: negative tabindex skips element
			*/
			// String grand_total_cash_rebate_percent = String.format("%.1f%%", (0.5f+100.0f*(float)totDraufgabe()/(totDraufgabe()+totQuantity())));
			String grand_total_cash_rebate_percent = String.format("%.1f%%", getGrandTotalCashRebate());
			float subtotal_profit_CHF = subtotal_selling_CHF-subtotal_buying_CHF;

			String draufgabe_str = "<td style=\"padding-top:8px\"></td>";
			if (totDraufgabe()>0)
				draufgabe_str = "<td style=\"padding-top:8px; text-align:right;\"><b>+ " + totDraufgabe() + "</b></td>";
			else
				draufgabe_str = "<td style=\"padding-top:8px; text-align:right;\"></td>";
			basket_html_str += "<tr id=\"Total\">"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px\"></td>"					
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + totQuantity() + "</b></td>"
					+ draufgabe_str				
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + Utilities.prettyFormat(subtotal_buying_CHF) + "</b></td>"					
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + Utilities.prettyFormat(subtotal_selling_CHF) + "</b></td>"				
					+ "<td style=\"padding-top:8px; text-align:right; color:green\"><b>" + Utilities.prettyFormat(subtotal_profit_CHF) + "</b></td>"						
					+ "<td style=\"padding-top:8px; text-align:right; color:green\"><b>" + grand_total_cash_rebate_percent + "</b></td>"											
					+ "</tr>";
			
			// Add bar charts
			double buying_percent = 0.0; 
			if (subtotal_selling_CHF>0.0)
				buying_percent = Math.floor(99.0f*subtotal_buying_CHF/subtotal_selling_CHF);
			double profit_percent = 99.0 - buying_percent;
			double selling_percent = 99.3;
			
			basket_html_str += "</table></form>";
			
			bar_charts_str += "<table width=\"99%25\">";
			
			bar_charts_str += "<tr style=\"height:10px;\"><td colspan=\"12\"></tr>"
					+ "<tr><td colspan=\"10\" class=\"chart\"><div id=\"Buying_Col\" style=\"width:" + buying_percent + "%; background-color:firebrick;\">" + m_rb.getString("expense") + ": " 
					+ Utilities.prettyFormat(subtotal_buying_CHF) + " CHF</div>" 
					+ "<div style=\"width:0.3%; padding:0px;\"></div><div id=\"Profit_Col\" style=\"width:" + profit_percent + "%; background-color:forestgreen;\">" + m_rb.getString("profit") + ": "  
					+ Utilities.prettyFormat(subtotal_profit_CHF) + " CHF</div></td></tr>"
					+ "<tr><td colspan=\"10\" class=\"chart\"><div id=\"Selling_Col\" style=\"width:" + selling_percent + "%; background-color:blue;\">" + m_rb.getString("proceeds") + ": "  
					+ Utilities.prettyFormat(subtotal_selling_CHF) + " CHF</div></td></tr>";
			
			bar_charts_str += "<tr><td colspan=\"4\">" + m_rb.getString("margin") + " "
					+ "<input type=\"number\" name=\"points\" maxlength=\"3\" min=\"1\" max=\"999\" style=\"width:40px; text-align:right;\"" 
					+ " value=\"" + m_margin_percent + "\" onkeydown=\"changeMarge('Warenkorb',this)\" id=\"marge\" /></td>"
					+ "</tr>";	
			
			bar_charts_str += "</table>";
			
			// Warenkorb löschen
			delete_all_button_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"deleteAll(this)\"><img src=\"" + m_images_dir + "delete_all_icon.png\" /></button></div></td>";
			// Generate pdf
			generate_pdf_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Generate_pdf\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"createPdf(this)\"><img src=\"" + m_images_dir + "pdf_save_icon.png\" /></button></div></td>";
			// Generate csv
			generate_csv_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Generate_csv\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"createCsv(this)\"><img src=\"" + m_images_dir + "csv_save_icon.png\" /></button></div></td>";		
			// Check out 
			checkout_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Check_out\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"checkOut(this)\"><img src=\"" + m_images_dir + "checkout_icon.png\" /></button></div></td>";;
			// Subtitles	
			delete_all_text = "<td><div class=\"right\">" + m_rb.getString("emptyBasket") + "</div></td>";		
			generate_pdf_text = "<td><div class=\"right\">" + m_rb.getString("makePdf") + "</div></td>";
			generate_csv_text = "<td><div class=\"right\">" + m_rb.getString("makeCsv") + "</div></td>";
			checkout_text = "<td><div class=\"right\">" + m_rb.getString("checkout") + "</div></td>";			
		} else {	
			// Warenkorb ist leer
			basket_html_str = "<div>" + m_rb.getString("emptyCart") + "<br><br></div>";
		}
		
		// Bestellungen und Schnellbestellungen
		load_order_str = "<button style=\"background-color:#b0b0b0; color:#ffffff;\" id=\"loadCart0\" tabindex=\"-1\" onclick=\"loadCart(this,0)\">" + m_rb.getString("allOrders") + "</button>";		
		for (int i=1; i<6; ++i) {
			String button_pressed = "";
			if (m_cart_index==i)
				button_pressed = "class=\"buttonPressed\""; 
			fast_order_str[i-1] = "<button " + button_pressed + " id=\"loadCart" + i + "\" tabindex=\"-1\" onclick=\"loadCart(this," + i + ")\">" + m_rb.getString("shoppingCart") + " " + i + "</button>";
		}
				
		String footnotes_str = "<hr><table style=\"background:white\">"
				+ "<tr><td style=\"color:'#999999'; text-align:center; padding-right:10px;\"><b>" + m_rb.getString("musterAbbr") + "</b></td><td>" + m_rb.getString("musterMsg") + "</td></tr>"
				+ "<tr><td style=\"text-align:center; padding-right:10px;\"><img src=\"" + m_images_dir + "checkmark_icon.png\" /></td><td>" + m_rb.getString("assortMsg") + "</td></tr>"
				+ "</table>";
		
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str+ "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_shopping_cart_str + "</head>"
				+ "<body>"
				+ "<div id=\"buttons\">" + load_order_str + fast_order_str[0] + fast_order_str[1] + fast_order_str[2] + fast_order_str[3] + fast_order_str[4] + "</div>"
				+ "<div id=\"shopping\">" + basket_html_str + bar_charts_str + "<br />"
				+ "<form><table class=\"container\"><tr>" + delete_all_button_str + generate_pdf_str + generate_csv_str + checkout_str + "</tr>"
				+ "<tr>" + delete_all_text + generate_pdf_text + generate_csv_text + checkout_text + "</tr></table></form>"
				+ footnotes_str
				+ "</div></body></html>";		
		
		return m_html_str;
	}
	
	public String getRowUpdateJS(String ean_code, Article article) {
		// Update assorted articles
		updateAssortedCart();			
		// Update draufgabe 										
		String draufgabe = ""; 		
		int dg = getDraufgabe(article);
		if (dg>=0) {	// Used to be dg>0! ... DOUBLE-CHECK!
			article.setDraufgabe(dg);									
			draufgabe = String.format("+ %d", dg);
		}
		// Update cash rebate
		String cash_rebate_percent = "0%";	
		
		float cr = getCashRebate(article);
		if (cr>=0.0f)	
			article.setCashRebate(cr);				
		cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
		
		String tot_buying_price_CHF = "";
		String tot_selling_price_CHF = "";
		String profit_CHF = "";
		String muster = "";
		
		System.out.println("cash rebate = " + cash_rebate_percent + " / " + cr);
		
		if (article.getCode()!=null && article.getCode().equals("ibsa")) {
			cr = 0.0f;
			if (dg<=0)
				cr = getCashRebate(article);
			if (article.isSpecial()) {
				tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice(cr));
				tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
				profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotBuyingPrice(cr));
			} else {
				tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice(cr));
				tot_selling_price_CHF = Utilities.prettyFormat(article.getTotSellingPrice());
				profit_CHF = Utilities.prettyFormat(article.getTotSellingPrice()-article.getTotBuyingPrice(cr));
			}			
			if (isMuster(article, cr)) {
				muster = "<b>" + m_rb.getString("musterAbbr") + "</b>";
			}
		} else {			
			tot_buying_price_CHF = Utilities.prettyFormat(article.getTotExfactoryPrice());
			tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
			profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotExfactoryPrice());
		}
				
		String js = "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[2].innerHTML=\"" + muster + "\";"  
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[6].innerHTML=\"" + draufgabe + "\";"  
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[7].innerHTML=\"" + tot_buying_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[8].innerHTML=\"" + tot_selling_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[9].innerHTML=\"" + profit_CHF + "\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[10].innerHTML=\"" + cash_rebate_percent + "\";";

		return js;
	}
	
	public String getTotalsUpdateJS() {
		float subtotal_buying = 0.0f;
		float subtotal_selling = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			if (article.getCode()!=null && article.getCode().equals("ibsa")) {
				float cr = 0.0f;		
				int dg = getDraufgabe(article);
				if (dg<=0)
					cr = article.getCashRebate();
				if (article.isSpecial()) {
					subtotal_buying += article.getTotBuyingPrice(cr);
					subtotal_selling += article.getTotPublicPrice();
				} else {
					subtotal_buying += article.getTotBuyingPrice(cr);
					subtotal_selling += article.getTotSellingPrice();					
				}
			} else {
				subtotal_buying += article.getTotExfactoryPrice();
				subtotal_selling += article.getTotPublicPrice();
			}
		}
		String subtotal_buying_CHF = Utilities.prettyFormat(subtotal_buying);
		String subtotal_selling_CHF = Utilities.prettyFormat(subtotal_selling);
		String total_cash_rebate_percent = String.format("%.1f%%", getGrandTotalCashRebate());
		String total_profit_CHF = Utilities.prettyFormat((subtotal_selling-subtotal_buying)/**1.08f*/);
		String tot_quantity = String.format("%d", totQuantity());
		String tot_draufgabe = String.format("%d", totDraufgabe());

		double buying_percent = 0.0;
		if (subtotal_selling>0.0)
			buying_percent = Math.floor(99.0*subtotal_buying/subtotal_selling);
		double profit_percent = 99.0 - buying_percent;
		double selling_percent = 99.3;
		
		String js =	"document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[5].innerHTML=\"<b>" + tot_quantity + "</b>\";";
		if (totDraufgabe()>0)
			js += "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[6].innerHTML=\"<b>+ " + tot_draufgabe + "</b>\";";
		else
			js += "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[6].innerHTML=\"\";";
		js += "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[7].innerHTML=\"<b>" + subtotal_buying_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[8].innerHTML=\"<b>" + subtotal_selling_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[9].innerHTML=\"<b>" + total_profit_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[10].innerHTML=\"<b>" + total_cash_rebate_percent + "</b>\";"
				+ "document.getElementById('Buying_Col').style.width=\"" + buying_percent + "%\";"
				+ "document.getElementById('Buying_Col').innerHTML=\"Tot.Aufwand: " + subtotal_buying_CHF + " CHF\";"
				+ "document.getElementById('Selling_Col').style.width=\"" + selling_percent + "%\";"
				+ "document.getElementById('Selling_Col').innerHTML=\"Tot.Erlös: " + subtotal_selling_CHF + " CHF\";"
				+ "document.getElementById('Profit_Col').style.width=\"" + profit_percent + "%\";"
				+ "document.getElementById('Profit_Col').innerHTML=\"Gewinn: " + total_profit_CHF + " CHF\";";
		
		return js;
	}
	
	public String checkoutHtml(HashMap<String, Address> address_map) {
		String load_order_str = "";
		String fast_order_str[] = {"", "", "", "", ""};		
		String jscript_str = "<script language=\"javascript\">" + FileOps.readFromFile(Constants.JS_FOLDER + "shopping_callbacks.js") + "</script>";
		String m_css_str = "<style type=\"text/css\">" + FileOps.readFromFile(Constants.SHOPPING_SHEET) + "</style>";
		String images_dir = System.getProperty("user.dir") + "/images/";	
		
		// Bestellungen und Schnellbestellungen
		load_order_str = "<button style=\"background-color:#b0b0b0; color:#ffffff;\" id=\"loadCart0\" tabindex=\"-1\" onclick=\"loadCart(this,0)\">" + m_rb.getString("allOrders") + "</button>";		
		for (int i=1; i<6; ++i) {
			String button_pressed = "";
			if (m_cart_index==i)
				button_pressed = "class=\"buttonPressed\""; 
			fast_order_str[i-1] = "<button " + button_pressed + " id=\"loadCart" + i + "\" tabindex=\"-1\" onclick=\"loadCart(this," + i + ")\">" + m_rb.getString("shoppingCart") + " " + i + "</button>";
		}
		
		String checkout_html_str = "<table id=\"Checkout\" width=\"99%25\">";
		
		checkout_html_str += "<tr>"
				+ "<td style=\"text-align:left; padding-top:8px; padding-bottom:8px;\";><b>" + m_rb.getString("owner") + "</b></td>"			
				+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\";><b>" + m_rb.getString("subtotal") + "</b></td>"		
				+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\";><b>" + m_rb.getString("shipping") + "</b></td>"				
				+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\";><b>" + m_rb.getString("vat") + " (2.5%)</b></td>"			
				+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\";><b>" + m_rb.getString("vat") + " (8.0%)</b></td>"							
				+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\";><b>" + m_rb.getString("total") + "</b></td>"			
				+ "</tr>";

		m_map_owner_total = new TreeMap<String, Owner>();		
		String author = "";
		// Generate set of authors
		Set<String> set_of_authors = new HashSet<String>();
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			set_of_authors.add(entry.getValue().getAuthor());
		}
		
		boolean shipping_free = true; 	// Some combinations of articles can be shipped for free... keep track
		float shipping_CHF = 0.0f;		
		String shipping_type = "BZ";
		
		// Loop through all articles in shopping basket
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();			
			float price = 0.0f;
			float vat = 0.0f;	// vat in [CHF]
			// Is the shipping still free?
			shipping_free &= checkForArticle(article.getPackTitle());
			// Special rule for "ibsa"
			if (article.getCode()!=null && article.getCode().equals("ibsa")) {
				float cr = 0.0f;		
				int dg = getDraufgabe(article);
				if (dg<=0)
					cr = article.getCashRebate();
				price = article.getTotBuyingPrice(cr);
			} else {
				price = article.getTotExfactoryPrice();						
			}
			
			// Update map from author/owner to total spent
			vat = price * article.getVat()/100.0f;
			// This is the manufacturer of the article
			author = article.getAuthor();
			// Loop through set of authors and find matches
			for (String a : set_of_authors) {
				if (a.toLowerCase().contains(author.toLowerCase())) 
					author = a;
			}			
			// System.out.println(author);
			if (author!=null) {
				float sum_price = price;
				float sum_vat25 = 0.0f;
				float sum_vat80 = 0.0f;
				if (m_map_owner_total.containsKey(author)) {
					sum_price += m_map_owner_total.get(author).subtotal_CHF;
					sum_vat25 = m_map_owner_total.get(author).vat25_CHF;
					sum_vat80 = m_map_owner_total.get(author).vat80_CHF;
				}
				// Set default shipping values
				if (!shipping_free) {
					if (sum_price<500.0f) {
						if (m_user_class=='A') {
							shipping_CHF = 7.95f;
							shipping_type = "A";
						} else {
							shipping_CHF = 7.35f;
							shipping_type = "B";
						}
					} else {
						if (m_user_class=='A') {
							shipping_CHF = 0.0f;
							shipping_type = "AZ";
						} else {
							shipping_CHF = 0.0f;
							shipping_type = "BZ";
						}
					}
				} else {
					shipping_CHF = 0.0f;
					shipping_type = "AZ";
				}
				
				// System.out.println("User class / shipping type / cost -> " + m_user_class + " / " + shipping_type + " / " + shipping_CHF + " / " + sum_price);
				
				if (article.getVat()==2.5f) {
					Owner o = new Owner(author, sum_price, sum_vat25 + vat, sum_vat80, shipping_CHF, shipping_type); 	
					m_map_owner_total.put(author, o);				
				} else {
					Owner o = new Owner(author, sum_price, sum_vat25, sum_vat80 + vat, shipping_CHF, shipping_type); 	
					m_map_owner_total.put(author, o);									
				}
			}
		}

		String saving_to_desktop_text = "";
		int index = 0;
		float sum_total_CHF = 0.0f;
		float sum_vat25_CHF = 0.0f;
		float sum_vat80_CHF = 0.0f;
		float sum_shipping_CHF = 0.0f;
		float grand_total_CHF = 0.0f;
		// Loop through all authors and generate html
		for (Map.Entry<String, Owner> e : m_map_owner_total.entrySet()) { 
			author = e.getKey();
			float subtotal_CHF = e.getValue().subtotal_CHF;			
			float vat25_CHF = e.getValue().vat25_CHF;
			float vat80_CHF = e.getValue().vat80_CHF;
			shipping_CHF = e.getValue().shipping_CHF;
			shipping_type = e.getValue().shipping_type;
			
			String versand_optionen = "";
			if (checkIfSponsor(author)) {
				if (shipping_type.equals("B")) 
					versand_optionen = "<option value=\"B\">" + m_rb.getString("BPost") + ": +7.35 CHF</option>"
							+ "<option value=\"A\">" + m_rb.getString("APost") + ": +7.95 CHF</option>"
							+ "<option value=\"E\">" + m_rb.getString("express") + ": +17.90 CHF</option>";
				else if (shipping_type.equals("A"))
					versand_optionen = "<option value=\"A\">" + m_rb.getString("APost") + ": +7.95 CHF</option>"
							+ "<option value=\"B\">" + m_rb.getString("BPost") + ": +7.35 CHF</option>"
							+ "<option value=\"E\">" + m_rb.getString("express") + ": +17.90 CHF</option>";
				else if (shipping_type.equals("BZ"))
					versand_optionen = "<option value=\"BZ\">" + m_rb.getString("BPost") + ": +0.00 CHF</option>"
							+ "<option value=\"A\">" + m_rb.getString("APost") + ": +7.95 CHF</option>"
							+ "<option value=\"E\">" + m_rb.getString("express") + ": +17.90 CHF</option>";					
				else if (shipping_type.equals("AZ"))
					versand_optionen = "<option value=\"AZ\">" + m_rb.getString("APost") + ": +0.00 CHF</option>"
							+ "<option value=\"BZ\">" + m_rb.getString("BPost") + ": +0.00 CHF</option>"
							+ "<option value=\"E\">" + m_rb.getString("express") + ": +17.90 CHF</option>";					
				else
					versand_optionen = "<option value=\"A\">" + m_rb.getString("APost") + ": +7.95 CHF</option>";			
			} else {
				versand_optionen = m_rb.getString("deskOrder");
				shipping_CHF = 0.0f;
				saving_to_desktop_text += author + ", ";
			}
			// Sum up including VAT (+8%) for shipping costs
			float total_author_CHF = subtotal_CHF + 1.08f*shipping_CHF + vat25_CHF + vat80_CHF;
			vat80_CHF += 0.08f*shipping_CHF; 
			
			// All sums
			sum_total_CHF += subtotal_CHF;
			sum_vat25_CHF += vat25_CHF;
			sum_vat80_CHF += vat80_CHF;
			sum_shipping_CHF += shipping_CHF;
			grand_total_CHF += total_author_CHF;
									
			index++;
			checkout_html_str += "<tr id=\"" + author + "\">"
					+ "<td style=\"text-align:left; padding-top:8px;\">" + author + "</td>"			
					+ "<td style=\"text-align:right; padding-top:8px;\";>" + Utilities.prettyFormat(subtotal_CHF) + "</td>";			// Subtotal (exkl. MwSt. + shipping costs)			
			if (checkIfSponsor(author)) {
					checkout_html_str += "<td style=\"text-align:right; padding-top:8px;\"><select id=\"selected" + index + "\" style=\"width:180px; direction:rtl; text-align:left;\""
						+ "onchange=\"changeShipping('Checkout',this," + index + ")\">"
						+ versand_optionen + "</select></td>";
			} else
				checkout_html_str += "<td style=\"text-align:right; padding-top:8px;\">" + versand_optionen + "</td>";
			checkout_html_str += "<td style=\"text-align:right; padding-top:8px;\";>" + Utilities.prettyFormat(vat25_CHF) + "</td>"	// MwSt (2.5%)								
					+ "<td style=\"text-align:right; padding-top:8px;\";>" + Utilities.prettyFormat(vat80_CHF) + "</td>"			// MwSt (8.0%)		
					+ "<td style=\"text-align:right; padding-top:8px;\";>" + Utilities.prettyFormat(total_author_CHF) + "</td>"		// Subtotal	(inkl. MwSt + shipping costs)
					+ "</tr>";
		}
		checkout_html_str += "<tr id=\"GrandTotal\">"
				+ "<td style=\"text-align:left; padding-top:16px; padding-bottom:8px;\"><b>" + m_rb.getString("gesamttotal") + "</b></td>"
				+ "<td style=\"text-align:right; padding-top:16px; padding-bottom:8px;\"><b>" + Utilities.prettyFormat(sum_total_CHF) + "</b></td>"
				+ "<td style=\"text-align:right; padding-top:16px; padding-bottom:8px;\"><b>" + Utilities.prettyFormat(sum_shipping_CHF) + "</b></td>"
				+ "<td style=\"text-align:right; padding-top:16px; padding-bottom:8px;\"><b>" + Utilities.prettyFormat(sum_vat25_CHF) + "</b></td>"				
				+ "<td style=\"text-align:right; padding-top:16px; padding-bottom:8px;\"><b>" + Utilities.prettyFormat(sum_vat80_CHF) + "</b></td>"
				+ "<td style=\"text-align:right; padding-top:16px; padding-bottom:8px;\"><b>" + Utilities.prettyFormat(grand_total_CHF) + "</b></td>"
				+ "</tr></table>";
		
		String address_text = "";
		
		if (address_map!=null) {			
			address_text = "<table style=\"background-color:#ffffff;\" width=\"90%25\"><tr>";

			Address shipping_addr = null;
			Address billing_addr = null;
			Address ordering_addr = null;
			
			int user_id = m_prefs.getInt("user", 0);
			if (user_id==18) {			
				shipping_addr = address_map.get("S");
				billing_addr = address_map.get("B");
				ordering_addr = address_map.get("O");						
			} else {
				// Default entries... empty
				byte[] def = FileOps.serialize(new Address());
				
				byte[] arr = m_prefs.getByteArray("lieferadresse", def);
				if (arr!=null)
					shipping_addr = (Address)FileOps.deserialize(arr);
				
				arr = m_prefs.getByteArray("rechnungsadresse", def);
				if (arr!=null)
					billing_addr = (Address)FileOps.deserialize(arr);
				
				arr = m_prefs.getByteArray("bestelladresse", def);
				if (arr!=null)
					ordering_addr = (Address)FileOps.deserialize(arr);	
			}
			
			if (shipping_addr==null)
				shipping_addr = new Address();			
			if (billing_addr==null)
				billing_addr = new Address();
			if (ordering_addr==null)
				ordering_addr = new Address();
						
			String color_change_str = "width=\"30%25\" style=\"cursor:pointer; background-color:#ffffff; padding-left:8px; padding-top:16px;\" "
						+ "onmouseover=\"changeColor(this,true);\" "
						+ "onmouseout=\"changeColor(this,false);\" ";

			if (shipping_addr!=null) {
				address_text += "<td " + color_change_str + "onclick=\"changeAddress(this,'S');\" " 
					+ shipping_addr.getAsHtmlString("S", m_rb) + "</td>";
			}
			if (billing_addr!=null) {
				address_text += "<td " + color_change_str + "onclick=\"changeAddress(this,'B');\" " 
					+ billing_addr.getAsHtmlString("B", m_rb) + "</td>";				
			} 
			if (ordering_addr!=null) {
				address_text += "<td " + color_change_str + "onclick=\"changeAddress(this,'O');\" "
					+ ordering_addr.getAsHtmlString("O", m_rb) + "</td>";
			} 
			address_text += "</tr></table>";
		}
		
		String agb_str = "<input type=\"checkbox\" style=\"margin-right:10px;\" onclick=\"agbsAccepted(this)\">" + m_rb.getString("agbsMsg") + "</input>";
		
		String send_order_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Send_order\">"
				+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"sendOrder(this)\"><img src=\"" + images_dir + "order_send_icon.png\" /></button></div></td>";
		String send_order_text = "<td><div class=\"right\">" + m_rb.getString("sendOrder") + "</div></td>";			
		
		if (!saving_to_desktop_text.isEmpty()) {
			saving_to_desktop_text = "<hr><p class=\"footnote\">" + m_rb.getString("saveDesk") + "</p>";
		}
		
		String html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_str + "</head>"
				+ "<div id=\"buttons\">" + load_order_str + fast_order_str[0] + fast_order_str[1] + fast_order_str[2] + fast_order_str[3] + fast_order_str[4] + "</div>"
				+ "<body><div id=\"shopping\">" + checkout_html_str + "</div>"
				+ address_text
				+ "<div id=\"shopping\" style=\"font-size:0.8em;\"><p>" + agb_str + " <a href=\"javascript:void(0)\" onClick=\"showAgbs();\" style=\"font-style:italic; color:'#0000bb'\">" + m_rb.getString("readAgbs") + "</a></p></div>"
				+ "<div id=\"shopping\">"
				+ "<form><table class=\"container\"><tr>" + send_order_str + "</tr>"
				+ "<tr>" + send_order_text + "</tr></table></form>"
				+ saving_to_desktop_text
				+ "</div></body></html>";		
			
		return html_str;
	}
	
	public String getCheckoutUpdateJS(String author, String shipping_type) {
		float shipping_CHF = 0.0f;
		if (shipping_type.equals("AZ"))
			shipping_CHF = 0.0f;
		else if (shipping_type.equals("BZ"))
			shipping_CHF = 0.0f;
		else if (shipping_type.equals("B"))
			shipping_CHF = 7.35f;
		else if (shipping_type.equals("A"))
			shipping_CHF = 7.95f;
		else if (shipping_type.equals("E"))
			shipping_CHF = 17.90f;
		// Update shipping costs
		float subtotal_CHF = m_map_owner_total.get(author).subtotal_CHF;	
		float vat25_CHF = m_map_owner_total.get(author).vat25_CHF;
		float vat80_CHF = m_map_owner_total.get(author).vat80_CHF;
		Owner owner = new Owner(author, subtotal_CHF, vat25_CHF, vat80_CHF, shipping_CHF, shipping_type);
		m_map_owner_total.put(author, owner);
		// Calculate new total
		float total_CHF = subtotal_CHF + vat25_CHF + vat80_CHF + 1.08f*shipping_CHF; 
		vat80_CHF += 0.08f*shipping_CHF;
		
		float sum_total_CHF = 0.0f;
		float sum_vat25_CHF = 0.0f;
		float sum_vat80_CHF = 0.0f;
		float sum_shipping_CHF = 0.0f;
		float grand_total_CHF = 0.0f;
		for (Map.Entry<String, Owner> e : m_map_owner_total.entrySet()) { 
			author = e.getKey();
			float t_CHF = e.getValue().subtotal_CHF + e.getValue().vat25_CHF + e.getValue().vat80_CHF + 1.08f*e.getValue().shipping_CHF;
			// All sums
			sum_total_CHF += e.getValue().subtotal_CHF;
			if (!shipping_type.contains("Z") && checkIfSponsor(author)) 
				sum_shipping_CHF += e.getValue().shipping_CHF;
			sum_vat25_CHF += e.getValue().vat25_CHF;
			sum_vat80_CHF += e.getValue().vat80_CHF + e.getValue().shipping_CHF*0.08f;
			grand_total_CHF += t_CHF;
		}		
		
		String js = "document.getElementById('Checkout').rows.namedItem(\"" + author + "\").cells[4].innerHTML=\"" + Utilities.prettyFormat(vat80_CHF) + "\";"
				+ "document.getElementById('Checkout').rows.namedItem(\"" + author + "\").cells[5].innerHTML=\"" + Utilities.prettyFormat(total_CHF) + "\";"
				+ "document.getElementById('Checkout').rows.namedItem(\"GrandTotal\").cells[1].innerHTML=\"<b>" + Utilities.prettyFormat(sum_total_CHF) + "</b>\";"
				+ "document.getElementById('Checkout').rows.namedItem(\"GrandTotal\").cells[2].innerHTML=\"<b>" + Utilities.prettyFormat(sum_shipping_CHF) + "</b>\";"
				+ "document.getElementById('Checkout').rows.namedItem(\"GrandTotal\").cells[3].innerHTML=\"<b>" + Utilities.prettyFormat(sum_vat25_CHF) + "</b>\";"
				+ "document.getElementById('Checkout').rows.namedItem(\"GrandTotal\").cells[4].innerHTML=\"<b>" + Utilities.prettyFormat(sum_vat80_CHF) + "</b>\";"
				+ "document.getElementById('Checkout').rows.namedItem(\"GrandTotal\").cells[5].innerHTML=\"<b>" + Utilities.prettyFormat(grand_total_CHF) + "</b>\";";
		
		return js;
	}
}
