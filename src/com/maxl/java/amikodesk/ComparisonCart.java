package com.maxl.java.amikodesk;

import java.util.ArrayList;
import java.util.Arrays;
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

	private static String m_images_dir = System.getProperty("user.dir") + "/images/";	
	
	private static Map<String, Article> m_comparison_basket = null;
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_str = null;	
	
	private static int button_state[] = new int[] {1, 1, 1, 1, 1, 1, 1, 1};
	
	public ComparisonCart() {
		// Load javascripts
		m_jscripts_str = FileOps.readFromFile(Constants.JS_FOLDER + "rose_callbacks.js");
		// Load shopping cart css style sheet
		m_css_str = "<style type=\"text/css\">" + FileOps.readFromFile(Constants.ROSE_SHEET) + "</style>";
		// 
		if (Utilities.appLanguage().equals("de"))
			m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));
		else if (Utilities.appLanguage().equals("fr"))
			m_rb = ResourceBundle.getBundle("amiko_fr_CH", new Locale("fr", "CH"));	
	}
	
	public void setComparisonBasket(Map<String, Article> comparison_basket) {
		m_comparison_basket = comparison_basket;
	}
	
	/**
	 * Note: assumes that comparison_basket has been set already!!
	 * @return
	 */
	public String updateComparisonCartHtml() {
		String atc_code = "";
		String atc_class = "";
		
		String basket_html_str = "<table id=\"Warenkorb\" width=\"99%25\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";

		if (m_comparison_basket!=null && m_comparison_basket.size()>0) {			
			
			String sort_name_button = "<button style=\"text-align:left;\" onclick=\"sortCart(this,1)\"><b>Artikel</b></button>";	
			String sort_supplier_button = "<button style=\"text-align:left;\" onclick=\"sortCart(this,2)\"><b>Lieferant</b></button>";		
			String sort_unit_button = "<button style=\"text-align:right;\" onclick=\"sortCart(this,3)\"><b>Stärke</b></button>";	
			String sort_size_button = "<button style=\"text-align:right;\" onclick=\"sortCart(this,4)\"><b>Packung</b></button>";	
			String sort_price_button = "<button	style=\"text-align:right;\" onclick=\"sortCart(this,5)\"><b>RBP</b></button>";
			String sort_stock_button = "<button	style=\"text-align:right;\" onclick=\"sortCart(this,6)\"><b>Lager</b></button>";
			
			basket_html_str += "<tr style=\"background-color:lightgray;\">"
					+ "<td padding-bottom:8px;\" width=\"35%\">" + sort_name_button + "</td>"							
					+ "<td style=\"text-align:left; padding-bottom:8px;\" width=\"7%\"><b>ATC Code</b></td>"										
					+ "<td padding-bottom:8px;\" width=\"30%\">" + sort_supplier_button + "</td>"			
					+ "<td style=\"text-align:right;\" padding-bottom:8px;\" width=\"10%\">" + sort_unit_button + "</td>"						
					+ "<td style=\"text-align:right;\" padding-bottom:8px;\" width=\"14%\">" + sort_size_button + "</td>"																				
					+ "<td style=\"text-align:right;\" padding-bottom:8px;\" width=\"6%\">" + sort_price_button + "</td>"		
					+ "<td style=\"text-align:right;\" padding-bottom:8px;\" width=\"5%\">" + sort_stock_button + "</td>"	
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
						+ "<td style=\"text-align:left;\">" + article.getAtcCode() + "</td>"								
						+ "<td style=\"text-align:left;\">" + article.getSupplier() + "</td>"					
						+ "<td style=\"text-align:right;\">" + article.getPackUnit() + "</td>"								
						+ "<td style=\"text-align:right;\">" + article.getPackSize() + " " + article.getPackGalen() + "</td>"														
						+ "<td style=\"text-align:right;\">" + String.format("%.2f",article.getExfactoryPriceAsFloat()) + "</td>"
						+ "<td style=\"text-align:right;\">" + article.getItemsOnStock() + "</td>";											
				basket_html_str += "</tr>";
				if (atc_code.isEmpty())
					atc_code = article.getAtcCode();
				if (atc_class.isEmpty())
					atc_class = article.getAtcClass();
				num_rows++;
			}
		}

		// Sorting buttons
		String sort_everything_button = "<button style=\"padding:0.3em;text-align:left;\" onclick=\"sortCart(this,0)\">" 
				+ "<img src=\"" + m_images_dir + "sort_up.png\" /><img src=\"" + m_images_dir + "sort_down.png\" />"				
				+ "Sortieren nach Lieferant, Stärke, Packung und Preis</button>";
		
		String atc_html_str = "ATC Code: " + atc_code + " [" + atc_class + "]";						
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str + "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_str + "</head>"
				+ "<body><div id=\"compare\">"
				+ "<p>" + atc_html_str + "</p>"
				+ "<div id=\"buttons\">" + sort_everything_button + "</div>"
				+ "<div>" + basket_html_str + "<br /></div>"
				+ "</div></body></html>";		
		
		return m_html_str;
	}

	/**
	 * Sorting according to Lieferant
	 */
	public int sortSupplier(Article a1, Article a2) {
		final List<String> custom_order = Arrays.asList("sandoz", "helvepharm", "mepha", "actavis");
		Collections.reverse(custom_order);		
		boolean custom = false;
		String supplier1 = a1.getSupplier().toLowerCase();
		String supplier2 = a2.getSupplier().toLowerCase();
		for (String l : custom_order) {
			if (supplier1.contains(l)) {
				supplier1 = l;
				custom = true;
			}
			if (supplier2.contains(l)) {
				supplier2 = l;
				custom = true;							
			}
		}
		if (custom==true) {
			return -Integer.valueOf(custom_order.indexOf(supplier1))
					.compareTo(Integer.valueOf(custom_order.indexOf(supplier2)));
		} else {
			return supplier1
					.compareTo(supplier2);
		}
	}

	/**
	 * Sorting according to stock (Lagerbestand)
	 */
	public int sortStock(Article a1, Article a2) {
		return (Integer.valueOf(a1.getItemsOnStock())
				.compareTo(a2.getItemsOnStock()));
	}
		
	/*
	 * Sort according to "Stärke" (unit, dosierung)
	 */
	public int sortUnit(Article a1, Article a2) {
		/*
		return a1.getPackUnit()
				.compareTo(a2.getPackUnit());
		*/
		return (new AlphanumComp().compare(a1.getPackUnit(), a2.getPackUnit()));
	}
	
	/*
	 * Sort according to package size (e.g. how many tablets, drops, etc.)
	 */
	public int sortSize(Article a1, Article a2) {
		/*
		return (Integer.valueOf(a1.getPackSize())
				.compareTo(Integer.valueOf(a2.getPackSize())));
		*/
		String o1 = a1.getPackSize() + " " + a1.getPackGalen();
		String o2 =  a2.getPackSize() + " " + a2.getPackGalen();
		return (new AlphanumComp().compare(o1, o2));
	}
	
	/**
	 * Sorting according to price
	 */
	public int sortPrice(Article a1, Article a2) {
		return Float.valueOf(a1.getExfactoryPriceAsFloat())
				.compareTo(a2.getExfactoryPriceAsFloat());
	}
		
	/**
	 * Sorting according to package name (Präparatname)
	 */
	public int sortName(Article a1, Article a2) {
		return (a1.getPackTitle())
				.compareTo(a2.getPackTitle());
	}
	
	public void sortCart(int type) {
		Set<Entry<String, Article>> set = m_comparison_basket.entrySet();
		List<Entry<String, Article>> list_of_entries = new ArrayList<Entry<String, Article>>(set);

		// Toggle state
		button_state[type] = -button_state[type];
		final int state = button_state[type];
		
		// Sort...
		if (type==0) {
			// Packungsname
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					Article article1 = a1.getValue();
					Article article2 = a2.getValue();
					int c = sortSupplier(article1, article2);
					/*
					if (c==0)
						c = -sortStock(article1, article2);
					*/
					if (c==0)
						c = sortUnit(article1, article2);
					if (c==0)
						c = sortSize(article1, article2);
					if (c==0)
						c = sortPrice(article1, article2);					
					return state*c;
				}
			});
		} else if (type==1) {
			// Packungsname
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortName(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==2) {
			// Hersteller / Lieferant
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortSupplier(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==3) {
			// Unit
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortUnit(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==4) {
			// Packung / Size
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortSize(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==5) {
			// Preis
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortPrice(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==6) {
			// Lagerbestand / Stock
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortStock(a1.getValue(), a2.getValue());
				}
			});
		}
		
		m_comparison_basket.clear();
		for (Entry<String, Article> e : list_of_entries) 
			m_comparison_basket.put(e.getKey(), e.getValue());
	}
}
