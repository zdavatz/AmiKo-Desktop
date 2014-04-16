package com.maxl.java.amikodesk;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InteractionsDb {
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ATC1 = "atc1";
	public static final String KEY_NAME1 = "name1";
	public static final String KEY_ATC2 = "atc2";
	public static final String KEY_NAME2 = "name2";	
	public static final String KEY_CONTENT = "content";
	
	private static final String INTERACTIONS_DB_TABLE = "interactionsdb";
	
	/**
	 * Table columns used for fast queries
	 */
	private static final String SHORT_TABLE = String.format("%s,%s,%s,%s,%s,%s", 
				KEY_ROWID, KEY_ATC1, KEY_NAME1, KEY_ATC2, KEY_NAME2, KEY_CONTENT);
	
	private Connection m_conn;
	private Statement m_stat;
	private ResultSet m_rs;
	
	public void loadDB(String db_lang) {
		try {
			// Initialize org.sqlite.JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Create connection to db
			String db_path = "./dbs/drug_interactions_idx_" + db_lang + ".db";
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
			// Check if file exists
			File f = new File(db_path);
			if (f.exists() && f.length()>0) {
				System.out.println("Loading alternative interaction database");
				// Initialize org.sqlite.JDBC driver
				Class.forName("org.sqlite.JDBC");
				// Create connection to db
				m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);		
				m_stat = m_conn.createStatement();
				// Trick: check if following two instructions force an exception to occur...
				String query = "select count(*) from " + INTERACTIONS_DB_TABLE; 
				m_rs = m_stat.executeQuery(query);
			} else {
				System.out.println("No alternative interactions database found");
				return 0;
			}
		} catch (SQLException e ) {
			System.err.println(">> SqlDatabase: SQLException in loadInteractionsDBFromPath!");
			return 0;
		} catch (ClassNotFoundException e) {
			System.err.println(">> SqlDatabase: ClassNotFoundException in loadInteractionsDBFromPath!");
			return 0;
		}
		return 1;
	}
	
	
	public List<String> searchATC(String atccode) {
		List<String> interactions = new ArrayList<String>();
		
		try {	    
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + INTERACTIONS_DB_TABLE + " where " 
					+ KEY_ATC1 + " like " + "'%" + atccode + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				// Adds content strings to list
				interactions.add(m_rs.getString(6));
			} 
		} catch (SQLException e) {
			System.err.println(">> InteractionsDb: SQLException in searchATC!");
		}
		
		return interactions;
	}
	
	public List<String> searchATC(String atccode1, String atccode2) {
		List<String> interactions = new ArrayList<String>();
		
		try {	    
			m_stat = m_conn.createStatement();
			String query = "select " + SHORT_TABLE + " from " + INTERACTIONS_DB_TABLE + " where " 
					+ KEY_ATC1 + " like " + "'%" + atccode1 + "%' and "
					+ KEY_ATC2 + " like " + "'%" + atccode2 + "%'";
			System.out.println(query);
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				// Adds content strings to list
				interactions.add(m_rs.getString(6));
			} 
		} catch (SQLException e) {
			System.err.println(">> InteractionsDb: SQLException in searchATC!");
		}
		
		return interactions;
	}
}
