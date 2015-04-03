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
import java.net.URL;
import java.util.Enumeration;
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

public class UpdateDb {

	private MainSqlDb m_sqldb;
	private Observer m_observer;
	private String m_app_lang;
	private String m_customization;
	private boolean m_loadedDBisZipped = false;
	private boolean m_full_update = false;
	
	UpdateDb(MainSqlDb sqldb) {
		m_sqldb = sqldb;
	}
	
	public void setDb(MainSqlDb sqldb) {
		m_sqldb = sqldb;
	}
	
	public boolean dbIsZipped() {
		return m_loadedDBisZipped;
	}
	
	public void addObserver(Observer observer) {
		m_observer = observer;
	}
		
	protected void notifyObserver(String msg) {
		m_observer.update(null, msg);
	}
	
	/**
	 * Downloads database from set URL, unzips it and transfers it to given folder
	 * @param frame
	 * @param db_lang
	 * @param app_folder
	 * @return
	 */
	public String doIt(JFrame frame, String db_lang, String custom, String app_folder, boolean full_update) {
		// Default is "de"
		String db_url = "http://pillbox.oddb.org/amiko_db_full_idx_de.zip";
		String amiko_report_url = "http://pillbox.oddb.org/amiko_report_de.html";
		String drug_interactions_url = "http://pillbox.oddb.org/drug_interactions_csv_de.zip";
		String shop_files_url = "http://pillbox.oddb.org/shop.zip";
		String rose_files_url = "http://pillbox.oddb.org/rose.zip";
		// These are the local filenames
		String db_unzipped = app_folder + "\\amiko_db_full_idx_de.db";
		String amiko_report_plain = app_folder + "\\amiko_report_de.html";
		String drug_interactions_unzipped = app_folder + "\\drug_interactions_csv_de.csv";
		String shop_files_zipped = app_folder + "\\shop_files.zip";
		String rose_files_zipped = app_folder + "\\rose_files.zip";		
		
		// ... works also for "fr"
		if (db_lang.equals("fr")) {
			db_url = "http://pillbox.oddb.org/amiko_db_full_idx_fr.zip";
			amiko_report_url = "http://pillbox.oddb.org/amiko_report_fr.html";
			drug_interactions_url = "http://pillbox.oddb.org/drug_interactions_csv_fr.zip";			
			// 
			db_unzipped = app_folder + "\\amiko_db_full_idx_fr.db";
			amiko_report_plain = app_folder + "\\amiko_report_fr.html";
			drug_interactions_unzipped = app_folder + "\\drug_interactions_csv_fr.csv";
		}
		
		m_app_lang = db_lang;
		m_customization = custom;
		m_full_update = full_update;
		
		if (Utilities.isInternetReachable())
			new DownloadDialog(
					db_url, amiko_report_url, 
					drug_interactions_url, 
					shop_files_url, 
					rose_files_url,
					amiko_report_plain, db_unzipped, 
					drug_interactions_unzipped, 
					shop_files_zipped, 
					rose_files_zipped);
		else {
			AmiKoDialogs cd = new AmiKoDialogs(db_lang, custom);
			cd.NoInternetDialog();
			return "";
		}	
		return db_unzipped;
	}
		
	private class DownloadDialog extends JFrame implements PropertyChangeListener {
		
		private JDialog dialog = new JDialog(this, "Updating database", true);
	    private JProgressBar progressBar = new JProgressBar(0, 100);
	    private JPanel panel = new JPanel();
	    private JLabel label = new JLabel();
	    private JButton okButton = new JButton();
	    
