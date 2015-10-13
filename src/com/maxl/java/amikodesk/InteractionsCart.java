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

import java.util.ArrayList;
import java.util.Map;

public class InteractionsCart {

	private static String m_application_data_folder = null;
	private static Map<String, String> m_interactions_map = null;
	private static String m_images_dir = "";
	private static String m_jscripts_str = null;
	private static String m_css_interactions_str = null;
	private static String[] m_section_titles = null;
	
	public InteractionsCart() {
		m_application_data_folder = Utilities.appDataFolder();
		m_interactions_map = FileOps.readFromCsvToMap(m_application_data_folder + "\\" + Constants.DEFAULT_INTERACTION_CSV_BASE + Utilities.appLanguage() + ".csv");
		if (m_interactions_map==null) {
			System.out.println("Loading default drug interactions csv file");
			m_interactions_map = FileOps.readFromCsvToMap("./dbs/" + Constants.DEFAULT_INTERACTION_CSV_BASE + Utilities.appLanguage() + ".csv");
		}
		//
		m_images_dir = System.getProperty("user.dir") + "/images/";	
		// Load javascripts
		m_jscripts_str = FileOps.readFromFile(Constants.JS_FOLDER + "interaction_callbacks.js");
		// Load interactions css style sheet
		m_css_interactions_str = "<style>" + FileOps.readFromFile(Constants.INTERACTIONS_SHEET) + "</style>";
	}
	
	public String[] sectionTitles() {
		return m_section_titles;
	}
	
	public String updateHtml(Map<String, Medication> med_basket) {
		// Redisplay selected meds
		String basket_html_str = "<table id=\"Interaktionen\" width=\"98%25\">";
		String delete_all_button_str = "";
		String interactions_html_str = "";
		String top_note_html_str = "";
		String legend_html_str = "";
		String bottom_note_html_str = "";
		String atc_code1 = "";
		String atc_code2 = "";
		String name1 = "";
		String delete_text = "löschen";
		String delete_all_text = "alle löschen";
		String[] m_code1 = null;
		String[] m_code2 = null;
		int med_counter = 1;

		if (Utilities.appLanguage().equals("de")) {
			delete_text = "löschen";
			delete_all_text = "alle löschen";
		} else if (Utilities.appLanguage().equals("fr")) {
			delete_text = "annuler";
			delete_all_text = "tout supprimer";
		}
		
		// Build interaction basket table
		if (med_basket.size()>0) {
			for (Map.Entry<String, Medication> entry1 : med_basket.entrySet()) {
				m_code1 = entry1.getValue().getAtcCode().split(";");
				atc_code1 = "k.A.";
				name1 = "k.A.";
				if (m_code1.length>1) {
					atc_code1 = m_code1[0];
					name1 = m_code1[1];
				}
				basket_html_str += "<tr>";
				basket_html_str += "<td>" + med_counter + "</td>"
						+ "<td>" + entry1.getKey() + " </td> " 
						+ "<td>" + atc_code1 + "</td>"
						+ "<td>" + name1 + "</td>"
						// + "<td align=\"right\">" + "<input type=\"button\" value=\"" + delete_text + "\" onclick=\"deleteRow('Interaktionen',this)\" />" + "</td>";
						+ "<td style=\"text-align:center;\">" + "<button type=\"button\" style=\"border:none;\" onclick=\"deleteRow('Interaktionen',this)\"><img src=\"" 
							+ m_images_dir + "trash_icon_2.png\" /></button>" + "</td>";

				basket_html_str += "</tr>";
				med_counter++;												
			}
			basket_html_str += "</table>";
			// Medikamentenkorb löschen
			delete_all_button_str = "<div id=\"Delete_all\"><input type=\"button\" value=\"" + delete_all_text + "\" onclick=\"deleteRow('Delete_all',this)\" /></div>";	
		} else {
			// Medikamentenkorb ist leer
			if (Utilities.appLanguage().equals("de"))
				basket_html_str = "<div>Ihr Medikamentenkorb ist leer.<br><br></div>";
			else if (Utilities.appLanguage().equals("fr"))
				basket_html_str = "<div>Votre panier de médicaments est vide.<br><br></div>";
		}

		// Build list of interactions
		ArrayList<String> section_str = new ArrayList<String>();
		// Add table to section titles
		if (Utilities.appLanguage().equals("de"))
			section_str.add("Interaktionen");
		else if (Utilities.appLanguage().equals("fr"))
			section_str.add("Interactions");
		if (med_counter>1) {
			for (Map.Entry<String, Medication> entry1 : med_basket.entrySet()) {
				m_code1 = entry1.getValue().getAtcCode().split(";");
				if (m_code1.length>1) {
					// Get ATC code of first drug, make sure to get the first in the list (the second one is not used)
					atc_code1 = m_code1[0].split(",")[0];
					for (Map.Entry<String, Medication> entry2 : med_basket.entrySet()) {
						m_code2 = entry2.getValue().getAtcCode().split(";");
						if (m_code2.length>1) {
							// Get ATC code of second drug
							atc_code2 = m_code2[0];						
							if (atc_code1!=null && atc_code2!=null && !atc_code1.equals(atc_code2)) {
								// Get html interaction content from drug interactions map
								String inter = m_interactions_map.get(atc_code1 + "-" + atc_code2);
								if (inter!=null) {
									inter = inter.replaceAll(atc_code1, entry1.getKey());
									inter = inter.replaceAll(atc_code2, entry2.getKey());
									interactions_html_str += (inter + "");
									// Add title to section title list
									if (!inter.isEmpty()) {
										if (Utilities.appCustomization().equals("ibsa"))
											section_str.add("<html>" + entry1.getKey() + " > " + entry2.getKey() + "</html>");
										else
											section_str.add("<html>" + entry1.getKey() + " &rarr; " + entry2.getKey() + "</html>");											
									}
								}
							}
						}
					}
				}
			}
		}
		
		if (med_basket.size()>0 && section_str.size()<2) {
			// Add note to indicate that there are no interactions
			if (Utilities.appLanguage().equals("de"))
				top_note_html_str = "<p class=\"paragraph0\">Zur Zeit sind keine Interaktionen zwischen diesen Medikamenten in der EPha.ch-Datenbank vorhanden. Weitere Informationen finden Sie in der Fachinformation.</p><br><br>";
			else if (Utilities.appLanguage().equals("fr"))
				top_note_html_str = "<p class=\"paragraph0\">Il n’y a aucune information dans la banque de données EPha.ch à propos d’une interaction entre les médicaments sélectionnés. Veuillez consulter les informations professionelles.</p><br><br>";
		} else if (med_basket.size()>0 && section_str.size()>1) {
			// Add color legend
			legend_html_str = addColorLegend();				
			// Add legend to section titles
			if (Utilities.appLanguage().equals("de"))
				section_str.add("Legende");
			else if (Utilities.appLanguage().equals("fr"))
				section_str.add("Légende");
		}
		if (Utilities.appLanguage().equals("de")) {
			bottom_note_html_str += "<p class=\"footnote\">1. Datenquelle: Public Domain Daten von EPha.ch.</p> " +
				"<p class=\"footnote\">2. Unterstützt durch:  IBSA Institut Biochimique SA.</p>";
		} else if (Utilities.appLanguage().equals("fr")) {
			bottom_note_html_str += "<p class=\"footnote\">1. Source des données: données du domaine publique de EPha.ch</p> " +
				"<p class=\"footnote\">2. Soutenu par: IBSA Institut Biochimique SA.</p>";
		}
		String jscript_str = "<script language=\"javascript\">" + m_jscripts_str + "</script>";
		String html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_interactions_str + "</head><body><div id=\"interactions\">" 
				+ basket_html_str + delete_all_button_str + "<br><br>" + top_note_html_str
				+ interactions_html_str + "<br>" + legend_html_str + "<br>" + bottom_note_html_str + "</div></body></html>";
		
		// Update section titles
		m_section_titles = section_str.toArray(new String[section_str.size()]);
		
		return html_str;
	}
	
