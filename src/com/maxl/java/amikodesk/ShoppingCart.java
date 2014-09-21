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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities.EscapeMode;

import chrriis.common.WebServer;

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

public class ShoppingCart {
	
	private static Map<String, Article> m_shopping_basket = null;
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
		
	public ShoppingCart() {
		// Load javascripts
		m_jscripts_str = Utilities.readFromFile(Constants.JS_FOLDER + "shopping_callbacks.js");
		// Load shopping cart css style sheet
		m_css_shopping_cart_str = "<style>" + Utilities.readFromFile(Constants.SHOPPING_SHEET) + "</style>";
	}

	/** Inner class to add a header and a footer. */
    static class HeaderFooter extends PdfPageEventHelper {

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            switch(writer.getPageNumber() % 2) {
                case 0:
                    ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_RIGHT, new Phrase("Generiert mit AmiKo. Bestell-Modul gesponsort von IBSA.", 
                        		font_norm_10), rect.getRight(), rect.getTop(), 0);
                    break;
                case 1:
                    ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_LEFT, new Phrase("Generiert mit AmiKo. Bestell-Modul gesponsort von IBSA.",
                        		font_norm_10), rect.getLeft(), rect.getTop(), 0);
                    break;
            }
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER, new Phrase(String.format("Seite %d", writer.getPageNumber())),
                    (rect.getLeft() + rect.getRight())/2, rect.getBottom()-18, 0);
        }
    }	
	
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<table id=\"Warenkorb\" width=\"98%25\">";
		String delete_all_button_str = "";
		String generate_pdf_str = "";
		String generate_csv_str = "";
		String delete_all_text = "alle löschen";		
		String generate_pdf_text = "PDF generieren";
		String generate_csv_text = "CSV generieren";
		float total_CHF = 0.0f;

		m_shopping_basket = shopping_basket;
		
		if (Utilities.appLanguage().equals("de")) {
			delete_all_text = "alle löschen";
		} else if (Utilities.appLanguage().equals("fr")) {
			delete_all_text = "tout supprimer";
		}
				
		String images_dir = System.getProperty("user.dir") + "/images/";	
		
		if (m_shopping_basket.size()>0) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				int quantity = article.getQuantity();
				String price = article.getPublicPrice();
				String price_pruned = price.replaceAll("[^\\d.]", "");
				float price_CHF = 0.0f;
				if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {
					price_CHF = article.getQuantity()*Float.parseFloat(price_pruned);
					total_CHF += price_CHF;
					price = String.format("%.2f CHF", price_CHF);
				} else {
					price = "k.A.";
				}				
				// String article_price = String.format("%.2f",  price_CHF);
				basket_html_str += "<tr>";
				basket_html_str += "<td>" + "<input type=\"number\" name=\"points\" maxlength=\"4\" min=\"1\" max=\"999\" style=\"width:40px; text-align:right\"" +
						" value=\"" + quantity + "\"" + " onkeypress=\"changeQty('Warenkorb',this)\" id=\"qty\" />" + "</td>"
						+ "<td>" + article.getEanCode() + "</td>"
						+ "<td>" + article.getPackTitle() + "</td>"
						+ "<td style=\"text-align:right;\">" + price + "</td>"
						+ "<td>" + "<input type=\"image\" src=\"" + images_dir + "trash_icon.png\" onmouseup=\"deleteRow('Warenkorb',this)\" />" + "</td>";
				basket_html_str += "</tr>";
			}
			basket_html_str += "<tr>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\">Subtotal</td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px; text-align:right\">" + String.format("%.2f", total_CHF) + " CHF</td>"					
					+ "</tr>";
			basket_html_str += "<tr>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\">MWSt (+8%)</td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px; text-align:right\">" + String.format("%.2f", total_CHF*0.08) + " CHF</td>"					
					+ "</tr>";
			basket_html_str += "<tr>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px\"><b>Total</b></td>"
					+ "<td style=\"padding-top:10px\"></td>"
					+ "<td style=\"padding-top:10px; text-align:right\"><b>" + String.format("%.2f", total_CHF*1.08) + " CHF</b></td>"					
					+ "</tr>";
						
			basket_html_str += "</table>";
	
			// Warenkorb löschen
			delete_all_button_str = "<div class=\"left\" id=\"Delete_all\"><input type=\"image\" src=\"" 
					+ images_dir + "delete_all_icon.png\" title=\"" + delete_all_text + "\" onmouseup=\"deleteRow('Delete_all',this)\" /></div>";	
			// Generate pdf button string
			generate_pdf_str = "<div class=\"left\" id=\"Generate_pdf\"><input type=\"image\" src=\"" 
					+ images_dir + "pdf_save_icon.png\" title=\"" + generate_pdf_text + "\" onmouseup=\"createPdf(this)\" /></div>";
			// Generate csv button string
			generate_csv_str = "<div class=\"left\" id=\"Generate_csv\"><input type=\"image\" src=\"" 
					+ images_dir + "csv_save_icon.png\" title=\"" + generate_csv_text + "\" onmouseup=\"createCsv(this)\" /></div>";			
			// delete_all_button_str = "<div id=\"Delete_all\"><input type=\"button\" value=\"" + delete_all_text + "\" onclick=\"deleteRow('Delete_all',this)\" /></div>";	
			// generate_pdf_str = "<div id=\"Delete_all\"><input type=\"button\" value=\"" + generate_text + "\" onclick=\"createPdf(this)\" /></div>";
			
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
				+ basket_html_str + "<br>" + "<div class=\"container\">" + delete_all_button_str + generate_pdf_str + generate_csv_str + "</div>" 
				+ "</div></body></html>";		
		
		return m_html_str;
	}
	
	public void generatePdf(String filename) {
		// A4: 8.267in x 11.692in => 595.224units x 841.824units (72units/inch)
		
		// marginLeft, marginRight, marginTop, marginBottom
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
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
        
        table.addCell(getStringCell("Pos", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));
		table.addCell(getStringCell("Qty", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("GTIN", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("Bezeichnung", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("Preis (CHF)", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_RIGHT, 1));
		        
        if (m_shopping_basket.size()>0) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				

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
				
				String price = article.getPublicPrice();
				table.addCell(getStringCell(price, font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 1));
				String price_pruned = price.replaceAll("[^\\d.]", "");
				if (!price_pruned.isEmpty() && !price_pruned.equals(".."))
					total_CHF += article.getQuantity()*Float.parseFloat(price_pruned);				
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
						+ "<td align=\"right\">" + article.getPublicPrice() + "</td>";
				basket_html_str += "</tr>";
				String price_pruned = article.getPublicPrice().replaceAll("[^\\d.]", "");
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