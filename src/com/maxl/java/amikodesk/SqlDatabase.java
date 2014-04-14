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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

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
	private Observer m_observer;
	private String m_app_lang;
	private String m_customization;
	private boolean m_loadedDBisZipped = false;
	private boolean m_operationCancelled = false;
	
	public boolean dbIsZipped() {
		return m_loadedDBisZipped;
	}
	
	public void addObserver(Observer observer) {
		m_observer = observer;
	}
	
	protected void notify(String str) {
		m_observer.update(null, str);
	}	
	
	protected void notifyObserver(String db) {
		notify("Loaded database: " + db);
	}
	
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
				System.out.println("Loading alternative database");
				// Initialize org.sqlite.JDBC driver
				Class.forName("org.sqlite.JDBC");
				// Create connection to db
				m_conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);		
				m_stat = m_conn.createStatement();
				// Trick: check if following two instructions force an exception to occur...
				String query = "select count(*) from " + DATABASE_TABLE; 
				m_rs = m_stat.executeQuery(query);
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
	
	/**
	 * Unzips file src in to file dst 
	 * @param src
	 * @param dst
	 */
	private void unzipSrcToDst(File src, File dst) {
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(src));
			byte buffer[] = new byte[4096];
		    int bytesRead;	
		    
			ZipEntry entry = null;		    
	        while ((entry = zin.getNextEntry()) != null) {
	            // Copy data from ZipEntry to file
	            FileOutputStream fos = new FileOutputStream(dst);
	            while ((bytesRead = zin.read(buffer)) != -1) {
	                fos.write(buffer, 0, bytesRead);
	            }
	            fos.close();
	        }
	        zin.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}		
	
	/**
	 * Downloads database from set URL, unzips it and transfers it to given folder
	 * @param frame
	 * @param db_lang
	 * @param app_folder
	 * @return
	 */
	public String updateDB(JFrame frame, String db_lang, String custom, String app_folder) {
		// Default is "de"
		String db_unzipped = app_folder + "\\amiko_db_full_idx_de.db";
		String amiko_report = app_folder + "\\amiko_report_de.html";
		String db_url = "http://pillbox.oddb.org/amiko_db_full_idx_de.zip";
		String report_url = "http://pillbox.oddb.org/amiko_report_de.html";
		// ... works also for "fr"
		if (db_lang.equals("fr")) {
			db_unzipped = app_folder + "\\amiko_db_full_idx_fr.db";
			amiko_report = app_folder + "\\amiko_report_fr.html";			
			db_url = "http://pillbox.oddb.org/amiko_db_full_idx_fr.zip";
			report_url = "http://pillbox.oddb.org/amiko_report_fr.html";
		}
		
		m_app_lang = db_lang;
		m_customization = custom;
		m_operationCancelled = false;
		if (isInternetReachable())
			new DownloadDialog(db_url, report_url, amiko_report, db_unzipped);
		else {
			AmiKoDialogs cd = new AmiKoDialogs(db_lang, custom);
			cd.NoInternetDialog();
			return "";
		}	
		return db_unzipped;
	}
	
	private static boolean isInternetReachable() {
        try {
            // Make a URL to a known source
            URL url = new URL("http://www.google.com");
            // Open a connection to that source
            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
            // Try to retrieve data from source. If no connection, this line will fail
            Object objData = urlConnect.getContent();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            return false;
        }
        return true;
    }	
		
	private class DownloadDialog extends JFrame implements PropertyChangeListener {
		
		private JDialog dialog = new JDialog(this, "Updating database", true);
	    private JProgressBar progressBar = new JProgressBar(0, 100);
	    private JPanel panel = new JPanel();
	    private JLabel label = new JLabel();
	    private JButton okButton = new JButton();
	    
	    public DownloadDialog(String databaseURL, String reportURL, String amikoReport, String unzippedDB) {
	    	progressBar.setPreferredSize(new Dimension(480, 30));
	    	progressBar.setStringPainted(true);			
			progressBar.setValue(0);	    	

			label.setText("Downloading...");
			
			// Button pane
			JPanel buttonPane = new JPanel();
		    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		    okButton.setText("Cancel");
		    okButton.setActionCommand("Cancel");
		    getRootPane().setDefaultButton(okButton);
		    buttonPane.add(okButton); 			    
		    
			panel = new JPanel(new BorderLayout(5, 5));
			panel.add(label, BorderLayout.NORTH);
			panel.add(progressBar, BorderLayout.CENTER);
			panel.add(buttonPane, BorderLayout.SOUTH);
			panel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));

			dialog.getContentPane().add(panel);			
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setModal(false);			
			ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
			if (m_customization.equals("desitin"))
				icon = new ImageIcon(Constants.DESITIN_ICON);
			else if (m_customization.equals("meddrugs"))
				icon = new ImageIcon(Constants.MEDDRUGS_ICON);
			dialog.setIconImage(icon.getImage());			
			dialog.setVisible(true);
			
			setLocationRelativeTo(null);    // center on screen
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			final DownloadWorker downloadWorker = new DownloadWorker(this, databaseURL, reportURL, 
					new File(amikoReport), new File(unzippedDB)); 
			// Attach property listener to it
    		downloadWorker.addPropertyChangeListener(this);
    		// Launch SwingWorker
    		downloadWorker.execute();
    		
		    okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					System.out.println("Database update done.");
					downloadWorker.cancel(true);
					dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING)); 
				}
			});     		
	    }
	    
	    public JLabel getLabel() {
	    	return label;
	    }
	    
	    public void setLabel(String l) {
	    	label.setText(l);
	    }
	    
	    public void setOKButton(String s) {
	    	okButton.setText(s);
	    }
	    
	    /**
	     * Update the progress bar's state whenever the unzip progress changes.
	     */
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
	        if ("progress" == evt.getPropertyName()) {
	            int progress = (Integer) evt.getNewValue();
	            progressBar.setValue(progress);
	        }
	    }
	}	
	
	private class DownloadWorker extends SwingWorker<Void, Integer> {
		private String mDatabaseURL;
		private String mReportURL;
		private File mAmikoDatabase;
		private File mAmikoReport;
		private DownloadDialog mDialog;

		public DownloadWorker(DownloadDialog dialog, String databaseURL, String reportURL, File amikoReport, File amikoDatabase) {
			mDatabaseURL = databaseURL;
			mReportURL = reportURL;
			mAmikoReport = amikoReport;
			mAmikoDatabase = amikoDatabase;
			mDialog = dialog;
		}
		
		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			byte buffer[] = new byte[4096];
			int bytesRead = -1;
			long totBytesRead = 0;
			int percentCompleted = 0;

			// Download database
			mDialog.setLabel("Downloading database file...");
			URL url = new URL(mDatabaseURL);
			InputStream is = url.openStream();
			// Retrieve file length from http header
			int sizeDB_in_bytes = Integer.parseInt(url.openConnection().getHeaderField("Content-Length"));
			File downloadedFile = new File(mAmikoDatabase.getAbsolutePath() + ".zip");
			OutputStream os = new FileOutputStream(downloadedFile);

			try {
				while (!isCancelled() && (bytesRead = is.read(buffer)) != -1) {
					os.write(buffer, 0, bytesRead);
					totBytesRead += bytesRead;
					percentCompleted = (int) (totBytesRead * 100 / (sizeDB_in_bytes));
					if (percentCompleted > 100)
						percentCompleted = 100;
					setProgress(percentCompleted);
					mDialog.setLabel("Downloading... " + totBytesRead/1000 + "kB out of " + sizeDB_in_bytes/1000 + "kB");
				}
				os.close();
			} catch (IOException e) {
				mDialog.getLabel().setText("Error downloading database file: " + e.getMessage());
				e.printStackTrace();
				setProgress(0);
				cancel(true);
			}
				
			// Unzip database file
			mDialog.setLabel("Unzipping...");
			
			try {
				if (!isCancelled()) {
					ZipFile zipFile = new ZipFile(downloadedFile);
					Enumeration<?> enu = zipFile.entries();
					while (enu.hasMoreElements()) {
						ZipEntry zipEntry = (ZipEntry) enu.nextElement();
						// String name = zipEntry.getName();
						long unzippedSize = zipEntry.getSize();
						// Zip file inputstream
						InputStream zin = zipFile.getInputStream(zipEntry);
						// Copy data from ZipEntry to file
						FileOutputStream fos = new FileOutputStream(mAmikoDatabase);
						totBytesRead = 0;
						while (!isCancelled() && (bytesRead = zin.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
							totBytesRead += bytesRead;
							// Note: 3.9 is a magic compression ratio...
							percentCompleted = (int) (totBytesRead * 100 / unzippedSize);
							if (percentCompleted > 100)
								percentCompleted = 100;
							setProgress(percentCompleted);
							mDialog.setLabel("Unzipping... " + totBytesRead/1000 + "kB out of " + (int)(unzippedSize/1000) + "kB");
						}
						fos.close();
						zin.close();					
					}
					zipFile.close();
				}
			} catch (IOException e) {
				mDialog.getLabel().setText("Error unzipping file: " + e.getMessage());
				e.printStackTrace();
				setProgress(0);
				cancel(true);
			}
			
			// Download report file
			if (!isCancelled()) {
				mDialog.setLabel("Downloading report file...");
				url = new URL(mReportURL);
				is = url.openStream();
				// Retrieve file length from http header
				sizeDB_in_bytes = Integer.parseInt(url.openConnection().getHeaderField("Content-Length"));
				downloadedFile = new File(mAmikoReport.getAbsolutePath());
				os = new FileOutputStream(downloadedFile);
				totBytesRead = 0;
				try {
					while (!isCancelled() && (bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
						totBytesRead += bytesRead;
						percentCompleted = (int) (totBytesRead * 100 / (sizeDB_in_bytes));
						if (percentCompleted > 100)
							percentCompleted = 100;
						setProgress(percentCompleted);
					}
					os.close();
				} catch (IOException e) {
					mDialog.getLabel().setText("Error downloading report file: " + e.getMessage());
					e.printStackTrace();
					setProgress(0);
					cancel(true);
				}
				is.close();
			}
			
			return null;
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			if (!isCancelled()) {
				setProgress(100);
				Toolkit.getDefaultToolkit().beep();
	        	// Close previous DB
	        	closeDB();
	        	String db_file = mAmikoDatabase.getAbsolutePath();
			    mDialog.setOKButton("OK");
			    mDialog.setOKButton("OK");
	        	if (loadDBFromPath(db_file)>0 && getNumRecords()>0) {
	        		// Setup icon
	        		ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
	    			if (m_customization.equals("desitin"))
	    				icon = new ImageIcon(Constants.DESITIN_ICON);	        		
	    	        Image img = icon.getImage();
	    		    Image scaled_img = img.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
	    		    icon = new ImageIcon(scaled_img);
	    		    // Display friendly message
	    		    if (m_app_lang.equals("de")) {
	    		    	mDialog.getLabel().setText("Neue AmiKo Datenbank mit " + getNumRecords() + " Fachinfos " +
	    		    			"erfolgreich geladen!");
	    		    } else if (m_app_lang.equals("fr")) {
	    		    	mDialog.getLabel().setText("Nouvelle base de données avec " + getNumRecords() + " notice infopro " +
	    		    			"chargée avec succès!");
	    		    }
	    		    // Notify to GUI, update database 		    
	    		    notifyObserver(db_file);
	        	} else {
	        		// Show message: db not kosher!
	        		if (m_app_lang.equals("de")) 
	        			mDialog.getLabel().setText("Fehler beim laden der Datenbank!");
	        		else if (m_app_lang.equals("fr"))
	        			mDialog.getLabel().setText("Erreurs lors du chargement de la base de données!");	        		
	        		// Load standard db
	        		loadDB("de");
	        	}							
			} else {
        		// Load standard db
        		loadDB("de");	
			}
		}
	}
	
	/**
	 * Loads chosen DB
	 * @param frame
	 * @param db_lang
	 * @return filename if success, empty string if error
	 */
	public String chooseDB(JFrame frame, String db_lang, String app_folder) {			
		JFileChooser fc = new JFileChooser();		
		
		// Add checkbox for zipped databases, this is the default!
		JCheckBox bc = new JCheckBox("Zip-Datei");
		bc.setSelected(true);
		fc.setAccessory(bc);		
		
		// Adjust fonts
        recursivelySetFonts(fc, new Font("Dialog", Font.PLAIN, 12));
        int returnVal = -1;
        
        // Language settings
        if (db_lang.equals("de"))
        	returnVal = fc.showDialog(frame, "Datenbank wählen");
        else if (db_lang.equals("fr"))
        	returnVal = fc.showDialog(frame, "Choisir banque de données");

        // Launch the file chooser
        if (returnVal==JFileChooser.APPROVE_OPTION) {
        	// Set db language
    		m_app_lang = db_lang;
        	// Get filename
        	String db_file = fc.getSelectedFile().toString();  	      	
        	// Do we have a zipped file?
        	if (bc.isSelected()) {
        		// Copy to temporary directory, unzip (copy is done in AmiKoDesk.java)
        		m_loadedDBisZipped = true;
        		String unzippedDB = app_folder + "\\amiko_db_full_idx_" + db_lang +".db";
        		final String f_db_file = db_file;
        		final String f_unzippedDB = unzippedDB;
        		new UnzipDialog(f_db_file, f_unzippedDB);
        		/*
        		SwingUtilities.invokeLater(new Runnable() {
        			@Override
        			public void run() {
        				new UnzipDialog(f_db_file, f_unzippedDB);
        			}
        		});
        		*/			  
        		db_file = unzippedDB;
        		return db_file;
        	} else {        	
	        	// Close previous DB
	        	closeDB();
	        	if (loadDBFromPath(db_file)>0 && getNumRecords()>0) {
	        		// Copy DB to application folder
	        		copyDB(new File(db_file), new File(app_folder + "\\amiko_db_full_idx_" + db_lang + ".db"));
	        		// Setup icon
	        		ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
	    			if (m_customization.equals("desitin"))
	    				icon = new ImageIcon(Constants.DESITIN_ICON);
	    	        Image img = icon.getImage();
	    		    Image scaled_img = img.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
	    		    icon = new ImageIcon(scaled_img);
	    		    // Display friendly message
	    		    if (db_lang.equals("de")) {
	    		    	JOptionPane.showMessageDialog(frame, "Neue AmiKo Datenbank mit " + getNumRecords() + " Fachinfos " +
	    		    			"erfolgreich geladen!", "Erfolg", JOptionPane.PLAIN_MESSAGE, icon);
	    		    } else if (db_lang.equals("fr")) {
	    		    	JOptionPane.showMessageDialog(frame, "Nouvelle base de données avec " + getNumRecords() + " notice infopro " +
	    		    			"chargée avec succès!", "Erfolg", JOptionPane.PLAIN_MESSAGE, icon);	
	    		    }
	        		 // Notify to GUI, update database 		    
	    		    notifyObserver(db_file);    		    
	        		return db_file;
	        	} else {
	        		// Show message: db not kosher!
	        		if (db_lang.equals("de")) {
	        			JOptionPane.showMessageDialog(frame, "Fehler beim laden der Datenbank!",
	        					"Fehler", JOptionPane.ERROR_MESSAGE);
	        		} else if (db_lang.equals("fr")) {
	        			JOptionPane.showMessageDialog(frame, "Fehler beim laden der Datenbank!",
	        					"Fehler", JOptionPane.ERROR_MESSAGE);
	        		}
	        		// Load standard db
	        		loadDB(db_lang);
	        		return "";
	        	}
        	}
        }
        return "";
	}
	
	private class UnzipDialog extends JFrame implements PropertyChangeListener {
		
		private JDialog dialog = new JDialog(this, "Updating database", true);
	    private JProgressBar progressBar = new JProgressBar(0, 100);
	    private JPanel panel = new JPanel();
	    private JLabel label = new JLabel();
	    
	    public UnzipDialog(String db_file, String unzippedDB) {
	    	progressBar.setPreferredSize(new Dimension(480, 30));
	    	progressBar.setStringPainted(true);			
			progressBar.setValue(0);	    	

			label.setText("Unzipping...");
			
			// Button pane
			JPanel buttonPane = new JPanel();
		    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		    JButton okButton = new JButton("OK");
		    getRootPane().setDefaultButton(okButton);
		    okButton.setActionCommand("OK");
		    buttonPane.add(okButton); 				
			
			panel = new JPanel(new BorderLayout(5, 5));
			panel.add(label, BorderLayout.NORTH);
			panel.add(progressBar, BorderLayout.CENTER);
			panel.add(buttonPane, BorderLayout.SOUTH);
			panel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));

			dialog.getContentPane().add(panel);			
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setModal(false);			
			ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
			if (m_customization.equals("desitin"))
				icon = new ImageIcon(Constants.DESITIN_ICON);
			dialog.setIconImage(icon.getImage());			
			dialog.setVisible(true);
			
			setLocationRelativeTo(null);    // center on screen
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
			final UnzipWorker unzipWorker = new UnzipWorker(this, new File(db_file), new File(unzippedDB)); 
			// Attach property listener to it
    		unzipWorker.addPropertyChangeListener(this);
    		// Launch SwingWorker
    		unzipWorker.execute();
    		
		    okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					System.out.println("Database update done.");
					unzipWorker.cancel(true);
					dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING)); 
				}
			});     		
	    }
	    
	    public JLabel getLabel() {
	    	return label;
	    }
	    
	    public void setLabel(String l) {
	    	label.setText(l);
	    }	    
	    
	    /**
	     * Update the progress bar's state whenever the unzip progress changes.
	     */
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
	        if ("progress" == evt.getPropertyName()) {
	            int progress = (Integer) evt.getNewValue();
	            progressBar.setValue(progress);
	        }
	    }
	}	
	
	private class UnzipWorker extends SwingWorker<Void, Integer> {
		private File mSrcFile;
		private File mDstFile;
		private UnzipDialog mDialog;

		public UnzipWorker(UnzipDialog dialog, File srcFile, File dstFile) {
			mSrcFile = srcFile;
			mDstFile = dstFile;
			mDialog = dialog;
		}
		
		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			byte buffer[] = new byte[4096];
			int bytesRead = -1;	
			long totBytesRead = 0;
			int percentCompleted = 0;

			try {
				ZipInputStream zin = new ZipInputStream(new FileInputStream(mSrcFile));
				ZipEntry entry = null;
				while ((entry = zin.getNextEntry()) != null) {
					// Copy data from ZipEntry to file					
					FileOutputStream fos = new FileOutputStream(mDstFile);
					while (!isCancelled() && (bytesRead = zin.read(buffer)) != -1) {
						fos.write(buffer, 0, bytesRead);
						totBytesRead += bytesRead;
						percentCompleted = (int)(totBytesRead*100/(entry.getSize()));
						if (percentCompleted>100)
							percentCompleted = 100;
						setProgress(percentCompleted);
						mDialog.setLabel("Unzipping... " + totBytesRead/1000 + "kB out of " + (int)(entry.getSize()/1000) + "kB");
					}
					fos.close();
				}
				zin.close();
			} catch (IOException e) {
				mDialog.setLabel("Error unzipping file: " + e.getMessage());
	            e.printStackTrace();
	            setProgress(0);
	            cancel(true);
			}
			
			return null;
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			if (!isCancelled()) {
				setProgress(100);
				Toolkit.getDefaultToolkit().beep();
	        	// Close previous DB
	        	closeDB();
	        	String db_file = mDstFile.getAbsolutePath();
	        	if (loadDBFromPath(db_file)>0 && getNumRecords()>0) {
	        		// Setup icon
	        		ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
	    			if (m_customization.equals("desitin"))
	    				icon = new ImageIcon(Constants.DESITIN_ICON);
	    	        Image img = icon.getImage();
	    		    Image scaled_img = img.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
	    		    icon = new ImageIcon(scaled_img);
	    		    // Display friendly message
	    		    if (m_app_lang.equals("de")) {
	    		    	mDialog.getLabel().setText("Neue AmiKo Datenbank mit " + getNumRecords() + " Fachinfos " +
	    		    			"erfolgreich geladen!");
	    		    } else if (m_app_lang.equals("fr")) {
	    		    	mDialog.getLabel().setText("Nouvelle base de données avec " + getNumRecords() + " notice infopro " +
	    		    			"chargée avec succès!");
	    		    }
	    		    // Notify to GUI, update database 		    
	    		    notifyObserver(db_file);
	        	} else {
	        		// Show message: db not kosher!
	        		if (m_app_lang.equals("de"))
	        			mDialog.setLabel("Fehler beim laden der Datenbank!");
	        		else if (m_app_lang.equals("fr"))
	        			mDialog.setLabel("Erreurs lors du chargement de la base de données!");	        			
	        		// Load standard db
	        		loadDB("de");
	        	}							
			} else {
        		// Load standard db
        		loadDB("de");
			}
		}
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
