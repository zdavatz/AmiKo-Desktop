package com.maxl.java.amikodesk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ComparisonCart implements java.io.Serializable {

	private static String m_images_dir = System.getProperty("user.dir") + "/images/";	
	private static Observer m_observer;	
	private static Preferences m_prefs = null;
	
	// Map of eancodes vs articles
	private static Map<String, Article> m_comparison_basket = null;
	// List of eancodes that will be uploaded...
	private static List<String> m_upload_list = new ArrayList<String>();
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_str = null;	

	private String m_el;
	private String m_ep;
	private String m_es;
	
	private static int button_state[] = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	
	public ComparisonCart() {
		// Load javascripts
		m_jscripts_str = FileOps.readFromFile(Constants.JS_FOLDER + "rose_callbacks.js");
		// Load shopping cart css style sheet
		m_css_str = "<style type=\"text/css\">" + FileOps.readFromFile(Constants.ROSE_SHEET) + "</style>";
		// Prefs
		m_prefs = Preferences.userRoot().node(SettingsPage.class.getName());
		// 
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\access_rose.ami.ser");
		if (encrypted_msg==null) {		
			encrypted_msg = FileOps.readBytesFromFile(Constants.ROSE_FOLDER + "access_rose.ami.ser");
			System.out.println("Loading access_rose.ami.ser from default folder...");
		}
		// Decrypt and deserialize
		if (encrypted_msg!=null) {
			m_el = "P_ywesee";
			Crypto crypto = new Crypto();
			byte[] serialized_bytes = crypto.decrypt(encrypted_msg);
			TreeMap<String, String> map = new TreeMap<String, String>();
			map = (TreeMap<String, String>)(FileOps.deserialize(serialized_bytes));					
			if (map!=null && map.containsKey(m_el)) {
				m_ep = ((String)map.get(m_el)).split(";")[0];
				m_es = ((String)map.get(m_el)).split(";")[1];
			} else {
				System.out.println("ComparisonCart: No map or no key in map!");
			}
		}
	}
	
	public void setComparisonBasket(Map<String, Article> comparison_basket) {
		m_comparison_basket = comparison_basket;
		m_upload_list.clear();
	}
	
	public void addObserver(Observer observer) {
		m_observer = observer;
	}
	
	protected void notifyObserver(String msg) {
		m_observer.update(null, msg);
	}
	
	public void clearUploadList() {
		m_upload_list.clear();
	}
	
	public void updateUploadList(String eancode) {
		if (m_upload_list.contains(eancode))
			m_upload_list.remove(eancode);
		else
			m_upload_list.add(eancode);
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
			// Artikel, Stärke, Packung, PC, GTIN (EAN), RBP, PP, Lieferant, Lager, Likes, kürz. Verfall
			String sort_name_button = 		"<button style=\"text-align:left;\"  onclick=\"sortCart(this,1)\"><b>Artikel</b></button>";		
			String sort_unit_button = 		"<button style=\"text-align:right;\" onclick=\"sortCart(this,2)\"><b>Stärke</b></button>";	
			String sort_size_button = 		"<button style=\"text-align:right;\" onclick=\"sortCart(this,3)\"><b>Packung</b></button>";					
			String sort_pharma_button = 	"<button style=\"text-align:right;\" onclick=\"sortCart(this,4)\"><b>Pharma</b></button>";
			String sort_ean_button = 		"<button style=\"text-align:right;\" onclick=\"sortCart(this,5)\"><b>EAN</b></button>";
			String sort_rb_price_button =	"<button style=\"text-align:right;\" onclick=\"sortCart(this,6)\"><b>RBP</b></button>";
			String sort_pu_price_button =   "<button style=\"text-align:right;\" onclick=\"sortCart(this,7)\"><b>PP</b></button>";
			String sort_supplier_button = 	"<button style=\"text-align:left;\"  onclick=\"sortCart(this,8)\"><b>Lieferant</b></button>";		
			String sort_stock_button = 		"<button style=\"text-align:right;\" onclick=\"sortCart(this,9)\"><b>Lager</b></button>";
			String sort_likes_button =		"<button style=\"text-align:right;\" onclick=\"sortCart(this,10)\"><b>Likes</b></button>";
			
			basket_html_str += "<tr style=\"background-color:lightgray;\">"
					+ "<td style=\"text-align:left; padding-bottom:8px;\" width=\"30%\">" + sort_name_button + "</td>"						
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"8%\">" + sort_unit_button + "</td>"							
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"12%\">" + sort_size_button + "</td>"							
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"8%\">" + sort_pharma_button + "</td>"	
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"8%\">" + sort_ean_button + "</td>"						
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"5%\">" + sort_rb_price_button + "</td>"	
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"5%\">" + sort_pu_price_button + "</td>"	
					+ "<td style=\"text-align:left; padding-bottom:8px;\" width=\"30%\">" + sort_supplier_button + "</td>"																								
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"5%\">" + sort_stock_button + "</td>"
					+ "<td style=\"text-align:right; padding-bottom:8px;\" width=\"5%\">" + sort_likes_button + "</td>"						
					+ "<td style=\"text-align:center; padding-bottom:8px;\"; width=\"3%\"></td>"					
					+ "</tr>";

			int num_rows = 0;
			for (Map.Entry<String, Article> entry : m_comparison_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();			
				if (num_rows%2==0) {
					basket_html_str += "<tr id=\"" + ean_code + "\" style=\"background-color:blanchedalmond;\" " +
							"onmouseover=\"changeColorEven(this,true);\" " +
							"onmouseout=\"changeColorEven(this,false);\" " +
							"onclick=\"uploadArticle(this);\">";
				} else { 
					basket_html_str += "<tr id=\"" + ean_code + "\" style=\"background-color:whitesmoke;\" " +
							"onmouseover=\"changeColorOdd(this,true);cursor:pointer;\" " +
							"onmouseout=\"changeColorOdd(this,false);\" " +
							"onclick=\"uploadArticle(this);\">";
				}
				// Artikel, Stärke, Packung, PC, GTIN (EAN), RBP, PP, Lieferant, Lager, kürz. Verfall
				basket_html_str += "<td style=\"text-align:left;\">" + article.getPackTitle() + "</td>"	
						+ "<td style=\"text-align:right;\">" + article.getPackUnit() + "</td>"														
						+ "<td style=\"text-align:right;\">" + article.getPackSize() + " " + article.getPackGalen() + "</td>"	
						+ "<td style=\"text-align:right;\">" + article.getPharmaCode() + "</td>"														
						+ "<td style=\"text-align:right;\">" + article.getEanCode() + "</td>"																										
						+ "<td style=\"text-align:right;\">" + String.format("%.2f",article.getExfactoryPriceAsFloat()) + "</td>"
						+ "<td style=\"text-align:right;\">" + String.format("%.2f",article.getPublicPriceAsFloat()) + "</td>"	
						+ "<td style=\"text-align:left;\">" + article.getSupplier() + "</td>"						
						+ "<td style=\"text-align:right;\">" + article.getItemsOnStock() + "</td>"
						+ "<td style=\"text-align:right;\">" + article.getLikes() + "</td>";	
				
				if (m_upload_list.contains(ean_code))			
					basket_html_str += "<td style=\"text-align:center;\"><img src=\"" + m_images_dir + "checkmark_icon_14.png\"></td>";						
				else
					basket_html_str += "<td style=\"text-align:center;\"><img src=\"" + m_images_dir + "empty_icon_14.png\"></td>";	
				basket_html_str += "</tr>";
				if (atc_code.isEmpty())
					atc_code = article.getAtcCode();
				if (atc_class.isEmpty())
					atc_class = article.getAtcClass();
				num_rows++;
			}
		}
		
		// Button 1
		String show_all_button = "<td style=\"text-align:center;\"><div class=\"center\" id=\"show_all\">"
				+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"showAll(this)\"><img src=\"" 
				+ m_images_dir + "show_all_icon.png\" /></button></div></td>";
		String show_all_text = "<td><div class=\"center\">" + "Komplett" + "</div></td>";
		
		// Button 2
		String sort_everything_button = "<td style=\"text-align:center;\"><div class=\"center\" id=\"sort_everything\">"
				+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"sortCart(this,0)\"><img src=\"" 
				+ m_images_dir + "filter_icon.png\" /></button></div></td>";
		String sort_everything_text = "<td><div class=\"center\">" + "Sortieren" + "</div></td>";
		
		// Button 3
		String upload_button = "";
		String upload_text = "";
		if (m_upload_list.size()>0) {
			upload_button = "<td style=\"text-align:center;\"><div class=\"center\" id=\"upload_server\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"uploadToServer(this)\"><img src=\"" 
					+ m_images_dir + "upload2_icon.png\" /></button></div></td>";	
			upload_text = "<td><div class=\"center\">" + "Upload" + "</div></td>";;
		}

		String atc_html_str = "ATC Code: " + atc_code + " [" + atc_class + "]";						
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str + "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_str + "</head>"
				+ "<body>"
				+ "<div id=\"compare\">"
				+ "<p style=\"font-size:0.85em; padding-left:0.4em;\">" + atc_html_str + "</p>"				
				+ "<form><table class=\"container\"><tr>" + show_all_button + sort_everything_button + upload_button + "</tr>"
				+ "<tr>" + show_all_text + sort_everything_text + upload_text + "</tr></table></form>"
				+ "<div>" + basket_html_str + "</div>"
				+ "</div></body>"
				+ "</html>";		
		
		return m_html_str;
	}

	public void uploadToServer() {
		if (m_upload_list.size()>0) {
			String string_to_write = "";
			for (String s : m_upload_list) {
				string_to_write += s + "\n";
			}
			// Get time stamp
			DateTime dT = new DateTime();
			DateTimeFormatter fmt = DateTimeFormat.forPattern("ddMMyyyy'T'HHmmss");
			// Generate user id
			String gln_code = m_prefs.get("glncode", "1234567899999");
			String id = "99999";
			if (gln_code.length()==13)
				id = gln_code.substring(8);
			// Generate file name
			String file_name = "rose_" + fmt.print(dT) + "_" + id +".csv";		
			// Save m_upload_list to file
			String local_dir = Utilities.appDataFolder() + "/rose/";
			try {				
				FileOps.writeToFile(string_to_write, local_dir, file_name, "UTF-8"); 
			} catch(IOException e) {
				e.printStackTrace();
			}
			// Upload to server
			uploadToFTPServer(file_name, local_dir + file_name);
		}
	}
	
	private void uploadToFTPServer(String remote_file, String local_path) {
		FTPClient ftp_client = new FTPClient();
	    try {
	    	ftp_client.connect(m_es, 21);
	    	ftp_client.login(m_el, m_ep);
	    	ftp_client.enterLocalPassiveMode(); 
	    	ftp_client.changeWorkingDirectory("ywesee in");
	    	ftp_client.setFileType(FTP.BINARY_FILE_TYPE);
            
            int reply = ftp_client.getReplyCode();                        
            if (!FTPReply.isPositiveCompletion(reply)) {
            	ftp_client.disconnect();
                System.err.println("FTP server refused connection.");
                return;
            } 
            
            File local_file = new File(local_path); 
            InputStream is = new FileInputStream(local_file); 
            System.out.print("Uploading file " + remote_file + " to server " + m_es + "... ");

            boolean done = ftp_client.storeFile(remote_file, is);
            if (done) {
                System.out.println("file uploaded successfully.");
                notifyObserver("Die Daten wurden erfolgreich übermittelt!");
            }
            else {
            	System.out.println("error.");
                notifyObserver("Fehler beim übermitteln den Daten...");            	
            }
            is.close();            
	     } catch (IOException ex) {
	    	 System.out.println("Error: " + ex.getMessage());
	         ex.printStackTrace();
	     } finally {
	    	 try {
	    		 if (ftp_client.isConnected()) {
	    			 ftp_client.logout();
	                 ftp_client.disconnect();
	    		 }
	    	 } catch (IOException ex) {
	    		 ex.printStackTrace();
	    	 }
	     }
	}
	
	/**
	 * Sorting according to package name (Präparatname)
	 */
	public int sortName(Article a1, Article a2) {
		return (a1.getPackTitle())
				.compareTo(a2.getPackTitle());
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
	
	/**
	 * Generic sorts
	 */
	public int sortFloats(float f1, float f2) {
		return Float.valueOf(f1)
				.compareTo(f2);
	}
	
	public int sortInts(int i1, int i2) {
		return Integer.valueOf(i1)
				.compareTo(i2);
	}
	
	public int sortStrings(String s1, String s2) {
		return new AlphanumComp().compare(s1, s2);
	}
	
	/**
	 * Main sort function
	 * @param type
	 * @param sort
	 */
	public void sortCart(int type, boolean sort) {
		Set<Entry<String, Article>> set = m_comparison_basket.entrySet();
		List<Entry<String, Article>> list_of_entries = new ArrayList<Entry<String, Article>>(set);

		// Toggle state
		if (sort==true)
			button_state[type] = -button_state[type];
		final int state = button_state[type];
		
		/**
		 * Types
		 *  1: Artikel 
		 *  2: Stärke
		 *  3: Packung
		 *  4: PharmaCode
		 *  5: GTIN (EAN) 
		 *  6: RBP
		 *  7: PP
		 *  8: Lieferant
		 *  9: Lager
		 * 10: kürz. Verfall
		 */
		
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
			// Packungsname (Artikel)
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortName(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==2) {
			// Packung / Unit
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortUnit(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==3) {
			// Stärke / Size
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortSize(a1.getValue(), a2.getValue());
				}
			});

		} else if (type==4) {
			// Pharmacode
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortStrings(a1.getValue().getPharmaCode(), 
							a2.getValue().getPharmaCode());
				}
			});	
		} else if (type==5) {			
			// Eancode
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortStrings(a1.getValue().getEanCode(), 
							a2.getValue().getEanCode());
				}
			});				
		} else if (type==6) {
			// Rose Basis Preis
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortPrice(a1.getValue(), a2.getValue());
				}
			});			
		} else if (type==7) {
			// Publikumspreis
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortFloats(a1.getValue().getPublicPriceAsFloat(), 
							a2.getValue().getPublicPriceAsFloat());
				}
			});						
		} else if (type==8) {
			// Hersteller / Lieferant
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortSupplier(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==9) {
			// Lagerbestand / Stock
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortStock(a1.getValue(), a2.getValue());
				}
			});
		} else if (type==10) {
			// Likes
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return state*sortInts(a1.getValue().getLikes(), a2.getValue().getLikes());
				}
			});
		} else if (type==11) {
			// kürz. Verfall
			Collections.sort(list_of_entries, new Comparator<Entry<String, Article>>() {
				@Override
				public int compare(Entry<String, Article> a1, Entry<String, Article> a2) {
					return 0;
				}
			});		
		}
		
		m_comparison_basket.clear();
		for (Entry<String, Article> e : list_of_entries) 
			m_comparison_basket.put(e.getKey(), e.getValue());
	}
}
