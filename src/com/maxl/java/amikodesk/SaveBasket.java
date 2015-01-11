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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.prefs.Preferences;

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

public class SaveBasket {

	private static Font font_norm_10 = FontFactory.getFont("Helvetica", 10, Font.NORMAL);
	private static Font font_bold_10 = FontFactory.getFont("Helvetica", 10, Font.BOLD);
	private static Font font_bold_16 = FontFactory.getFont("Helvetica", 16, Font.BOLD);

	private static String LogoImageID = "logo";
	private static String BestellAdresseID = "bestelladresse";
	private static String LieferAdresseID = "lieferadresse";
	private static String RechnungsAdresseID = "rechnungsadresse";
	
	private static Map<String, Article> m_shopping_basket = null;
	private static Map<String, Author> m_map_of_authors = null;

	public SaveBasket(Map<String, Article> shopping_basket) {
		m_shopping_basket = shopping_basket;
	}
	
	/** Class to add a header and a footer. */
    static class HeaderFooter extends PdfPageEventHelper {

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            switch(writer.getPageNumber() % 2) {
                case 0:
                    ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_RIGHT, new Phrase("Erstellt mit AmiKo. Bestellmodul gesponsort von IBSA.", 
                        		font_norm_10), rect.getRight()-18, rect.getTop(), 0);
                    break;
                case 1:
                    ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_LEFT, new Phrase("Erstellt mit AmiKo. Bestellmodul gesponsort von IBSA.",
                        		font_norm_10), rect.getLeft(), rect.getTop(), 0);
                    break;
            }
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER, new Phrase(String.format("Seite %d", writer.getPageNumber()), 
                    		font_norm_10), (rect.getLeft() + rect.getRight())/2, rect.getBottom()-18, 0);
        }
    }	    
    
    private boolean anyElemIsContained(Map<String, Author> map_of_str, String str) {
    	if (map_of_str.containsKey(str))
   			return true;
    	return false;
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
    
    public void setAuthorList(List<Author> authors) {
    	if (authors!=null) {
    		m_map_of_authors = new HashMap<String, Author>();
    		for (Author a : authors) {
    			m_map_of_authors.put(a.getShortName(), a);
    		}
    	}
    }    
    
    public int getMedsForAuthor(String author) {
    	int tot_med = 0;    	
    	if (m_shopping_basket!=null && m_shopping_basket.size()>0) {
    		for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();								
				if (article.getAuthor().trim().toLowerCase().contains(author)) {	
					tot_med++;
				}
    		}
    	}
    	return tot_med;
    }
    
    public int getMedsWithNoAuthor() {
    	if (m_shopping_basket!=null) {
	    	int tot_med = m_shopping_basket.size();
	    	for (Map.Entry<String, Author> entry : m_map_of_authors.entrySet())
	    		tot_med -= getMedsForAuthor(entry.getKey());
	    	return tot_med;
    	}
    	return 0;
    }
    
	public void generatePdf(Author author, String filename, String type) {
		// A4: 8.267in x 11.692in => 595.224units x 841.824units (72units/inch)
		
		// marginLeft, marginRight, marginTop, marginBottom
        Document document = new Document(PageSize.A4, 50, 50, 80, 50);
        try {
            if (m_shopping_basket.size()>0) {
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
	      		if (type.equals("specific"))
	      			document.add(getShoppingBasketForAuthor(author, cb));
	      		else if (type.equals("all"))
	      			document.add(getFullShoppingBasket(cb, "all"));
	      		else if (type.equals("rest"))
	      			document.add(getFullShoppingBasket(cb, "rest"));
	        	LineSeparator separator = new LineSeparator();
	        	document.add(separator);
            }
        } catch (IOException e) {
        	
        } catch (DocumentException e) {
        	
        }
        
        document.close();        
        // System.out.println("Saved PDF to " + filename);
	}	
	
	public PdfPTable getShoppingBasketForAuthor(Author a, PdfContentByte cb) {
		int position = 0;
		float subtotal_CHF = 0.0f;
		float shipping_CHF = 0.0f;
		float vat25_CHF = 0.0f;
		float vat80_CHF = 0.0f;

		String author = a.getShortName();
		
		BarcodeEAN codeEAN = new BarcodeEAN();
		
		// Pos | Menge | Eancode | Bezeichnung | MwSt | Preis
        PdfPTable table = new PdfPTable(new float[] {1,2,3,6,1,2});
        table.setWidthPercentage(100f);
        table.getDefaultCell().setPadding(5);
        table.setSpacingAfter(5f);
        
		PdfPCell cell = new PdfPCell();	
        
        table.addCell(getStringCell("Pos.", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));
		table.addCell(getStringCell("Menge", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("EAN", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("Artikel", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));    
        table.addCell(getStringCell("MwSt", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_RIGHT, 1));
        table.addCell(getStringCell("Preis (CHF)", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_RIGHT, 1));
		        
        if (m_shopping_basket.size()>0 && !author.isEmpty()) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();							
				if (article.getAuthor().trim().toLowerCase().contains(author)) {
					String price_pruned = "";					
					/*
					if (article.getCode()!=null && article.getCode().equals("ibsa")) {
						float cr = article.getCashRebate();
						if (article.getDraufgabe()>0)
							price_pruned = String.format("%.2f", article.getBuyingPrice(0.0f));
						else
							price_pruned = String.format("%.2f", article.getBuyingPrice(cr));
					} else {
						price_pruned = article.getCleanExfactoryPrice();						
					}
					*/
					String total_price_CHF = "";
					if (article.getCode()!=null && article.getCode().equals("ibsa")) {
						float cr = article.getCashRebate();
						if (article.getDraufgabe()>0) {
							price_pruned = String.format("%.2f", article.getBuyingPrice(0.0f));
							total_price_CHF = String.format("%.2f", article.getTotBuyingPrice(0.0f));
						} else {
							price_pruned = String.format("%.2f", article.getBuyingPrice(cr));
							total_price_CHF = String.format("%.2f", article.getTotBuyingPrice(cr));
						}
					} else {
						price_pruned = article.getCleanExfactoryPrice();
						total_price_CHF = String.format("%.2f", article.getTotExfactoryPrice());
					}
					
					if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {						
						// Index
						table.addCell(getStringCell(Integer.toString(++position), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));
						// Anzahl
						table.addCell(getStringCell(Integer.toString(article.getQuantity()), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));						
						// EAN code
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
				        // Artikelbezeichnung
						table.addCell(getStringCell(article.getPackTitle(), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 1));		        
						// MwSt						
						table.addCell(getStringCell(String.format("%.1f%%", article.getVat()), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 1));
						// Preis (exkl. MwSt)
						// float price_CHF = article.getQuantity()*Float.parseFloat(price_pruned);
						table.addCell(getStringCell(total_price_CHF, font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 1));						
					}
				}
			}
			
			subtotal_CHF = a.getSubtotal();					
			shipping_CHF = a.getShippingCosts();
			vat25_CHF = a.getVat25();
			vat80_CHF = a.getVat80() + a.getShippingCosts()*0.08f;
			
			float fulltotal_CHF = subtotal_CHF + shipping_CHF + vat25_CHF + vat80_CHF;
			
			table.addCell(getStringCell("Subtotal", font_bold_10, Rectangle.TOP, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_bold_10, Rectangle.TOP, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", subtotal_CHF), font_bold_10, Rectangle.TOP, Element.ALIGN_RIGHT, 2));
			
			table.addCell(getStringCell("Versand", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", shipping_CHF), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));	
			
			table.addCell(getStringCell("MwSt (2.5%)", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", vat25_CHF), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));	
			
			table.addCell(getStringCell("MwSt (8.0%)", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", vat80_CHF), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));	
			
			table.addCell(getStringCell("Gesamttotal", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", fulltotal_CHF), font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));		
		}
        return table;
	}
	
	public PdfPTable getFullShoppingBasket(PdfContentByte cb, String mode) {
		int position = 0;
		float sub_total_CHF = 0.0f;

		BarcodeEAN codeEAN = new BarcodeEAN();
		
		// Pos | Menge | Eancode | Bezeichnung | Preis
        PdfPTable table = new PdfPTable(new float[] {1,1,3,6,2});
        table.setWidthPercentage(100f);
        table.getDefaultCell().setPadding(5);
        table.setSpacingAfter(5f);
        
		PdfPCell cell = new PdfPCell();	
        
        table.addCell(getStringCell("Pos.", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));
		table.addCell(getStringCell("Menge", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("EAN", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("Artikel", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_MIDDLE, 1));        
        table.addCell(getStringCell("Preis (CHF)", font_bold_10, Rectangle.TOP|Rectangle.BOTTOM, Element.ALIGN_RIGHT, 1));
        
        if (m_shopping_basket.size()>0) {
			for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
				Article article = entry.getValue();				
					
				if (mode.equals("all") 
						|| (mode.equals("rest") && (m_map_of_authors==null || !anyElemIsContained(m_map_of_authors, article.getAuthor().trim().toLowerCase())))) {	
					String price_pruned = "";					
					if (article.getCode()!=null && article.getCode().equals("ibsa")) {
						float cr = article.getCashRebate();
						if (article.getDraufgabe()>0)
							price_pruned = String.format("%.2f", article.getBuyingPrice(0.0f));
						else
							price_pruned = String.format("%.2f", article.getBuyingPrice(cr));
					} else {
						price_pruned = article.getCleanExfactoryPrice();						
					}
					
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
						sub_total_CHF += price_CHF;					
						table.addCell(getStringCell(String.format("%.2f", price_CHF), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 1));						
					}
				}
			}
			
			table.addCell(getStringCell("Subtotal", font_bold_10, Rectangle.TOP, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_bold_10, Rectangle.TOP, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", sub_total_CHF), font_bold_10, Rectangle.TOP, Element.ALIGN_RIGHT, 2));			
				
			table.addCell(getStringCell("MwSt (8%)", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", sub_total_CHF*0.08f), font_norm_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));	
				
			table.addCell(getStringCell("Total", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell("", font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_MIDDLE, 2));
			table.addCell(getStringCell(String.format("%.2f", sub_total_CHF*1.08f), font_bold_10, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT, 2));		
		}
        return table;
	}
	
	public void generateCsv(Author author, String filename, String type) {
		String name_split[] = filename.split("_");
		String date = name_split[name_split.length-1];		
		if (date.contains(".")) 
			date = date.substring(0, date.lastIndexOf("."));
		Preferences prefs = Preferences.userRoot().node(SettingsPage.class.getName());		
		String gln_code = prefs.get("glncode", "7610000000000");	
		String email_address = prefs.get("emailadresse", "");
		if (type.equals("specific")) {
			// These are all authors which are specifically listed (e.g. ibsa, desitin)
	        if (m_shopping_basket.size()>0) {
	        	int pos = 0;
	        	String total_price_CHF = "";
	        	String shopping_basket_str = "";
				for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
					Article article = entry.getValue();									
					if (article.getAuthor().trim().toLowerCase().contains(author.getShortName())) {						
						String price_pruned = "";				
						if (article.getCode()!=null && article.getCode().equals("ibsa")) {
							float cr = article.getCashRebate();
							if (article.getDraufgabe()>0) {
								price_pruned = String.format("%.2f", article.getBuyingPrice(0.0f));
								total_price_CHF = String.format("%.2f", article.getTotBuyingPrice(0.0f));
							} else {
								price_pruned = String.format("%.2f", article.getBuyingPrice(cr));
								total_price_CHF = String.format("%.2f", article.getTotBuyingPrice(cr));
							}
						} else {
							price_pruned = article.getCleanExfactoryPrice();
							total_price_CHF = String.format("%.2f", article.getTotExfactoryPrice());
						}						
						if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {	
							/*
							double price = Math.ceil(article.getQuantity()*Float.parseFloat(price_pruned)*100.0f)/100.0f;
							String total_price_CHF = String.format("%.2f", price);
							*/
							char shipping_type = 'U';	// unknown
							if (m_map_of_authors.containsKey(author.getShortName())) 
								shipping_type = m_map_of_authors.get(author.getShortName()).getShippingType();
							if (article.getQuantity()>0) {
								shopping_basket_str += (++pos) + "|" + date + "|" 
										+ gln_code + "|" + email_address + "|"
										+ article.getEanCode() + "|" 
										+ article.getPackTitle() + "|" 
										+ "Bezahlt|" + article.getQuantity() + "|" 										
										+ price_pruned + "|"
										+ total_price_CHF + "|" + article.getVat() + "|"
										+ shipping_type + "\n"; 
							}
							if (article.getDraufgabe()>0) {
								shopping_basket_str += (++pos) + "|" + date + "|" 
										+ gln_code + "|" + email_address + "|"
										+ article.getEanCode() + "|" 
										+ article.getPackTitle() + "|"
										+ "Gratis|" + article.getDraufgabe() + "|"
										+ shipping_type + "\n";
							}
						}
					}
				}
				/*
				// Add shipping costs at very end of file
				Author a = m_map_of_authors.get(author.getShortName());
				shopping_basket_str += a.getShippingCosts() + "\n";
				*/
				
				try {
					CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
				   	encoder.onMalformedInput(CodingErrorAction.REPORT);
				   	encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
					OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), encoder);
					BufferedWriter bw = new BufferedWriter(osw);      			
					bw.write(shopping_basket_str);
					bw.close();
					// System.out.println("Saved CSV to " + filename);
		        } catch(IOException e) {
		        	System.out.println("Could not save CSV file...");
		        }
	        }
		} else {
			// These are all authors which are not specifically listed...
	        if (m_shopping_basket.size()>0) {
	        	int pos = 0;
	        	String shopping_basket_str = "";
				for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
					Article article = entry.getValue();							
					if (type.equals("all") 
							|| (type.equals("rest") && (m_map_of_authors==null || !anyElemIsContained(m_map_of_authors, article.getAuthor().trim().toLowerCase())))) {	
						String price_pruned = "";
						if (article.getCode()!=null && article.getCode().equals("ibsa")) {
							float cr = article.getCashRebate();
							if (article.getDraufgabe()>0)
								price_pruned = String.format("%.2f", article.getBuyingPrice(0.0f));
							else
								price_pruned = String.format("%.2f", article.getBuyingPrice(cr));
						} else {
							price_pruned = article.getCleanExfactoryPrice();						
						}
						if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {	
							float price_CHF = article.getQuantity()*Float.parseFloat(price_pruned);
							shopping_basket_str += (++pos) + "|" + date + "|" 
									+ gln_code + "|" + email_address + "|"
									+ article.getQuantity() + "|" 
									+ article.getEanCode() + "|" 
									+ article.getPackTitle() + "|" 
									+ price_pruned + "|"
									+ price_CHF + "\n"; 
						}
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
					// System.out.println("Saved CSV to " + filename);
		        } catch(IOException e) {
		        	System.out.println("Could not save CSV file...");
		        }
	        }
		}
	}	
}
