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

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities.EscapeMode;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class ShoppingCart {
	
	private static Map<String, Article> m_shopping_basket = null;
	private static String m_html_str = "";
	private static String m_js_deleterow_str = null;
	private static String m_js_generate_str = null;
	private static String m_css_shopping_cart_str = null;
	
	public ShoppingCart() {
		// Load delete row javascript
		m_js_deleterow_str = Utilities.readFromFile(Constants.JS_FOLDER + "deleterow.js");
		// Load generate javascript
		m_js_generate_str = Utilities.readFromFile(Constants.JS_FOLDER + "invoke.js");
		// Load interactions css style sheet
		m_css_shopping_cart_str = "<style>" + Utilities.readFromFile(Constants.INTERACTIONS_SHEET) + "</style>";
	}
	
	public String updateShoppingCartHtml(Map<String, Article> shopping_basket) {
		String basket_html_str = "<table id=\"Warenkorb\" width=\"98%25\">";
		String delete_all_button_str = "";
		String delete_text = "löschen";
		String delete_all_text = "alle löschen";
		String generate_pdf_str = "";
		String generate_text = "generate pdf";
		float total_CHF = 0.0f;

		m_shopping_basket = shopping_basket;
		
		if (Utilities.appLanguage().equals("de")) {
			delete_text = "löschen";
			delete_all_text = "alle löschen";
		} else if (Utilities.appLanguage().equals("fr")) {
			delete_text = "annuler";
			delete_all_text = "tout supprimer";
		}
		
		if (m_shopping_basket.size()>0) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();
				basket_html_str += "<tr>";
				basket_html_str += "<td>" + article.getQuantity() + "</td>"
						+ "<td>" + article.getPharmaCode() + "</td>"
						+ "<td>" + article.getPackTitle() + "</td>"
						+ "<td>" + article.getPublicPrice() + "</td>"
						+ "<td align=\"right\">" + "<input type=\"button\" value=\"" + delete_text + "\" onclick=\"deleteRow('Warenkorb',this)\" />" + "</td>";
				basket_html_str += "</tr>";
				String price_pruned = article.getPublicPrice().replaceAll("[^\\d.]", "");
				if (!price_pruned.isEmpty() && !price_pruned.equals(".."))
					total_CHF += article.getQuantity()*Float.parseFloat(price_pruned);
			}
			basket_html_str += "<tr>"
					+ "<td></td>"
					+ "<td>Subtotal</td>"
					+ "<td></td>"
					+ "<td>CHF " + String.format("%.2f", total_CHF) + "</td>"					
					+ "</tr>";
			basket_html_str += "<tr>"
					+ "<td></td>"
					+ "<td>MWSt</td>"
					+ "<td></td>"
					+ "<td>CHF " + String.format("%.2f", total_CHF*0.08) + "</td>"					
					+ "</tr>";
			basket_html_str += "<tr>"
					+ "<td></td>"
					+ "<td><b>Total</b></td>"
					+ "<td></td>"
					+ "<td><b>CHF " + String.format("%.2f", total_CHF*1.08) + "</b></td>"					
					+ "</tr>";
						
			basket_html_str += "</table>";
			// Warenkorb löschen
			delete_all_button_str = "<div id=\"Delete_all\"><input type=\"button\" value=\"" + delete_all_text + "\" onclick=\"deleteRow('Delete_all',this)\" /></div>";	
			// Generate pdf button string
			generate_pdf_str = "<div id=\"Delete_all\"><input type=\"button\" value=\"" + generate_text + "\" onclick=\"invoke(this)\" /></div>";

		} else {
			// Warenkorb ist leer
			if (Utilities.appLanguage().equals("de"))
				basket_html_str = "<div>Ihr Warenkorb ist leer.<br><br></div>";
			else if (Utilities.appLanguage().equals("fr"))
				basket_html_str = "<div>Votre panier d'achat est vide.<br><br></div>";
		}
		
		String jscript_str = "<script> language=\"javascript\">" + m_js_deleterow_str + m_js_generate_str + "</script>";
		m_html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_shopping_cart_str + "</head><body><div id=\"interactions\">" 
				+ basket_html_str + delete_all_button_str + "<br><br>" + generate_pdf_str + "<br><br>" + "</body></div></html>";		
		
		return m_html_str;
	}
	
	public void generatePdf() {
        Document document = new Document();
        try {
        	if (!m_html_str.isEmpty()) {
        		/*
        		String html_str = prettyHtml(createHtml());
        		Utilities.writeToFile(html_str, "", "test.html");
        		*/
        		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("test.pdf"));        		
        		
        		document.open();
        		
        		PdfContentByte cb = writer.getDirectContent();
        		
        		document.addAuthor("ywesee GmbH");
        		document.addCreator("AmiKo for Windows");
        		
        		Image logo = Image.getInstance("./images/desitin_logo.png");
        		logo.scalePercent(30);
        		logo.setAlignment(Rectangle.ALIGN_RIGHT);
        		document.add(logo);        		
        		
        		Paragraph p = new Paragraph("Bestellung", FontFactory.getFont("Helvetica", 16, Font.BOLD));
        		document.add(p);
        		
        		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        		Date date = new Date();
        		p = new Paragraph("Datum: " + dateFormat.format(date), FontFactory.getFont("Helvetica", 12, Font.BOLD));
        		document.add(p);
        		document.add(Chunk.NEWLINE);
        		
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

		BarcodeEAN codeEAN = new BarcodeEAN();
		
		// Pos | Menge | Eancode | Bezeichnung | Preis
        PdfPTable table = new PdfPTable(new float[] {1,1,3,6,2});
        table.setWidthPercentage(100f);
        table.setSpacingAfter(5f);
        
		PdfPCell cell = new PdfPCell();	
        
        table.addCell(getStringCell("Pos", FontFactory.getFont("Helvetica", 12, Font.BOLD), 
        		Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE));
		table.addCell(getStringCell("Qty", FontFactory.getFont("Helvetica", 12, Font.BOLD), 
				Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE));        
        table.addCell(getStringCell("Ean-Code", FontFactory.getFont("Helvetica", 12, Font.BOLD), 
        		Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE));        
        table.addCell(getStringCell("Bezeichnung", FontFactory.getFont("Helvetica", 12, Font.BOLD), 
        		Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE));        
        table.addCell(getStringCell("Preis", FontFactory.getFont("Helvetica", 12, Font.BOLD), 
        		Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE));
		        
        if (m_shopping_basket.size()>0) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				

				table.addCell(getStringCell(Integer.toString(++position), FontFactory.getFont("Helvetica"), 
						PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE));
				table.addCell(getStringCell(Integer.toString(article.getQuantity()), FontFactory.getFont("Helvetica"), 
						PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE));
				
		        codeEAN.setCode(article.getEanCode());
		        Image img = codeEAN.createImageWithBarcode(cb, null, null);
		        img.scalePercent(120);
		        cell = new PdfPCell(img);
		        cell.setBorder(Rectangle.NO_BORDER);
		        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		        cell.setUseBorderPadding(true);
		        cell.setBorderWidth(5f);
		        table.addCell(cell);
		        
				table.addCell(getStringCell(article.getPackTitle(), FontFactory.getFont("Helvetica"), 
						PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE));		        
				table.addCell(getStringCell(article.getPublicPrice(), FontFactory.getFont("Helvetica"), 
						PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE));
			}
		}
        return table;
	}
	
	
	private PdfPCell getStringCell(String str, Font font, int border, int align) {
		PdfPCell cell = new PdfPCell();

		cell.setBorderWidth(1f);
		cell.setBorder(border);
		cell.setVerticalAlignment(align);
		cell.addElement(new Paragraph(str, font));

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