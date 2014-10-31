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
	
	public ShoppingCart() {
		// Load javascripts
		m_jscripts_str = Utilities.readFromFile(Constants.JS_FOLDER + "shopping_callbacks.js");
		// Load shopping cart css style sheet
		m_css_shopping_cart_str = "<style type=\"text/css\">" + Utilities.readFromFile(Constants.SHOPPING_SHEET) + "</style>";
		// Load key
		String key = Utilities.readFromFile(Constants.SHOP_FOLDER+"secret.txt").trim();
		// Load encrypted files
		byte[] encrypted_msg = Utilities.readBytesFromFile(Constants.SHOP_FOLDER+"ibsa_conditions.ser");
		// Decrypt and deserialize
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] plain_msg = crypto.decrypt(encrypted_msg);	
			System.out.println(Arrays.toString(plain_msg).substring(0, 128));
			// m_map_conditions = new TreeMap<String, Conditions>();
			m_map_conditions = (TreeMap<String, Conditions>)deserialize(plain_msg);
		}
	}
		
	private Object deserialize(byte[] byteArray) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(byteArray);
			ObjectInputStream sin = new ObjectInputStream(bin);
			return sin.readObject();
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
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
			if (m_map_conditions.containsKey(ean_code))
				return (int)(units*m_map_conditions.get(ean_code).getDiscountDoc(category).get(units)/100.0f);
			else
				return 0;
		}
		return 0;
	}

	public int calcProfit(float tot_buying, float tot_selling) {
		if (tot_buying>0.0f)
			return (int)((tot_selling/tot_buying-1.0f)*100.0f);
		else 
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
		String basket_html_str = "<form id=\"my_form\"><table id=\"Warenkorb\" width=\"100%25\">";
		String delete_all_button_str = "";
		String generate_pdf_str = "";
		String generate_csv_str = "";
		String delete_all_text = "";		
		String generate_pdf_text = "";
		String generate_csv_text = "";
		float subtotal_buying_CHF = 0.0f;
		float subtotal_selling_CHF = 0.0f;
		
		m_shopping_basket = shopping_basket;
				
		String images_dir = System.getProperty("user.dir") + "/images/";	

		if (m_shopping_basket.size()>0) {
			int index = 1;			
			
			basket_html_str += "<tr><td><b>EAN</b></td>"
					+ "<td><b>Artikel</b></td>"					
					+ "<td><b>Menge</b></td>"
					+ "<td style=\"text-align:right;\"><b>Draufgabe</b></td>"
					+ "<td style=\"text-align:right;\"><b>Einkauf</b></td>"
					+ "<td style=\"text-align:right;\"><b>Tot Einkauf</b></td>"
					+ "<td style=\"text-align:right;\"><b>Verkauf</b></td>"
					+ "<td style=\"text-align:right;\"><b>Tot Verkauf</b></td>"					
					+ "<td style=\"text-align:right;\"><b>Gewinn</b></td>"
					+ "<td style=\"text-align:right;\"><b>Gewinn(%)</b></td>"					
					+ "<td style=\"text-align:center;\"><b>Löschen</b></td></tr>";
			
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				String ean_code = article.getEanCode();
				// Check if ean code is in conditions map (special treatment)
				if (m_map_conditions.containsKey(ean_code)) {
					article.setSpecial(true);
					Conditions c = m_map_conditions.get(ean_code);
					// Extract rebate conditions for particular doctor/pharmacy
					TreeMap<Integer, Float> rebate = c.getDiscountDoc('A');
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
					
					article.setBuyingPrice(c.fep_chf);
					article.setSellingPrice(c.fep_chf, 0.8f);
					subtotal_buying_CHF += article.getTotBuyingPrice();
					subtotal_selling_CHF += article.getTotSellingPrice();
					
					String buying_price_CHF = String.format("%.2f", article.getBuyingPrice());
					String tot_buying_price_CHF = String.format("%.2f", article.getTotBuyingPrice());
					String selling_price_CHF = String.format("%.2f", article.getSellingPrice());
					String tot_selling_price_CHF = String.format("%.2f", article.getTotSellingPrice());
					String profit_CHF = String.format("%.2f", article.getTotSellingPrice()-article.getTotBuyingPrice());
					String profit_percent = String.format("%d%%", calcProfit(article.getTotBuyingPrice(), article.getTotSellingPrice()));
					
					basket_html_str += "<tr id=\"" + ean_code + "\">";
					basket_html_str += "<td>" + ean_code + "</td>"
							+ "<td>" + c.name + "</td>"						
							+ "<td>" + "<select id=\"selected" + index + "\" style=\"width:50px; direction:rtl; text-align:right;\" onchange=\"onSelect('Warenkorb',this," + index + ")\"" +
								" tabindex=\"" + index + "\">" + value_str + "</select></td>"
							+ "<td style=\"text-align:right;\">+ " + m_draufgabe + "</td>"	
							+ "<td style=\"text-align:right;\">" + buying_price_CHF + "</td>"	
							+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
							+ "<td style=\"text-align:right;\">" + selling_price_CHF + "</td>"
							+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
							+ "<td style=\"text-align:right; color:green\"><b>" + profit_CHF + "</b></td>"
							+ "<td style=\"text-align:right; color:green\"><b>" + profit_percent + "</b></td>"							
							+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" tabindex=\"-1\" onclick=\"deleteRow('Warenkorb',this)\"><img src=\"" 
								+ images_dir + "trash_icon.png\" /></button>" + "</td>";
					basket_html_str += "</tr>";						
				} else {
					int quantity = article.getQuantity();
					subtotal_buying_CHF += article.getTotExfactoryPrice();
					subtotal_selling_CHF += article.getTotPublicPrice();

					String buying_price_CHF = String.format("%.2f", article.getExfactoryPriceAsFloat());
					String tot_buying_price_CHF = String.format("%.2f", article.getTotExfactoryPrice());
					String selling_price_CHF = String.format("%.2f", article.getPublicPriceAsFloat());
					String tot_selling_price_CHF = String.format("%.2f", article.getTotPublicPrice());
					String profit_CHF = String.format("%.2f", article.getTotPublicPrice()-article.getTotExfactoryPrice());
					String profit_percent = String.format("%d%%", calcProfit(article.getTotExfactoryPrice(), article.getTotPublicPrice()));
					
					basket_html_str += "<tr id=\"" + ean_code + "\">";
					basket_html_str += "<td>" + ean_code + "</td>"
							+ "<td>" + article.getPackTitle() + "</td>"						
							+ "<td>" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:50px; text-align:right;\"" +
								" value=\"" + quantity + "\"" + " onkeydown=\"changeQty('Warenkorb',this)\" id=\"" + index + "\" tabindex=\"" + index + "\" />" + "</td>"
							+ "<td style=\"text-align:right;\"></td>"	
							+ "<td style=\"text-align:right;\">" + buying_price_CHF + "</td>"	
							+ "<td style=\"text-align:right;\">" + tot_buying_price_CHF + "</td>"
							+ "<td style=\"text-align:right;\">" + selling_price_CHF + "</td>"
							+ "<td style=\"text-align:right;\">" + tot_selling_price_CHF + "</td>"							
							+ "<td style=\"text-align:right; color:green\"><b>" + profit_CHF + "</b></td>"
							+ "<td style=\"text-align:right; color:green\"><b>" + profit_percent + "</b></td>"							
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
			float subtotal_profit_CHF = subtotal_selling_CHF-subtotal_buying_CHF;
			int subtotal_profit_percent = 0;
			if (subtotal_buying_CHF>0.0f)
				subtotal_profit_percent = (int)((0.5f+(subtotal_selling_CHF/subtotal_buying_CHF-1.0f)*100.0f));
			basket_html_str += "<tr id=\"Subtotal\">"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\">Subtotal</td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"	
					+ "<td style=\"padding-top:10px; text-align:right;\">" + String.format("%.2f", subtotal_buying_CHF) + "</td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px; text-align:right;\">" + String.format("%.2f", subtotal_selling_CHF) + "</td>"				
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"						
					+ "</tr>";
			basket_html_str += "<tr id=\"MWSt\">"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\">MWSt (+8%)</td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px; text-align:right;\">" + String.format("%.2f", subtotal_buying_CHF*0.08) + "</td>"
					+ "<td style=\"padding-top:10px\"></td>"	
					+ "<td style=\"padding-top:10px; text-align:right;\">" + String.format("%.2f", subtotal_selling_CHF*0.08) + "</td>"
					+ "<td style=\"padding-top:10px\"></td>"					
					+ "<td style=\"padding-top:10px\"></td>"
					+ "</tr>";
			basket_html_str += "<tr id=\"Total\">"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"><b>Total</b></td>"
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>" + totQuantity() + "</b></td>"
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>+ " + totDraufgabe() + "</b></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>" + String.format("%.2f", subtotal_buying_CHF*1.08) + "</b></td>"					
					+ "<td style=\"padding-top:10px\"></td>"	
					+ "<td style=\"padding-top:10px; text-align:right;\"><b>" + String.format("%.2f", subtotal_selling_CHF*1.08) + "</b></td>"				
					+ "<td style=\"padding-top:10px; text-align:right; color:green\"><b>" + String.format("%.2f", subtotal_profit_CHF*1.08) + "</b></td>"						
					+ "<td style=\"padding-top:10px; text-align:right; color:green\"><b>" + String.format("%d%%", subtotal_profit_percent) + "</b></td>"											
					+ "</tr>";
						
			basket_html_str += "</table></form>";
	
			// Warenkorb löschen
			delete_all_button_str = "<td align=center valign=middle><div class=\"right\" id=\"Delete_all\"><input type=\"image\" src=\"" 
					+ images_dir + "delete_all_icon.png\" title=\"" + delete_all_text + "\" onmouseup=\"deleteRow('Delete_all',this)\" tabindex=\"-1\" /></div></td>";	
			// Generate pdf button string
			generate_pdf_str = "<td align=center valign=middle><div class=\"right\" id=\"Generate_pdf\"><input type=\"image\" src=\"" 
					+ images_dir + "pdf_save_icon.png\" title=\"" + generate_pdf_text + "\" onmouseup=\"createPdf(this)\" tabindex=\"-1\" /></div></td>";
			// Generate csv button string
			generate_csv_str = "<td align=center valign=middle><div class=\"right\" id=\"Generate_csv\"><input type=\"image\" src=\"" 
					+ images_dir + "csv_save_icon.png\" title=\"" + generate_csv_text + "\" onmouseup=\"createCsv(this)\" tabindex=\"-1\" /></div></td>";			
			//
			delete_all_text = "<td><div class=\"right\">Alle löschen</div></td>";		
			generate_pdf_text = "<td><div class=\"right\">PDF generieren</div></td>";
			generate_csv_text = "<td><div class=\"right\">CSV generieren</div></td>";
			
		} else {
			// Warenkorb ist leer
			if (Utilities.appLanguage().equals("de"))
				basket_html_str = "<div>Ihr Warenkorb ist leer.<br><br></div>";
			else if (Utilities.appLanguage().equals("fr"))
				basket_html_str = "<div>Votre panier d'achat est vide.<br><br></div>";
		}
		
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str+ "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_shopping_cart_str + "</head>"
				+ "<body><div id=\"shopping\">" 
				+ basket_html_str + "<br />" 
				// + "<div class=\"container\">" + delete_all_button_str + generate_pdf_str + generate_csv_str + "</div>" 
				+ "<form><table class=\"container\"><tr>" + delete_all_button_str + generate_pdf_str + generate_csv_str + "</tr>"
				+ "<tr>" + delete_all_text + generate_pdf_text + generate_csv_text + "</tr></table></form>"
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
			basket_html_str = "<table id=\"Warenkorb\" width=\"100%\" style=\"border-spacing: 5 10\">";
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