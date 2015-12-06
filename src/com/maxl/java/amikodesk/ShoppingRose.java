package com.maxl.java.amikodesk;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class ShoppingRose extends ShoppingCart implements java.io.Serializable {

	private static LinkedHashMap<String, Float> m_map_rose_conditions = null;
	private static HashMap<String, Float> m_sales_figures_map = null;
	private static HashMap<String, List<Article>> m_map_similar_articles = null;
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_shopping_cart_str = null;		
	private static String m_images_dir = "";

	private static Preferences m_prefs = null;
	
	static private String[] m_fav_suppliers = {"actavis", "helvepharm", "mepha", "sandoz", "sanofi", "spirig", "teva"};
	
	private class Pair<T, U> {         
	    public final T first;
	    public final U second;

	    public Pair(T first, U second) {         
	        this.first = first;
	        this.second = second;
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
	
    private String shortSupplier(String longSupplier) {
    	for (String s : m_fav_suppliers) {
    		if (longSupplier.toLowerCase().contains(s))
    			return s;
    	}
    	return "";
    }
    
	private float getCashRebate(Article article) {
		if (m_map_rose_conditions!=null) {
			String supplier = shortSupplier(article.getSupplier());
			if (m_map_rose_conditions.containsKey(supplier)) {
				return m_map_rose_conditions.get(supplier);
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
	
	/**
	 * Calculates minimum stock (MB) for the given article 
	 * Return -1 if article is not listed
	 * @param article
	 * @return min stock/inventory
	 */
	private int minStock(Article article) {
		float sales_figure = 0.0f;
		// Average of sales figure over last 12 days times safety margin
		if (m_sales_figures_map.containsKey(article.getPharmaCode()))
			sales_figure = 2.5f*m_sales_figures_map.get(article.getPharmaCode())/12.0f + 1.0f;
		else
			return -1;
		return (int)sales_figure;
	}
	
	/**
	 * Calculates shipping status as an integer given an article an arbitrary quantity
	 * @param article
	 * @param quantity
	 * @return shipping status
	 */
	private int shippingStatus(Article article, int quantity) {
		// Calculate min stock
		int mstock = minStock(article);
		if (mstock>=0) {
			int curstock = article.getItemsOnStock();
			if (curstock>=mstock && curstock>=quantity && mstock>=quantity)
				return 1;	// GREEN
			else if (curstock<mstock && curstock>=quantity && mstock>quantity)
				return 2;	// YELLOW
			else if (curstock>mstock && curstock>=quantity && mstock<quantity)
				return 3;	// YELLOW
			else if (curstock<=mstock && curstock<quantity)
				return 4;	// ORANGE
			else
				return 5;	// RED
		} 
		return 1000;		// NEVER
	}
	
	private Pair<String, String> shippingStatusColor(int status) {
		switch(status)
		{
		case 1:
			return new Pair<String, String>("greenyellow", "&#9899;");
		case 2:
			return new Pair<String, String>("gold", "&#9899;");
		case 3:
			return new Pair<String, String>("gold", "&#9899;");
		case 4:
			return new Pair<String, String>("orange", "&#9899;");
		case 5:
			return new Pair<String, String>("red", "&#9899;");
		}
		return new Pair<String, String>("red", "-");
	}

	private void sortSimilarArticles(final int quantity) {
		// Loop through all entries and sort/filter
		for (Map.Entry<String, List<Article>> entry : m_map_similar_articles.entrySet()) {
			String ean_code = entry.getKey();
			List<Article> list_of_articles = entry.getValue();
			// Sort: Lieferfähigkeit
			Collections.sort(list_of_articles, new Comparator<Article>() {
				@Override
				public int compare(Article a1, Article a2) {
					 return (shippingStatus(a1, quantity)-shippingStatus(a2, quantity));
				}
			});
			// Sort: Autogenerika
			// Sort: Präferenz Arzt
			// Sort: Präferenz Rose
			m_map_similar_articles.put(ean_code, list_of_articles);
		}
	}
	
	/**
	 * Constructor
	 */
	public ShoppingRose() {
		// Load sales figures file
		m_sales_figures_map = (new FileLoader()).loadRoseSalesFigures(Constants.ROSE_FOLDER + "Abverkaufszahlen.csv");
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
	
	public void setMap(Map<String, Float> conditions) {
		m_map_rose_conditions = (LinkedHashMap<String, Float>)conditions;		
	}	
	
	public void updateMapSimilarArticles(Map<String, List<Article>> similar_articles) {
		m_map_similar_articles = (HashMap<String, List<Article>>)similar_articles;
	}
	
	/**
	 * Html pages
	 */	
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<table style=\"background-color:#ffffff\" id=\"Warenkorb\" width=\"99%25\" >";
		
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
		
		m_shopping_basket = shopping_basket;
				
		if (m_shopping_basket!=null && m_shopping_basket.size()>0) {
			int index = 1;						
			basket_html_str += "<tr>"
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"8%\"><b>" + m_rb.getString("ean") + "</b></td>"
					+ "<td style=\"text-align:left; padding-bottom:8px;\"; width=\"40%\"><b>" + m_rb.getString("article") + "</b></td>"
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"5%\"><b>" + m_rb.getString("quantity") + "</b></td>"
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"10%\"><b>" + m_rb.getString("price") + "</b></td>"	
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"10%\"><b>" + m_rb.getString("rebate") + "</b></td>"
					+ "<td style=\"text-align:right; padding-bottom:8px;\"; width=\"10%\"><b>" + m_rb.getString("info") + "</b></td>"	
					+ "<td style=\"text-align:center; padding-bottom:8px;\"; width=\"3%\"></td>"	// Lieferfrist (Ampel)
					+ "<td style=\"text-align:center; padding-bottom:8px;\"; width=\"3%\"></td>"	// Delete icon					
					+ "</tr>";
						
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				
				// Get cash rebate
				float cr = getCashRebate(article);
				article.setCashRebate(cr);
				// Set buying price
				article.setBuyingPrice(article.getExfactoryPriceAsFloat());														
				// For the sum
				subtotal_buying_CHF += (article.getTotBuyingPrice(cr));
				
				int quantity = article.getQuantity();															
				String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice(cr));
				String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
				
				String ean_code = article.getEanCode();
				
				String feedback = article.getAvailability();
				if (article.getAvailability().equals("-1")) {
					feedback = "a.H.";
				}
				
				// Flags
				String flags_str = "";
				if (!article.getFlags().isEmpty())
					flags_str = "<div>" + article.getFlags() + "</div>";
				
				// Shipping status
				Pair<String, String> shipping_status = shippingStatusColor(shippingStatus(article, article.getQuantity()));

				if (index%2==1) {
					basket_html_str += "<tr id=\"" + ean_code + "\" style=\"background-color:blanchedalmond;\" " +
							"onmouseover=\"changeColorEven(this,true);\" " +
							"onmouseout=\"changeColorEven(this,false);\">";
				} else { 
					basket_html_str += "<tr id=\"" + ean_code + "\" style=\"background-color:whitesmoke;\" " +
							"onmouseover=\"changeColorOdd(this,true);cursor:pointer;\" " +
							"onmouseout=\"changeColorOdd(this,false);\">";
				}
												
				basket_html_str += "<td>" + ean_code + "</td>"
						+ "<td><a href=\"#\" class=\"tooltip2\">" + article.getPackTitle() + flags_str + "</a></td>"															
						+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:56px; text-align:right;\"" +
							" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this,2)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"					
						+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
						+ "<td style=\"text-align:right; color:green\">" + cash_rebate_percent + "</td>"							
						+ "<td style=\"text-align:right;\">" + feedback + "</td>"
						+ "<td style=\"text-align:center; color:" + shipping_status.first + "\">" + shipping_status.second + "</td>"													
						+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
							+ m_images_dir + "trash_icon_3.png\" /></button>" + "</td>";
				basket_html_str += "</tr>";	
 				index++;
			}				
			
			String grand_total_cash_rebate_percent = ""; 
			
			basket_html_str += "<tr id=\"Total\">"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px\"></td>"
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + totQuantity() + "</b></td>"
					+ "<td style=\"padding-top:8px; text-align:right;\"><b>" + Utilities.prettyFormat(subtotal_buying_CHF) + "</b></td>"					
					+ "<td style=\"padding-top:8px; text-align:right; color:green\"><b>" + grand_total_cash_rebate_percent + "</b></td>"
					+ "<td style=\"text-align:right;\">" + "</td>"
					+ "<td style=\"text-align:right;\">" + "</td>"
					+ "<td style=\"text-align:right;\">" + "</td>"
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
				+ "<div id=\"shopping\">" + basket_html_str + "<br />"
				+ "<form><table class=\"container\"><tr>" + delete_all_button_str + generate_pdf_str + generate_csv_str + checkout_str + "</tr>"
				+ "<tr>" + delete_all_text + generate_pdf_text + generate_csv_text + checkout_text + "</tr></table></form>"
				// + footnotes_str
				+ "</div></body></html>";		
		
		return m_html_str;
	}
	
	public String suggestionPageHtml() {		
		return "";
	}
	
	public String checkoutPageHtml() {
		return "";
	}
	
	/**
	 * Javascript strings
	 */
	public String getRowUpdateJS(String ean_code, Article article) {
		// Update cash rebate	
		float cr = getCashRebate(article);
		if (cr>=0.0f)	
			article.setCashRebate(cr);				
		String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
		
		String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotExfactoryPrice());
			
		// Shipping status
		Pair<String, String> shipping_status = shippingStatusColor(shippingStatus(article, article.getQuantity()));
				
		String js = "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[3].innerHTML=\"" + tot_buying_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[4].innerHTML=\"" + cash_rebate_percent + "\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[6].style.color=\"" + shipping_status.first + "\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[6].innerHTML=\"" + shipping_status.second + "\";";

		// TEST
		sortSimilarArticles(article.getQuantity());

		for (Map.Entry<String, List<Article>> s : m_map_similar_articles.entrySet()) {
			String ean = s.getKey();
			Article a1 = m_shopping_basket.get(ean);
			System.out.println("-----------------------");
			for (Article a2 : s.getValue()) {
				System.out.println(a2.getEanCode() + ": " + shippingStatus(a2, a1.getQuantity()) + " -> " + a2.getPackTitle());
			}
		}
		
		return js;
	}
	
	public String getTotalsUpdateJS() {
		float subtotal_buying = 0.0f;
		
		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
			Article article = entry.getValue();
			subtotal_buying += article.getTotExfactoryPrice();
		}
		String subtotal_buying_CHF = Utilities.prettyFormat(subtotal_buying);
		String total_cash_rebate_percent = String.format("%.1f%%", getGrandTotalCashRebate());
		String tot_quantity = String.format("%d", totQuantity());

		String js =	"document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[2].innerHTML=\"<b>" + tot_quantity + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[3].innerHTML=\"<b>" + subtotal_buying_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[4].innerHTML=\"<b>" + total_cash_rebate_percent + "</b>\";";
		
		return js;
	}
	
	public String getCheckoutUpdateJS() {
		String js = "";
		return js;
	}
}
