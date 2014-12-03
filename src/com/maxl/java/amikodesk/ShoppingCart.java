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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities.EscapeMode;

import com.maxl.java.shared.Conditions;

public class ShoppingCart implements java.io.Serializable {
	
	private static Map<String, Article> m_shopping_basket = null;
	private static Map<String, Conditions> m_map_ibsa_conditions = null;
	private static Map<String, String> m_map_ibsa_glns = null;
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_shopping_cart_str = null;		
	
	private static Preferences m_prefs;
	
	private static int m_margin_percent = 80;	// default
	
	private static int m_cart_index = -1;
	
	public ShoppingCart() {
		// Load javascripts
		m_jscripts_str = FileOps.readFromFile(Constants.JS_FOLDER + "shopping_callbacks.js");
		// Load shopping cart css style sheet
		m_css_shopping_cart_str = "<style type=\"text/css\">" + FileOps.readFromFile(Constants.SHOPPING_SHEET) + "</style>";
		// Preferences
		m_prefs = Preferences.userRoot().node(SettingsPage.class.getName());
		// Load encrypted conditions files
		byte[] encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER+"ibsa_conditions.ser");
		// Decrypt and deserialize
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			m_map_ibsa_conditions = (TreeMap<String, Conditions>)FileOps.deserialize(plain_msg);
		}
		// Load encrypted glns files
		encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER+"ibsa_glns.ser");
		// Decrypt and deserialize
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			m_map_ibsa_glns = (TreeMap<String, String>)FileOps.deserialize(plain_msg);
		}
	}
	
	public void setCartIndex(int index) {
		m_cart_index = index;
	}
	
	public int getCartIndex() {
		return m_cart_index;
	}
	
	public void setMarginPercent(int margin) {
		m_margin_percent = margin;
	}
	
	public void setShoppingBasket(Map<String, Article> shopping_basket) {
		m_shopping_basket = shopping_basket;
	}
    
	Map<String, Article> getShoppingBasket() {
		return m_shopping_basket;
	}
	
	public TreeMap<Integer, Float> getRebateMap(String ean_code) {
		TreeMap<Integer, Float> rebate_map = null;
		String gln_code = m_prefs.get("glncode", "7610000000000");
		if (m_map_ibsa_glns.containsKey(gln_code)) {
			char user_class = m_map_ibsa_glns.get(gln_code).charAt(0);
			// Is the user human or corporate?
			String user_type = m_prefs.get("type", "arzt");
			// Get rebate conditions
			Conditions c = m_map_ibsa_conditions.get(ean_code);
			// Extract rebate conditions for particular doctor/pharmacy	
			if (user_type.equals("arzt")) {
				rebate_map = c.getDiscountDoc(user_class);
			} else {
				if (user_type.equals("spital") || user_type.equals("wissenschaft") || user_type.equals("behörde")) {
					rebate_map = c.getDiscountHospital(user_class);
				} else if (user_type.equals("apotheke")) {
					if (c.isPromoTime(user_class))
						rebate_map = c.getDiscountPromo(user_class);
					else
						rebate_map = c.getDiscountFarma(user_class);
				}
			}
		}
		return rebate_map;
	}

	public List<String> getAssortList(String ean_code) {		
		if (m_map_ibsa_conditions.containsKey(ean_code)) {
			List<String> assort_list = null;
			String gln_code = m_prefs.get("glncode", "7610000000000");
			if (m_map_ibsa_glns.containsKey(gln_code)) {
				char user_class = m_map_ibsa_glns.get(gln_code).charAt(0);
				// Is the user human or corporate?			
				String user_type = m_prefs.get("type", "arzt");
				// System.out.println("Category for GLN " + gln_code + ": " + user_class + "-" + user_type);
				// Get rebate conditions
				Conditions c = m_map_ibsa_conditions.get(ean_code);			
				if (user_type.equals("arzt")) {
					assort_list = c.getAssortDoc();
				} else {
					if (user_type.equals("apotheke")) {
						if (c.isPromoTime(user_class))
							assort_list = c.getAssortPromo();
						else
							assort_list = c.getAssortFarma();
					}
				}		
			}
			return assort_list;
		}
		return null;
	}
	
	public int getDraufgabe(String ean_code, int units) {
		if (m_map_ibsa_conditions!=null) {
			if (m_map_ibsa_conditions.containsKey(ean_code)) {
				NavigableMap<Integer, Float> rebate = getRebateMap(ean_code);
				if (rebate!=null && rebate.size()>0) {
					// if rebate.get(units)>0 -> Warenrabatt [#]					
					if (rebate.containsKey(units)) {
						if (rebate.get(units)>0)
							return (int)(units*rebate.get(units)/100.0f);
					} else {
						// System.out.println(rebate.containsKey(units) + " / " + rebate.get(units));
						int floor_units = rebate.floorKey(units);
						// System.out.println(ean_code + " - draufgabe units (" + units + ") not in map, selecting " + floor_units);						
						return (int)(floor_units*rebate.get(floor_units)/100.0f);
					}
				}
			}
		}
		return 0;
	}
    
	public float getCashRebate(String ean_code, int units) {
		if (m_map_ibsa_conditions!=null) {
			if (m_map_ibsa_conditions.containsKey(ean_code)) {
				NavigableMap<Integer, Float> rebate = getRebateMap(ean_code);
				if (rebate!=null && rebate.size()>0) {
					// if rebate.get(units)<0 -> Barrabatt [%]
					if (rebate.containsKey(units)) {
						if (rebate.get(units)<=0)
							return -rebate.get(units);
					} else {
						int floor_units = rebate.floorKey(units);
						// System.out.println(ean_code + " - cashrebate units (" + units + ") not in map, selecting " + floor_units);						
						return -rebate.get(floor_units);
					}
				}
			}
		}
		return 0.0f;
	}
	
	public float getGrandTotalCashRebate() {	
		// float sum_weighted_draufgabe = 0.0f;
		float sum_weighted_tot_quantity = 0.0f;
		float sum_weighted_cash_rebate = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			// sum_weighted_draufgabe += article.getBuyingPrice()*article.getDraufgabe();
			sum_weighted_tot_quantity += article.getBuyingPrice()*(article.getDraufgabe()+article.getQuantity());		
			if (article.getDraufgabe()>0)
				sum_weighted_cash_rebate += article.getBuyingPrice()*article.getQuantity();
			else
				sum_weighted_cash_rebate += article.getBuyingPrice()*article.getQuantity()*(1.0f-article.getCashRebate()/100.0f);
		}				
		if (sum_weighted_tot_quantity>0.0f)
			return (100.0f*(sum_weighted_tot_quantity-sum_weighted_cash_rebate)/sum_weighted_tot_quantity);
		else 
			return 0.0f;
	}
	
	public int totQuantity() {
		int qty = 0;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			qty += article.getQuantity();
		}
		return qty;
	}	
    
	public int totDraufgabe() {
		int draufgabe = 0;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			draufgabe += article.getDraufgabe();
		}
		return draufgabe;
	}
    	 	
	public float totBuyingPrice() {
		float tot_buying = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			tot_buying += article.getQuantity()*article.getBuyingPrice();
		}
		return tot_buying;
	}
	
    public float totSellingPrice() {
		float tot_selling = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			tot_selling += article.getQuantity()*article.getSellingPrice();
		}
		return tot_selling;
    }
    
    public void updateAssortedCart() {
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
						if (m_shopping_basket.containsKey(al)) {
							assort_qty += m_shopping_basket.get(al).getQuantity();
						}
					}
				}
				article.setAssortedQuantity(assort_qty);
			}
		}
    }
    
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<form id=\"my_form\"><table id=\"Warenkorb\" width=\"99%25\">";
		String bar_charts_str = "";
		
		String load_order_str = "";
		String fast_order_str[] = {"", "", "", "", ""};
	
		String delete_all_button_str = "";
		String generate_pdf_str = "";
		String generate_csv_str = "";
		String send_order_str = "";
		
		String delete_all_text = "";	
		String generate_pdf_text = "";
		String generate_csv_text = "";
		String send_order_text = "";
		
		float subtotal_buying_CHF = 0.0f;
		float subtotal_selling_CHF = 0.0f;
		
		m_shopping_basket = shopping_basket;
				
		String images_dir = System.getProperty("user.dir") + "/images/";	
		
		if (m_shopping_basket!=null && m_shopping_basket.size()>0) {
			int index = 1;						
			basket_html_str += "<tr>"
					+ "<td style=\"text-align:left\"; width=\"10%\";><b>EAN</b></td>"			// 9
					+ "<td style=\"text-align:left\"; width=\"28%\";><b>Artikel</b></td>"		// 36
					+ "<td style=\"text-align:left\"; width=\"3%\"; ><b>Kat</b></td>"			// 39			
					+ "<td style=\"text-align:right\"; width=\"7%\";><b>Menge</b></td>"			// 46
					+ "<td style=\"text-align:right\"; width=\"5%\";><b>Bonus</b></td>"			// 50
					+ "<td style=\"text-align:right;\"; width=\"6%\";><b>Aufwand</b></td>"		// 56
					+ "<td style=\"text-align:right;\"; width=\"6%\";><b>Erlös</b></td>"		// 62			
					+ "<td style=\"text-align:right;\"; width=\"9%\";><b>Tot.Aufwand</b></td>"	// 71
					+ "<td style=\"text-align:right;\"; width=\"9%\";><b>Tot.Erlös</b></td>"	// 80				
					+ "<td style=\"text-align:right;\"; width=\"9%\";><b>Gewinn</b></td>"		// 89
					+ "<td style=\"text-align:right;\"; width=\"5%\";><b>Rabatt</b></td>"		// 96			
					+ "<td style=\"text-align:center;\"; width=\"3%\";></td>"					// 100
					+ "</tr>";
			
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();
				int draufgabe = 0;
				// Check if ean code is in conditions map (special treatment)
				if (m_map_ibsa_conditions.containsKey(ean_code)) {
					article.setSpecial(true);
					// Get rebate conditions
					Conditions c = m_map_ibsa_conditions.get(ean_code);
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
						article.setBuyingPrice(c.fep_chf);
						article.setMargin(m_margin_percent/100.0f);	
						article.setDropDownStr(value_str);						
					} else {
						// --> These medis are like any other medis, the prices, however, come from the IBSA Excel file	
						article.setBuyingPrice(c.fep_chf);
					}
				}
				index++;				
			}

			updateAssortedCart();
			
			index = 1;
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();
				// Check if ean code is in conditions map (special treatment)
				if (m_map_ibsa_conditions.containsKey(ean_code)) {	
					// Extract rebate conditions for particular doctor/pharmacy
					TreeMap<Integer, Float> rebate_map = getRebateMap(ean_code);
					// --> These medis have a drop-down menu with the given "Naturalrabatt"
					if (rebate_map!=null && rebate_map.size()>0) {
						if (article.getDraufgabe()>0)
							subtotal_buying_CHF += article.getTotBuyingPrice();							
						else
							subtotal_buying_CHF += article.getTotBuyingPrice()*(1.0f-article.getCashRebate()/100.0f);
						subtotal_selling_CHF += article.getTotSellingPrice();					
						
						String category = article.getCategories();
						
						String buying_price_CHF = String.format("%.2f", article.getBuyingPrice());
						String tot_buying_price_CHF = String.format("%.2f", article.getTotBuyingPrice());
						String selling_price_CHF = String.format("%.2f", article.getSellingPrice());
						String tot_selling_price_CHF = String.format("%.2f", article.getTotSellingPrice());
						String profit_CHF = String.format("%.2f", article.getTotSellingPrice()-article.getTotBuyingPrice());
						// Update draufgabe 	
						String bonus = "";
						int dg = getDraufgabe(ean_code, article.getQuantity()+article.getAssortedQuantity());
						if (dg>0) {
							article.setDraufgabe(dg);									
							bonus = String.format("+ %d", dg);
						}						
						// Update cash rebate
						String cash_rebate_percent = "0%";
						float cr = getCashRebate(ean_code, article.getQuantity()+article.getAssortedQuantity());
						if (cr>0.0f)
							article.setCashRebate(cr);				
						cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
						
						basket_html_str += "<tr id=\"" + ean_code + "\">";
						basket_html_str += "<td>" + ean_code + "</td>"
								+ "<td>" + article.getPackTitle() + "</td>"	
								+ "<td>" + category + "</td>"
								+ "<td style=\"text-align:right;\">" + "<select id=\"selected" + index + "\" style=\"width:50px; direction:rtl; text-align:right;\" onchange=\"onSelect('Warenkorb',this," + index + ")\"" +
									" tabindex=\"" + index + "\">" + article.getDropDownStr()+ "</select></td>"
								+ "<td style=\"text-align:right;\">" + bonus + "</td>"	
								+ "<td style=\"text-align:right;\">" + buying_price_CHF + "</td>"	
								+ "<td style=\"text-align:right;\">" + selling_price_CHF + "</td>"							
								+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
								+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
								+ "<td style=\"text-align:right; color:green\"><b>" + profit_CHF + "</b></td>"
								+ "<td style=\"text-align:right; color:green\"><b>" + cash_rebate_percent /* profit_percent */ + "</b></td>"							
								+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
									+ images_dir + "trash_icon.png\" /></button>" + "</td>";
						basket_html_str += "</tr>";	
					} else {
						int quantity = article.getQuantity();	
						
						subtotal_buying_CHF += article.getTotBuyingPrice();
						subtotal_selling_CHF += article.getTotSellingPrice();
						
						String category = article.getCategories();
						
						String buying_price_CHF = String.format("%.2f", article.getBuyingPrice());
						String tot_buying_price_CHF = String.format("%.2f", article.getTotBuyingPrice());
						String selling_price_CHF = String.format("%.2f", article.getSellingPrice());
						String tot_selling_price_CHF = String.format("%.2f", article.getTotSellingPrice());
						String profit_CHF = String.format("%.2f", article.getTotSellingPrice()-article.getTotBuyingPrice());
						String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
						
						basket_html_str += "<tr id=\"" + ean_code + "\">";
						basket_html_str += "<td>" + ean_code + "</td>"
								+ "<td>" + article.getPackTitle() + "</td>"		
								+ "<td>" + category + "</td>"
								+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:50px; text-align:right;\"" +
									" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"
								+ "<td style=\"text-align:right;\"></td>"	
								+ "<td style=\"text-align:right;\">" + buying_price_CHF + "</td>"	
								+ "<td style=\"text-align:right;\">" + selling_price_CHF + "</td>"							
								+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
								+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
								+ "<td style=\"text-align:right; color:green\"><b>" + profit_CHF + "</b></td>"
								+ "<td style=\"text-align:right; color:green\"><b>" + cash_rebate_percent /* profit_percent */  + "</b></td>"							
								// + "<td>" + "<input type=\"image\" src=\"" + images_dir + "trash_icon.png\" onmouseup=\"deleteRow('Warenkorb',this)\" tabindex=\"-1\" />" + "</td>";
								+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
									+ images_dir + "trash_icon.png\" /></button>" + "</td>";
						basket_html_str += "</tr>";	
					}
				} else {
					int quantity = article.getQuantity();
					subtotal_buying_CHF += article.getTotExfactoryPrice();
					subtotal_selling_CHF += article.getTotPublicPrice();

					String category = article.getCategories();	
					
					String buying_price_CHF = Utilities.prettyFormat(article.getExfactoryPriceAsFloat());
					String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotExfactoryPrice());
					String selling_price_CHF = Utilities.prettyFormat(article.getPublicPriceAsFloat());
					String tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
					String profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotExfactoryPrice());
					String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
					
					basket_html_str += "<tr id=\"" + ean_code + "\">";
					basket_html_str += "<td>" + ean_code + "</td>"
							+ "<td>" + article.getPackTitle() + "</td>"		
							+ "<td>" + category + "</td>"
							+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:50px; text-align:right;\"" +
								" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"
							+ "<td style=\"text-align:right;\"></td>"	
							+ "<td style=\"text-align:right;\">" + buying_price_CHF + "</td>"	
							+ "<td style=\"text-align:right;\">" + selling_price_CHF + "</td>"							
							+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
							+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
							+ "<td style=\"text-align:right; color:green\"><b>" + profit_CHF + "</b></td>"
							+ "<td style=\"text-align:right; color:green\"><b>" + cash_rebate_percent /* profit_percent */  + "</b></td>"							
							// + "<td>" + "<input type=\"image\" src=\"" + images_dir + "trash_icon.png\" onmouseup=\"deleteRow('Warenkorb',this)\" tabindex=\"-1\" />" + "</td>";
							+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
								+ images_dir + "trash_icon.png\" /></button>" + "</td>";
					basket_html_str += "</tr>";			
				}
				index++;
			}
			/*
			 * Note: negative tabindex skips element
			*/
			String grand_total_buying_CHF = Utilities.prettyFormat(subtotal_buying_CHF*1.08f);
			String grand_total_selling_CHF = Utilities.prettyFormat(subtotal_selling_CHF*1.08f);
			String grand_total_profit_CHF = Utilities.prettyFormat((subtotal_selling_CHF-subtotal_buying_CHF)*1.08f);
			// String grand_total_cash_rebate_percent = String.format("%.1f%%", (0.5f+100.0f*(float)totDraufgabe()/(totDraufgabe()+totQuantity())));
			String grand_total_cash_rebate_percent = String.format("%.1f%%", getGrandTotalCashRebate());
			float subtotal_profit_CHF = subtotal_selling_CHF-subtotal_buying_CHF;
			basket_html_str += "<tr id=\"Subtotal\">"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\">Subtotal</td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"	
					+ "<td style=\"padding-top:10px\"></td>"						
					+ "<td style=\"padding-top:10px; text-align:right;\">" + Utilities.prettyFormat(subtotal_buying_CHF) + "</td>"			
					+ "<td style=\"padding-top:10px; text-align:right;\">" + Utilities.prettyFormat(subtotal_selling_CHF) + "</td>"				
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "</tr>";
			basket_html_str += "<tr id=\"MWSt\">"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\">MWSt (+8%)</td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px\"></td>"						
					+ "<td style=\"padding-top:10px; text-align:right;\">" + Utilities.prettyFormat(subtotal_buying_CHF*0.08f) + "</td>"
					+ "<td style=\"padding-top:10px; text-align:right;\">" + Utilities.prettyFormat(subtotal_selling_CHF*0.08f) + "</td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px\"></td>"
					+ "</tr>";
			basket_html_str += "<tr id=\"Total\">"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"><b>Total</b></td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>" + totQuantity() + "</b></td>"
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>+ " + totDraufgabe() + "</b></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>" + grand_total_buying_CHF + "</b></td>"					
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>" + grand_total_selling_CHF + "</b></td>"				
					+ "<td style=\"padding-top:10px; text-align:right; color:green\"><b>" + Utilities.prettyFormat(subtotal_profit_CHF*1.08f) + "</b></td>"						
					+ "<td style=\"padding-top:10px; text-align:right; color:green\"><b>" + grand_total_cash_rebate_percent + "</b></td>"											
					+ "</tr>";
	
			// Add bar charts
			int totBuying_width = (int)(0.5f+subtotal_buying_CHF/20.0f);
			int totSelling_width = (int)(0.5f+subtotal_selling_CHF/20.0f);
			int totProfit_width = (int)(0.5f+(subtotal_selling_CHF-subtotal_buying_CHF)/20.0f)-3;
			if (totSelling_width>800) {
				totBuying_width = (int)(0.5f+800.0f*subtotal_buying_CHF/subtotal_selling_CHF);
				totSelling_width = 800;
				totProfit_width = (int)(0.5f+800.0f*(subtotal_selling_CHF-subtotal_buying_CHF)/subtotal_selling_CHF)-3;
			} else if (totSelling_width<300) {
				totBuying_width = (int)(0.5f+300.0f*subtotal_buying_CHF/subtotal_selling_CHF);
				totSelling_width = 300;
				totProfit_width = (int)(0.5f+300.0f*(subtotal_selling_CHF-subtotal_buying_CHF)/subtotal_selling_CHF)-3;
			}

			basket_html_str += "<tr style=\"height:20px;\"><td colspan=\"11\"></td></tr>"
					+ "<tr><td colspan=\"10\" class=\"chart\"><div id=\"Buying_Col\" style=\"width:" + totBuying_width + "px; background-color:firebrick;\">Tot.Aufwand: " + grand_total_buying_CHF + " CHF</div>" 
					+ "<div id=\"Profit_Col\" style=\"width:" + totProfit_width + "px; background-color:blue;\">Gewinn: " + grand_total_profit_CHF + " CHF</div></td></tr>"
					+ "<tr><td colspan=\"10\" class=\"chart\"><div id=\"Selling_Col\" style=\"width:" + totSelling_width + "px; background-color:forestgreen;\">Tot.Erlös: " + grand_total_selling_CHF + " CHF</div></td></tr>";
			
			basket_html_str += "<tr>"
					+ "<td>Vertriebsanteil (%)</td>"
					+ "<td style=\"text-align:left;\"><input type=\"number\" name=\"points\" maxlength=\"3\" min=\"1\" max=\"999\" style=\"width:40px; text-align:right;\"" 
					+ " value=\"" + m_margin_percent + "\" onkeydown=\"changeMarge('Warenkorb',this)\" id=\"marge\" /></td>"
					+ "</tr>";		
			
			basket_html_str += "</table></form>";
			
			// Warenkorb löschen
			delete_all_button_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"deleteAll(this)\"><img src=\"" + images_dir + "delete_all_icon.png\" /></button></div></td>";
			// Generate pdf
			generate_pdf_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Generate_pdf\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"createPdf(this)\"><img src=\"" + images_dir + "pdf_save_icon.png\" /></button></div></td>";
			// Generate csv
			generate_csv_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Generate_csv\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"createCsv(this)\"><img src=\"" + images_dir + "csv_save_icon.png\" /></button></div></td>";		
			// Send order 
			send_order_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Send_order\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"sendOrder(this)\"><img src=\"" + images_dir + "order_send_icon.png\" /></button></div></td>";;
			// Subtitles	
			delete_all_text = "<td><div class=\"right\">Korb leeren</div></td>";		
			generate_pdf_text = "<td><div class=\"right\">PDF erstellen</div></td>";
			generate_csv_text = "<td><div class=\"right\">CSV erstellen</div></td>";
			send_order_text = "<td><div class=\"right\">Bestellung senden</div></td>";			
		} else {	
			// Warenkorb ist leer
			if (Utilities.appLanguage().equals("de"))
				basket_html_str = "<div>Ihr Warenkorb ist leer.<br><br></div>";
			else if (Utilities.appLanguage().equals("fr"))
				basket_html_str = "<div>Votre panier d'achat est vide.<br><br></div>";
		}
		
		// Bestellungen und Schnellbestellungen
		load_order_str = "<button id=\"loadCart0\" tabindex=\"-1\" onclick=\"loadCart(this,0)\">Alle Bestellungen</button>";	
		for (int i=1; i<6; ++i) {
			String button_pressed = "";
			if (m_cart_index==i)
				button_pressed = "class=\"buttonPressed\""; 
			fast_order_str[i-1] = "<button " + button_pressed + " id=\"loadCart" + i + "\" tabindex=\"-1\" onclick=\"loadCart(this," + i + ")\">Warenkorb " + i + "</button>";
		}
		
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str+ "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_shopping_cart_str + "</head>"
				+ "<body>"
				+ "<div id=\"buttons\">" + load_order_str + fast_order_str[0] + fast_order_str[1] + fast_order_str[2] + fast_order_str[3] + fast_order_str[4] + "</div>"
				+ "<div id=\"shopping\">" + basket_html_str + bar_charts_str + "<br />"
				+ "<form><table class=\"container\"><tr>" + delete_all_button_str + generate_pdf_str + generate_csv_str + send_order_str + "</tr>"
				+ "<tr>" + delete_all_text + generate_pdf_text + generate_csv_text + send_order_text + "</tr></table></form>"
				+ "</div></body></html>";		
		
		return m_html_str;
	}
	
	public String getRowUpdateJS(String ean_code, Article article) {
		// Update assorted articles
		updateAssortedCart();			
		// Update draufgabe 										
		String draufgabe = ""; 		
		int dg = getDraufgabe(ean_code, article.getQuantity()+article.getAssortedQuantity());
		if (dg>0) {
			article.setDraufgabe(dg);									
			draufgabe = String.format("+ %d", dg);
		}
		// Update cash rebate
		String cash_rebate_percent = "0%";
		float cr = getCashRebate(ean_code, article.getQuantity()+article.getAssortedQuantity());
		if (cr>0.0f)
			article.setCashRebate(cr);				
		cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
		
		String buying_price_CHF = "";
		String selling_price_CHF = "";
		String tot_buying_price_CHF = "";
		String tot_selling_price_CHF = "";
		String profit_CHF = "";
		//
		if (article.isSpecial()) {
			buying_price_CHF = Utilities.prettyFormat(article.getBuyingPrice()*(1.0f-cr/100.0f));
			selling_price_CHF = Utilities.prettyFormat(article.getSellingPrice());
			tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice()*(1.0f-cr/100.0f));
			tot_selling_price_CHF = Utilities.prettyFormat(article.getTotSellingPrice());
			profit_CHF = Utilities.prettyFormat(article.getTotSellingPrice()-article.getTotBuyingPrice()*(1.0f-cr/100.0f));
		} else {
			buying_price_CHF = Utilities.prettyFormat(article.getExfactoryPriceAsFloat());
			selling_price_CHF = Utilities.prettyFormat(article.getPublicPriceAsFloat());				
			tot_buying_price_CHF = Utilities.prettyFormat(article.getTotExfactoryPrice());
			tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
			profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotExfactoryPrice());
		}
		
		String js = "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[4].innerHTML=\"" + draufgabe + "\";"  
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[5].innerHTML=\"" + buying_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[6].innerHTML=\"" + selling_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[7].innerHTML=\"" + tot_buying_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[8].innerHTML=\"" + tot_selling_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[9].innerHTML=\"<b>" + profit_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[10].innerHTML=\"<b>" + cash_rebate_percent + "</b>\";";

		return js;
	}
	
	public String getTotalsUpdateJS() {
		float subtotal_buying = 0.0f;
		float subtotal_selling = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article a = entry.getValue();
			if (a.isSpecial()) {
				float cr = a.getCashRebate();
				subtotal_buying += a.getTotBuyingPrice()*(1.0f-cr/100.0f);
				subtotal_selling += a.getTotSellingPrice();
			} else {
				subtotal_buying += a.getTotExfactoryPrice();
				subtotal_selling += a.getTotPublicPrice();
			}
		}
		String subtotal_buying_CHF = Utilities.prettyFormat(subtotal_buying);
		String mwst_buying_CHF = Utilities.prettyFormat(subtotal_buying*0.08f);
		String total_buying_CHF = Utilities.prettyFormat(subtotal_buying*1.08f);
		String subtotal_selling_CHF = Utilities.prettyFormat(subtotal_selling);
		String mwst_selling_CHF = Utilities.prettyFormat(subtotal_selling*0.08f);
		String total_selling_CHF = Utilities.prettyFormat(subtotal_selling*1.08f);
		String total_cash_rebate_percent = String.format("%.1f%%", getGrandTotalCashRebate());
		String total_profit_CHF = Utilities.prettyFormat((subtotal_selling-subtotal_buying)*1.08f);
		String tot_quantity = String.format("%d", totQuantity());
		String tot_draufgabe = String.format("%d", totDraufgabe());

		int width_tot_buying = (int)(0.5f+subtotal_buying/20.0f);
		int width_tot_selling = (int)(0.5f+subtotal_selling/20.0f);
		int width_tot_profit = (int)(0.5f+(subtotal_selling-subtotal_buying)/20.0f)-3;
		if (width_tot_selling>800) {
			width_tot_buying = (int)(0.5f+800.0f*subtotal_buying/subtotal_selling);
			width_tot_selling = 800;
			width_tot_profit = (int)(0.5f+800.0f*(subtotal_selling-subtotal_buying)/subtotal_selling)-3;
		} else if (width_tot_selling<300) {
			width_tot_buying = (int)(0.5f+300.0f*subtotal_buying/subtotal_selling);
			width_tot_selling = 300;
			width_tot_profit = (int)(0.5f+300.0f*(subtotal_selling-subtotal_buying)/subtotal_selling)-3;
		}
		
		String js = "document.getElementById('Warenkorb').rows.namedItem(\"Subtotal\").cells[7].innerHTML=\"" + subtotal_buying_CHF + "\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Subtotal\").cells[8].innerHTML=\"" + subtotal_selling_CHF + "\";"										
				+ "document.getElementById('Warenkorb').rows.namedItem(\"MWSt\").cells[7].innerHTML=\"" + mwst_buying_CHF + "\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"MWSt\").cells[8].innerHTML=\"" + mwst_selling_CHF + "\";"										
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[3].innerHTML=\"<b>" + tot_quantity + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[4].innerHTML=\"<b>" + tot_draufgabe + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[7].innerHTML=\"<b>" + total_buying_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[8].innerHTML=\"<b>" + total_selling_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[9].innerHTML=\"<b>" + total_profit_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[10].innerHTML=\"<b>" + total_cash_rebate_percent + "</b>\";"
				+ "document.getElementById('Buying_Col').style.width=\"" + width_tot_buying + "\";"
				+ "document.getElementById('Buying_Col').innerHTML=\"Tot.Aufwand: " + total_buying_CHF + " CHF\";"
				+ "document.getElementById('Selling_Col').style.width=\"" + width_tot_selling + "\";"
				+ "document.getElementById('Selling_Col').innerHTML=\"Tot.Erlös: " + total_selling_CHF + " CHF\";"
				+ "document.getElementById('Profit_Col').style.width=\"" + width_tot_profit + "\";"
				+ "document.getElementById('Profit_Col').innerHTML=\"Gewinn: " + total_profit_CHF + " CHF\";";
		
		return js;
	}
	
	public String createHtml() {
		String clean_html_str = "";	
		String header_html_str = "";
		String top_html_str = "";
		String basket_html_str = "";
		String bottom_html_str = "";
		String footer_html_str = "";
		
		int position = 0;
		float total_CHF = 0.0f;		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		header_html_str = "<img width=120 src=\"./images/desitin_logo.png\"></img>"
				+ "<h3>Bestellung</h3>"
				+ "Datum: " + dateFormat.format(date);
		
		if (m_shopping_basket.size()>0) {
			// basket_html_str = "<table id=\"Warenkorb\" width=\"100%25\" cellspacing=\"20\">";
			basket_html_str = "<table id=\"Warenkorb\" width=\"99%\" style=\"border-spacing: 5 10\">";
			basket_html_str += "<tr>";
			basket_html_str += "<td>Pos</td>"
					+ "<td align=\"right\">Menge</td>"
					+ "<td align=\"right\">PharmaCode</td>"
					+ "<td colspan=\"2\">Bezeichnung</td>" 
					+ "<td align=\"right\">Preis</td>";
			basket_html_str += "</tr>";
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				basket_html_str += "<tr>";
				basket_html_str += "<td>" + (++position) + "</td>" 
						+ "<td align=\"right\">" + article.getQuantity() + "</td>"
						+ "<td align=\"right\">" + article.getPharmaCode() + "</td>"
						+ "<td colspan=\"2\">" + article.getPackTitle() + "</td>"
						+ "<td align=\"right\">" + article.getExfactoryPrice() + "</td>";
				basket_html_str += "</tr>";
				String price_pruned = article.getExfactoryPrice().replaceAll("[^\\d.]", "");
				if (!price_pruned.isEmpty() && !price_pruned.equals(".."))
					total_CHF += article.getQuantity()*Float.parseFloat(price_pruned);
			}
			basket_html_str += "<tr>"
					+ "<td></td>"
					+ "<td></td>"
					+ "<td align=\"right\">Subtotal</td>"
					+ "<td colspan=\"2\"></td>"
					+ "<td align=\"right\">CHF " + String.format("%.2f", total_CHF) + "</td>"					
					+ "</tr>";
			basket_html_str += "<tr>"
					+ "<td></td>"					
					+ "<td></td>"
					+ "<td align=\"right\">MWSt</td>"
					+ "<td colspan=\"2\"></td>"
					+ "<td align=\"right\">CHF " + String.format("%.2f", total_CHF*0.08) + "</td>"					
					+ "</tr>";
			basket_html_str += "<tr>"
					+ "<td></td>"
					+ "<td></td>"
					+ "<td align=\"right\"><b>Total</b></td>"
					+ "<td colspan=\"2\"></td>"
					+ "<td align=\"right\"><b>CHF " + String.format("%.2f", total_CHF*1.08) + "</b></td>"					
					+ "</tr>";
						
			basket_html_str += "</table>";
		} 
		
		footer_html_str = "<p>Footer will go here...</p>";
		
		clean_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /></head>"
				+ "<body>" + header_html_str + "<br>" + top_html_str + basket_html_str + "<br>" 
				+ bottom_html_str + "<hr>" 
				+ footer_html_str + "</body></html>";		
		
		return clean_html_str;
	}

	
	
	public String prettyHtml(String str) {
		org.jsoup.nodes.Document mDoc = Jsoup.parse(str);
		
		mDoc.outputSettings().escapeMode(EscapeMode.xhtml);
		mDoc.outputSettings().prettyPrint(true);
		mDoc.outputSettings().indentAmount(2);
		
		return mDoc.toString();
	}
}