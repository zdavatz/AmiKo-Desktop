/*
Copyright (c) 2013 Max Lungarella <cybrmx@gmail.com>

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

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SqlDatabase {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_AUTH = "auth";
	public static final String KEY_ATCCODE = "atc";
	public static final String KEY_SUBSTANCES = "substances";
	public static final String KEY_REGNRS = "regnrs";
	public static final String KEY_ATCCLASS = "atc_class";
	public static final String KEY_THERAPY = "tindex_str";
	public static final String KEY_APPLICATION = "application_str";
	public static final String KEY_INDICATIONS = "indications_str";
	public static final String KEY_CUSTOMER_ID = "customer_id";	
	public static final String KEY_PACK_INFO = "pack_info_str";
	public static final String KEY_ADDINFO = "add_info_str";
	public static final String KEY_IDS = "ids_str";
	public static final String KEY_SECTIONS = "titles_str";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_STYLE = "style_str";
	
	private static final String DATABASE_TABLE = "amikodb";
	
	/**
	 * Table columns used for fast queries
	 */
	private static final String SHORT_TABLE = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
				KEY_ROWID, KEY_TITLE, KEY_AUTH, KEY_ATCCODE, KEY_SUBSTANCES, KEY_REGNRS, 
				KEY_ATCCLASS, KEY_THERAPY, KEY_APPLICATION, KEY_INDICATIONS,
				KEY_CUSTOMER_ID, KEY_PACK_INFO, KEY_CONTENT);
	
	private Connection m_conn;
	private Statement m_stat;
	private ResultSet m_rs;
	
	public void loadDB(String db_lang) {
		try {
			// Initialize org.sqlite.JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Create connection to db
			String db_path = System.getProperty("user.dir") + "/dbs/amiko_db_full_idx_" + db_lang + ".db";
			m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);		
			m_stat = m_conn.createStatement();
		} catch (SQLException e ) {
			System.err.println(">> SqlDatabase: SQLException in loadDB!");
		} catch (ClassNotFoundException e) {
			System.err.println(">> SqlDatabase: ClassNotFoundException in loadDB!");
		}
	}
	
	public int loadDBFromPath(String db_path) {
		try {
			// Initialize org.sqlite.JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Create connection to db
			m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);		
			m_stat = m_conn.createStatement();
		} catch (SQLException e ) {
			System.err.println(">> SqlDatabase: SQLException in loadDB!");
			return 0;
		} catch (ClassNotFoundException e) {
			System.err.println(">> SqlDatabase: ClassNotFoundException in loadDB!");
			return 0;
		}
		return 1;
	}
	
	
	public void closeDB() {
		try {
			m_rs.close();
			m_stat.close();
			m_conn.close();
		} catch(SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in closeDB!");
		}
	}
	
	public int chooseDB(JFrame frame, String db_lang) {
		JFileChooser fc = new JFileChooser();
        recursivelySetFonts(fc, new Font("Dialog", Font.PLAIN, 12));
        int returnVal = -1;
        if (db_lang.equals("de"))
        	returnVal = fc.showDialog(frame, "Datenbank wählen");
        else if (db_lang.equals("fr"))
        	returnVal = fc.showDialog(frame, "Choisir banque de données");
        if (returnVal==JFileChooser.APPROVE_OPTION) {
        	closeDB();
        	String db_file = fc.getSelectedFile().toString();
        	System.out.println("Selected db: " + db_file);
        	if (loadDBFromPath(db_file)>0 && getNumRecords()>0) {
        		// Setup icon
        		ImageIcon icon = new ImageIcon("./icons/amiko_icon.png");
    	        Image img = icon.getImage();
    		    Image scaled_img = img.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
    		    icon = new ImageIcon(scaled_img);
    		    // Display friendly message
        		JOptionPane.showMessageDialog(frame, "AmiKo Datenbank mit " + getNumRecords() + " Zeilen " +
        				"erfolgreich geladen!", "Erfolg", JOptionPane.PLAIN_MESSAGE, icon);
        		return 1;
        	} else {
        		// Show message: db not kosher!
        		JOptionPane.showMessageDialog(frame, "Fehler beim laden der Datenbank!",
        				"Fehler", JOptionPane.ERROR_MESSAGE);        		
        		// Load standard db
        		loadDB(db_lang);
        		return 0;
        	}
        }
        return 0;
	}
	
    private void recursivelySetFonts(Component comp, Font font) {
        comp.setFont(font);
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            for(int j=0, ub=cont.getComponentCount(); j<ub; ++j)
                recursivelySetFonts(cont.getComponent(j), font);
        }
    }
	
	public int getNumRecords() {
		int num_rec = 0;
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select count(*) from " + DATABASE_TABLE; 
			m_rs = m_stat.executeQuery(query);
			num_rec = m_rs.getInt(1);
		} catch(SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getNumRecords!");
		}
		
		return num_rec; 
	}
	
	public List<Medication> getAllTitles() {
		List<Medication> med_titles = new ArrayList<Medication>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE;
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_titles.add(cursorToShortMedi(m_rs));
			}
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getAllTitles!");
		}
		
		return med_titles;
	}
	
	public List<String> getAllContents() {
		List<String> med_contents = new ArrayList<String>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + DATABASE_TABLE;
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_contents.add(m_rs.getString(15));
			}
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getAllTitles!");
		}
		
		return med_contents;
	}
	
	public List<Medication> searchTitle(String title) {
		List<Medication> med_titles = new ArrayList<Medication>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE
					+ " where " + KEY_TITLE + " like " + "'" + title + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_titles.add(cursorToShortMedi(m_rs));
			} 
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchTitle!");
		}
		
		return med_titles;
	}
	
	public List<Medication> searchAuth(String auth) {
		List<Medication> med_auth = new ArrayList<Medication>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE 
					+ " where " + KEY_AUTH + " like " + "'" + auth + "%'";	
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_auth.add(cursorToShortMedi(m_rs));
			} 
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchTitle!");
		}
		
		return med_auth;
	}
	
	public List<Medication> searchATC(String atccode) {
		List<Medication> med_atccode = new ArrayList<Medication>();
		
		try {
			/*
		    NSString *query = [NSString stringWithFormat:@"select %@ from %@ where %@ like '%%;%@%%' or %@ like '%@%%' or %@ like '%% %@%%' or %@ like '%@%%' or %@ like '%%;%@%%'",
		                       SHORT_TABLE, DATABASE_TABLE, KEY_ATCCODE, atccode, KEY_ATCCODE, atccode, KEY_ATCCODE, atccode, KEY_ATCCLASS, atccode, KEY_ATCCLASS, atccode];
		  */
		    
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE + " where " 
					+ KEY_ATCCODE + " like " + "'%;" + atccode + "%' or "
					+ KEY_ATCCODE + " like " + "'" + atccode + "%' or "
					+ KEY_ATCCODE + " like " + "'% " + atccode + "%' or "
					+ KEY_ATCCLASS + " like " + "'" + atccode + "%' or "
					+ KEY_ATCCLASS + " like " + "'%;" + atccode + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_atccode.add(cursorToShortMedi(m_rs));
			} 
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchTitle!");
		}
		
		return med_atccode;
	}
	
	public List<Medication> searchIngredient(String ingredient) {
		List<Medication> med_ingredient = new ArrayList<Medication>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE + " where "
					+ KEY_SUBSTANCES + " like " + "'%, " + ingredient + "%' or "
					+ KEY_SUBSTANCES + " like " + "'" + ingredient + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_ingredient.add(cursorToShortMedi(m_rs));
			} 
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchTitle!");
		}
		
		return med_ingredient;
	}
	
	public List<Medication> searchRegNr(String regnr) {
		List<Medication> med_regnr = new ArrayList<Medication>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE + " where "
					+ KEY_REGNRS + " like " + "'%, " + regnr + "%' or "
					+ KEY_REGNRS + " like " + "'" + regnr + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_regnr.add(cursorToShortMedi(m_rs));
			} 
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchTitle!");
		}
		
		return med_regnr;
	}
	
	public List<Medication> searchApplication(String application) {
		List<Medication> med_application = new ArrayList<Medication>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE + " where "
					+ KEY_APPLICATION + " like " + "'%," + application + "%' or "
					+ KEY_APPLICATION + " like " + "'" + application + "%' or "
					+ KEY_APPLICATION + " like " + "'% " + application + "%' or "
					+ KEY_APPLICATION + " like " + "'%;" + application +"%' or "
					+ KEY_INDICATIONS + " like " + "'" + application + "%' or "					
					+ KEY_INDICATIONS + " like " + "'%;" + application + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_application.add(cursorToShortMedi(m_rs));
			} 
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchTitle!");
		}
		
		return med_application;
	}
	
	/**
	 * Retrieves database entry based on id
	 * @param rowId
	 * @return database entry
	 */
	public Medication getMediWithId(long rowId) {
		Medication medi = null;
		
		try {
			medi = cursorToMedi(getRecord(rowId));
		} catch(SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getMediWithId!");
		}
		
		return medi;
	}

	/**
	 * Retrieves database entry based on id
	 * @param rowId
	 * @return database entry
	 */
	public String getContentWithId(long rowId) {
		Medication medi = null;
		
		try {
			medi = cursorToMedi(getRecord(rowId));
			return medi.getContent();
		} catch(SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getContentWithId!");
		}
		
		return null;
	}
	
	/**
	 * Retrieves specific record
	 * @param rowId
	 * @return cursor
	 * @throws SQLException
	 */
	public ResultSet getRecord(long rowId) throws SQLException {
		ResultSet mResult = null;
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + DATABASE_TABLE + " where " + KEY_ROWID + "=" + rowId;
			m_rs = m_stat.executeQuery(query);
			mResult = m_rs;
		} catch(SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getRecord!");
		}
		
		return mResult;
	}
	
	/**
	 * Maps cursor to medication (short version, fast)
	 * @param cursor
	 * @return
	 */
	private Medication cursorToShortMedi(ResultSet result) {
		Medication medi = new Medication();		
		try {
			medi.setId(result.getLong(1));				// KEY_ROWID
			medi.setTitle(result.getString(2));			// KEY_TITLE
			medi.setAuth(result.getString(3));			// KEY_AUTH
			medi.setAtcCode(result.getString(4));		// KEY_ATCCODE
			medi.setSubstances(result.getString(5));	// KEY_SUBSTANCES
			medi.setRegnrs(result.getString(6));		// KEY_REGNRS
			medi.setAtcClass(result.getString(7));		// KEY_ATCCLASS
			medi.setTherapy(result.getString(8));		// KEY_THERAPY
			medi.setApplication(result.getString(9));	// KEY_APPLICATION
			medi.setIndications(result.getString(10));	// KEY_INDICATIONS
			medi.setCustomerId(result.getInt(11));		// KEY_CUSTOMER_ID
			medi.setPackInfo(result.getString(12));		// KEY_PACK_INFO
		} catch(SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in cursorToShortMedi");
		}
		return medi;
	}
	
	/**
	 * Maps cursor to medication (long version, slow)
	 * @param cursor
	 * @return
	 */
	private Medication cursorToMedi(ResultSet result) {
		Medication medi = new Medication();
		try {
			medi.setId(result.getLong(1));
			medi.setTitle(result.getString(2));				// KEY_TITLE
			medi.setAuth(result.getString(3));				// KEY_AUTH
			medi.setAtcCode(result.getString(4));			// KEY_ATCCODE
			medi.setSubstances(result.getString(5));		// KEY_SUBSTANCES
			medi.setRegnrs(result.getString(6));			// KEY_REGNRS
			medi.setAtcClass(result.getString(7));			// KEY_ATCCLASS
			medi.setTherapy(result.getString(8));			// KEY_THERAPY
			medi.setApplication(result.getString(9));		// KEY_APPLICATION
			medi.setIndications(result.getString(10));		// KEY_INDICATIONS
			medi.setCustomerId(result.getInt(11));			// KEY_CUSTOMER_ID
			medi.setPackInfo(result.getString(12));			// KEY_PACK_INFO
			medi.setAddInfo(result.getString(13));			// KEY_ADD_INFO
			medi.setSectionIds(result.getString(14));		// KEY_SECTION_IDS
			medi.setSectionTitles(result.getString(15));	// KEY_SECTION_TITLES
			medi.setContent(result.getString(16));			// KEY_CONTENT
			medi.setStyle(result.getString(17));			// KEY_STYLE
		} catch(SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in cursorToMedi");
		}
		return medi;
	}
}
