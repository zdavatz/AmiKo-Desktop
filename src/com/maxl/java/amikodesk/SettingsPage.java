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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import com.maxl.java.shared.User;

public class SettingsPage implements java.io.Serializable {

	private static String UpdateID = "update";
	private static String ComparisonID = "update-comp";
	private static String LogoImageID = "logo";
	private static String GLNCodeID = "glncode";
	private static String HumanID = "ishuman";
	private static String UserID = "user";	// Default: 17, Ibsa-Innendienst: 18
	private static String NameID = "name";
	private static String TypeID = "type"; 
	private static String BestellAdresseID = "bestelladresse";
	private static String LieferAdresseID = "lieferadresse";
	private static String RechnungsAdresseID = "rechnungsadresse";
	private static String EmailAdresseID = "emailadresse";
	private static String PhoneNumberID = "phonenumber";
	
	private Map<String, User> m_user_map = null;;
	private User m_user = null;
	
	private JFrame mFrame = null;
	private JFileChooser mFc = null;
	
	private JDialog mUserDialog = null;
	private JButton mButtonLogo = null;
	private Preferences mPrefs = null;
	private JTextField mTextFieldGLN = null;	
	private JTextArea mTextAreaBestell = null;
	private JTextArea mTextAreaLiefer = null;
	private JTextArea mTextAreaRechnung = null;
	private JTextField mTextFieldEmail = null;
	private JTextField mTextFieldPhone = null;	
	private AddressPanel mShippingAddress = null;	// Lieferadresse
	private AddressPanel mBillingAddress = null;	// Rechnungsadresse
	private AddressPanel mOfficeAddress = null;		// Bestelladresse

	private JDialog mCustomerDialog = null;
	private JLabel mCustomerGlnCode = null;
	private AddressPanel mCustomerAddress = null;
	private String mAddrType = "";
	
	// Colors
	private static Color color_white = new Color(255,255,255);
	private static Color color_ok = new Color(220,255,220);
	// private static Color color_green = new Color(220,255,220);
	private static Color color_red = new Color(255,220,220);
	private static Color color_yellow = new Color(255,255,220);
	
	private static ResourceBundle m_rb;
	
	private Map<String, String> m_work_id;
	
	private String m_glncode = "";
	private String m_email = "";
	
	private Observer m_observer;

	static private int border = 1;

	boolean validateCode() {
		if (m_glncode.matches("[\\d]{7}")) {
			if (m_work_id.containsKey(m_glncode)) {
				String[] value = m_work_id.get(m_glncode).split(";",-1);
				String email = value[0].toLowerCase();
				// System.out.println(email + " -> " + m_email + " -> " + m_work_id.get(m_glncode));
				if (mShippingAddress!=null)
					m_email = mShippingAddress.aTextFieldEmail.getText();
				if (email.equals(m_email.toLowerCase())) {
					mTextFieldGLN.setBorder(new LineBorder(color_ok, 1, false));
					mTextFieldGLN.setBackground(color_ok);
					if (mShippingAddress!=null && mShippingAddress.validateEmail(m_email)) {					
						mShippingAddress.aTextFieldEmail.setBorder(new LineBorder(color_ok, 1, false));
						mShippingAddress.aTextFieldEmail.setBackground(color_ok);	
						String name = value[1];
						if (name.contains(".")) {
							// '.' means any character in regex
							String[] n = name.split("\\.",-1);
							mPrefs.put(NameID, n[0] + " " + n[1]);
							mShippingAddress.aTextFieldFName.setText(n[0]);
							mShippingAddress.aTextFieldLName.setText(n[1]);						
						}
						if (value.length>2) {
							int id = Integer.valueOf(value[2]);
							mPrefs.putInt(UserID, id);	// ibsa = 18, zurrose = 19
						} else
							mPrefs.putInt(UserID, 18);
					}
					mPrefs.put(GLNCodeID, m_glncode);
					mPrefs.put(EmailAdresseID, m_email);
					if (Utilities.appCustomization().equals("ibsa"))
						mPrefs.put(TypeID, "ibsa-innendienst");
					else if (Utilities.appCustomization().equals("zurrose"))
						mPrefs.put(TypeID, "zurrose-innendienst");						
					mPrefs.put(HumanID, "yes");
					if (m_user_map!=null)
						m_user =  m_user_map.get("0S");	// is not a standard user...
					return true;
				}
			}
		} else {
			m_email = "";
			mPrefs.put(EmailAdresseID, m_email);
			mPrefs.putInt(UserID, 17);
		}
		return false;
	}
	
	class AddressPanel extends JPanel {
		
		JTextField aTextFieldTitle = null;
		JTextField aTextFieldFName = null;
		JTextField aTextFieldLName = null;
		JTextField aTextFieldName1 = null;
		JTextField aTextFieldName2 = null;
		JTextField aTextFieldName3 = null;		
		JTextField aTextFieldAddress = null;
		JTextField aTextFieldZip = null;
		JTextField aTextFieldCity = null;
		JTextField aTextFieldEmail = null;
		JTextField aTextFieldPhone = null;
				
		JLabel aLabelName1 = null;
		JLabel aLabelName2 = null;
		JLabel aLabelName3 = null;
		
		private int pad_left = 8;
		
		private String m_address_type = "";
		
		public AddressPanel(final String address_type, String border_title) {

			m_address_type = address_type;

			setLayout(new GridBagLayout());
			setOpaque(false);
			if (!border_title.isEmpty()) {
				setBorder(new CompoundBorder(
						new TitledBorder(border_title),
						new EmptyBorder(5, 5, 5, 5)));		
			}
			GridBagConstraints gbc = new GridBagConstraints();		
			gbc.insets = new Insets(5, 5, 5, 5);
			
			/*  NOTE
				weightx is 1.0 for fields, 0.0 for labels
	        	gridwidth is REMAINDER for fields, 1 for labels
			*/
			
			// -----------------------------------------------------------
			JLabel aLabelTitle = new JLabel("Titel");
			aLabelTitle.setHorizontalAlignment(JLabel.LEFT);
			gbc = getGbc(0,0, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;
			this.add(aLabelTitle, gbc);
			
			aTextFieldTitle = new JTextField("");
			aTextFieldTitle.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));
			gbc = getGbc(1,0 ,1.0,1.0, GridBagConstraints.HORIZONTAL);	
			gbc.gridwidth = 1;
			this.add(aTextFieldTitle, gbc);
			
