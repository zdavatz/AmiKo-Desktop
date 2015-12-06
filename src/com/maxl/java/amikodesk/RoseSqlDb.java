package com.maxl.java.amikodesk;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoseSqlDb {

	private static final String KEY_ROWID = "_id";
	private static final String KEY_TITLE = "title";
	// private static final String KEY_SIZE = "size";
	// private static final String KEY_GALEN = "galen";
	// private static final String KEY_UNIT = "unit";
	private static final String KEY_EAN = "eancode";
	private static final String KEY_PHARMA = "pharmacode";
	private static final String KEY_ATC = "atc";
	// private static final String KEY_THERAPY = "theracode";
	// private static final String KEY_STOCK = "stock";
	// private static final String KEY_LIKES = "likes";
	// private static final String KEY_PRICE = "price";
	// private static final String KEY_AVAIL = "availability";
	private static final String KEY_SUPPLIER = "supplier";
	
	private static final String ROSE_DB_TABLE = "rosedb";
	
	private Connection m_conn;
	private Statement m_stat;
	private ResultSet m_rs;	
	
	public void loadDB() {
		try {
			// Initialize org.sqlite.JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Create connection to db
			String db_path = Constants.ROSE_FOLDER + "/rose_db_new_full.db";
			m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);		
			m_stat = m_conn.createStatement();
		} catch (SQLException e ) {
			System.err.println(">> RoseSqlDb: SQLException in loadDB!");
		} catch (ClassNotFoundException e) {
			System.err.println(">> RoseSqlDb: ClassNotFoundException in loadDB!");
		}
	}
	
	public int loadDBFromPath(String db_path) {
		try {
			File f = new File(db_path);
			// Check if file exists
			if (f.exists() && f.length()>0) {
				// Initialize org.sqlite.JDBC driver
				Class.forName("org.sqlite.JDBC");
				// Create connection to db
				m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);		
				m_stat = m_conn.createStatement();
				// Trick: check if following two instructions force an exception to occur...
				String query = "select count(*) from " + ROSE_DB_TABLE; 
				m_rs = m_stat.executeQuery(query);
			} else {
				System.out.println("No rosedb database found");
				return 0;
			}
		} catch (SQLException e ) {
			System.err.println(">> RoseSqlDb: SQLException in loadDBFromPath!");
			return 0;
		} catch (ClassNotFoundException e) {
			System.err.println(">> RoseSqlDb: ClassNotFoundException in loadDBFromPath!");
			return 0;
		}
		return 1;
	}
	
	private ResultSet getRecord(long rowId) throws SQLException {
		ResultSet result = null;
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + ROSE_DB_TABLE + " where "
					+ KEY_ROWID + "=" + rowId;
			m_rs = m_stat.executeQuery(query);
			result = m_rs;
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getRecord!");
		}

		return result;
	}
	
	public Article getArticleWithId(long rowId) {
		Article article = null;

		try {
			article = cursorToArticle(getRecord(rowId));
		} catch (SQLException e) {
			System.err.println(">> SqlDatabase: SQLException in getMediWithId!");
		}

		return article;
	}
	
	public List<Article> searchTitle(String title) {
		List<Article> list_of_articles = new ArrayList<Article>();

		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + ROSE_DB_TABLE + " where " 
					+ KEY_TITLE + " like " + "'" + title + "%'"; 
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				list_of_articles.add(cursorToArticle(m_rs));
			}
			
		} catch(SQLException e) {
			System.err.println(">> RoseSqlDb: SQLException in searchTitle!");
		}
		
		return list_of_articles;
	}

	public List<Article> searchSupplier(String supplier) {
		List<Article> list_of_articles = new ArrayList<Article>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + ROSE_DB_TABLE + " where " 
					+ KEY_SUPPLIER + " like " + "'" + supplier + "%'"; 
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				list_of_articles.add(cursorToArticle(m_rs));
			}
			
		} catch(SQLException e) {
			System.err.println(">> RoseSqlDb: SQLException in searchSupplier!");
		}
		
		return list_of_articles;
	}
	
	public List<Article> searchATC(String atccode) {
		List<Article> list_of_articles = new ArrayList<Article>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + ROSE_DB_TABLE + " where " 
					+ KEY_ATC + " like " + "'" + atccode + "%' or "
					+ KEY_ATC + " like " + "'%;" + atccode + "%'"; 
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				list_of_articles.add(cursorToArticle(m_rs));
			}

		} catch(SQLException e) {
			System.err.println(">> RoseSqlDb: SQLException in searchATC!");
		}
		
		return list_of_articles;		
	}
	
	public List<Article> searchEan(String code) {
		List<Article> list_of_articles = new ArrayList<Article>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + ROSE_DB_TABLE + " where " 
					+ KEY_EAN + " like " + "'" + code + "%' or "
					+ KEY_EAN + " like " + "'%;" + code + "%' or "
					+ KEY_PHARMA + " like " + "'" + code + "%'";
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				list_of_articles.add(cursorToArticle(m_rs));
			}
			
		} catch(SQLException e) {
			System.err.println(">> RoseSqlDb: SQLException in searchEan!");
		}
		
		return list_of_articles;
	}
	
	public List<Article> searchTherapy(String therapy) {
		List<Article> list_of_articles = new ArrayList<Article>();
		
		try {
			m_stat = m_conn.createStatement();
			String query = "select * from " + ROSE_DB_TABLE 
					+ " where " + KEY_TITLE + " like " + "'" + therapy + "%'"; 
			m_rs = m_stat.executeQuery(query);
			while (m_rs.next()) {
				list_of_articles.add(cursorToArticle(m_rs));
			}
			
		} catch(SQLException e) {
			System.err.println(">> RoseSqlDb: SQLException in searchTherapy!");
		}
		
		return list_of_articles;
	}
		
	private Article cursorToArticle(ResultSet result) {
		Article article = new Article();		

		try {
			article.setId(result.getLong(1));			 	// KEY_ROWID
			article.setPackTitle(result.getString(2));	 	// KEY_TITLE
			article.setPackSize(String.valueOf(result.getInt(3)));	// KEY_SIZE
			article.setPackGalen(result.getString(4));	 	// KEY_GALEN
			article.setPackUnit(result.getString(5));	 	// KEY_UNIT
			String ean_str = result.getString(6);			// KEY_EAN
			if (ean_str!=null) {
				String[] m_atc = ean_str.split(";");
				if (m_atc.length>1) {
					article.setEanCode(m_atc[0]);
					article.setRegnr(m_atc[1].trim());
				} else if (m_atc.length==1)
					article.setEanCode(m_atc[0]);
			}
			article.setPharmaCode(result.getString(7));	 	// KEY_PHARMA			
			String atc_str = result.getString(8);			// KEY_ATC
			if (atc_str!=null) {
				String[] m_code = atc_str.split(";");
				if (m_code.length>1) {
					article.setAtcCode(m_code[0]);			
					article.setAtcClass(m_code[1].trim());
				} else if (m_code.length==1)
					article.setAtcCode(m_code[0]);
			}
			article.setTherapyCode(result.getString(9)); 	// KEY_THERAPY
			article.setItemsOnStock(result.getInt(10));	 	// KEY_STOCK
			article.setExfactoryPrice(result.getString(11));// KEY_PRICE (= Rose Basis Preis)	
			article.setAvailability(result.getString(12));	// KEY_AVAIL
			article.setSupplier(result.getString(13));		// KEY_SUPPLIER
			article.setLikes(result.getInt(14));			// KEY_LIKES	
			boolean off_market = result.getBoolean(17);
			if (off_market)
				article.setAvailability("-1");				// -1 -> not on the market anymore!
			article.setFlags(result.getString(18));
		} catch (SQLException e) {
			System.err.println(">> RoseSqlDb: SQLException in cursorToArticle");
		}
		
		return article;
	}
	
}
