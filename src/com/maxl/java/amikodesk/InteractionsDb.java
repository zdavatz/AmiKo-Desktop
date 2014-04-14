package com.maxl.java.amikodesk;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InteractionsDb {
		
	private static final String INTERACTIONS_DB_TABLE = "interactionsdb";
	
	private Connection m_conn;
	private Statement m_stat;
	private ResultSet m_rs;
	
	public void loadDB(String db_lang) {
		try {
			// Initialize org.sqlite.JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Create connection to db
			String db_path = "./dbs/interactions_db_idx_" + db_lang + ".db";
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
}