	    public DownloadDialog(
	    		String databaseURL, String reportURL, 
	    		String drugInteractionsURL, 
	    		String shopFilesURL, 
	    		String roseFilesURL,
	    		String amikoReport, String unzippedDB, 
	    		String drugInteractionsUnzipped, 
	    		String shopFilesZipped, 
	    		String roseFilesZipped) {
	    	progressBar.setPreferredSize(new Dimension(640, 30));
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
			dialog.setResizable(false);
			ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
			if (m_customization.equals("desitin"))
				icon = new ImageIcon(Constants.DESITIN_ICON);
			else if (m_customization.equals("meddrugs"))
				icon = new ImageIcon(Constants.MEDDRUGS_ICON);
			dialog.setIconImage(icon.getImage());			
			dialog.setVisible(true);
			
			setLocationRelativeTo(null);    // center on screen
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
	        // These files are downloaded either daily or later...
			final MainDownloadWorker mainDownloadWorker = new MainDownloadWorker(this, 
					databaseURL, reportURL, 
					drugInteractionsURL, 
					shopFilesURL, 
					roseFilesURL,
					new File(unzippedDB), new File(amikoReport), 
					new File(drugInteractionsUnzipped), 
					new File(shopFilesZipped),
					new File(roseFilesZipped)); 
			// Attach property listener to it
    		mainDownloadWorker.addPropertyChangeListener(this);
    		// Launch SwingWorker
    		mainDownloadWorker.execute();
    		
		    okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					System.out.println("Database update done.");
					mainDownloadWorker.cancel(true);
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
	
	private class MainDownloadWorker extends SwingWorker<Void, Integer> {

		private DownloadDialog mDialog;
		private String mDatabaseURL;
		private String mReportURL;
		private String mDrugInteractionsURL;
		private String mShopFilesURL;
		private String mRoseFilesURL;
		private File mAmikoDatabase;
		private File mAmikoReport;
		private File mDrugInteractions;
		private File mShopFiles;
		private File mRoseFiles;

		public MainDownloadWorker(DownloadDialog dialog, 
				String databaseURL, String reportURL, 
				String drugInteractionsURL, 
				String shopFilesURL,
				String roseFilesURL,
				File amikoDatabase, File amikoReport, 
				File drugInteractions, 
				File shopFiles,
				File roseFiles) {
			mDialog = dialog;			
			mDatabaseURL = databaseURL;
			mReportURL = reportURL;
			mDrugInteractionsURL = drugInteractionsURL;			
			mShopFilesURL = shopFilesURL;
			mRoseFilesURL = roseFilesURL;
			mAmikoDatabase = amikoDatabase;
			mAmikoReport = amikoReport;
			mDrugInteractions = drugInteractions;
			mShopFiles = shopFiles;
			mRoseFiles = roseFiles;
		}
		
		private File downloader(String what, String download_url, File file) throws Exception {
			byte buffer[] = new byte[4096];
			int bytesRead = -1;
			long totBytesRead = 0;
			int percentCompleted = 0;
			
			mDialog.setLabel("Downloading " + what + "...");
			
			URL url = new URL(download_url);
			InputStream is = url.openStream();
			// Retrieve file length from http header
			int sizeDB_in_bytes = Integer.parseInt(url.openConnection().getHeaderField("Content-Length"));
			File downloadedFile = new File(file.getAbsolutePath() + ".zip");
			OutputStream os = new FileOutputStream(downloadedFile);

			try {
				while (!isCancelled() && (bytesRead = is.read(buffer)) != -1) {
					os.write(buffer, 0, bytesRead);
					totBytesRead += bytesRead;
					percentCompleted = (int) (totBytesRead * 100 / (sizeDB_in_bytes));
					if (percentCompleted > 100)
						percentCompleted = 100;
					setProgress(percentCompleted);
					mDialog.setLabel("Downloading " + what + "... " + totBytesRead/1000 + "kB out of " + sizeDB_in_bytes/1000 + "kB");
				}
				os.close();
			} catch (IOException e) {
				mDialog.getLabel().setText("Error downloading " + what + " file: " + e.getMessage());
				e.printStackTrace();
				setProgress(0);
				cancel(true);
			}
			
			return downloadedFile;
		}
		
		private void unzipper(File downloadedFile, File unzippedFile) {
			byte buffer[] = new byte[4096];
			int bytesRead = -1;
			long totBytesRead = 0;
			int percentCompleted = 0;
			String unzippedPath = "";
			String file_being_unzipped = unzippedFile.getName();
			
			// Unzip database file
			mDialog.setLabel("Unzipping " + file_being_unzipped + "...");

			System.out.println("Unzipping "+ file_being_unzipped);
			
			String dataFolder = Utilities.appDataFolder();
			
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
						unzippedPath = dataFolder + "\\" + zipEntry.getName();		
						FileOutputStream fos = new FileOutputStream( /*unzippedFile*/ unzippedPath);
						totBytesRead = 0;
						while (!isCancelled() && (bytesRead = zin.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
							totBytesRead += bytesRead;
							percentCompleted = (int) (totBytesRead * 100.0f / unzippedSize);
							if (percentCompleted > 100)
								percentCompleted = 100;
							setProgress(percentCompleted);
							if (totBytesRead>1000)
								mDialog.setLabel("Unzipping " + file_being_unzipped + "... " + (int)(totBytesRead/1000.0f) + "kB out of " + (int)(unzippedSize/1000.0f) + "kB");
							else
								mDialog.setLabel("Unzipping " + file_being_unzipped + "... " + (int)(totBytesRead) + "bytes out of " + (int)(unzippedSize) + "bytes");
						}
						fos.close();
						zin.close();					
					}
					zipFile.close();
				}
			} catch (IOException e) {
				mDialog.getLabel().setText("Error unzipping " + unzippedPath + " file: " + e.getMessage());
				e.printStackTrace();
				setProgress(0);
				cancel(true);
			}
		}
		
		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			File downloadedFile = null;