	private String addColorLegend() {
		String legend = "<table id=\"Legende\" width=\"98%25\">";
	    /*
	     Risikoklassen
	     -------------
		     A: Keine Massnahmen notwendig (grün)
		     B: Vorsichtsmassnahmen empfohlen (gelb)
		     C: Regelmässige Überwachung (orange)
		     D: Kombination vermeiden (pinky)
		     X: Kontraindiziert (hellrot)
		     0: Keine Angaben (grau)
	    */
		// Sets the anchor
		if (Utilities.appLanguage().equals("de")) {
			legend = "<table id=\"Legende\" width=\"98%25\">";
			legend += "<tr><td bgcolor=\"#caff70\"></td>" +
					"<td>A</td>" +
					"<td>Keine Massnahmen notwendig</td></tr>";
			legend += "<tr><td bgcolor=\"#ffec8b\"></td>" +
					"<td>B</td>" +
					"<td>Vorsichtsmassnahmen empfohlen</td></tr>";
			legend += "<tr><td bgcolor=\"#ffb90f\"></td>" +
					"<td>C</td>" +
					"<td>Regelmässige Überwachung</td></tr>";
			legend += "<tr><td bgcolor=\"#ff82ab\"></td>" +
					"<td>D</td>" +
					"<td>Kombination vermeiden</td></tr>";
			legend += "<tr><td bgcolor=\"#ff6a6a\"></td>" +
					"<td>X</td>" +
					"<td>Kontraindiziert</td></tr>";				
		} else if (Utilities.appLanguage().equals("fr")) {
			legend = "<table id=\"Légende\" width=\"98%25\">";
			legend += "<tr><td bgcolor=\"#caff70\"></td>" +
					"<td>A</td>" +
					"<td>Aucune mesure nécessaire</td></tr>";
			legend += "<tr><td bgcolor=\"#ffec8b\"></td>" +
					"<td>B</td>" +
					"<td>Mesures de précaution sont recommandées</td></tr>";
			legend += "<tr><td bgcolor=\"#ffb90f\"></td>" +
					"<td>C</td>" +
					"<td>Doit être régulièrement surveillée</td></tr>";
			legend += "<tr><td bgcolor=\"#ff82ab\"></td>" +
					"<td>D</td>" +
					"<td>Eviter la combinaison</td></tr>";
			legend += "<tr><td bgcolor=\"#ff6a6a\"></td>" +
					"<td>X</td>" +
					"<td>Contre-indiquée</td></tr>";								
		}
		/*
		legend += "<tr><td bgcolor=\"#dddddd\"></td>" +
				"<td>0</td>" +
				"<td>Keine Angaben</td></tr>";				
		*/
		legend += "</table>";
		
		return legend;
	}	
}
