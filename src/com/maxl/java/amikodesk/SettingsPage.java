package com.maxl.java.amikodesk;

import java.awt.Color;
import java.awt.Dimension;
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
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

public class SettingsPage extends JDialog {

	private static String UpdateID = "update";
	private static String LogoImageID = "logo";
	private static String GLNCodeID = "glncode";
	private static String BestellAdresseID = "bestelladresse";
	private static String LieferAdresseID = "lieferadresse";
	private static String RechnungsAdresseID = "rechnungsadresse";
	private static String EmailAdresseID = "emailadresse";
	private static String PhoneNumberID = "phonenumber";
	
	private Map<String, User> m_user_map = null;;
	private User m_user = null;
	
	private JFrame mFrame = null;
	private JFileChooser mFc = null;
	private JButton mButtonLogo = null;
	private Preferences mPrefs = null;
	private JTextField mTextFieldGLN = null;
	private JTextArea mTextAreaBestell = null;
	private JTextArea mTextAreaLiefer = null;
	private JTextArea mTextAreaRechnung = null;
	private JTextField mTextFieldEmail = null;
	private JTextField mTextFieldPhone = null;
	
	// Colors
	private static Color color_white = new Color(255,255,255);
	// private static Color color_green = new Color(220,255,220);
	private static Color color_red = new Color(255,220,220);
	
