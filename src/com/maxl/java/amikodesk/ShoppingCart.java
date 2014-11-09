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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities.EscapeMode;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.maxl.java.shared.Conditions;

public class ShoppingCart implements java.io.Serializable {
	
	private static Map<String, Article> m_shopping_basket = null;
	private static Map<String, Conditions> m_map_conditions = null;
	
	private static String m_html_str = "";
	private static String m_jscripts_str = null;
	private static String m_css_shopping_cart_str = null;		
	
	private static Font font_norm_10 = FontFactory.getFont("Helvetica", 10, Font.NORMAL);
	private static Font font_bold_10 = FontFactory.getFont("Helvetica", 10, Font.BOLD);
	private static Font font_bold_16 = FontFactory.getFont("Helvetica", 16, Font.BOLD);

	private static String LogoImageID = "logo";
	private static String BestellAdresseID = "bestelladresse";
	private static String LieferAdresseID = "lieferadresse";
	private static String RechnungsAdresseID = "rechnungsadresse";
	
	private static int m_draufgabe = 0;
	private static int m_margin_percent = 80;	// default
	
	public ShoppingCart() {
		// Load javascripts
		m_jscripts_str = FileOps.readFromFile(Constants.JS_FOLDER + "shopping_callbacks.js");
		// Load shopping cart css style sheet
		m_css_shopping_cart_str = "<style type=\"text/css\">" + FileOps.readFromFile(Constants.SHOPPING_SHEET) + "</style>";
		// Load key
		String key = FileOps.readFromFile(Constants.SHOP_FOLDER+"secret.txt").trim();
		// Load encrypted files
		byte[] encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER+"ibsa_conditions.ser");
		// Decrypt and deserialize
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			System.out.println(Arrays.toString(plain_msg).substring(0, 128));
			// m_map_conditions = new TreeMap<String, Conditions>();
			m_map_conditions = (TreeMap<String, Conditions>)FileOps.deserialize(plain_msg);
		}
	}
	
	public void setMarginPercent(int margin) {
		m_margin_percent = margin;
	}
	
	public void setShoppingBasket(Map<String, Article> shopping_basket) {
		m_shopping_basket = shopping_basket;
	}
	
	/** Class to add a header and a footer. */
    static class HeaderFooter extends PdfPageEventHelper {

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            switch(writer.getPageNumber() % 2) {
                case 0:
                    ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_RIGHT, new Phrase("Generiert mit AmiKo. Bestell-Modul gesponsort von IBSA.", 
                        		font_norm_10), rect.getRight()-18, rect.getTop(), 0);
                    break;
                case 1:
                    ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_LEFT, new Phrase("Generiert mit AmiKo. Bestell-Modul gesponsort von IBSA.",
                        		font_norm_10), rect.getLeft(), rect.getTop(), 0);
                    break;
            }
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER, new Phrase(String.format("Seite %d", writer.getPageNumber()), 
                    		font_norm_10), (rect.getLeft() + rect.getRight())/2, rect.getBottom()-18, 0);
        }
    }	
    
	public int getDraufgabe(String ean_code, int units, char category) {
		if (m_map_conditions!=null) {
			if (m_map_conditions.containsKey(ean_code)) {
				TreeMap<Integer, Float> ddoc = m_map_conditions.get(ean_code).getDiscountDoc(category);
				if (ddoc.size()>0)
					return (int)(units*ddoc.get(units)/100.0f);
			}
		}
		return 0;
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
    
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<form id=\"my_form\"><table id=\"Warenkorb\" width=\"99%25\">";
		String bar_charts_str = "";
		
		String load_cart_str = "";
		String save_cart_str = "";
		String delete_all_button_str = "";
		String generate_pdf_str = "";
		String generate_csv_str = "";
		
		String load_cart_text = "";
		String save_cart_text = "";
		String delete_all_text = "";	
		String generate_pdf_text = "";
		String generate_csv_text = "";
		
		float subtotal_buying_CHF = 0.0f;
		float subtotal_selling_CHF = 0.0f;
		
		m_shopping_basket = shopping_basket;
				
		String images_dir = System.getProperty("user.dir") + "/images/";	
		
		if (m_shopping_basket.size()>0) {
			int index = 1;			
			
			basket_html_str += "<tr>"
					+ "<td style=\"text-align:left\"; width=\"9%\";><b>EAN</b></td>"			// 9
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
					+ "<td style=\"text-align:center;\"; width=\"4%\";><b>Löschen</b></td>"		// 100
					+ "</tr>";
			
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();
				// Check if ean code is in conditions map (special treatment)
				if (m_map_conditions.containsKey(ean_code)) {
					article.setSpecial(true);
					Conditions c = m_map_conditions.get(ean_code);
					// Extract rebate conditions for particular doctor/pharmacy
					TreeMap<Integer, Float> rebate = c.getDiscountDoc('A');
					// --> These medis have a drop-down menu with the given "Naturalrabatt"
					if (rebate.size()>0) {
						// Initialize draufgabe
						int qty = rebate.firstEntry().getKey();		
						float reb = rebate.firstEntry().getValue(); // [%]
						m_draufgabe = (int)(qty*reb/100.0f);
						article.setDraufgabe(m_draufgabe);	
						// Loop through all possible packages (units -> rebate mapping)					
						boolean qty_found = false;
						String value_str = "";					
						for (Map.Entry<Integer, Float> e : rebate.entrySet()) {
							qty = e.getKey();
							reb = e.getValue();
							if (qty==article.getQuantity()) {
								value_str += "<option selected=\"selected\" value=\"" + qty + "\">" + qty + "</option>";
								m_draufgabe = (int)(qty*reb/100.0f);
								article.setDraufgabe(m_draufgabe);
								qty_found = true;
							} else {
								value_str += "<option value=\"" + qty + "\">" + qty + "</option>";	
							}
						}
	
						if (!qty_found)
							article.setQuantity(rebate.firstEntry().getKey());
						
						article.setMargin(m_margin_percent/100.0f);
						article.setBuyingPrice(c.fep_chf);
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
								+ "<td>" + c.name + "</td>"	
								+ "<td>" + category + "</td>"
								+ "<td style=\"text-align:right;\">" + "<select id=\"selected" + index + "\" style=\"width:50px; direction:rtl; text-align:right;\" onchange=\"onSelect('Warenkorb',this," + index + ")\"" +
									" tabindex=\"" + index + "\">" + value_str + "</select></td>"
								+ "<td style=\"text-align:right;\">+ " + m_draufgabe + "</td>"	
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
						// --> These medis are tread like any other medi, the prices, however, come from the IBSA Excel file
						int quantity = article.getQuantity();
						
						article.setBuyingPrice(c.fep_chf);
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
			String grand_total_cash_rebate_percent = String.format("%.1f%%", (0.5f+100.0f*(float)totDraufgabe()/(totDraufgabe()+totQuantity())));
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
					+ "<td>Marge (%)</td>"
					+ "<td style=\"text-align:left;\"><input type=\"number\" name=\"points\" maxlength=\"3\" min=\"1\" max=\"999\" style=\"width:40px; text-align:right;\"" 
					+ " value=\"" + m_margin_percent + "\" onkeydown=\"changeMarge('Warenkorb',this)\" id=\"marge\" /></td>"
					+ "</tr>";		
			
			basket_html_str += "</table></form>";
			
			// Warenkorb laden
			load_cart_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"loadCart(this)\"><img src=\"" + images_dir + "load_cart_icon.png\" /></button></div></td>";
			// Warenkorb speichern
			save_cart_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"saveCart(this)\"><img src=\"" + images_dir + "save_cart_icon.png\" /></button></div></td>";
			// Warenkorb löschen
			delete_all_button_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"deleteAll(this)\"><img src=\"" + images_dir + "delete_all_icon.png\" /></button></div></td>";
			// Generate pdf button string
			generate_pdf_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"createPdf(this)\"><img src=\"" + images_dir + "pdf_save_icon.png\" /></button></div></td>";
			// Generate csv button string
			generate_csv_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"createCsv(this)\"><img src=\"" + images_dir + "csv_save_icon.png\" /></button></div></td>";		
			//			
			load_cart_text = "<td><div class=\"right\">Korb laden</div></td>";
			save_cart_text = "<td><div class=\"right\">Korb speichern</div></td>";
			delete_all_text = "<td><div class=\"right\">Korb löschen</div></td>";		
			generate_pdf_text = "<td><div class=\"right\">PDF generieren</div></td>";
			generate_csv_text = "<td><div class=\"right\">CSV generieren</div></td>";
			
		} else {	
			// Warenkorb ist leer
			if (Utilities.appLanguage().equals("de"))
				basket_html_str = "<div>Ihr Warenkorb ist leer.<br><br></div>";
			else if (Utilities.appLanguage().equals("fr"))
				basket_html_str = "<div>Votre panier d'achat est vide.<br><br></div>";
			
			// Warenkorb laden
			load_cart_str = "<td style=\"text-align:center;\"><div class=\"right\" id=\"Delete_all\">"
					+ "<button type=\"button\" tabindex=\"-1\" onmouseup=\"loadCart(this)\"><img src=\"" + images_dir + "load_cart_icon.png\" /></button></div></td>";
			//
			load_cart_text = "<td><div class=\"right\">Korb laden</div></td>";
		}
		
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str+ "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_shopping_cart_str + "</head>"
				+ "<body><div id=\"shopping\">" 
				+ basket_html_str
				+ bar_charts_str + "<br />"
				+ "<form><table class=\"container\"><tr>" + load_cart_str + save_cart_str + delete_all_button_str + generate_pdf_str + generate_csv_str + "</tr>"
				+ "<tr>" + load_cart_text + save_cart_text + delete_all_text + generate_pdf_text + generate_csv_text + "</tr></table></form>"
				+ "</div></body></html>";		
		
		return m_html_str;
	}
	
	public void generatePdf(String filename) {
		// A4: 8.267in x 11.692in => 595.224units x 841.824units (72units/inch)
		
		// marginLeft, marginRight, marginTop, marginBottom
        Document document = new Document(PageSize.A4, 50, 50, 80, 50);
        try {
        	if (!m_html_str.isEmpty()) {
        		/*
        		String html_str = prettyHtml(createHtml());
        		Utilities.writeToFile(html_str, "", "test.html");
        		*/
        		
        		Preferences mPrefs = Preferences.userRoot().node(SettingsPage.class.getName());
        		        		
        		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));        		
        		writer.setBoxSize("art", new Rectangle(50, 50, 560, 790));
        		
        		HeaderFooter event = new HeaderFooter();
                writer.setPageEvent(event);        		
        		
        		document.open();
        		
        		PdfContentByte cb = writer.getDirectContent();
        		
        		document.addAuthor("ywesee GmbH");
        		document.addCreator("AmiKo for Windows");
        		document.addCreationDate();
        		
        		// Logo
        		String logoImageStr = mPrefs.get(LogoImageID, Constants.IMG_FOLDER + "empty_logo.png");	
        		File logoFile = new File(logoImageStr);
        		if (!logoFile.exists())
        			logoImageStr = Constants.IMG_FOLDER + "empty_logo.png";        		
        		
        		Image logo = Image.getInstance(logoImageStr);
        		logo.scalePercent(30);
        		logo.setAlignment(Rectangle.ALIGN_RIGHT);
        		document.add(logo);        		

        		document.add(Chunk.NEWLINE);
        		
        		// Bestelladresse
        		String bestellAdrStr = mPrefs.get(BestellAdresseID, "Keine Bestelladresse");
        		Paragraph p = new Paragraph(12);
        		// p.setIndentationLeft(60);
        		p.add(new Chunk(bestellAdrStr, font_norm_10));
        		document.add(p);

        		document.add(Chunk.NEWLINE);
        		
        		// Title
        		p = new Paragraph("Bestellung", font_bold_16);
        		document.add(p);
                
                // Date
        		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        		Date date = new Date();
        		p = new Paragraph("Datum: " + dateFormat.format(date), font_bold_10);
        		p.setSpacingAfter(20);
        		document.add(p);

        		// document.add(Chunk.NEWLINE);
        		
        		// Add addresses (Lieferadresse + Rechnungsadresse)
        		String lieferAdrStr = mPrefs.get(LieferAdresseID, "Keine Lieferadresse");
        		String rechnungsAdrStr = mPrefs.get(RechnungsAdresseID, "Keine Rechnungsadresse");        		
        		
                PdfPTable addressTable = new PdfPTable(new float[] {1,1});
                addressTable.setWidthPercentage(100f);
                addressTable.getDefaultCell().setPadding(5);
                addressTable.setSpacingAfter(5f);
                addressTable.addCell(getStringCell("Lieferadresse", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));
                addressTable.addCell(getStringCell("Rechnungsdresse", font_bold_10,	PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));     		
                addressTable.addCell(getStringCell(lieferAdrStr, font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));
                addressTable.addCell(getStringCell(rechnungsAdrStr, font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));
                document.add(addressTable);
                
        		document.add(Chunk.NEWLINE);                
                
        		// Add shopping basket
        		document.add(getShoppingBasket(cb));    
        		
        		LineSeparator separator = new LineSeparator();
        		document.add(separator);
        		/*
        		XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
        		worker.parseXHtml(writer, document, new StringReader(html_str));
        		*/
        	}
        } catch (IOException e) {
        	
        } catch (DocumentException e) {
        	
        }
        
        document.close();        
        System.out.println( "PDF Created!" );
	}	
	
	public PdfPTable getShoppingBasket(PdfContentByte cb) {
		int position = 0;
		float total_CHF = 0.0f;
		float rebate_percent = 0.05f;

		BarcodeEAN codeEAN = new BarcodeEAN();
		
		// Pos | Menge | Eancode | Bezeichnung | Preis
        PdfPTable table = new PdfPTable(new float[] {1,1,3,6,2});
        table.setWidthPercentage(100f);
        table.getDefaultCell().setPadding(5);
        table.setSpacingAfter(5f);
        
		PdfPCell cell = new PdfPCell();	
        
        table.addCell(getStringCell("Pos.", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));
		table.addCell(getStringCell("Anz.", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("GTIN", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("Bezeichnung", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("Preis (CHF)", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_RIGHT, 1));
		        
        if (m_shopping_basket.size()>0) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				
				
				String price_pruned = article.getExfactoryPrice().replaceAll("[^\\d.]", "");

				if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {						
					table.addCell(getStringCell(Integer.toString(++position), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));
					table.addCell(getStringCell(Integer.toString(article.getQuantity()), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));
					
			        codeEAN.setCode(article.getEanCode());
			        Image img = codeEAN.createImageWithBarcode(cb, null, null);
			        img.scalePercent(120);
			        cell = new PdfPCell(img);
			        cell.setBorder(Rectangle.NO_BORDER);
			        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			        cell.setUseBorderPadding(true);
			        cell.setBorderWidth(5);
			        if (position==1)
			        	cell.setPaddingTop(8);
			        else
			        	cell.setPaddingTop(0);
			        cell.setPaddingBottom(8);
			        table.addCell(cell);
			        
					table.addCell(getStringCell(article.getPackTitle(), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));		        
					
					float price_CHF = article.getQuantity()*Float.parseFloat(price_pruned);
					total_CHF += price_CHF;					
					table.addCell(getStringCell(String.format("%.2f", price_CHF), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 1));						
				}
			}
			
			table.addCell(getStringCell("Warenwert", font_norm_10, Rectangle.TOP, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_norm_10, Rectangle.TOP, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", total_CHF), font_norm_10, Rectangle.TOP, Element.ALIGN_RIGHT, 2));

			table.addCell(getStringCell("Rabatt (" + 100*rebate_percent + "%)", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("-%.2f", total_CHF*rebate_percent), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));	
			
			float zwischen_total_CHF = total_CHF*(1.0f-rebate_percent);

			table.addCell(getStringCell("Zwischentotal", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", zwischen_total_CHF), font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));					
			
			table.addCell(getStringCell("MwSt (8%)", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", zwischen_total_CHF*0.08f), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));	
			
			table.addCell(getStringCell("Total", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", zwischen_total_CHF*1.08f), font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));		
		}
        return table;
	}
	
	public void generateCsv(String filename) {
        if (m_shopping_basket.size()>0) {
        	int pos = 0;
        	String shopping_basket_str = "";
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				
				
				String price_pruned = article.getExfactoryPrice().replaceAll("[^\\d.]", "");
				if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {	
					float price_CHF = article.getQuantity()*Float.parseFloat(price_pruned);
					shopping_basket_str += (++pos) + "|" 
							+ article.getQuantity() + "|" 
							+ article.getEanCode() + "|" 
							+ article.getPackTitle() + "|" 
							+ price_pruned + "|"
							+ price_CHF + "\n"; 
				}
			}
			try {
				CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
			   	encoder.onMalformedInput(CodingErrorAction.REPORT);
			   	encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
				OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), encoder);
				BufferedWriter bw = new BufferedWriter(osw);      			
				bw.write(shopping_basket_str);
				bw.close();
	        } catch(IOException e) {
	        	System.out.println("Could not save the csv file...");
	        }
        }
	}
	
	private PdfPCell getStringCell(String str, Font font, int border, int align, int colspan) {
		PdfPCell cell = new PdfPCell(new Paragraph(str, font));
		cell.setPaddingTop(5);
		cell.setPaddingBottom(5);
		cell.setBorderWidth(1);
		cell.setBorder(border);
		cell.setHorizontalAlignment(align);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE /*.ALIGN_CENTER*/);
		cell.setColspan(colspan);

		return cell;
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