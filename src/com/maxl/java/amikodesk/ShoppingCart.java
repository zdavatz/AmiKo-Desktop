package com.maxl.java.amikodesk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.maxl.java.shared.Conditions;

/**
 * Abstract shopping cart class. This class cannot be instantiated directly.
 * @author Max
 *
 */
public abstract class ShoppingCart {

	/**
	 * This class is used to calculate the total costs (incl. shipping cost) per article manufacturer
	 * @author Max
	 *
	 */
	protected class Owner {		
		
		String author;
		float subtotal_CHF;
		float vat25_CHF;
		float vat80_CHF;
		float shipping_CHF;		
		String shipping_type;		// AZ, BZ: gratis, A: A-Post, B: B-Post, E: Express
		boolean shipping_free;
		
		public Owner(String auth, float subtotal, float vat25, float vat80, float shipping, String type) {
			author = auth.toLowerCase();
			subtotal_CHF = subtotal;
			vat25_CHF = vat25;
			vat80_CHF = vat80;
			shipping_CHF = shipping;
			shipping_type = type;
		}
		
		public void updateShippingCosts(char category) {
			if (author.contains("desitin")) {
				// Default
				shipping_CHF = 0.0f;
				shipping_type = "AZ";
				shipping_free = true;
				// 
				switch(category) {
				case 'S':	// Spitalapotheke (Spital)
					if (subtotal_CHF<100.0f) {
						shipping_CHF = 10.0f;
						shipping_type = "A";
						shipping_free = false;
					}
					break;
				case 'P':	// Praxisapotheke (Arzt)
					if (subtotal_CHF<200.0f) {
						shipping_CHF = 15.0f;
						shipping_type = "A";
						shipping_free = false;
					}
					break;
				case 'O':	// Offizinalapotheke
					if (subtotal_CHF<1000.0f) {
						shipping_CHF = 15.0f;
						shipping_type = "A";	
						shipping_free = false;
					}
					break;
				case 'G':	// Grossist
					if (subtotal_CHF<500.0f) {
						shipping_CHF = 15.0f;
						shipping_type = "A";	
						shipping_free = false;
					} else if (subtotal_CHF>=500.0f && subtotal_CHF<=5000.0f) {
						shipping_CHF = 15.0f + subtotal_CHF*0.01f;
						shipping_type = "A";
						shipping_free = false;
					}
					break;
				}				
			}
		}
	}
	
