package com.maxl.java.amikodesk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MainSqlDb {

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
	public static final String KEY_PACKAGES = "packages";
	
	private static final String DATABASE_TABLE = "amikodb";
	
	/**
	 * Table columns used for fast queries
	 */
	private static final String SHORT_TABLE = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
			KEY_ROWID, KEY_TITLE, KEY_AUTH, KEY_ATCCODE, KEY_SUBSTANCES, KEY_REGNRS, 
			KEY_ATCCLASS, KEY_THERAPY, KEY_APPLICATION, KEY_INDICATIONS,
			KEY_CUSTOMER_ID, KEY_PACK_INFO, KEY_ADDINFO);
	
	private static final String PACKAGES_TABLE = String.format("%s,%s,%s,%s,%s", 
			KEY_ROWID, KEY_TITLE, KEY_AUTH, KEY_REGNRS, KEY_PACKAGES);
	
	private Connection m_conn;
	private Statement m_stat;
	private ResultSet m_rs;

	public void loadDB(String db_lang) {
		try {
			// Initialize org.sqlite.JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Create connection to db
			String db_path = "./dbs/amiko_db_full_idx_" + db_lang + ".db";
			m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);		
			m_stat = m_conn.createStatement();
		} catch (SQLException e ) {
			System.err.println(">> SqlDatabase: SQLException in loadDB!");
		} catch (ClassNotFoundException e) {
			System.err.println(">> SqlDatabase: ClassNotFoundException in loadDB!");
		}
	}
	
	/**
	 * Load database from path
	 * @param db_path
	 * @return 1 if success, 0 if error/exception
	 */
	public int loadDBFromPath(String db_path) {
		try {
			// Check if file exists
			File f = new File(db_path);
			if (f.exists() && f.length()>0) {
				// Initialize org.sqlite.JDBC driver
				Class.forName("org.sqlite.JDBC");
				// Create connection to db
				m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);	
				m_stat = m_conn.createStatement();
				// Trick: check if following two instructions force an exception to occur...
				String query = "select count(*) from " + DATABASE_TABLE; 
				m_rs = m_stat.executeQuery(query);
				// Display user version
				System.out.println("Loaded alternative database ver." + getUserVersion());
			} else {
				System.out.println("No alternative database found");
				return 0;
			}
		} catch (SQLException e ) {
			System.err.println(">> SqlDatabase: SQLException in loadDBFromPath!");
			return 0;
		} catch (ClassNotFoundException e) {
			System.err.println(">> SqlDatabase: ClassNotFoundException in loadDBFromPath!");
			return 0;
		}
		return 1;
	}
		
	/**
	 * Copy file from src to dst
	 * @param src_file
	 * @param dst_file
	 */
	@SuppressWarnings("resource")
	public void copyDB(File src_file, File dst_file){
		try {
			if (!src_file.exists ())
				return;
			if (!dst_file.exists())
				dst_file.createNewFile();
			FileChannel source = null;
			FileChannel destination = null;
			source = new FileInputStream(src_file).getChannel();
			destination = new FileOutputStream(dst_file).getChannel();
			// Transfer source file to destination
			if (destination!=null && source!=null)
				destination.transferFrom(source, 0, source.size());
			if (source!=null)
				source.close();
			if (destination!=null)
				destination.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
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
	
	public int getNumLinesInFile(String filename) {
		try {
			FileInputStream fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));
			int count = 0;
			while (br.readLine() != null)
				count++;
			br.close();
			return count;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getUserVersion() {
		int user_version = 0;

		try {
			m_stat = m_conn.createStatement();
			String query = "pragma user_version";
			m_rs = m_stat.executeQuery(query);
			user_version = m_rs.getInt(1);
		} catch (SQLException e) {
			System.err
					.println(">> SqlDatabase: SQLException in getUserVersion!");
		}

		return user_version;
	}

	public int getNumRecords() {
		int num_rec = 0;

		try {
			m_stat = m_conn.createStatement();
			String query = "select count(*) from " + DATABASE_TABLE;
			m_rs = m_stat.executeQuery(query);
			num_rec = m_rs.getInt(1);
		} catch (SQLException e) {
			System.err
					.println(">> SqlDatabase: SQLException in getNumRecords!");
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
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE + " where " 
					+ KEY_TITLE + " like " + "'" + title + "%' or "
					+ KEY_TITLE + " like " + "'%" + title + "%'";
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
			System.err.println(">> SqlDatabase: SQLException in searchAuth!");
		}

		return med_auth;
	}

	public List<Medication> searchATC(String atccode) {
		List<Medication> med_atccode = new ArrayList<Medication>();

		try {
			/*
			 * NSString *query = [NSString stringWithFormat:@
			 * "select %@ from %@ where %@ like '%%;%@%%' or %@ like '%@%%' or %@ like '%% %@%%' or %@ like '%@%%' or %@ like '%%;%@%%'"
			 * , SHORT_TABLE, DATABASE_TABLE, KEY_ATCCODE, atccode, KEY_ATCCODE,
			 * atccode, KEY_ATCCODE, atccode, KEY_ATCCLASS, atccode,
			 * KEY_ATCCLASS, atccode];
			 */

			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE + " where " 
					+ KEY_ATCCODE + " like " + "'%;" + atccode + "%' or " 
					+ KEY_ATCCODE + " like " + "'" + atccode + "%' or " 
					+ KEY_ATCCODE + " like " + "'% " + atccode + "%' or " 
					+ KEY_ATCCLASS + " like " + "'" + atccode + "%' or " 
					+ KEY_ATCCLASS + " like " + "'%;" + atccode + "%' or " 
					+ KEY_ATCCLASS + " like " + "'%#" + atccode + "%' or " 
					+ KEY_SUBSTANCES + " like " + "'%, " + atccode + "%' or " 
					+ KEY_SUBSTANCES + " like " + "'" + atccode + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_atccode.add(cursorToShortMedi(m_rs));
			}
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchATC!");
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
			System.err.println(">> SqlDatabase: SQLException in searchIngredient!");
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
			System.err.println(">> SqlDatabase: SQLException in searchRegNr!");
		}

		return med_regnr;
	}

	public List<Medication> searchEanCode(String eancode) {
		List<Medication> med_eancode = new ArrayList<Medication>();

		try {
			m_stat = m_conn.createStatement();
			String query = "select " + PACKAGES_TABLE + " from " + DATABASE_TABLE + " where " 
					+ KEY_PACKAGES + " like " + "'%" + eancode + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_eancode.add(cursorToPackMedi(m_rs));
			}
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchEanCode!");
		}

		return med_eancode;
	}
	
	public List<Medication> searchApplication(String application) {
		List<Medication> med_application = new ArrayList<Medication>();

		try {
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + DATABASE_TABLE
					+ " where " + KEY_APPLICATION + " like " + "'%,"
					+ application + "%' or " + KEY_APPLICATION + " like " + "'"
					+ application + "%' or " + KEY_APPLICATION + " like "
					+ "'% " + application + "%' or " + KEY_APPLICATION
					+ " like " + "'%;" + application + "%' or "
					+ KEY_INDICATIONS + " like " + "'" + application + "%' or "
					+ KEY_INDICATIONS + " like " + "'%;" + application + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				med_application.add(cursorToShortMedi(m_rs));
			}
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in searchApplication!");
		}

		return med_application;
	}

	/**
	 * Retrieves database entry based on id
	 * 
	 * @param rowId
	 * @return database entry
	 */
	public Medication getMediWithId(long rowId) {
		Medication medi = null;

		try {
			medi = cursorToMedi(getRecord(rowId));
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getMediWithId!");
		}

		return medi;
	}

	/**
	 * Retrieves database entry based on id
	 * 
	 * @param rowId
	 * @return database entry
	 */
	public String getContentWithId(long rowId) {
		Medication medi = null;

		try {
			medi = cursorToMedi(getRecord(rowId));
			return medi.getContent();
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getContentWithId!");
		}

		return null;
	}

	/**
	 * Retrieves specific record
	 * 
	 * @param rowId
	 * @return cursor
	 * @throws SQLException
	 */
	public ResultSet getRecord(long rowId) throws SQLException {
		ResultSet mResult = null;

		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + DATABASE_TABLE + " where "
					+ KEY_ROWID + "=" + rowId;
			m_rs = m_stat.executeQuery(query);
			mResult = m_rs;
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getRecord!");
		}

		return mResult;
	}

	/**
	 * Maps cursor to medication (short version, fast)
	 * 
	 * @param cursor
	 * @return
	 */
	private Medication cursorToShortMedi(ResultSet result) {
		Medication medi = new Medication();
		try {
			medi.setId(result.getLong(1)); // KEY_ROWID
			medi.setTitle(result.getString(2)); // KEY_TITLE
			medi.setAuth(result.getString(3)); // KEY_AUTH
			medi.setAtcCode(result.getString(4)); // KEY_ATCCODE
			medi.setSubstances(result.getString(5)); // KEY_SUBSTANCES
			medi.setRegnrs(result.getString(6)); // KEY_REGNRS
			medi.setAtcClass(result.getString(7)); // KEY_ATCCLASS
			medi.setTherapy(result.getString(8)); // KEY_THERAPY
			medi.setApplication(result.getString(9)); // KEY_APPLICATION
			medi.setIndications(result.getString(10)); // KEY_INDICATIONS
			medi.setCustomerId(result.getInt(11)); // KEY_CUSTOMER_ID
			medi.setPackInfo(result.getString(12)); // KEY_PACK_INFO
			medi.setAddInfo(result.getString(13)); // KEY_ADD_INFO
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in cursorToShortMedi");
		}
		return medi;
	}

	private Medication cursorToPackMedi(ResultSet result) {
		Medication medi = new Medication();
		try {
			medi.setId(result.getLong(1));	// KEY_ROWID
			medi.setTitle(result.getString(2)); // KEY_TITLE
			medi.setAuth(result.getString(3)); // KEY_AUTH
			medi.setRegnrs(result.getString(4)); // KEY_REGNRS
			medi.setPackages(result.getString(5)); // KEY_PACKAGES
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in cursorToPackMedi");
		}
		return medi;
	}
	
	/**
	 * Maps cursor to medication (long version, slow)
	 * 
	 * @param cursor
	 * @return
	 */
	private Medication cursorToMedi(ResultSet result) {
		Medication medi = new Medication();
		try {
			medi.setId(result.getLong(1)); // KEY_ROWID
			medi.setTitle(result.getString(2)); // KEY_TITLE
			medi.setAuth(result.getString(3)); // KEY_AUTH
			medi.setAtcCode(result.getString(4)); // KEY_ATCCODE
			medi.setSubstances(result.getString(5)); // KEY_SUBSTANCES
			medi.setRegnrs(result.getString(6)); // KEY_REGNRS
			medi.setAtcClass(result.getString(7)); // KEY_ATCCLASS
			medi.setTherapy(result.getString(8)); // KEY_THERAPY
			medi.setApplication(result.getString(9)); // KEY_APPLICATION
			medi.setIndications(result.getString(10)); // KEY_INDICATIONS
			medi.setCustomerId(result.getInt(11)); // KEY_CUSTOMER_ID
			medi.setPackInfo(result.getString(12)); // KEY_PACK_INFO
			medi.setAddInfo(result.getString(13)); // KEY_ADD_INFO
			medi.setSectionIds(result.getString(14)); // KEY_SECTION_IDS
			medi.setSectionTitles(result.getString(15)); // KEY_SECTION_TITLES
			medi.setContent(result.getString(16)); // KEY_CONTENT
													// KEY_STYLE... (ignore)
			medi.setPackages(result.getString(18)); // KEY_PACKAGES
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in cursorToMedi");
		}
		return medi;
	}
}