	public SettingsPage(JFrame frame) {
		mFrame = frame;
		mFc = new JFileChooser();
		// Defines a node in which the preferences can be stored
		mPrefs = Preferences.userRoot().node(this.getClass().getName());
				
		// Load gln codes file and create map
		String m_application_data_folder = Utilities.appDataFolder();
		m_user_map = readFromCsvToMap(m_application_data_folder + "\\" + Constants.GLN_CODES_FILE);
		if (m_user_map==null) {
			System.out.println("Loading default gln codes file");
			m_user_map = readFromCsvToMap("./dbs/" + Constants.GLN_CODES_FILE);
		}
		
		
		this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel jplInnerPanel1 = globalAmiKoSettings();
		this.add(jplInnerPanel1);
		
		add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel jplInnerPanel2 = shoppingBasketSettings();
		this.add(jplInnerPanel2);
		
		add(Box.createRigidArea(new Dimension(0, 10)));
		
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setTitle("Einstellungen");		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// Centers the dialog
		this.setLocationRelativeTo(null);
		// Set size
		this.setSize(512,640);		
		this.setResizable(false);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
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
		});
	}
	
	public void display() {
		// Visualize
		this.setVisible(true);
	}
	
	protected JPanel globalAmiKoSettings() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridLayout(1, 4));
		
		ButtonGroup bg = new ButtonGroup();
		
		jPanel.setOpaque(false);
		jPanel.setBorder(new CompoundBorder(
				new TitledBorder("Update"),
				new EmptyBorder(5,5,5,5)));		
		
		JCheckBox updateNeverCBox = new JCheckBox("manuell");
		JCheckBox updateDailyCBox = new JCheckBox("täglich");
		JCheckBox updateWeeklyCBox = new JCheckBox("wöchentlich");
		JCheckBox updateMonthlyCBox = new JCheckBox("monatlich");		
		
		// Add to buttongroup to ensure that only one box is selected at a time
		bg.add(updateNeverCBox);
		bg.add(updateDailyCBox);
		bg.add(updateWeeklyCBox);
		bg.add(updateMonthlyCBox);

		// Retrieve update frequency from preferences...
		// Default: manual update
		switch(mPrefs.getInt(UpdateID, 0)) {
		case 0:
			updateNeverCBox.setSelected(true);
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
		
		updateNeverCBox.addItemListener(new ItemListener() {
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
		jPanel.add(updateNeverCBox);
		jPanel.add(updateDailyCBox);		
		jPanel.add(updateWeeklyCBox);
		jPanel.add(updateMonthlyCBox);
				
		return jPanel;
	}
	
	protected JPanel shoppingBasketSettings() {
		String GLNCodeStr = mPrefs.get(GLNCodeID, "7610");
		String bestellAdrStr = mPrefs.get(BestellAdresseID, "Keine Bestelladresse");
		String lieferAdrStr = mPrefs.get(LieferAdresseID, "Keine Lieferadresse");
		String rechnungsAdrStr = mPrefs.get(RechnungsAdresseID, "Keine Rechnungsadresse");
		String EmailStr = mPrefs.get(EmailAdresseID, "");
		String PhoneNumberStr = mPrefs.get(PhoneNumberID, "+41-");
		
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();		
		
		jPanel.setOpaque(false);
		jPanel.setBorder(new CompoundBorder(
				new TitledBorder("Warenkorb"), new EmptyBorder(5,5,5,5)));		
		
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
		mButtonLogo.setPreferredSize(new Dimension(128, 128));
		mButtonLogo.setMargin(new Insets(10,10,10,10));
		if (!logoFile.exists())
			mButtonLogo.setBackground(color_red);
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
		JLabel jlabelGLN = new JLabel("GLN Code");
		jlabelGLN.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,1, 0.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelGLN, gbc);
		
		mTextFieldGLN = new JTextField(GLNCodeStr);
		if (!GLNCodeStr.matches("[\\d]{13}")) {
			mTextFieldGLN.setBorder(new LineBorder(color_red, 5, false));
			mTextFieldGLN.setBackground(color_red);
		} else {
			mTextFieldGLN.setBorder(new LineBorder(color_white, 5, false));
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
						System.out.println(m_user.getGlnCode() + " - " + m_user.getName1() + ", " + m_user.getName2());
						mTextFieldGLN.setBorder(new LineBorder(color_white, 5, false));
						mTextFieldGLN.setBackground(color_white);
						String address = "";
						if (m_user.isHuman()) {
							if (!m_user.getStreet().isEmpty()) {
								address = "Dr. med. " + m_user.getName2() + " " + m_user.getName1() + "\n" 
									+ m_user.getStreet() + "\n"
									+ m_user.getPostCode() + " " + m_user.getCity() + "\n"
									+ "Schweiz";
							} else {
								address = "Dr. med. " + m_user.getName2() + " " + m_user.getName1() + "\n" 
									+ "***Strasse fehlt***" + "\n"
									+ m_user.getPostCode() + " " + m_user.getCity() + "\n"
									+ "Schweiz";
							}
						} else {
							address = m_user.getName2() + "\n" 
								+ m_user.getName1() + "\n" 
								+ m_user.getStreet() + "\n"
								+ m_user.getPostCode() + " " + m_user.getCity() + "\n"
								+ "Schweiz";
						}
						m_user.setFullAddress(address);
						mTextAreaBestell.setText(address);
						mTextAreaLiefer.setText(address);
						mTextAreaRechnung.setText(address);
						if (m_user.getStreet().isEmpty()) {
							mTextAreaBestell.setBorder(new LineBorder(color_red, 5, false));
							mTextAreaBestell.setBackground(color_red);
							mTextAreaLiefer.setBorder(new LineBorder(color_red, 5, false));
							mTextAreaLiefer.setBackground(color_red);
							mTextAreaRechnung.setBorder(new LineBorder(color_red, 5, false));
							mTextAreaRechnung.setBackground(color_red);							
						}
						return;
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
		JLabel jlabelBestell = new JLabel("Bestelladresse");
		jlabelBestell.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,2,0.5,1.0,GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelBestell, gbc);
		
		mTextAreaBestell = new JTextArea(bestellAdrStr);
		mTextAreaBestell.setPreferredSize(new Dimension(128, 256));
		mTextAreaBestell.setMargin(new Insets(5,5,5,5));
		gbc = getGbc(1,2,3.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(mTextAreaBestell, gbc);
		
		// -----------------------------
		JLabel jlabelLiefer = new JLabel("Lieferadresse");
		jlabelLiefer.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,3,0.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(jlabelLiefer, gbc);

		mTextAreaLiefer = new JTextArea(lieferAdrStr);
		mTextAreaLiefer.setPreferredSize(new Dimension(128, 128));
		mTextAreaLiefer.setMargin(new Insets(5,5,5,5));
		gbc = getGbc(1,3,2.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(mTextAreaLiefer, gbc);
		
		// -----------------------------
		JLabel jlabelRechnung = new JLabel("Rechnungsadresse");
		jlabelRechnung.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,4,0.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(jlabelRechnung, gbc);

		mTextAreaRechnung = new JTextArea(rechnungsAdrStr);
		mTextAreaRechnung.setPreferredSize(new Dimension(128, 128));
		mTextAreaRechnung.setMargin(new Insets(5,5,5,5));
		gbc = getGbc(1,4,2.5,1.0,GridBagConstraints.HORIZONTAL);
		jPanel.add(mTextAreaRechnung, gbc);
		
		// -----------------------------
		JLabel jlabelEmail = new JLabel("Emailadresse");
		jlabelEmail.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,5, 0.5,1.0, GridBagConstraints.HORIZONTAL);
		jPanel.add(jlabelEmail, gbc);
		
		mTextFieldEmail = new JTextField(EmailStr);
		if (!EmailStr.matches("^[_\\w-\\+]+(\\.[_\\w-]+)*@[\\w-]+(\\.[\\w]+)*(\\.[A-Za-z]{2,})$")) {
			mTextFieldEmail.setBorder(new LineBorder(color_red, 5, false));
			mTextFieldEmail.setBackground(color_red);  
		} else {
			mTextFieldEmail.setBorder(new LineBorder(color_white, 5, false));
			mTextFieldEmail.setBackground(color_white); 
		}
		gbc = getGbc(1,5 ,2.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mTextFieldEmail, gbc);
		
		mTextFieldEmail.addKeyListener(new KeyListener() { 
			@Override 
			public void keyPressed(KeyEvent keyEvent) {
				//
			}
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				// Validate email address
				String mEmailStr = mTextFieldEmail.getText();
				if (mEmailStr.matches("^[_\\w-\\+]+(\\.[_\\w-]+)*@[\\w-]+(\\.[\\w]+)*(\\.[A-Za-z]{2,})$")) {
					mTextFieldEmail.setBorder(new LineBorder(color_white, 5, false));
					mTextFieldEmail.setBackground(color_white);
					mPrefs.put(EmailAdresseID, mEmailStr);			
				} else { 
					mTextFieldEmail.setBorder(new LineBorder(color_red, 5, false));
					mTextFieldEmail.setBackground(color_red);
				}
			}
			@Override 
			public void keyTyped(KeyEvent keyEvent) {
				//
			}
		});
		
		// -----------------------------	
		JLabel jlabelPhone = new JLabel("Telephonnummer");
		jlabelPhone.setHorizontalAlignment(JLabel.LEFT);
		gbc = getGbc(0,6, 0.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(jlabelPhone, gbc);
		
		mTextFieldPhone = new JTextField(PhoneNumberStr);
		if (PhoneNumberStr.matches("[+][\\d]+-[\\d]+")) {
			mTextFieldPhone.setBorder(new LineBorder(color_white, 5, false));
			mTextFieldPhone.setBackground(color_white);
		} else {
			mTextFieldPhone.setBorder(new LineBorder(color_red, 5, false));
			mTextFieldPhone.setBackground(color_red);
		}
		gbc = getGbc(1,6 ,2.5,1.0, GridBagConstraints.HORIZONTAL);		
		jPanel.add(mTextFieldPhone, gbc);

		mTextFieldPhone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mPhoneStr = mTextFieldPhone.getText();
				// Validate phone number
				if (mPhoneStr.matches("[+][\\d]+-[\\d]+")) {
					mTextFieldPhone.setBorder(new LineBorder(color_white, 5, false));
					mTextFieldPhone.setBackground(color_white);
					mPrefs.put(PhoneNumberID, mPhoneStr);
					System.out.println(mPhoneStr);					
				} else { 
					mTextFieldPhone.setBorder(new LineBorder(color_red, 5, false));
					mTextFieldPhone.setBackground(color_red);
				}
			}
		});

		return jPanel;
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
				// Human - 7601000900487|Bauer|Wibke Cornelia|8596|Münsterlingen||Ärztin/Arzt|Ja|Nein
				// Corporate - 7601001059900|Ostschweizer Kinderspital||9006|St. Gallen|Claudiusstrasse 6|Spitalapotheke, Andere|||
				String token[] = line.split("\\|", -1);	// -1 -> don't discard empty strings at the end				
				User user = new User();
				user.setGlnCode(token[0]);
				user.setName1(token[1]);
				user.setName2(token[2]);
				user.setPostCode(token[3]);
				user.setCity(token[4]);				
				user.setStreet(token[5]);
				user.setType(token[6]);
				user.setHuman(!token[7].isEmpty()&&!token[8].isEmpty());
				user.setDispensationPermit(!token[7].isEmpty());
				user.setAnaesthesiaPermit(!token[8].isEmpty());
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
	
	private void generateAndSendEmail() throws AddressException, MessagingException {
		Properties mailServerProperties;
		Session getMailSession;
		MimeMessage generateMailMessage;

		// Step1
		System.out.println("\n 1st ===> setup Mail Server Properties..");
		mailServerProperties = System.getProperties();
		mailServerProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		mailServerProperties.put("mail.transport.protocol", "smtps");
		mailServerProperties.put("mail.smtps.host", "smtp.ifi.uzh.ch");
		mailServerProperties.put("mail.smtps.auth", "true"); // Enable Authentication

		// Step2
		System.out.println("\n\n 2nd ===> get Mail Session..");
		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		getMailSession.setDebug(true);
		generateMailMessage = new MimeMessage(getMailSession);
		generateMailMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress("user1@hostname.com"));
		generateMailMessage.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress("user2@hostname.com"));
		generateMailMessage.setFrom(new InternetAddress("user3@hostname.com"));
		generateMailMessage.setSubject("Greetings from Cybermax...");
		String emailBody = "Test email by Crunchify.com JavaMail API example. "
				+ "<br><br> Regards, <br>Crunchify Admin";
		generateMailMessage.setContent(emailBody, "text/html");

		// Step3
		Transport transport = getMailSession.getTransport("smtps");
		// Enter your correct gmail UserID and Password
		transport.connect("smtp.ifi.uzh.ch", 465, "username", "password");
		transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
		transport.close();
	}
}