	protected ResourceBundle m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));
	
	protected boolean m_agbs_accepted = false;
	
	protected int m_cart_index = 1;
	protected int m_margin_percent = 80;	// default
	
	protected String m_customer_gln_code = "";
	protected String m_application_data_folder;
	
	protected Map<String, Article> m_shopping_basket = null;
	protected Map<String, Owner> m_map_owner_total = null;
		
	/**
	 *  Virtual methods - implement in derived class, if required.
	 */
	public void loadFiles() {
		// VIRTUAL
	}
	
	public void setMap(Map<String, Float> conditions) {
		// VIRTUAL
	}
	
	public void setMaps(Map<String, String> glns, TreeMap<String, Conditions> conditions) {
		// VIRTUAL
	}
	
	public void setMaps(Map<String, Float> conditions_A, Map<String, Float> conditions_B) {
		// VIRTUAL
	}
	
	public void updateMapSimilarArticles(Map<String, List<Article>> similar_articles) {
		// VIRTUAL
	}
	
	public void setUserCategory(String cat) {
		// VIRTUAL
	}
	
	public void setFilterState(boolean state) {
		// VIRTUAL
	}
	
	public void updateSelectList(String ean) {
		// VIRTUAL
	}
	
	public List<Article> getSelectList() {
		return null;
	}
	
	public List<String> getAssortList(String ean_code) {
		return null;
	}
	
	public Map<String, String> getAssortedArticles(String ean_code) {
		return null;
	}
	
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		return null;
	}
	
	public String getRowUpdateJS(String ean_code, Article article) {
		return null;
	}
	
	public String getTotalsUpdateJS() {
		return null;
	}
	
	public String checkoutHtml(HashMap<String, Address> address_map) {
		return null;
	}
	
	public String getCheckoutUpdateJS(String author, String shipping_type) {
		return null;
	}
	
	/**
	 *  Getter and setters
	 */
	public void setAgbsAccepted(boolean a) {
		m_agbs_accepted = a;
	}
	
	public boolean getAgbsAccepted() {
		return m_agbs_accepted;
	}
	
	public void setCartIndex(int index) {
		m_cart_index = index;
	}
	
	public int getCartIndex() {
		return m_cart_index;
	}
	
	public void setShoppingBasket(Map<String, Article> shopping_basket) {
		synchronized(this) {
			m_shopping_basket = shopping_basket;
		}
	}
    
	public ResourceBundle getRB() {
		return m_rb;
	}
	
	public void setCustomerGlnCode(String code) {
		m_customer_gln_code = code;
	}
	
	public String getCustomerGlnCode() {
		return m_customer_gln_code;
	}
		
	public Map<String, Article> getShoppingBasket() {
		return m_shopping_basket;
	}
	
	public void setMarginPercent(int margin) {
		m_margin_percent = margin;
	}
	
	/**
	 *  All other methods
	 */
	/**
	 * Updates authors with costs and vats
	 * @param authors_list
	 * @return
	 */
	public List<Author> updateAuthors(final List<Author> authors_list) {
		List<Author> list_of_authors = new ArrayList<Author>(authors_list);
		for (Map.Entry<String, Owner> e : m_map_owner_total.entrySet()) { 
			String author_name = e.getKey().toLowerCase();
			Owner owner = e.getValue();
			for (Author a : authors_list) {
				if (author_name.contains(a.getName().toLowerCase())) {
					Author author = new Author(a);
					author.setCosts(owner.subtotal_CHF, owner.vat25_CHF, owner.vat80_CHF, owner.shipping_CHF, owner.shipping_type);
					list_of_authors.remove(a);
					list_of_authors.add(author);
				}
			}
		}		
		return list_of_authors;
	}
	
	public void printShoppingBasket() {
		if (m_shopping_basket!=null) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				String eancode = entry.getKey();
				Article article = m_shopping_basket.get(eancode);
				System.out.println("[" + article.getEanCode() + "] " + article.getPackTitle() + ": " + article.getQuantity() + " (" + article.getDraufgabe() + ")");
			}
		}
	}
	
	public void save(Map<String, Article> basket) {
		m_shopping_basket = basket;
		DateTime dT = new DateTime();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("ddMMyyyy'T'HHmmss");
		String subfolder = "";
		if (!m_customer_gln_code.isEmpty())
			subfolder = "\\" + m_customer_gln_code;
		String path_name = m_application_data_folder + "\\shop" + subfolder;
		File wdir = new File(path_name);
		if (!wdir.exists())
			wdir.mkdirs();
		File file = null;
		if (Utilities.appLanguage().equals("de"))
			file = new File(path_name + "\\WK" + fmt.print(dT) + ".ser");
		else if (Utilities.appLanguage().equals("fr"))
			file = new File(path_name + "\\PA" + fmt.print(dT) + ".ser");
		if (file != null) {
			String filename = file.getAbsolutePath();
			byte[] serialized_bytes = FileOps.serialize(m_shopping_basket);
			if (serialized_bytes != null) {
				FileOps.writeBytesToFile(filename, serialized_bytes);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Article> loadWithIndex(final int n) {
		int index = n;
		// If n<0 then load default cart
		if (index<0)
			index = m_cart_index;
		String subfolder = "";
		if (!m_customer_gln_code.isEmpty())
			subfolder = "\\" + m_customer_gln_code;
		File file = new File(Utilities.appDataFolder() + "\\shop" + subfolder + "\\korb" + index + ".ser");
		if (file.exists()) {
			// Load and deserialize m_shopping_basket
			String filename = file.getAbsolutePath();
			byte[] serialized_bytes = FileOps.readBytesFromFile(filename);
			if (serialized_bytes!=null) {
				m_shopping_basket = (LinkedHashMap<String, Article>)FileOps.deserialize(serialized_bytes);
				// If there are problems deserializing, delete file and generate new shopping cart
				if (m_shopping_basket==null) {
					System.out.println("Shopping cart " + index + " is corrupted... deleting it!");
					file.delete();
					m_shopping_basket = new LinkedHashMap<String, Article>();					
				}
				return m_shopping_basket;							
			}
		} else {
			System.out.println("Shopping cart " + index + " does not exist... generating new one!");
			m_shopping_basket = new LinkedHashMap<String, Article>();
			return m_shopping_basket;
		}
		return null;
	}
		
	public void saveWithIndex(Map<String, Article> basket) {
		m_shopping_basket = basket;
		int index = getCartIndex();
		if (index>0) {
			String subfolder = "";	
			if (!m_customer_gln_code.isEmpty())
				subfolder = "\\" + m_customer_gln_code;			
			String path_name = m_application_data_folder + "\\shop" + subfolder;
			File wdir = new File(path_name);
			if (!wdir.exists())
				wdir.mkdirs();
			File file = new File(path_name + "\\korb" + index + ".ser");
			if (file != null) {
				String filename = file.getAbsolutePath();
				byte[] serialized_bytes = FileOps.serialize(m_shopping_basket);
				if (serialized_bytes != null) {
					FileOps.writeBytesToFile(filename, serialized_bytes);
				}
			}
		}
	}
}