			aTextFieldTitle.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldTitle);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			aLabelName1 = new JLabel("Firma");
			aLabelName1.setHorizontalAlignment(JLabel.LEFT);
			aLabelName1.setBorder(new EmptyBorder(0,pad_left,0,0));	
			gbc = getGbc(2,0, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;
			this.add(aLabelName1, gbc);
			
			aTextFieldName1 = new JTextField("");
			aTextFieldName1.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(3,0 ,1.0,1.0, GridBagConstraints.HORIZONTAL);		
			gbc.gridwidth = 3;
			this.add(aTextFieldName1, gbc);

			aTextFieldName1.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldName1);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			// -----------------------------------------------------------
			JLabel aLabelFName = new JLabel("Vorname");
			aLabelFName.setHorizontalAlignment(JLabel.LEFT);
			gbc = getGbc(0,1, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;
			this.add(aLabelFName, gbc);
			
			aTextFieldFName = new JTextField("");
			aTextFieldFName.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(1,1 ,1.0,1.0, GridBagConstraints.HORIZONTAL);		
			gbc.gridwidth = 1;
			this.add(aTextFieldFName, gbc);

			aTextFieldFName.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldFName);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			JLabel aLabelLName = new JLabel("Name");
			aLabelLName.setHorizontalAlignment(JLabel.LEFT);	
			aLabelLName.setBorder(new EmptyBorder(0,pad_left,0,0));		
			gbc = getGbc(2,1, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;
			this.add(aLabelLName, gbc);
			
			aTextFieldLName = new JTextField("");
			aTextFieldLName.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(3,1 ,1.0,1.0, GridBagConstraints.HORIZONTAL);	
			gbc.gridwidth = 3;
			this.add(aTextFieldLName, gbc);
			
			aTextFieldLName.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldLName);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			// -----------------------------------------------------------		
			aLabelName2 = new JLabel("Name 2");
			aLabelName2.setHorizontalAlignment(JLabel.LEFT);
			gbc = getGbc(0,2, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;
			this.add(aLabelName2, gbc);
			
			aTextFieldName2 = new JTextField("");
			aTextFieldName2.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(1,2 ,1.0,1.0, GridBagConstraints.HORIZONTAL);		
			gbc.gridwidth = 1;
			this.add(aTextFieldName2, gbc);

			aTextFieldName2.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldName2);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			aLabelName3 = new JLabel("Name 3");
			aLabelName3.setHorizontalAlignment(JLabel.LEFT);
			aLabelName3.setBorder(new EmptyBorder(0,pad_left,0,0));	
			gbc = getGbc(2,2, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;
			this.add(aLabelName3, gbc);
			
			aTextFieldName3 = new JTextField("");
			aTextFieldName3.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(3,2 ,1.0,1.0, GridBagConstraints.HORIZONTAL);		
			gbc.gridwidth = 3;
			this.add(aTextFieldName3, gbc);

			aTextFieldName3.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldName3);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			// -----------------------------------------------------------
			JLabel jlabelAddress = new JLabel("Strasse");
			jlabelAddress.setHorizontalAlignment(JLabel.LEFT);
			gbc = getGbc(0,3, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;			
			this.add(jlabelAddress, gbc);
			
			aTextFieldAddress = new JTextField("");
			aTextFieldAddress.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(1,3 ,1.0,1.0, GridBagConstraints.HORIZONTAL);		
			gbc.gridwidth = 1;
			this.add(aTextFieldAddress, gbc);
			
			aTextFieldAddress.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldAddress);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});

			JLabel jlabelZip = new JLabel("PLZ");
			jlabelZip.setHorizontalAlignment(JLabel.LEFT);
			jlabelZip.setBorder(new EmptyBorder(0,pad_left,0,0));	
			gbc = getGbc(2,3, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;			
			this.add(jlabelZip, gbc);
			
			aTextFieldZip = new JTextField("");
			aTextFieldZip.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(3,3 ,1.0,1.0, GridBagConstraints.HORIZONTAL);	
			gbc.gridwidth = 1;
			this.add(aTextFieldZip, gbc);

			aTextFieldZip.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateZip(aTextFieldZip.getText());
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			JLabel jlabelCity = new JLabel("Ort");
			jlabelCity.setBorder(new EmptyBorder(0,pad_left,0,0));		
			jlabelCity.setHorizontalAlignment(JLabel.LEFT);			
			gbc = getGbc(4,3, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;			
			this.add(jlabelCity, gbc);
			
			aTextFieldCity = new JTextField("");
			aTextFieldCity.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));				
			gbc = getGbc(5,3, 1.0,1.0, GridBagConstraints.HORIZONTAL);	
			gbc.gridwidth = 1;
			this.add(aTextFieldCity, gbc);
			
			aTextFieldCity.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validateText(aTextFieldCity);
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			// -----------------------------------------------------------
			JLabel jlabelPhone = new JLabel("Telefon");
			jlabelPhone.setHorizontalAlignment(JLabel.LEFT);
			gbc = getGbc(0,4, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;			
			this.add(jlabelPhone, gbc);
			
			aTextFieldPhone = new JTextField("");
			aTextFieldPhone.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(1,4 ,1.0,1.0, GridBagConstraints.HORIZONTAL);	
			gbc.gridwidth = 1;
			this.add(aTextFieldPhone, gbc);

			aTextFieldPhone.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					validatePhone(aTextFieldPhone.getText());
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});
			
			JLabel jlabelEmail = new JLabel("Email");
			jlabelEmail.setHorizontalAlignment(JLabel.LEFT);
			jlabelEmail.setBorder(new EmptyBorder(0,pad_left,0,0));		
			gbc = getGbc(2,4, 0.1,1.0, GridBagConstraints.HORIZONTAL);
			gbc.gridwidth = 1;			
			this.add(jlabelEmail, gbc);
			
			aTextFieldEmail = new JTextField("");
			aTextFieldEmail.setBorder(new CompoundBorder(new LineBorder(color_white), new EmptyBorder(0,0,0,0)));			
			gbc = getGbc(3,4 ,1.0,1.0, GridBagConstraints.HORIZONTAL);	
			gbc.gridwidth = 3;
			this.add(aTextFieldEmail, gbc);
			
			aTextFieldEmail.addKeyListener(new KeyListener() { 
				@Override 
				public void keyPressed(KeyEvent keyEvent) { }
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					String emailStr = aTextFieldEmail.getText();
					m_email = emailStr;
					if (validateCode())
						return;
					if (validateEmail(emailStr)) {
						if (address_type.equals("S")) {
							mPrefs.put(EmailAdresseID, emailStr);	
						}
					}
				}
				@Override 
				public void keyTyped(KeyEvent keyEvent) { }
			});

			/*
			// -----------------------------------------------------------
			if (!address_type.equals("S")) {
				final JCheckBox jcheckAddress = new JCheckBox("Lieferadresse übernehmen");
				jcheckAddress.setBorder(new EmptyBorder(new Insets(1,1,1,1)));
				jcheckAddress.setHorizontalAlignment(JLabel.LEFT);
				gbc = getGbc(5,4, 0.1,1.0, GridBagConstraints.HORIZONTAL);
				gbc.gridwidth = 1;
				this.add(jcheckAddress, gbc);
				
				jcheckAddress.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (jcheckAddress.isSelected()) {
							copyDataFromShippingPanel();
							revalidate();
							repaint();
						} else
							clearData();
					}
				});
			}
			*/
		}
		
		boolean validateText(JTextField textField) {
			if (textField.getText().matches("^[âôéèàöÖäÄüÜß'.a-zA-Z0-9 \\-]{1,}$")) {
				textField.setBorder(new LineBorder(color_white, border, false));
				textField.setBackground(color_white);				
				return true;
			} else {
				textField.setBorder(new LineBorder(color_yellow, border, false));
				textField.setBackground(color_yellow);				
				return false;
			}
		}
		
		boolean validateZip(String zipStr) {
			if (zipStr.matches("[\\d]{2,6}")) {
				aTextFieldZip.setBorder(new LineBorder(color_white, border, false));
				aTextFieldZip.setBackground(color_white);
				return true;
			} else {
				aTextFieldZip.setBorder(new LineBorder(color_yellow, border, false));
				aTextFieldZip.setBackground(color_yellow);
				return false;
			}
		}
		
		boolean validatePhone(String phoneStr) {
			if (phoneStr.matches("(?:[0-9] ?){6,14}[0-9]")) {
				aTextFieldPhone.setBorder(new LineBorder(color_white, border, false));
				aTextFieldPhone.setBackground(color_white);
				return true;
			} else {
				aTextFieldPhone.setBorder(new LineBorder(color_yellow, border, false));
				aTextFieldPhone.setBackground(color_yellow);
				return false;
			}
		}

		boolean validateEmail(String emailStr) {
			if (emailStr.matches("^[_\\w-\\+]+(\\.[_\\w-]+)*@[\\w-]+(\\.[\\w]+)*(\\.[A-Za-z]{2,})$")) {
				aTextFieldEmail.setBorder(new LineBorder(color_white, border, false));
				aTextFieldEmail.setBackground(color_white);  
				return true;
			} else {
				if (m_address_type.equals("S")) {
					aTextFieldEmail.setBorder(new LineBorder(color_red, border, false));
					aTextFieldEmail.setBackground(color_red); 
				} else {
					aTextFieldEmail.setBorder(new LineBorder(color_yellow, border, false));
					aTextFieldEmail.setBackground(color_yellow); 
				}
				return false;
			}
		}
		
		void validateFields() {
			validateText(aTextFieldTitle);
			validateText(aTextFieldFName);
			validateText(aTextFieldLName);
			validateText(aTextFieldName1);
			validateText(aTextFieldName2);
			validateText(aTextFieldName3);			
			validateText(aTextFieldAddress);
			validateText(aTextFieldCity);
			validateZip(aTextFieldZip.getText());
			validatePhone(aTextFieldPhone.getText());
			validateEmail(aTextFieldEmail.getText());
		}
		
        void setNameLabels(String name1, String name2, String name3) {
            aLabelName1.setText(name1);
            aLabelName2.setText(name2);
            aLabelName3.setText(name3);
        }
		
		void setDataWithUserInfo(User u) {
			aTextFieldTitle.setText(u.title);
			aTextFieldFName.setText(u.first_name);
			aTextFieldLName.setText(u.last_name);
			aTextFieldName1.setText(u.name1);
			aTextFieldName2.setText(u.name2);
			aTextFieldName3.setText(u.name3);
			aTextFieldAddress.setText(u.street);
			aTextFieldZip.setText(u.zip);
			aTextFieldCity.setText(u.city);
			aTextFieldPhone.setText(u.phone);
			// Validate
			validateFields();
		}
		
		void clearData() {
			aTextFieldTitle.setText("");
			aTextFieldFName.setText("");
			aTextFieldLName.setText("");
			aTextFieldName1.setText("");
			aTextFieldName2.setText("");
			aTextFieldName3.setText("");
			aTextFieldAddress.setText("");
			aTextFieldZip.setText("");
			aTextFieldCity.setText("");
			aTextFieldPhone.setText("");
			aTextFieldEmail.setText("");
			// Validate
			validateFields();
		}
		
		void copyDataFromShippingPanel() {
			aTextFieldTitle.setText(mShippingAddress.aTextFieldTitle.getText());
			aTextFieldFName.setText(mShippingAddress.aTextFieldFName.getText());
			aTextFieldLName.setText(mShippingAddress.aTextFieldLName.getText());
			aTextFieldName1.setText(mShippingAddress.aTextFieldName1.getText());
			aTextFieldName2.setText(mShippingAddress.aTextFieldName2.getText());
			aTextFieldName3.setText(mShippingAddress.aTextFieldName3.getText());			
			aTextFieldAddress.setText(mShippingAddress.aTextFieldAddress.getText());
			aTextFieldZip.setText(mShippingAddress.aTextFieldZip.getText());
			aTextFieldCity.setText(mShippingAddress.aTextFieldCity.getText());
			aTextFieldPhone.setText(mShippingAddress.aTextFieldPhone.getText());
			aTextFieldEmail.setText(mShippingAddress.aTextFieldEmail.getText());
			// Validate
			validateFields();
		}
		
		void storeDataToPreferences(boolean is_human) {
			Address addr = new Address();
			addr.idealeId = "";
			addr.xprisId = "";
			addr.title = aTextFieldTitle.getText();
			addr.fname = aTextFieldFName.getText();
			addr.lname = aTextFieldLName.getText();
			addr.name1 = aTextFieldName1.getText();
			addr.name2 = aTextFieldName2.getText();
			addr.name3 = aTextFieldName3.getText();			
			addr.street = aTextFieldAddress.getText();
			addr.zip = aTextFieldZip.getText();
			addr.city = aTextFieldCity.getText();
			addr.phone = aTextFieldPhone.getText();
			addr.email = aTextFieldEmail.getText();
			addr.isHuman = is_human;
			// Store addr to preferences
			byte[] arr = FileOps.serialize(addr);			
			if (m_address_type.equals("S")) {
				mPrefs.putByteArray(LieferAdresseID, arr);
				mPrefs.put(EmailAdresseID, addr.email);							
				if (is_human)
					mPrefs.put(NameID, addr.title + " " + addr.fname + " " + addr.lname);
				else
					mPrefs.put(NameID, addr.name1 + " " + addr.name2);
			} else if (m_address_type.equals("B")) {
				mPrefs.putByteArray(RechnungsAdresseID, arr);
			} else if (m_address_type.equals("O")) {
				mPrefs.putByteArray(BestellAdresseID, arr);
			}
		}
		
		void retrieveDataFromPreferences() {
			Address addr = new Address();
			// Default entries... empty
			byte[] def = FileOps.serialize(addr);	
			if (m_address_type.equals("S")) {
				byte[] arr = mPrefs.getByteArray(LieferAdresseID, def);
				if (arr!=null)
					addr = (Address)FileOps.deserialize(arr);
			} else if (m_address_type.equals("B")) {
				byte[] arr = mPrefs.getByteArray(RechnungsAdresseID, def);
				if (arr!=null)
					addr = (Address)FileOps.deserialize(arr);			
			} else if (m_address_type.equals("O")) {
				byte[] arr = mPrefs.getByteArray(BestellAdresseID, def);
				if (arr!=null)
					addr = (Address)FileOps.deserialize(arr);		
			}
			// Fill all fields
			if (addr!=null) {
				if (addr.title!=null)
					aTextFieldTitle.setText(addr.title);
				if (addr.fname!=null)
					aTextFieldFName.setText(addr.fname);
				if (addr.lname!=null)
					aTextFieldLName.setText(addr.lname);
				if (addr.name1!=null)
					aTextFieldName1.setText(addr.name1);
				if (addr.name2!=null)			
					aTextFieldName2.setText(addr.name2);
				if (addr.name3!=null)
					aTextFieldName3.setText(addr.name3);			
				if (addr.street!=null)
					aTextFieldAddress.setText(addr.street);
				if (addr.zip!=null)
					aTextFieldZip.setText(addr.zip);
				if (addr.city!=null)
					aTextFieldCity.setText(addr.city);
				if (addr.phone!=null)
					aTextFieldPhone.setText(addr.phone);
				if (addr.email!=null)
					aTextFieldEmail.setText(addr.email);
				// Validate
				validateFields();
			}
		}
	}
	
	public FastAccessData getUserDb() {
		ArrayList<User> list_of_users = new ArrayList<User>();
		for (Map.Entry<String, User> entry : m_user_map.entrySet()) {
			User u = entry.getValue();
			list_of_users.add(u);
		}
		FastAccessData fad = new FastAccessData();
		fad.addUsers(list_of_users);
		
		return fad;
	}
	
	public FastAccessData getGlnCodesCsv() {
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		ArrayList<String> list3 = new ArrayList<String>();
		ArrayList<String> list4 = new ArrayList<String>();
		for (Map.Entry<String, User> entry : m_user_map.entrySet()) {
			User u = entry.getValue();
			list1.add(u.first_name);
			list2.add(u.last_name);
			list3.add(u.zip);
			list4.add(u.city);
		}
		FastAccessData fad = new FastAccessData();
		fad.addList(list1);
		fad.addList(list2);
		fad.addList(list3);
		fad.addList(list4);	
		
		return fad;
	}
	
	public void loadAccess() {
		m_work_id = (new Crypto()).loadMap();			
	}		
	
	public void addObserver(Observer observer) {
		m_observer = observer;
	}
	
	protected void notify(String str) {
		m_observer.update(null, str);
	}	
	
	protected void notify(Address addr) {
		m_observer.update(null, addr);
	}
	
	protected void notifyMe(String str) {
		notify(str);
	}
	
	protected void notifyMeToo(Address addr) {
		notify(addr);
	}
	
	public SettingsPage(JFrame frame, ResourceBundle rb, HashMap<String, User> user_map) {
		mFrame = frame;
		m_rb = rb;
		m_user_map = user_map;
	}
	
	public void initUserSettings() {
		mFc = new JFileChooser();
		// Defines a node in which the preferences can be stored
		mPrefs = Preferences.userRoot().node(this.getClass().getName());		
		
		// Load access data
		if (Utilities.showFullSettings())
			loadAccess();
		// Load user map
		String gln_code_str = mPrefs.get(GLNCodeID, "7610000000000");		
		if (m_user_map!=null && m_user_map.containsKey(gln_code_str+"S")) {
			m_user = m_user_map.get(gln_code_str+"S");
		}
		
		// Layout stuff
		mUserDialog = new JDialog();
		mUserDialog.setLayout(new BoxLayout(mUserDialog.getContentPane(), BoxLayout.Y_AXIS));		
		mUserDialog.add(Box.createRigidArea(new Dimension(0, 10)));		
		JPanel jplInnerPanel1 = globalAmiKoSettings();
		mUserDialog.add(jplInnerPanel1);

		// Custom
		if (Utilities.appCustomization().equals("zurrose")) {
			mUserDialog.add(Box.createRigidArea(new Dimension(0, 10)));		
			JPanel jplInnerPanel2 = comparisonSettings();
			mUserDialog.add(jplInnerPanel2);
		}
		// Custom
		mUserDialog.add(Box.createRigidArea(new Dimension(0, 10)));		
		if (Utilities.showFullSettings()) {
			JPanel jplInnerPanel3 = shoppingBasketSettings2();
			mUserDialog.add(jplInnerPanel3);
		} else {
			JPanel jplInnerPanel3 = shoppingBasketSettings();
			mUserDialog.add(jplInnerPanel3);
		}		
		mUserDialog.add(Box.createRigidArea(new Dimension(0, 10)));
		
		mUserDialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		mUserDialog.setTitle(m_rb.getString("settings"));		
		mUserDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// Centers the dialog
		mUserDialog.setLocationRelativeTo(null);
		// Set size
		if (Utilities.showFullSettings())
			mUserDialog.setSize(640, 800);		
		else
			mUserDialog.setSize(512, 680);
		mUserDialog.setResizable(false);
		
		mUserDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (Utilities.showFullSettings()) {	
					if (m_user!=null) {
						// Store user info
						mShippingAddress.storeDataToPreferences(m_user.is_human);
						mBillingAddress.storeDataToPreferences(m_user.is_human);
						mOfficeAddress.storeDataToPreferences(m_user.is_human);
						// Update frame name
						notifyMe("user updated...");
					} else if ((mPrefs.getInt(UserID, 0)==18 || mPrefs.getInt(UserID, 0)==19)) {
						// Innendienst Mitarbeiter
						mShippingAddress.storeDataToPreferences(true);
						notifyMe("user updated...");
					} else {
						// All other cases
						notifyMe("user updated...");						
					}
				} else {
					String address = mTextAreaBestell.getText();
					if (address!=null)
						mPrefs.put(BestellAdresseID, address);
					address = mTextAreaLiefer.getText();
					if (address!=null)
						mPrefs.put(LieferAdresseID, address);
					address = mTextAreaRechnung.getText();
					if (address!=null)
						mPrefs.put(RechnungsAdresseID, address);
				}
			}
		});
	}
	
	public void displayUserSettings() {
		// Visualize
		mUserDialog.setVisible(true);
	}

	public void initCustomerSettings() {
		mCustomerDialog = new JDialog();		
		// Layout stuff
		mCustomerDialog.setLayout(new BoxLayout(mCustomerDialog.getContentPane(), BoxLayout.Y_AXIS));		
		mCustomerDialog.add(Box.createRigidArea(new Dimension(0, 10)));				
		JPanel jplInnerPanel = customerSettings();
		mCustomerDialog.add(jplInnerPanel);
		// Other settings
		mCustomerDialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		mCustomerDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// Centers dialog
		mCustomerDialog.setLocationRelativeTo(null);
		// Set size
		mCustomerDialog.setSize(640, 240);		
		mCustomerDialog.setResizable(false);
		// Add window listener
		mCustomerDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Address customerAddress = new Address();
				customerAddress.type = mAddrType;
				customerAddress.title = mCustomerAddress.aTextFieldTitle.getText();
				customerAddress.fname = mCustomerAddress.aTextFieldFName.getText();
				customerAddress.lname = mCustomerAddress.aTextFieldLName.getText();
				customerAddress.name1 = mCustomerAddress.aTextFieldName1.getText();
				customerAddress.name2 = mCustomerAddress.aTextFieldName2.getText();
				customerAddress.name3 = mCustomerAddress.aTextFieldName3.getText();
				customerAddress.street = mCustomerAddress.aTextFieldAddress.getText();
				customerAddress.zip = mCustomerAddress.aTextFieldZip.getText();
				customerAddress.city = mCustomerAddress.aTextFieldCity.getText();
				customerAddress.email = mCustomerAddress.aTextFieldEmail.getText();
				customerAddress.phone = mCustomerAddress.aTextFieldPhone.getText();
				notifyMeToo(customerAddress);
			}
		});
	}
	
	public void displayCustomerSettings(String addr_type, String gln_code, HashMap<String, Address> address_map) {
		if (address_map!=null) {
			Address addr = null;
			mAddrType = addr_type;		
			if (addr_type.equals("S")) {
				mCustomerDialog.setTitle(m_rb.getString("shipaddress"));		
				addr = address_map.get("S");
			} else if (addr_type.equals("B")) {
				mCustomerDialog.setTitle(m_rb.getString("billaddress"));		
				addr = address_map.get("B");				
			} else if (addr_type.equals("O")) {
				mCustomerDialog.setTitle(m_rb.getString("ordaddress"));
				addr = address_map.get("O");				
			}
			
			if (addr==null)
				addr = new Address();
			
			mCustomerGlnCode.setText(gln_code);
			mCustomerAddress.aTextFieldTitle.setText(addr.title);
			mCustomerAddress.aTextFieldFName.setText(addr.fname);
			mCustomerAddress.aTextFieldLName.setText(addr.lname);
			mCustomerAddress.aTextFieldName1.setText(addr.name1);
			mCustomerAddress.aTextFieldName2.setText(addr.name2);
			mCustomerAddress.aTextFieldName3.setText(addr.name3);
			mCustomerAddress.aTextFieldAddress.setText(addr.street);
			mCustomerAddress.aTextFieldZip.setText(addr.zip);
			mCustomerAddress.aTextFieldCity.setText(addr.city);
			mCustomerAddress.aTextFieldEmail.setText(addr.email);
			mCustomerAddress.aTextFieldPhone.setText(addr.phone);		
			// Visualize
			mCustomerDialog.setVisible(true);
		}
	}
	
	protected JPanel globalAmiKoSettings() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridLayout(1, 4));
		
		ButtonGroup bg = new ButtonGroup();
		
		jPanel.setOpaque(false);
		jPanel.setBorder(new CompoundBorder(
				new TitledBorder(m_rb.getString("data-update")),
				new EmptyBorder(5,5,5,5)));		
		
		JCheckBox updateManualCBox = new JCheckBox(m_rb.getString("manual"));
		JCheckBox updateDailyCBox = new JCheckBox(m_rb.getString("daily"));
		JCheckBox updateWeeklyCBox = new JCheckBox(m_rb.getString("weekly"));
		JCheckBox updateMonthlyCBox = new JCheckBox(m_rb.getString("monthly"));		
		
		// Add to buttongroup to ensure that only one box is selected at a time
		bg.add(updateManualCBox);
		bg.add(updateDailyCBox);
		bg.add(updateWeeklyCBox);
		bg.add(updateMonthlyCBox);

		// Retrieve update frequency from preferences...
		// Default: manual update
		switch(mPrefs.getInt(UpdateID, 0)) {
		case 0:
			updateManualCBox.setSelected(true);
			break;		
		case 1:
			updateDailyCBox.setSelected(true);
			break;				
		case 2:
			updateWeeklyCBox.setSelected(true);				
			break;				
		case 3:
			updateMonthlyCBox.setSelected(true);
			break;
		default:
			break;
		}
		
		updateManualCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(UpdateID, 0);
			}
		});
		updateDailyCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(UpdateID, 1);
			}
		});
		updateWeeklyCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(UpdateID, 2);
			}
		});
		updateMonthlyCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(UpdateID, 3);
			}
		});
		jPanel.add(updateManualCBox);
		jPanel.add(updateDailyCBox);		
		jPanel.add(updateWeeklyCBox);
		jPanel.add(updateMonthlyCBox);
		
		return jPanel;
	}
	
	protected JPanel comparisonSettings() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridLayout(1, 4));
		
		ButtonGroup bg = new ButtonGroup();
		
		jPanel.setOpaque(false);
		jPanel.setBorder(new CompoundBorder(
				new TitledBorder(m_rb.getString("comp-update")),
				new EmptyBorder(5,5,5,5)));		
		
		JCheckBox updateManualCBox = new JCheckBox(m_rb.getString("manual"));
		JCheckBox updateHalfHourlyCBox = new JCheckBox(m_rb.getString("half-hourly"));
		JCheckBox updateHourlyCBox = new JCheckBox(m_rb.getString("hourly"));	
		JCheckBox updateHalfDailyCBox = new JCheckBox(m_rb.getString("half-daily"));
		
		// Add to buttongroup to ensure that only one box is selected at a time
		bg.add(updateManualCBox);
		bg.add(updateHalfHourlyCBox);
		bg.add(updateHourlyCBox);
		bg.add(updateHalfDailyCBox);

		// Retrieve update frequency from preferences...
		// Default: manual update
		switch(mPrefs.getInt(ComparisonID, 0)) {
		case 0:
			updateManualCBox.setSelected(true);
			break;
		case 1:
			updateHalfHourlyCBox.setSelected(true);
			break;			
		case 2:
			updateHourlyCBox.setSelected(true);
			break;			
		case 3:
			updateHalfDailyCBox.setSelected(true);
			break;
		default:
			break;
		}
		
		updateManualCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(ComparisonID, 0);
			}
		});
		updateHalfHourlyCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(ComparisonID, 1);
			}
		});	
		updateHourlyCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(ComparisonID, 2);
			}
		});	
		updateHalfDailyCBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mPrefs.putInt(ComparisonID, 3);
			}
		});	
		
		jPanel.add(updateManualCBox);
		jPanel.add(updateHalfHourlyCBox);		
		jPanel.add(updateHourlyCBox);
		jPanel.add(updateHalfDailyCBox);
		
		return jPanel;
	}
	
	private void deleteShoppingCarts() {
		System.out.println("User type changed...");
		for (int index=1; index<6; ++index) {
			File file = new File(Utilities.appDataFolder() + "\\shop\\korb" + index + ".ser");
			if (file.exists()) {		
				file.delete();
			    notify("# Deleted shopping cart " + index); 	// Notify GUI   
			}
		}
	}
	
	protected JPanel shoppingBasketSettings() {
		String GLNCodeStr = mPrefs.get(GLNCodeID, "7610000000000");
		String bestellAdrStr = mPrefs.get(BestellAdresseID, m_rb.getString("noaddress1"));
		String lieferAdrStr = mPrefs.get(LieferAdresseID, m_rb.getString("noaddress2"));
		String rechnungsAdrStr = mPrefs.get(RechnungsAdresseID, m_rb.getString("noaddress3"));
		String EmailStr = mPrefs.get(EmailAdresseID, "");
		String PhoneNumberStr = mPrefs.get(PhoneNumberID, "+41-");
		
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();		
		
		jPanel.setOpaque(false);
		jPanel.setBorder(new CompoundBorder(
				new TitledBorder(m_rb.getString("shoppingCart")), new EmptyBorder(5,5,5,5)));		
		
		// -----------------------------
		JLabel jlabelLogo = new JLabel("Logo");
		jlabelLogo.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,0, 0.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelLogo, gbc);
		
		String logoImageStr = mPrefs.get(LogoImageID, Constants.IMG_FOLDER + "empty_logo.png");	
		File logoFile = new File(logoImageStr);
		if (!logoFile.exists())
			logoImageStr = Constants.IMG_FOLDER + "empty_logo.png";
		ImageIcon icon = getImageIconFromFile(logoImageStr);
		mButtonLogo = new JButton(icon);
		mButtonLogo.setPreferredSize(new Dimension(128,128));
		mButtonLogo.setMargin(new Insets(10,10,10,10));
		if (!logoFile.exists())
			mButtonLogo.setBackground(color_yellow);
		else
			mButtonLogo.setBackground(color_white);
		mButtonLogo.setBorder(new CompoundBorder(
				new LineBorder(color_white), new EmptyBorder(0,3,0,0)));	
		gbc = getGbc(1,0, 2.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mButtonLogo, gbc);
		
		mButtonLogo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createFileChooser();
			}
		});
		
		// -----------------------------
		JLabel jlabelGLN = new JLabel("GLN Code*");
		jlabelGLN.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,1, 0.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelGLN, gbc);
		
		mTextFieldGLN = new JTextField(GLNCodeStr);
		if (!GLNCodeStr.matches("[\\d]{13}")) {
			mTextFieldGLN.setBorder(new LineBorder(color_red, 1, false));
			mTextFieldGLN.setBackground(color_red);
		} else {
			mTextFieldGLN.setBorder(new LineBorder(color_white, 1, false));
			mTextFieldGLN.setBackground(color_white);
		}
			
		gbc = getGbc(1,1 ,2.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mTextFieldGLN, gbc);
		
		mTextFieldGLN.addKeyListener(new KeyListener() { 
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
			// public void actionPerformed(ActionEvent e) {
				String mGLNCodeStr = mTextFieldGLN.getText();
				if (mGLNCodeStr.matches("[\\d]{13}")) {
					mPrefs.put(GLNCodeID, mGLNCodeStr);
					m_user = m_user_map.get(mGLNCodeStr);
					if (m_user!=null) {
						m_user.is_human = userIsHuman(m_user.category);
						if (m_user.is_human)
							mPrefs.put(HumanID, "yes");	
						else 
							mPrefs.put(HumanID, "no");
						String old_user_type = mPrefs.get(TypeID, "");
						String new_user_type = m_user.category.toLowerCase();
						mPrefs.put(TypeID, new_user_type);
						mPrefs.putInt(UserID, 17);	// Default
						if (m_user.is_human) {
							System.out.println("Person: " + m_user.gln_code + " - " + m_user.category + ", " + m_user.first_name + ", " + m_user.last_name);
						} else {
							System.out.println("Company: " + m_user.gln_code + " - " + m_user.category + ", " + m_user.name1 + ", " + m_user.name2);
						}
						mTextFieldGLN.setBorder(new LineBorder(color_ok, 5, false));
						mTextFieldGLN.setBackground(color_ok);
						String address = "";
						if (m_user.is_human) {
							if (!m_user.street.isEmpty()) {
								address = "Dr. med. " + m_user.first_name + " " + m_user.last_name + "\n" 
									+ m_user.street + "\n"
									+ m_user.zip + " " + m_user.city + "\n"
									+ "Schweiz";
							} else {
								address = "Dr. med. " + m_user.first_name + " " + m_user.last_name + "\n" 
									+ "***Strasse fehlt***" + "\n"
									+ m_user.zip + " " + m_user.city + "\n"
									+ "Schweiz";
							}
						} else {
							address = m_user.name1 + "\n" 
								+ m_user.name2 + "\n" 
								+ m_user.street + "\n"
								+ m_user.zip + " " + m_user.city + "\n"
								+ "Schweiz";
						}
						mTextAreaBestell.setText(address);
						mTextAreaLiefer.setText(address);
						mTextAreaRechnung.setText(address);
						if (m_user.street.isEmpty()) {
							mTextAreaBestell.setBorder(new LineBorder(color_red, 5, false));
							mTextAreaBestell.setBackground(color_red);
							mTextAreaLiefer.setBorder(new LineBorder(color_red, 5, false));
							mTextAreaLiefer.setBackground(color_red);
							mTextAreaRechnung.setBorder(new LineBorder(color_red, 5, false));
							mTextAreaRechnung.setBackground(color_red);							
						}						
						// If old user and new user do not match, delete ALL shopping carts
						if (!old_user_type.equals(new_user_type))
							deleteShoppingCarts();
						return;
					} else {
						// Innendienst identification
						if (Utilities.appCustomization().equals("zurrose")) {
							m_user = new User();
							m_user.gln_code = mGLNCodeStr;
						}
					}
				}
				mTextFieldGLN.setBorder(new LineBorder(color_red, 5, false));
				mTextFieldGLN.setBackground(color_red);
				mTextAreaBestell.setText("");
				mTextAreaLiefer.setText("");
				mTextAreaRechnung.setText("");
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});
		
		// -----------------------------
		JLabel jlabelBestell = new JLabel(m_rb.getString("ordaddress") + "*");		
		jlabelBestell.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,2,0.5,1.0,GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelBestell, gbc);
		
		mTextAreaBestell = new JTextArea(bestellAdrStr);	
		validateAddress(mTextAreaBestell);
		mTextAreaBestell.setPreferredSize(new Dimension(128, 256));
		mTextAreaBestell.setMargin(new Insets(5,5,5,5));
		gbc = getGbc(1,2,3.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(mTextAreaBestell, gbc);
		
		mTextAreaBestell.addKeyListener(new KeyListener() { 
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				// Validate email addres: do a quick sanity check on the email address
				validateAddress(mTextAreaBestell);
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});
		
		// -----------------------------
		JLabel jlabelLiefer = new JLabel(m_rb.getString("shipaddress") + "*");
		jlabelLiefer.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,3,0.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(jlabelLiefer, gbc);

		mTextAreaLiefer = new JTextArea(lieferAdrStr);
		validateAddress(mTextAreaLiefer);		
		mTextAreaLiefer.setPreferredSize(new Dimension(128, 128));
		mTextAreaLiefer.setMargin(new Insets(5,5,5,5));
		gbc = getGbc(1,3,2.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(mTextAreaLiefer, gbc);
		
		mTextAreaLiefer.addKeyListener(new KeyListener() { 
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				// Validate email addres: do a quick sanity check on the email address
				validateAddress(mTextAreaLiefer);
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});
		
		// -----------------------------
		JLabel jlabelRechnung = new JLabel(m_rb.getString("billaddress") + "*");
		jlabelRechnung.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,4,0.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(jlabelRechnung, gbc);

		mTextAreaRechnung = new JTextArea(rechnungsAdrStr);
		validateAddress(mTextAreaRechnung);	
		mTextAreaRechnung.setPreferredSize(new Dimension(128, 128));
		mTextAreaRechnung.setMargin(new Insets(5,5,5,5));
		gbc = getGbc(1,4,2.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(mTextAreaRechnung, gbc);
		
		mTextAreaRechnung.addKeyListener(new KeyListener() { 
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				// Validate email addres: do a quick sanity check on the email address
				validateAddress(mTextAreaRechnung);
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});
		
		// -----------------------------
		JLabel jlabelEmail = new JLabel(m_rb.getString("emailaddress"));
		jlabelEmail.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,5, 0.5,1.0, GridBagConstraints.HORIZONTAL);
		jPanel.add(jlabelEmail, gbc);
		
		mTextFieldEmail = new JTextField(EmailStr);
		validateEmail(EmailStr);
		gbc = getGbc(1,5 ,2.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mTextFieldEmail, gbc);		
		
		mTextFieldEmail.addKeyListener(new KeyListener() { 
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				// Validate email addres: do a quick sanity check on the email address
				String mEmailStr = mTextFieldEmail.getText();
				if (validateEmail(mEmailStr))
					mPrefs.put(EmailAdresseID, mEmailStr);			
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});
		
		// -----------------------------	
		JLabel jlabelPhone = new JLabel(m_rb.getString("telephone"));
		jlabelPhone.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,6, 0.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelPhone, gbc);
		
		mTextFieldPhone = new JTextField(PhoneNumberStr);
		// Validate phone number: do a quick sanity check on the phone number
		validatePhone(PhoneNumberStr);
		gbc = getGbc(1,6 ,2.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mTextFieldPhone, gbc);

		mTextFieldPhone.addKeyListener(new KeyListener() {
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				String mPhoneStr = mTextFieldPhone.getText();
				if (validatePhone(mPhoneStr))
					mPrefs.put(PhoneNumberID, mPhoneStr);								
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});

		JLabel jlabelFootnote = new JLabel("*" + m_rb.getString("medreg"));
		jlabelFootnote.setFont(new Font("Dialog", Font.ITALIC, 11));
		jlabelFootnote.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,7, 0.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelFootnote, gbc);
		
		return jPanel;
	}

	protected JPanel shoppingBasketSettings2() {
		String GLNCodeStr = mPrefs.get(GLNCodeID, "7610000000000");
		m_glncode = GLNCodeStr;
		m_email = mPrefs.get(EmailAdresseID, "");
		
		final JCheckBox jcheckAddress1 = new JCheckBox("Rechnungsadresse = Lieferadresse");
		final JCheckBox jcheckAddress2 = new JCheckBox("Bestelladresse = Lieferadresse");
		
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();		
		
		jPanel.setOpaque(false);
		jPanel.setBorder(new CompoundBorder(
				new TitledBorder(m_rb.getString("shoppingCart")), new EmptyBorder(5,5,5,5)));	
		
		// -----------------------------
		JLabel jlabelGLN = new JLabel("GLN Code*");
		jlabelGLN.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,0, 0.1,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelGLN, gbc);
		
		mTextFieldGLN = new JTextField(GLNCodeStr);
		if (!GLNCodeStr.matches("[\\d]{13}")) {
			if (!validateCode()) {
				mTextFieldGLN.setBorder(new LineBorder(color_red, 1, false));
				mTextFieldGLN.setBackground(color_red);			
			}
		} else {
			mTextFieldGLN.setBorder(new LineBorder(color_white, 1, false));
			mTextFieldGLN.setBackground(color_white);
		}			
		gbc = getGbc(1,0 ,3.0,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mTextFieldGLN, gbc);
		
		mTextFieldGLN.addKeyListener(new KeyListener() { 
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
			// public void actionPerformed(ActionEvent e) {
				String mGLNCodeStr = mTextFieldGLN.getText();
				if (mGLNCodeStr.matches("[\\d]{13}")) {
					mPrefs.put(GLNCodeID, mGLNCodeStr);
					m_user = m_user_map.get(mGLNCodeStr+"S");
					if (m_user!=null) {
						//
						jcheckAddress1.setSelected(false);
						jcheckAddress2.setSelected(false);
						//
						m_user.is_human = userIsHuman(m_user.category);
						if (m_user.is_human)
							mPrefs.put(HumanID, "yes");	
						else 
							mPrefs.put(HumanID, "no");
						String old_user_type = mPrefs.get(TypeID, "");
						String new_user_type = m_user.category.toLowerCase();
						mPrefs.put(TypeID, new_user_type);
						mPrefs.putInt(UserID, 17);	// Default
						
						// Change color of GLN field to denote success
						mTextFieldGLN.setBorder(new LineBorder(color_white, 1, false));
						mTextFieldGLN.setBackground(color_white);	
						
						// If necessary change labels
						if (m_user.is_human  || !m_user.title.isEmpty()) {
							mShippingAddress.setNameLabels("Funkt.", "Abt.", "Firma");
							mBillingAddress.setNameLabels("Funkt.", "Abt.", "Firma");
							mOfficeAddress.setNameLabels("Funkt.", "Abt.", "Firma");							
							System.out.println("Person: " + m_user.gln_code + " - " + m_user.category + ", " 
									+ m_user.title + ", " + m_user.first_name + ", " + m_user.last_name);
						} else {
							mShippingAddress.setNameLabels("Einheit", "Abt.", "Firma");
							mBillingAddress.setNameLabels("Einheit", "Abt.", "Firma");
							mOfficeAddress.setNameLabels("Einheit", "Abt.", "Firma");	
							System.out.println("Company: " + m_user.gln_code + " - " + m_user.category + ", " 
									+ m_user.name1 + ", " + m_user.name2 + ", " + m_user.name3);
						}									
						// Set shipping address
						mShippingAddress.setDataWithUserInfo(m_user);
						// Set billing address (if contained in DB)
						if (m_user_map.containsKey(mGLNCodeStr+"B")) {
							m_user = m_user_map.get(mGLNCodeStr+"B");
							if (m_user!=null) {
								m_user.is_human = userIsHuman(m_user.category);
								mBillingAddress.setDataWithUserInfo(m_user);
							} else {
								mBillingAddress.clearData();
							}
						} else {
							mBillingAddress.clearData();
						}
						// Set office address (if contained in DB)
						if (m_user_map.containsKey(mGLNCodeStr+"O")) {
							m_user = m_user_map.get(mGLNCodeStr+"O");							
							if (m_user!=null) {							
								m_user.is_human = userIsHuman(m_user.category);
								mOfficeAddress.setDataWithUserInfo(m_user);							
							} else {
								mOfficeAddress.clearData();
							}
						} else {
							mOfficeAddress.clearData();
						}
						
						// If old user and new user do not match, delete ALL shopping carts
						if (!old_user_type.equals(new_user_type))
							deleteShoppingCarts();
						return;
					} else {
						// Innendienst identification
						if (Utilities.appCustomization().equals("zurrose")) {
							m_user = new User();
							m_user.gln_code = mGLNCodeStr;
						}
					}
				} else {
					// Check only 7-digits codes
					m_glncode = mGLNCodeStr;
					if (validateCode()) {
						return;
					}
					// Clear all data
					mShippingAddress.clearData();
					mBillingAddress.clearData();
					mOfficeAddress.clearData();	
					// else...
					mTextFieldGLN.setBorder(new LineBorder(color_red, 1, false));
					mTextFieldGLN.setBackground(color_red);
				}
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});
		
		jcheckAddress1.setSelected(false);
		jcheckAddress1.setBorder(new EmptyBorder(new Insets(1,1,1,1)));
		jcheckAddress1.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,1, 1.0,1.0, GridBagConstraints.HORIZONTAL);
		gbc.gridwidth = 2;
		jPanel.add(jcheckAddress1, gbc);
		
		jcheckAddress1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (jcheckAddress1.isSelected()) {
					mBillingAddress.copyDataFromShippingPanel();
					mUserDialog.revalidate();
					mUserDialog.repaint();
				} else
					mBillingAddress.clearData();
			}
		});
		
		jcheckAddress2.setSelected(false);	
		jcheckAddress2.setBorder(new EmptyBorder(new Insets(1,1,1,1)));
		jcheckAddress2.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,2, 1.0,1.0, GridBagConstraints.HORIZONTAL);
		gbc.gridwidth = 2;
		jPanel.add(jcheckAddress2, gbc);
		
		jcheckAddress2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (jcheckAddress2.isSelected()) {
					mOfficeAddress.copyDataFromShippingPanel();
					mUserDialog.revalidate();
					mUserDialog.repaint();
				} else
					mOfficeAddress.clearData();
			}
		});
		
		// -----------------------------
		JLabel jlabelLogo = new JLabel("Logo");
		jlabelLogo.setHorizontalAlignment(JLabel.LEFT);
		jlabelLogo.setBorder(new EmptyBorder(0,8,0,0));			
		gbc = getGbc(2,1, 0.1,1.0, GridBagConstraints.HORIZONTAL);	
		gbc.gridwidth = 1;
		jPanel.add(jlabelLogo, gbc);
		
		String logoImageStr = mPrefs.get(LogoImageID, Constants.IMG_FOLDER + "empty_logo.png");	
		File logoFile = new File(logoImageStr);
		if (!logoFile.exists())
			logoImageStr = Constants.IMG_FOLDER + "empty_logo.png";
		ImageIcon icon = getImageIconFromFile(logoImageStr);
		mButtonLogo = new JButton(icon);
		mButtonLogo.setPreferredSize(new Dimension(128, 128));
		mButtonLogo.setMargin(new Insets(10,10,10,10));
		if (!logoFile.exists())
			mButtonLogo.setBackground(color_yellow);
		else
			mButtonLogo.setBackground(color_white);
		mButtonLogo.setBorder(new CompoundBorder(
				new LineBorder(color_white), new EmptyBorder(0,3,0,0)));	
		gbc = getGbc(3,0, 3.0,1.0, GridBagConstraints.HORIZONTAL);		
		gbc.gridheight = 3;
		jPanel.add(mButtonLogo, gbc);
		
		mButtonLogo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createFileChooser();
			}
		});

		// -----------------------------
		gbc = getGbc(0,3, 1.0,1.0, GridBagConstraints.HORIZONTAL);		
		gbc.gridwidth = 4;
		mShippingAddress = new AddressPanel("S", m_rb.getString("shipaddress"));
		mShippingAddress.retrieveDataFromPreferences();
		jPanel.add(mShippingAddress, gbc);

		// -----------------------------
		gbc = getGbc(0,4, 1.0,1.0, GridBagConstraints.HORIZONTAL);		
		gbc.gridwidth = 4;		
		mBillingAddress = new AddressPanel("B", m_rb.getString("billaddress"));
		mBillingAddress.retrieveDataFromPreferences();		
		jPanel.add(mBillingAddress, gbc);

		// -----------------------------
		gbc = getGbc(0,5, 1.0,1.0, GridBagConstraints.HORIZONTAL);				
		gbc.gridwidth = 4;		
		mOfficeAddress = new AddressPanel("O", m_rb.getString("ordaddress"));
		mOfficeAddress.retrieveDataFromPreferences();		
		jPanel.add(mOfficeAddress, gbc);

		// -----------------------------
		JLabel jlabelFootnote = new JLabel("*" + m_rb.getString("medreg"));
		jlabelFootnote.setFont(new Font("Dialog", Font.ITALIC, 11));
		jlabelFootnote.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,6, 0.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelFootnote, gbc);
		
		return jPanel;
	}
	
	protected JPanel customerSettings() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();		
		
		jPanel.setOpaque(false);
		jPanel.setBorder(new EmptyBorder(5,5,5,5));	
		
		// -----------------------------
		JLabel jlabelGLN = new JLabel("GLN Code:");
		jlabelGLN.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,0, 0.1,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelGLN, gbc);
		
		mCustomerGlnCode = new JLabel("761000000000");
		gbc = getGbc(1,0, 3.0,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mCustomerGlnCode, gbc);

		// -----------------------------
		gbc = getGbc(0,3, 1.0,1.0, GridBagConstraints.HORIZONTAL);		
		gbc.gridwidth = 4;
		mCustomerAddress = new AddressPanel("S", "");
		jPanel.add(mCustomerAddress, gbc);
		
		return jPanel;
	}
	
	private boolean userIsHuman(String category) {
		return category.toLowerCase().equals("arzt");
	}
	
	private boolean validateAddress(JTextArea textArea) {
		if (textArea.getText().contains("***")) {
			textArea.setBorder(new LineBorder(color_red, 5, false));
			textArea.setBackground(color_red);  
			return false;			
		} else {
			textArea.setBorder(new LineBorder(color_ok, 5, false));
			textArea.setBackground(color_ok);  
			return true;			
		}
	}
	
	/**
	 * Validate email addres: do a quick sanity check on the email address
	 * @param emailStr
	 * @return
	 */
	private boolean validateEmail(String emailStr) {
		if (mTextFieldEmail!=null) {
			if (emailStr.matches("^[_\\w-\\+]+(\\.[_\\w-]+)*@[\\w-]+(\\.[\\w]+)*(\\.[A-Za-z]{2,})$")) {
				mTextFieldEmail.setBorder(new LineBorder(color_ok, 5, false));
				mTextFieldEmail.setBackground(color_ok);  
				return true;
			} else {
				mTextFieldEmail.setBorder(new LineBorder(color_red, 5, false));
				mTextFieldEmail.setBackground(color_red); 
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Validate phone number: do a quick sanity check on the phone number
	 * @param phoneStr
	 * @return
	 */
	private boolean validatePhone(String phoneStr) {
		if (phoneStr.matches("[+][\\d]+-[\\d]+")) {
			mTextFieldPhone.setBorder(new LineBorder(color_ok, 5, false));
			mTextFieldPhone.setBackground(color_ok);
			return true;
		} else {
			mTextFieldPhone.setBorder(new LineBorder(color_red, 5, false));
			mTextFieldPhone.setBackground(color_red);
			return false;
		}
	}
		
	private GridBagConstraints getGbc(int x, int y, double wx, double wy, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(8,0,0,0); // Insets(int top, int left, int bottom, int right)		
		gbc.fill = fill;
		gbc.weightx = wx;
		gbc.weighty = wy;
		gbc.gridx = x;
		gbc.gridy = y;
		
		return gbc;
	}
	
	private Map<String, User> readFromCsvToMap(String filename) {
		Map<String, User> map = new TreeMap<String, User>();
		try {
			File file = new File(filename);
			if (!file.exists()) 
				return null;
			FileInputStream fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				/*
					Person  - 7601000900487|Bauer|Wibke Cornelia|8596|Münsterlingen||Ärztin/Arzt|Ja|Nein
				 	Company - 7601001059900|Ostschweizer Kinderspital||9006|St. Gallen|Claudiusstrasse 6|Spitalapotheke, Andere|||
				 	Person -> Arzt
				 	Company -> Apotheke, Spital, Wissenschaft, Behörde
				*/
				String token[] = line.split("\\|", -1);	// -1 -> don't discard empty strings at the end				
				User user = new User();
				user.gln_code = token[0];
				user.is_human = !token[7].isEmpty()&&!token[8].isEmpty();
				if (user.is_human) {
					user.first_name = token[1];
					user.last_name = token[2];
				} else {
					user.name1 = token[1];
					user.name2 = token[2];
				}
				user.zip = token[3];
				user.city = token[4];
				user.street = token[5];
				user.category = token[6];
				user.selbst_disp = !token[7].isEmpty();
				user.bet_mittel = !token[8].isEmpty();
				map.put(token[0], user);
			}
			br.close();
		} catch (Exception e) {
			System.err.println(">> Error in reading csv file");
		}
		
		return map;
	}
	
	private ImageIcon getImageIconFromFile(String filename) {
		ImageIcon imgIcon = null;
		try {
			Image img = null;
			BufferedImage myLogo = ImageIO.read(new File(filename));
			int width = myLogo.getWidth();
			int height = myLogo.getHeight();
			float f = 1.0f;
			if (width>128)
				f = width/128.0f;
			else if (height>128)
				f = height/128.0f;
			img = myLogo.getScaledInstance((int)(width/f), (int)(height/f), java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			imgIcon = new ImageIcon(img);
		} catch (IOException e) {
			// Do nothing...
		}
		return imgIcon;
	}
	
	private void createFileChooser() {		
		if (mFc!=null) {
			mFc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return (f.getName().toLowerCase().endsWith(".png") ||
							f.getName().toLowerCase().endsWith(".jpg") ||
							f.isDirectory());
				}
				public String getDescription() {
					return "*.png, *.jpg";
				}
			});
					
			mFc.setDialogTitle("Logo einstellen");
			mFc.setApproveButtonText("Wählen");
			int r = mFc.showOpenDialog(mFrame);
			if (r==JFileChooser.APPROVE_OPTION) {
				// File file = fc.getSelectedFile();
				String filename = mFc.getSelectedFile().getPath();	
				ImageIcon icon = getImageIconFromFile(filename);	
				mButtonLogo.setIcon(icon);
				mPrefs.put(LogoImageID, filename);
				System.out.println("SettingsPage - opening " + filename);				
			} else {
				System.out.println("SettingsPage - open command cancelled by the user...");
			}
		}
	}
}