			if (m_full_update==true) {
				downloadedFile = downloader("database", mDatabaseURL, mAmikoDatabase);			
				unzipper(downloadedFile, mAmikoDatabase);
				
				downloadedFile = downloader("report", mReportURL, mAmikoReport);
	
				downloadedFile = downloader("drug interactions", mDrugInteractionsURL, mDrugInteractions);
				unzipper(downloadedFile, mDrugInteractions);
				
				downloadedFile = downloader("shopping files", mShopFilesURL, mShopFiles);
				unzipper(downloadedFile, mShopFiles);
			}
						
			downloadedFile = downloader("rose files", mRoseFilesURL, mRoseFiles);
			unzipper(downloadedFile, mRoseFiles);
			
			return null;
		}
		
		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			if (m_full_update==true) {
				if (!isCancelled()) {
					setProgress(100);
					Toolkit.getDefaultToolkit().beep();
		        	// Close previous DB
					m_sqldb.closeDB();
		        	String db_file = mAmikoDatabase.getAbsolutePath();
				    mDialog.setOKButton("OK");
		        	if (m_sqldb.loadDBFromPath(db_file)>0 && m_sqldb.getUserVersion()>0 && m_sqldb.getNumRecords()>0) {
		        		// Setup icon
		        		ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
		    			if (m_customization.equals("desitin"))
		    				icon = new ImageIcon(Constants.DESITIN_ICON);	        		
		    	        Image img = icon.getImage();
		    		    Image scaled_img = img.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
		    		    icon = new ImageIcon(scaled_img);
		    		    // Get number of interactions in interaction database
		    		    int numInteractions = m_sqldb.getNumLinesInFile(mDrugInteractions.getAbsolutePath());
		    		    // Display friendly message
		    		    if (numInteractions>0) {
			    		    if (m_app_lang.equals("de")) {
			    		    	mDialog.getLabel().setText("<html>Neue AmiKo Datenbank mit " + m_sqldb.getNumRecords() + " Fachinfos und "
			    		    			+ numInteractions + " Interaktionen erfolgreich geladen!</html>");
			    		    } else if (m_app_lang.equals("fr")) {
			    		    	mDialog.getLabel().setText("<html>Nouvelle base de données avec " + m_sqldb.getNumRecords() + " notice infopro et "
			    		    			+ numInteractions + " interactions chargée avec succès!</html>");
			    		    }
		    		    } else {
			    		    if (m_app_lang.equals("de")) {
			    		    	mDialog.getLabel().setText("Neue AmiKo Datenbank mit " + m_sqldb.getNumRecords() + " Fachinfos "
			    		    			+ "erfolgreich geladen!");
			    		    } else if (m_app_lang.equals("fr")) {
			    		    	mDialog.getLabel().setText("Nouvelle base de données avec " + m_sqldb.getNumRecords() + " notice infopro"
			    		    			+ " chargée avec succès!");
			    		    }
		    		    }
		    		    // Notify to GUI, update database 		    
		    		    notifyObserver("Main update done!");
		        	} else {
		        		// Show message: db not kosher!
		        		if (m_app_lang.equals("de")) 
		        			mDialog.getLabel().setText("Fehler beim laden der Datenbank mit Version " + m_sqldb.getUserVersion() + "!");
		        		else if (m_app_lang.equals("fr"))
		        			mDialog.getLabel().setText("Erreurs lors du chargement de la base de données version " + m_sqldb.getUserVersion() + "!");	        		
		        		// Load standard db
		        		m_sqldb.loadDB("de");
		        	}							
				} else {
	        		// Load standard db
					m_sqldb.loadDB("de");	
				}
			} else {
				mDialog.setOKButton("OK");
                mDialog.dispose();
				/*
		        Timer timer = new Timer(2000, new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                mDialog.setVisible(false);
		                mDialog.dispose();
		            }
		        });
		        timer.setRepeats(false);
		        timer.start();
				*/
				notifyObserver("Quick update done!");
			}
		}
	}
	
	/**
	 * Loads chosen DB
	 * @param frame
	 * @param db_lang
	 * @return filename if success, empty string if error
	 */
	public String chooseFromFile(JFrame frame, String db_lang, String custom, String app_folder) {			
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
        	// Set db language and customization
    		m_app_lang = db_lang;
    		m_customization = custom;
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
        		m_sqldb.closeDB();
	        	if (m_sqldb.loadDBFromPath(db_file)>0 && m_sqldb.getUserVersion()>0 && m_sqldb.getNumRecords()>0) {
	        		// Copy DB to application folder
	        		m_sqldb.copyDB(new File(db_file), new File(app_folder + "\\amiko_db_full_idx_" + db_lang + ".db"));
	        		// Setup icon
	        		ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
	    			if (m_customization.equals("desitin"))
	    				icon = new ImageIcon(Constants.DESITIN_ICON);
	    	        Image img = icon.getImage();
	    		    Image scaled_img = img.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
	    		    icon = new ImageIcon(scaled_img);
	    		    // Display friendly message
	    		    if (db_lang.equals("de")) {
	    		    	JOptionPane.showMessageDialog(frame, "Neue AmiKo Datenbank mit " + m_sqldb.getNumRecords() + " Fachinfos " +
	    		    			"erfolgreich geladen!", "Erfolg", JOptionPane.PLAIN_MESSAGE, icon);
	    		    } else if (db_lang.equals("fr")) {
	    		    	JOptionPane.showMessageDialog(frame, "Nouvelle base de données avec " + m_sqldb.getNumRecords() + " notice infopro " +
	    		    			"chargée avec succès!", "Erfolg", JOptionPane.PLAIN_MESSAGE, icon);	
	    		    }
	        		 // Notify to GUI, update database 		    
	    		    notifyObserver(db_file);    		    
	        		return db_file;
	        	} else {
	        		// Show message: db not kosher!
	        		if (db_lang.equals("de")) {
	        			JOptionPane.showMessageDialog(frame, "Fehler beim laden der Datenbank mit Version " + m_sqldb.getUserVersion() + "!",
	        					"Fehler", JOptionPane.ERROR_MESSAGE);
	        		} else if (db_lang.equals("fr")) {
	        			JOptionPane.showMessageDialog(frame, "Erreurs lors du chargement de la base de données version " + m_sqldb.getUserVersion() + "!",
	        					"Fehler", JOptionPane.ERROR_MESSAGE);
	        		}
	        		// Load standard db
	        		m_sqldb.loadDB(db_lang);
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
				m_sqldb.closeDB();
	        	String db_file = mDstFile.getAbsolutePath();
	        	if (m_sqldb.loadDBFromPath(db_file)>0 && m_sqldb.getNumRecords()>0) {
	        		// Setup icon
	        		ImageIcon icon = new ImageIcon(Constants.AMIKO_ICON);
	    			if (m_customization.equals("desitin"))
	    				icon = new ImageIcon(Constants.DESITIN_ICON);
	    	        Image img = icon.getImage();
	    		    Image scaled_img = img.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
	    		    icon = new ImageIcon(scaled_img);
	    		    // Display friendly message
	    		    if (m_app_lang.equals("de")) {
	    		    	mDialog.getLabel().setText("Neue AmiKo Datenbank mit " + m_sqldb.getNumRecords() + " Fachinfos " +
	    		    			"erfolgreich geladen!");
	    		    } else if (m_app_lang.equals("fr")) {
	    		    	mDialog.getLabel().setText("Nouvelle base de données avec " + m_sqldb.getNumRecords() + " notice infopro " +
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
	        		m_sqldb.loadDB("de");
	        	}							
			} else {
        		// Load standard db
				m_sqldb.loadDB("de");
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
}
