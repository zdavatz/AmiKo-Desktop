package com.maxl.java.amikodesk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class ShoppingRose extends ShoppingCart implements java.io.Serializable {
	
	private static LinkedHashMap<String, Float> m_rebate_map = null;
	private static LinkedHashMap<String, Float> m_expenses_map = null;
	private static HashMap<String, Float> m_sales_figures_map = null;
	private static ArrayList<String> m_auto_generika_list = null;
	private static HashMap<String, List<Article>> m_map_similar_articles = null;	
	private static ArrayList<String> m_select_articles_list = new ArrayList<String>();
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_shopping_cart_str = null;		
	private static String m_images_dir = "";
	
	private static boolean m_filter_state = false;
	
	private static Preferences m_prefs = null;
	
	static private String[] m_fav_suppliers = {"actavis", "helvepharm", "mepha", "sandoz", "sanofi", "spirig", "teva"};

	private static final int m_min_articles = 2;
	
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
		if (m_rebate_map!=null) {
			String supplier = shortSupplier(article.getSupplier());
			if (m_rebate_map.containsKey(supplier)) {
				return m_rebate_map.get(supplier);
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
		// Beschaffungsartikel sind immer orange
		if (article.getSupplier().toLowerCase().contains("voigt")) {
			return 4;
		}
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
		return 10;		// NEVER
	}
	
	private Pair<String, String> shippingStatusColor(int status) {
		switch(status)
		{
		case 1:
			return new Pair<String, String>("greenyellow", m_images_dir + "circ_green.png"); //&#9899;");
		case 2:
			return new Pair<String, String>("gold", m_images_dir + "circ_yellow.png"); //"&#9899;");
		case 3:
			return new Pair<String, String>("gold", m_images_dir + "circ_yellow.png"); //"&#9899;");
		case 4:
			return new Pair<String, String>("orange", m_images_dir + "circ_orange.png"); //"&#9899;");
		case 5:
			return new Pair<String, String>("red", m_images_dir + "circ_red.png"); //"&#9899;");
		}
		return new Pair<String, String>("red", m_images_dir + "ausser_handel.png");
	}

	private float rebateForSupplier(Article article) {
		String supplier = article.getSupplier().toLowerCase();
		String short_supplier = "";
		for (String p : Utilities.doctorPreferences.keySet()) {
			if (supplier.contains(p))
				short_supplier = p;
		}
		float value = 0.0f;
		if (m_rebate_map.containsKey(short_supplier))
			value = m_rebate_map.get(short_supplier);
		return value;
	}
	
	private float salesFiguresForSupplier(Article article) {
		String supplier = article.getSupplier().toLowerCase();
		String short_supplier = "";
		for (String p : Utilities.doctorPreferences.keySet()) {
			if (supplier.contains(p))
				short_supplier = p;
		}
		float value = 0.0f;
		if (m_expenses_map.containsKey(short_supplier))
			value = m_expenses_map.get(short_supplier);
		return value;
		
	}
	
	private boolean isAutoGenerikum(String ean) {
		return m_auto_generika_list.contains(ean);
	}
	
	private void sortSimilarArticles(final int quantity) {
		// Loop through all entries and sort/filter
		for (Map.Entry<String, List<Article>> entry : m_map_similar_articles.entrySet()) {		
			// Ean code of "key" article
			String ean_code = entry.getKey();
			// Copy list of similar/comparable articles
			LinkedList<Article> list_of_similar_articles = new LinkedList<Article>(entry.getValue());
			
			// Remove the key article itself
			for (Iterator<Article> iterator = list_of_similar_articles.iterator(); iterator.hasNext(); ) {
				Article article = iterator.next();
				if (article.getEanCode().equals(ean_code)) {
					iterator.remove();
					break;
				}
			}
			
			if (list_of_similar_articles.size()>m_min_articles) {
				// Sort: Lieferfähigkeit
				Article min_lead = Collections.min(list_of_similar_articles, new Comparator<Article>() {
					@Override
					public int compare(Article a1, Article a2) {
						 return (shippingStatus(a1, quantity) - shippingStatus(a2, quantity));
					}
				});		
				// Generate map of articles with min lead time
				HashMap<String, Article> map_of_min_lead_articles = new HashMap<String, Article>();
				int min_lead_time = shippingStatus(min_lead, quantity);
				if (min_lead_time>=0) {
					for (Article article : list_of_similar_articles) {
						if (shippingStatus(article, quantity)==min_lead_time) {
							map_of_min_lead_articles.put(article.getEanCode(), article);
						}
					}
				}
				// Assert
				assert(map_of_min_lead_articles.size()>0);			
				
				list_of_similar_articles = new LinkedList<Article>(map_of_min_lead_articles.values());
				
				// Sort: Autogenerika		
				if (map_of_min_lead_articles.size()>m_min_articles) {
					LinkedList<Article> list_of_auto_generika = new LinkedList<Article>();
					for (String ean : m_auto_generika_list) {
						if (map_of_min_lead_articles.containsKey(ean)) {
							list_of_auto_generika.add(map_of_min_lead_articles.get(ean));
						}
					}
					// Mark all articles which are autogenerika
					if (list_of_auto_generika.size()>0) {
						list_of_similar_articles = list_of_auto_generika;
						// Make sure that in the end with have 'm_min_articles' articles
						if (list_of_auto_generika.size()<m_min_articles) {
							int index = list_of_auto_generika.size();
							for (Map.Entry<String, Article> e : map_of_min_lead_articles.entrySet()) {
								if (!m_auto_generika_list.contains(e.getKey())) {
									if (index>=m_min_articles)
										break;
									list_of_similar_articles.add(e.getValue());
									index++;
								}
							}
						}
					}
				}					
				// Assert
				assert(list_of_similar_articles.size()>0);

				// Sort: Präferenz Arzt (rebate %)
				if (list_of_similar_articles.size()>m_min_articles) {
					// Sort according to rebate
					Collections.sort(list_of_similar_articles, new Comparator<Article>() {
						@Override
						public int compare(Article a1, Article a2) {
							float value1 = rebateForSupplier(a1);
							float value2 = rebateForSupplier(a2);	
							return (int)(value1 - value2);
						}
					});
					// Make sure that in the end we have more than 'm_min_articles' articles										
					int index = list_of_similar_articles.size();					
					if (index>0) {
						for (Iterator<Article> iterator = list_of_similar_articles.iterator(); iterator.hasNext(); ) {
							if (index>=m_min_articles)
								break;
							iterator.next();
							iterator.remove();
							index--;
						}
					}
				}
				// Assert
				assert(list_of_similar_articles.size()>0);
				
				if (list_of_similar_articles.size()>m_min_articles) {
					// Sort according to sales figure
					Article max_sales_article = Collections.max(list_of_similar_articles, new Comparator<Article>() {
						@Override
						public int compare(Article a1, Article a2) {
							float value1 = salesFiguresForSupplier(a1);
							float value2 = salesFiguresForSupplier(a2);						
							return (int)(value1 - value2);
						}
					});
					float max_sales = salesFiguresForSupplier(max_sales_article);
					if (max_sales>=0.0f) {						
						int index = list_of_similar_articles.size();
						for (Iterator<Article> iterator = list_of_similar_articles.iterator(); iterator.hasNext(); ) {
							if (index<=m_min_articles)
								break;
							Article article = iterator.next();							
							if (salesFiguresForSupplier(article)!=max_sales) {
								iterator.remove();
								index--;
							}
						}
					}
				}
				// Assert
				assert(list_of_similar_articles.size()>0);							

				// Sort: Präferenz Rose
				/*
				if (list_of_similar_articles.size()>m_min_articles) {
					Collections.sort(list_of_similar_articles, new Comparator<Article>(){
						@Override
						public int compare(Article a1, Article a2) {
							float value1 = rebateForSupplier(a1);
							float value2 = rebateForSupplier(a2);	
							return (int)(value1 - value2);
						}
					});
				}
				*/
				
				m_map_similar_articles.put(ean_code, list_of_similar_articles);
			}
			
			/*
			// TEST
			for (Map.Entry<String, List<Article>> s : m_map_similar_articles.entrySet()) {
				String ean = s.getKey();
				Article a1 = m_shopping_basket.get(ean);
				System.out.println("-----------------------");
				for (Article a2 : s.getValue()) {
					System.out.println(a2.getEanCode() + ": " + shippingStatus(a2, a1.getQuantity()) + " -> " + a2.getPackTitle() + " (" + a2.getSupplier() + ")");
				}
			}
			*/
		}
	}
	
	/**
	 * Constructor
	 */
	public ShoppingRose() {
		// Load sales figures file
		m_sales_figures_map = (new FileLoader()).loadRoseSalesFigures(Constants.ROSE_FOLDER + "Abverkaufszahlen.csv");
		// Load auto generika file
		m_auto_generika_list = (new FileLoader()).loadRoseAutoGenerika(Constants.ROSE_FOLDER + "Autogenerika.csv");
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
		// NOTE: The following line is necessary, because html setAttribute seems to be confused by "/"
		m_images_dir = m_images_dir.replaceAll("\\\\", "/");	
		// 
		if (Utilities.appLanguage().equals("de"))
			m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));
		else if (Utilities.appLanguage().equals("fr"))
			m_rb = ResourceBundle.getBundle("amiko_fr_CH", new Locale("fr", "CH"));	
		
	}
	
	public void setMaps(Map<String, Float> rebate_map, Map<String, Float> expenses_map) {
		m_rebate_map = (LinkedHashMap<String, Float>)rebate_map;
		m_expenses_map = (LinkedHashMap<String, Float>)expenses_map;
	}
	
	public void updateMapSimilarArticles(Map<String, List<Article>> similar_articles) {
		// Make a copy of the hash map
		m_map_similar_articles = new HashMap<String, List<Article>>(similar_articles);
		
		/*
		// TEST
		int index = 0;
		for (Map.Entry<String, List<Article>> entry : m_map_similar_articles.entrySet()) {
			System.out.println("\n" + entry.getKey() + " -> " + index + " (" + entry.getValue().size() + ")");
			System.out.println("-------------");
			for (Article a : entry.getValue()) {
				System.out.println(a.getEanCode() + " -> " + a.getAtcCode() + " -> " + a.getPackTitle());
			}
			index++;
		}
		*/		
	}
	
	public void setFilterState(boolean state) {
		m_filter_state = state;
	}
	
	public void updateSelectList(String ean) {
		if (!m_select_articles_list.contains(ean))
			m_select_articles_list.add(ean);
		else 
			m_select_articles_list.remove(ean);		
	}
	
	/**
	 * Html pages
	 */	
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<table style=\"background-color:#ffffff\" id=\"Warenkorb\" width=\"99%25\" >";
		
		String load_order_str = "";
		String fast_order_str[] = {"", "", "", "", ""};
	
		String show_all_button_str = "";
		String delete_all_button_str = "";
		String generate_pdf_str = "";
		String generate_csv_str = "";
		String checkout_str = "";
		
		String show_all_button_text = "";
		String delete_all_text = "";	
		String generate_pdf_text = "";
		String generate_csv_text = "";
		String checkout_text = "";
		
		float subtotal_buying_CHF = 0.0f;
		
		m_shopping_basket = shopping_basket;
				
		if (m_shopping_basket!=null && m_shopping_basket.size()>0) {
			int index = 1;						
			basket_html_str += "<tr style=\"background-color:lightskyblue;\">"
					+ "<td style=\"text-align:left; padding-top:8px; padding-bottom:8px; display:none;\"><b>" + m_rb.getString("ean") + "</b></td>"
					+ "<td colspan=\"2\" style=\"text-align:left; padding-top:8px; padding-bottom:8px;\"; width=\"40%\"><b>" + m_rb.getString("article") + "</b></td>"
					+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\"; width=\"5%\"><b>" + m_rb.getString("quantity") + "</b></td>"
					+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\"; width=\"8%\"><b>" + m_rb.getString("info") + "</b></td>"	
					+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\"; width=\"8%\"><b>" + m_rb.getString("price") + "</b></td>"	
					+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\"; width=\"8%\"><b>" + m_rb.getString("rebate") + "</b></td>"
					+ "<td style=\"text-align:right; padding-top:8px; padding-bottom:8px;\"; width=\"5%\"><b>Präferenz</b></td>"					
					+ "<td style=\"text-align:center; padding-top:8px; padding-bottom:8px;\"; width=\"5%\"><b>Frist</b></td>"	// Lieferfrist (Ampel)
					+ "<td style=\"text-align:center; padding-top:8px; padding-bottom:8px;\"; width=\"3%\"><img src=\"" + m_images_dir + "empty_icon_14.png\"></td>"	// Select icon	
					+ "<td style=\"text-align:center; padding-top:8px; padding-bottom:8px;\"; width=\"3%\"></td>"	// Delete icon					
					+ "</tr>";
												
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				
				// Get cash rebate
				float cr = getCashRebate(article);
				if (cr>=0.0f)
					article.setCashRebate(cr);
				// Set buying price
				article.setBuyingPrice(article.getExfactoryPriceAsFloat());														
				// For the sum
				subtotal_buying_CHF += (article.getTotBuyingPrice(cr));
				
				int quantity = article.getQuantity();															
				String tot_buying_price_CHF = Utilities.prettyFormat(article.getTotBuyingPrice(cr));
				String cash_rebate_percent = String.format("%.1f%%", article.getCashRebate());
				
				String ean_code = article.getEanCode();
				String flags_str = article.getFlags();
				String preference_str = "";
				if (isAutoGenerikum(ean_code))
					preference_str = "AG";
								
				// Shipping status
				Pair<String, String> shipping_status = shippingStatusColor(shippingStatus(article, quantity));

				// Select
				String select_str = "<img src=\"" + m_images_dir + "empty_icon_14.png\">";
				if (m_select_articles_list.contains(ean_code)) {
					select_str = "<img src=\"" + m_images_dir + "checkmark_icon_14.png\">";
				} 
				
				basket_html_str += "<tr id=\"" + ean_code + "\" style=\"background-color:lavender;\" " +
						"onmouseover=\"changeColorEven(this,true);this.style.cursor='pointer';\" " +
						"onmouseout=\"changeColorEven(this,false);\" " +
						"onclick=\"selectArticle(this);\">";												
				
				basket_html_str += "<td style=\"display:none;\">" + ean_code + "</td>"
						+ "<td colspan=\"2\">" + article.getPackTitle() + "</td>"															
						+ "<td style=\"text-align:right;\">" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:56px; text-align:right;\"" +
							" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this,2)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"					
						+ "<td style=\"text-align:right;\">" + flags_str + "</td>"
						+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
						+ "<td style=\"text-align:right; color:green\">" + cash_rebate_percent + "</td>"
						+ "<td style=\"text-align:right;\">" + preference_str + "</td>"
						+ "<td style=\"text-align:center;\"><img src=\"" + shipping_status.second + "\" /></td>"
						+ "<td style=\"text-align:center;\">" + select_str + "</td>"
						+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
							+ m_images_dir + "trash_icon_4.png\" /></button>" + "</td>";
				
				basket_html_str += "</tr>";	
				
				// article points to object which was inserted last...
				if (article!=null) {
					if (m_map_similar_articles.containsKey(ean_code)) {
						if (m_filter_state)
							sortSimilarArticles(quantity);
						List<Article> la = m_map_similar_articles.get(ean_code);
						for (Article a : la) {
							if (!a.getEanCode().equals(ean_code)) {
								cr = getCashRebate(a);
								if (cr>=0.0f)
									a.setCashRebate(cr);
								a.setBuyingPrice(a.getExfactoryPriceAsFloat());	
								a.setQuantity(quantity);
								cash_rebate_percent = String.format("%.1f%%", a.getCashRebate());
								shipping_status = shippingStatusColor(shippingStatus(a, a.getQuantity()));
								
								flags_str = a.getFlags();
								preference_str = "";
								if (isAutoGenerikum(a.getEanCode()))
									preference_str = "AG";
								
								// Select
								select_str = "<img src=\"" + m_images_dir + "empty_icon_14.png\">";
								if (m_select_articles_list.contains(a.getEanCode()))
									select_str = "<img src=\"" + m_images_dir + "checkmark_icon_14.png\">";
								
								String row_id = a.getEanCode() + "-" + ean_code;
								
								basket_html_str += "<tr id=\"" + row_id + "\" style=\"background-color:mistyrose;\" " +
										"onmouseover=\"changeColorOdd(this,true);this.style.cursor='pointer';\" " +
										"onmouseout=\"changeColorOdd(this,false);\" " +
										"onclick=\"selectArticle(this);\">";								
								
								basket_html_str += "<td style=\"display:none;\">" + a.getEanCode() + "</td>"
										+ "<td style=\"background-color:white; text-align:right;\"><img src=\"" + m_images_dir + "pointing-right.png\" /></td>"
										+ "<td>" + a.getPackTitle() + "</td>"															
										+ "<td style=\"text-align:right; padding-right:7px;\">" + a.getQuantity() + "</td>"					
										+ "<td style=\"text-align:right;\">" + flags_str + "</td>"										
										+ "<td style=\"text-align:right;\">" + Utilities.prettyFormat(a.getTotBuyingPrice(cr)) + "</td>"
										+ "<td style=\"text-align:right; color:green;\">" + cash_rebate_percent + "</td>"
										+ "<td style=\"text-align:right;\">" + preference_str + "</td>"
										+ "<td style=\"text-align:center;\"><img src=\"" + shipping_status.second + "\" /></td>"	
										+ "<td style=\"text-align:center;\">" + select_str + "</td>"
										+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"swapArticles('Warenkorb'," + a.getEanCode() + "," + ean_code + ")\"><img src=\"" 
											+ m_images_dir + "replace_icon.png\" /></button>" + "</td>";
						
								basket_html_str += "</tr>";
							}
						}							
					}
				}
				
 				index++;
			}				

			basket_html_str += "</table></form>";
						
			// Show all button
			show_all_button_str = "<td style=\"text-align:center;\"><div class=\"center\" id=\"show_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"showAll(this)\"><img src=\"" + m_images_dir + "show_all_icon_2.png\" /></button></div></td>";			
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
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"checkOut(this)\"><img src=\"" + m_images_dir + "checkout_icon.png\" /></button></div></td>";
			
			// Subtitles	
			show_all_button_text = "<td><div class=\"right\">" + "Komplett" + "</div></td>";
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
				
		String header_str = "<p><b>Total Bestellung: " + Utilities.prettyFormat(subtotal_buying_CHF) + " CHF</b></p>";
		
		String footnotes_str = "<hr><table style=\"background:white\">"
				+ "<tr><td style=\"text-align:center; padding-right:16px;\"><img src=\"" + m_images_dir + "circ_green.png\" /></td><td>" + "mit nächster Lieferung" + "</td></tr>"
				+ "<tr><td style=\"text-align:center; padding-right:16px;\"><img src=\"" + m_images_dir + "circ_yellow.png\" /></td><td>" + "wahrscheinlich mit nächster Lieferung" + "</td></tr>"
				+ "<tr><td style=\"text-align:center; padding-right:16px;\"><img src=\"" + m_images_dir + "circ_orange.png\" /></td><td>" + "Lieferfrist 2-7 Tage" + "</td></tr>"
				+ "<tr><td style=\"text-align:center; padding-right:16px;\"><img src=\"" + m_images_dir + "circ_red.png\" /></td><td>" + "Fehlt auf unbestimmte Zeit" + "</td></tr>"
				+ "<tr><td style=\"text-align:center; padding-right:16px;\"><img src=\"" + m_images_dir + "ausser_handel.png\" /></td><td>" + "ausser Handel" + "</td></tr>"
				+ "</table>";
		
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str+ "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_shopping_cart_str + "</head>"
				+ "<body>"
				+ "<div id=\"buttons\">" + load_order_str + fast_order_str[0] + fast_order_str[1] + fast_order_str[2] + fast_order_str[3] + fast_order_str[4] + "</div>"
				+ "<div id=\"header\" style=\"font-family:sans-serif;font-size:0.9em;margin-left:8px;\">" + header_str + "</div>"
				+ "<div id=\"shopping\">" + basket_html_str + "<br />"
				+ "<form><table class=\"container\"><tr>" + show_all_button_str + delete_all_button_str + generate_pdf_str + generate_csv_str + checkout_str + "</tr>"
				+ "<tr>" + show_all_button_text + delete_all_text + generate_pdf_text + generate_csv_text + checkout_text + "</tr></table></form>"
				+ footnotes_str
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
		int quantity = article.getQuantity();
		Pair<String, String> shipping_status = shippingStatusColor(shippingStatus(article, quantity));

		String js = "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[4].innerHTML=\"" + tot_buying_price_CHF + "\";" 
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").cells[5].innerHTML=\"" + cash_rebate_percent + "\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").getElementsByTagName('img')[0].setAttribute('src',\"" + shipping_status.second + "\");";
				// + "alert(document.getElementById('Warenkorb').rows.namedItem(\"" + ean_code + "\").getElementsByTagName('img')[0].getAttribute('src'));";
				
		if (article!=null) {
			if (m_map_similar_articles.containsKey(ean_code)) {
				if (m_filter_state)
					sortSimilarArticles(quantity);
				List<Article> la = m_map_similar_articles.get(ean_code);
				for (Article a : la) {
					String simil_ean_code = a.getEanCode();
					if (!simil_ean_code.equals(ean_code)) {
						a.setQuantity(quantity);
						a.setBuyingPrice(a.getExfactoryPriceAsFloat());	
						cr = getCashRebate(a);
						if (cr>=0.0f)
							a.setCashRebate(cr);
						cash_rebate_percent = String.format("%.1f%%", a.getCashRebate());

						tot_buying_price_CHF = Utilities.prettyFormat(a.getTotBuyingPrice(cr));						
						
						shipping_status = shippingStatusColor(shippingStatus(a, quantity));
						
						String row_id = simil_ean_code + "-" + ean_code;
						
						js += "document.getElementById('Warenkorb').rows.namedItem(\"" + row_id + "\").cells[3].innerHTML=\"" + quantity + "\";"
								+ "document.getElementById('Warenkorb').rows.namedItem(\"" + row_id + "\").cells[5].innerHTML=\"" + tot_buying_price_CHF + "\";" 
								+ "document.getElementById('Warenkorb').rows.namedItem(\"" + row_id + "\").cells[6].innerHTML=\"" + cash_rebate_percent + "\";"
								+ "document.getElementById('Warenkorb').rows.namedItem(\"" + row_id + "\").getElementsByTagName('img')[1].setAttribute('src',\"" + shipping_status.second + "\");";

					}
				}							
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

		String js = "document.getElementById('Header').innerHTML=\"<p><b>Total Bestellung: " + subtotal_buying_CHF + " CHF</b></p>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[2].innerHTML=\"<b>" + tot_quantity + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[3].innerHTML=\"<b>" + subtotal_buying_CHF + "</b>\";"
				+ "document.getElementById('Warenkorb').rows.namedItem(\"Total\").cells[4].innerHTML=\"<b>" + total_cash_rebate_percent + "</b>\";";		
		
		return js;
	}
	
	public String getCheckoutUpdateJS() {
		String js = "";
		return js;
	}
}
