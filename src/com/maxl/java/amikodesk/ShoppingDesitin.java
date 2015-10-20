package com.maxl.java.amikodesk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

public class ShoppingDesitin extends ShoppingCart implements java.io.Serializable {
	
	private static TreeMap<String, Float> m_map_desitin_conditions = null;
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_shopping_cart_str = null;		
	private static String m_images_dir = "";
	
	private static Preferences m_prefs;
	
	private static char m_user_class = ' ';
	
	private boolean checkForArticle(String n) {
		// For these articles the shipping is free
		return false;
	}
	
	private boolean checkIfSponsor(String a) {
		String author = a.toLowerCase();
		return (author.contains("desitin")
				/*|| author.contains("ibsa")*/);
	}
	
    private int totQuantity() {
		int qty = 0;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			qty += article.getQuantity();
		}
		return qty;
	}	
	
	private float getCashRebate(Article article) {
		if (m_map_desitin_conditions!=null) {
			String ean_code = article.getEanCode();
			if (m_map_desitin_conditions.containsKey(ean_code)) {
				return m_map_desitin_conditions.get(ean_code);
			}
		}
		return 0.0f;
	}
    
	private float getGrandTotalCashRebate() {
		float sum_buying_price = 0.0f;
		float sum_weighted_buying_price = 0.0f;
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			float cr = article.getCashRebate();
			sum_buying_price += article.getBuyingPrice(0.0f)*article.getQuantity();
			sum_weighted_buying_price += article.getBuyingPrice(cr)*article.getQuantity();
		}				
		float delta_buying_price = sum_buying_price - sum_weighted_buying_price;
		if (delta_buying_price>0.0f)
			return (100.0f*(delta_buying_price/sum_buying_price));
		else 
			return 0.0f;
	}
	
	public ShoppingDesitin() {
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
	
	public void setMap(TreeMap<String, Float> conditions) {
		m_map_desitin_conditions = conditions;
	}
	
	public void setUserCategory(String cat) {
		switch(cat) {
		case "Spital":
			m_user_class = 'S';
			break;
		case "Arzt":
			m_user_class = 'P'; // Praxisapotheke
			break;
		case "Apotheke":
			m_user_class = 'O'; // Offizinalapotheke
			break;
		case "Grossist":
			m_user_class = 'G';
			break;
		}
	}	
	
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<table style=\"background-color:#ffffe0\" id=\"Warenkorb\" width=\"99%25\" >";
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
				
		if (m_shopping_basket!=null && m_shopping_basket.size()>0) {
			int index = 1;						
			basket_html_str += "<tr>"
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"11%\"><b>" + m_rb.getString("ean") + "</b></td>"			 
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"5%\"><b>" + m_rb.getString("category") + "</b></td>"		 
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"40%\"><b>" + m_rb.getString("article") + "</b></td>"		
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"4%\"><b>" + m_rb.getString("quantity") + "</b></td>"	// 60		
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"9%\"><b>" + m_rb.getString("expense") + "</b></td>"	
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"9%\"><b>" + m_rb.getString("proceeds") + "</b></td>"				
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"9%\"><b>" + m_rb.getString("profit") + "</b></td>"		
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"10%\"><b>" + m_rb.getString("rebate") + "</b></td>"		// +43
					+ "<td style=\"text-align:center; padding-bottom:8px;\"; width=\"3%\"></td>"				
					+ "</tr>";
						
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();
				String category = article.getCategories();	
				// Check if ean code is in conditions map (special treatment)
				if (m_map_desitin_conditions!=null && m_map_desitin_conditions.containsKey(ean_code)) {
					float cr = m_map_desitin_conditions.get(ean_code);
					// Update cash rebate
					String cash_rebate_percent = "0%";
					if (cr>=0.0f)
						article.setCashRebate(cr);
					// Set buying price
					article.setBuyingPrice(article.getExfactoryPriceAsFloat());					
															
					// For the sum
					subtotal_buying_CHF += (article.getTotBuyingPrice(cr));
					if (article.isSpecial())	// article is in SL Liste
						subtotal_selling_CHF += article.getTotPublicPrice();
					else	// article is OTC (over-the-counter)
						subtotal_selling_CHF += article.getTotSellingPrice();	
					
					int quantity = article.getQuantity();															
					String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice(cr));
					String tot_selling_price_CHF = Utilities.prettyFormat(article.getTotSellingPrice());
					String profit_CHF = Utilities.prettyFormat(article.getTotSellingPrice()-article.getTotBuyingPrice(cr));
					if (article.isSpecial()) {
						tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
						profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotBuyingPrice(cr));
					}
					
					cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
					
					basket_html_str += "<tr id=\"" + ean_code + "\">";
					basket_html_str += "<td>" + ean_code + "</td>"
							+ "<td>" + category + "</td>"
							+ "<td>" + article.getPackTitle() + "</td>"															
							+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:56px; text-align:right;\"" +
								" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this,3)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"					
							+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
							+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
							+ "<td style=\"text-align:right; color:green\">" + profit_CHF + "</td>"
							+ "<td style=\"text-align:right; color:green\">" + cash_rebate_percent /* profit_percent */  + "</td>"							
							+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
								+ m_images_dir + "trash_icon.png\" /></button>" + "</td>";
					basket_html_str += "</tr>";	
				} else {
					// --> Non-rebated meds
					int quantity = article.getQuantity();
					subtotal_buying_CHF += article.getTotExfactoryPrice();
					subtotal_selling_CHF += article.getTotPublicPrice();
					
					String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotExfactoryPrice());
					String tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
					String profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotExfactoryPrice());
					String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());

					basket_html_str += "<tr id=\"" + ean_code + "\">";
					basket_html_str += "<td>" + ean_code + "</td>"
							+ "<td>" + category + "</td>"
							+ "<td>" + article.getPackTitle() + "</td>"															
							+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:56px; text-align:right;\"" +
								" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this,3)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"					
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
			
			float subtotal_profit_CHF = subtotal_selling_CHF-subtotal_buying_CHF;
			String grand_total_cash_rebate_percent = ""; // String.format("%.1f%%", getGrandTotalCashRebate());
			
			basket_html_str += "<tr id=\"Total\">"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + totQuantity() + "</b></td>"
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + Utilities.prettyFormat(subtotal_buying_CHF) + "</b></td>"					
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + Utilities.prettyFormat(subtotal_selling_CHF) + "</b></td>"				
					+ "<td style=\"padding-top:8px; text-align:right; color:green\"><b>" + Utilities.prettyFormat(subtotal_profit_CHF) + "</b></td>"						
					+ "<td style=\"padding-top:8px; text-align:right; color:green\"><b>" + grand_total_cash_rebate_percent + "</b></td>"											
					+ "</tr>";						
			basket_html_str += "</table></form>";
						
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
				+ "</table>";
		
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str+ "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_shopping_cart_str + "</head>"
				+ "<body>"
				+ "<div id=\"buttons\">" + load_order_str + fast_order_str[0] + fast_order_str[1] + fast_order_str[2] + fast_order_str[3] + fast_order_str[4] + "</div>"
				+ "<div id=\"shopping\">" + basket_html_str + bar_charts_str + "<br />"
				+ "<form><table class=\"container\"><tr>" + delete_all_button_str + generate_pdf_str + generate_csv_str + checkout_str + "</tr>"
				+ "<tr>" + delete_all_text + generate_pdf_text + generate_csv_text + checkout_text + "</tr></table></form>"
				// + footnotes_str
				+ "</div></body></html>";		
		
		return m_html_str;
	}
	
	public String getRowUpdateJS(String ean_code, Article article) {
		// Update cash rebate
		String cash_rebate_percent = "0%";	
		
		float cr = getCashRebate(article);
		if (cr>=0.0f)	// Used to be cr>0.0f... DOUBLE-CHECK!
			article.setCashRebate(cr);				
		cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
		
		String tot_buying_price_CHF = "";
		String tot_selling_price_CHF = "";
		String profit_CHF = "";
				
		if (article.getCode()!=null && article.getCode().equals("ibsa")) {
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
		} else {			
			tot_buying_price_CHF = Utilities.prettyFormat(article.getTotExfactoryPrice());
			tot_selling_price_CHF = Utilities.prettyFormat(article.getTotPublicPrice());
			profit_CHF = Utilities.prettyFormat(article.getTotPublicPrice()-article.getTotExfactoryPrice());
		}
				
		String js = "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[4].innerHTML=\"" + tot_buying_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[5].innerHTML=\"" + tot_selling_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[6].innerHTML=\"" + profit_CHF + "\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[7].innerHTML=\"" + cash_rebate_percent + "\";";

		return js;
	}
	
	public String getTotalsUpdateJS() {
		float subtotal_buying = 0.0f;
		float subtotal_selling = 0.0f;
		
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			if (article.getCode()!=null && article.getCode().equals("desitin")) {
				float cr = article.getCashRebate();
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

		String js =	"document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[3].innerHTML=\"<b>" + tot_quantity + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[4].innerHTML=\"<b>" + subtotal_buying_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[5].innerHTML=\"<b>" + subtotal_selling_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[6].innerHTML=\"<b>" + total_profit_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[7].innerHTML=\"<b>" + total_cash_rebate_percent + "</b>\";";
		
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
		
		String checkout_html_str = "<table style=\"background-color:#ffffe0\" id=\"Checkout\" width=\"99%25\">";
		
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
		// Generate set of authors for all articles listed in shopping basket
		Set<String> set_of_authors = new HashSet<String>();
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			set_of_authors.add(entry.getValue().getAuthor());
		}
			
		// Loop through all articles in shopping basket
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();			
			float price = 0.0f;
			float vat = 0.0f;	// vat in [CHF]
			// Special rule for "desitin"
			if (article.getCode()!=null && article.getCode().equals("desitin")) {
				float cr = article.getCashRebate();
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
				
				if (article.getVat()==2.5f) {
					Owner o = new Owner(author, sum_price, sum_vat25 + vat, sum_vat80, 0.0f, "AZ"); 	// Default: B-Post
					o.updateShippingCosts(m_user_class);
					m_map_owner_total.put(author, o);				
				} else {
					Owner o = new Owner(author, sum_price, sum_vat25, sum_vat80 + vat, 0.0f, "AZ"); 	// Default: B-Post
					o.updateShippingCosts(m_user_class);
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
			Owner owner = e.getValue();
			float subtotal_CHF = owner.subtotal_CHF;			
			float vat25_CHF = owner.vat25_CHF;
			float vat80_CHF = owner.vat80_CHF;
			float shipping_CHF = owner.shipping_CHF;
			boolean shipping_free = owner.shipping_free;			
			String versand_optionen = "";

			if (checkIfSponsor(author)) {
				if (!shipping_free) {
					String shipping_APost_CHF = "+" + String.format("%.2f", shipping_CHF) + " CHF";
					String shipping_express_CHF = "+" + String.format("%.2f", (shipping_CHF + 40.0)) + " CHF";
					versand_optionen = "<option value=\"A\">" + m_rb.getString("APost") + ": " + shipping_APost_CHF + "</option>"
							+ "<option value=\"E\">" + m_rb.getString("express") + ": " + shipping_express_CHF + "</option>";					
				} else {
					shipping_CHF = 0.0f;
					versand_optionen = "<option value=\"AZ\">" + m_rb.getString("APost") + ": +0.00 CHF</option>"
						+ "<option value=\"E\">" + m_rb.getString("express") + ": +40.00 CHF</option>";
				}
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
					+ "<td style=\"text-align:right; padding-top:8px;\";>" + Utilities.prettyFormat(subtotal_CHF) + "</td>";		// Subtotal (exkl. MwSt. + shipping costs)			
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
		Owner owner = m_map_owner_total.get(author);
		
		float subtotal_CHF = owner.subtotal_CHF;	
		float vat25_CHF = owner.vat25_CHF;
		float vat80_CHF = owner.vat80_CHF;
		owner.updateShippingCosts(m_user_class);
		float shipping_CHF = owner.shipping_CHF;
		if (shipping_type.equals("E"))
			shipping_CHF += 40.0f;
		owner.shipping_CHF = shipping_CHF;
		m_map_owner_total.put(author, owner);
		
		// Calculate new total
		float total_CHF = subtotal_CHF + vat25_CHF + vat80_CHF + 1.08f*shipping_CHF; 
		vat80_CHF += 0.08f*shipping_CHF;
		
		float sum_total_CHF = 0.0f;
		float sum_vat25_CHF = 0.0f;
		float sum_vat80_CHF = 0.0f;
		float sum_shipping_CHF = 0.0f;
		float grand_total_CHF = 0.0f;
		// Loop through all owners...		
		for (Map.Entry<String, Owner> e : m_map_owner_total.entrySet()) { 
			Owner o = e.getValue();
			// All sums
			sum_total_CHF += o.subtotal_CHF;
			sum_vat25_CHF += o.vat25_CHF;
			sum_vat80_CHF += o.vat80_CHF + o.shipping_CHF*0.08f;
			grand_total_CHF += o.subtotal_CHF + o.vat25_CHF + o.vat80_CHF + 1.08f*o.shipping_CHF;;
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
