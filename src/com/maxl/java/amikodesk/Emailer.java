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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Emailer {

	private String m_subject;
	private String m_from;
	private String m_recipient;
	private String m_singlecc = "";
	private String m_replyTo = "";
	private String m_body;
	private Map<String, String> m_map_of_attachments;
	private Preferences m_prefs;
	private String m_el;
	private String m_ep;
	private String m_es;
	private String m_fl;
	private String m_fp;
	private String m_fs;
	
	public Emailer() {
		m_el = "amiko@ywesee.com";
		m_fl = "IbsaAmiko";
		m_map_of_attachments = new TreeMap<String, String>();
		m_prefs = Preferences.userRoot().node(SettingsPage.class.getName());
		loadMap();
	}
	
	public Emailer(String l, String p) {
		m_el = l;
		m_ep = p;
		m_map_of_attachments = new TreeMap<String, String>();
		m_prefs = Preferences.userRoot().node(SettingsPage.class.getName());
	}
	
	public Emailer(String subject) {
		m_subject = subject;
		m_map_of_attachments = new TreeMap<String, String>();
		m_prefs = Preferences.userRoot().node(SettingsPage.class.getName());
	}

	public void loadMap() {
		byte[] encrypted_msg = FileOps.readBytesFromFile(Utilities.appDataFolder() + "\\access.ami.ser");
		if (encrypted_msg==null) {		
			encrypted_msg = FileOps.readBytesFromFile(Constants.SHOP_FOLDER + "access.ami.ser");
			System.out.println("Loading access.ami.ser from default folder...");
		}
		// Decrypt and deserialize
		if (encrypted_msg!=null) {
			Crypto crypto = new Crypto();
			byte[] serialized_bytes = crypto.decrypt(encrypted_msg);
			TreeMap<String, String> map = new TreeMap<String, String>();
			map = (TreeMap<String, String>)(FileOps.deserialize(serialized_bytes));									
			m_ep = ((String)map.get(m_el)).split(";")[0];
			m_es = ((String)map.get(m_el)).split(";")[1];
			m_fp = ((String)map.get(m_fl)).split(";")[0];
			m_fs = ((String)map.get(m_fl)).split(";")[1];
		}
	}
	
	public void setSubject(String subject) {
		m_subject = subject;
	}
	
	public void setBody(String body) {
		m_body = body;
	}

	public void setFrom(String from) {
		m_from = from;
	}
	
	public void setRecipient(String recipient) {
		m_recipient = recipient;
	}
	
	public void setSingleCC(String singlecc) {
		m_singlecc = singlecc;
	}
		
	public void setReplyTo(String replyTo) {
		m_replyTo = replyTo;
	}	
	
	public void addAttachment(String attachment_name, String attachment_path) {
		m_map_of_attachments.put(attachment_name, attachment_path);
	}
	
	private void uploadToFTPServer(String name, String path) {
		FTPClient ftp_client = new FTPClient();
	    try {
	    	ftp_client.connect(m_fs, 21);
	    	ftp_client.login(m_fl, m_fp);
	    	ftp_client.enterLocalPassiveMode(); 
	    	ftp_client.changeWorkingDirectory("orders");
	    	ftp_client.setFileType(FTP.BINARY_FILE_TYPE);
            
            int reply = ftp_client.getReplyCode();                        
            if (!FTPReply.isPositiveCompletion(reply)) {
            	ftp_client.disconnect();
                System.err.println("FTP server refused connection.");
                return;
            } 
            
            File local_file = new File(path); 
            String remote_file = name;
            InputStream is = new FileInputStream(local_file); 
            System.out.print("Uploading file " + name + " to server " + m_fs + "... ");

            boolean done = ftp_client.storeFile(remote_file, is);
            if (done)
                System.out.println("file uploaded successfully.");
            else
            	System.out.println("error.");
            is.close();            
	     } catch (IOException ex) {
	    	 System.out.println("Error: " + ex.getMessage());
	         ex.printStackTrace();
	     } finally {
	    	 try {
	    		 if (ftp_client.isConnected()) {
	    			 ftp_client.logout();
	                 ftp_client.disconnect();
	    		 }
	    	 } catch (IOException ex) {
	    		 ex.printStackTrace();
	    	 }
	     }
	}
		
	private void sendWithAttachment(Author author, String attachment_name, String attachment_path) {
		String gln_code = m_prefs.get("glncode", "7610000000000");
		String address = m_prefs.get("bestelladresse", "Keine Bestelladresse");
		String email_address = m_prefs.get("emailadresse", m_el);
			
		setSubject("AmiKo Bestellung " + attachment_name);
		
		setFrom(m_el);
		setRecipient(author.getEmail());
		setSingleCC(author.getEmailCC());
		setReplyTo(email_address);			
		setBody(author.getSalutation() + "\n\nSie haben eine neue Bestellung erhalten.\n\n"
				+ "GLN code: " + gln_code + "\n\n" 
				+ "Bestelladresse:\n" + address + "\n\n"
				+ "Siehe PDF Attachment.\n\nMit freundlichen Grüssen\nZeno Davatz");
		addAttachment(attachment_name + ".pdf", attachment_path + ".pdf");
		addAttachment(attachment_name + ".csv", attachment_path + ".csv");

		send();
	}
	
	public void send() {	
		try {
			MultiPartEmail email = new MultiPartEmail();
			email.setHostName(m_es);
			email.setSmtpPort(465);
			email.setAuthenticator(new DefaultAuthenticator(m_el, m_ep));
			email.setSSLOnConnect(true);

			// Create email message
			email.setSubject(m_subject);		// Subject			
			email.setMsg(m_body);				// Body
			email.setFrom(m_from);				// From field		
			email.addTo(m_recipient);			// Recipient	
			if (m_singlecc!=null && !m_singlecc.isEmpty())
				email.addCc(m_singlecc);		// CC
			if (m_replyTo!=null && !m_replyTo.isEmpty())
				email.addReplyTo(m_replyTo);	// Reply-To
			// Add attachments
			for (Map.Entry<String, String> entry : m_map_of_attachments.entrySet()) {
				EmailAttachment attachment = new EmailAttachment();					
				attachment.setName(entry.getKey());			
				attachment.setPath(entry.getValue());
				attachment.setDisposition(EmailAttachment.ATTACHMENT);
				email.attach(attachment);				
			}	
			
			// Send email
			email.send();
			
			// Clear map
			m_map_of_attachments.clear();
		} catch(EmailException e) {
			e.printStackTrace();
		}	
	}
	
	public void sendAllOrders(List<Author> list_of_authors, SaveBasket save_basket) {
		// Proceed and send order
		new SendOrderDialog(list_of_authors, save_basket);		
	}
	
	public String orderFileName() {
		String gln_code = m_prefs.get("glncode", "7610000000000");
		DateTime dT = new DateTime();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
		return (gln_code + "_" + fmt.print(dT));			
	}
	
	private class SendOrderDialog extends JFrame implements PropertyChangeListener {
		
		private JDialog dialog = new JDialog(this, "Sending orders", true);
	    private JProgressBar progressBar = new JProgressBar(0, 100);		
		private JPanel panel = new JPanel();
		private JLabel label = new JLabel();
		private JButton okButton = new JButton();
		
		public SendOrderDialog(List<Author> list_of_authors, SaveBasket sbasket) {			
	    	progressBar.setPreferredSize(new Dimension(640, 30));
	    	progressBar.setStringPainted(true);			
			progressBar.setValue(0);
			
			label.setText("Bestellungen Versand");
			
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
			if (Utilities.appCustomization().equals("desitin"))
				icon = new ImageIcon(Constants.DESITIN_ICON);
			else if (Utilities.appCustomization().equals("meddrugs"))
				icon = new ImageIcon(Constants.MEDDRUGS_ICON);
			dialog.setIconImage(icon.getImage());			
			dialog.setVisible(true);
			
			setLocationRelativeTo(null);    // center on screen
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	        final SendOrderWorker emailWorker = new SendOrderWorker(this, list_of_authors, sbasket);
			// Attach property listener to it
	        emailWorker.addPropertyChangeListener(this);
    		// Launch SwingWorker
	        emailWorker.execute();
	        
		    okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					System.out.println("Order(s) sent.");
					emailWorker.cancel(true);
					dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING)); 
				}
			});  
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
	
	private class SendOrderWorker extends SwingWorker<Void, Integer> {
		
		private SendOrderDialog mDialog;
		private SaveBasket mSbasket;
		private List<Author> m_list_of_authors;
		
		public SendOrderWorker(SendOrderDialog dialog, List<Author> list_of_authors, SaveBasket sbasket) {
			mDialog = dialog;
			m_list_of_authors = list_of_authors;
			mSbasket = sbasket;
		}
		
		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {		
			String path = Utilities.appDataFolder() + "\\shop";
			String name = orderFileName();		
			int num_authors = 0;
			for (Author author : m_list_of_authors) {
				if (mSbasket.getMedsForAuthor(author.getName())>0) {
					num_authors++;
				}
			}
			if (num_authors>0) {
				if (Utilities.isInternetReachable()) {
					// Send emails and upload files
					int index = 0;
					for (Author author : m_list_of_authors) {
						if (mSbasket.getMedsForAuthor(author.getName())>0 && !isCancelled()) {
							String auth = author.getShortName();
							String p = path + "\\" + auth + "_" + name;
							mSbasket.generatePdf(auth, p + ".pdf");	
							mSbasket.generateCsv(auth, p + ".csv");
							index++;
							mDialog.setLabel("Sending " + author.getCompany() + " order...");
							// Send email							
							sendWithAttachment(author, name, p);
							// If ibsa send FTP					
							if (author.getName().equals("ibsa"))
								uploadToFTPServer(name, p + ".csv");
							setProgress((int)(100.0f*index/(float)num_authors));
						}
					}
					mDialog.setLabel("Successfully sent " + index + " orders.");
				} else
					mDialog.setLabel("Sorry. No Internet connection.");
			}
			// Save the rest on the desktop
			File desktop = new File(System.getProperty("user.home"), "Desktop");
			path = desktop.getAbsolutePath();
			if (mSbasket.getMedsWithNoAuthor()>0) {
				mSbasket.generatePdf("rest", path + "\\" + "amiko_" + name + ".pdf");	
				mSbasket.generateCsv("rest", path + "\\" + "amiko_" + name + ".csv");
			}			
			if (num_authors==0)
				mDialog.setLabel("Order pdfs saved to desktop.");
			
			return null;
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			if (!isCancelled())
				setProgress(100);
			mDialog.setOKButton("OK");	
		}		
	}	
}