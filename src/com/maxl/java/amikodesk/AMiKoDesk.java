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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import chrriis.dj.nativeswing.NSComponentOptions;
import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserFunction;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

import com.maxl.java.shared.Conditions;
import com.maxl.java.shared.User;

public class AMiKoDesk {

	// Important Constants
	private static final int BigCellNumber = 512;

	// Constants for command line options
	private static boolean CML_OPT_SERVER = false;
	private static int CML_OPT_WIDTH = 1024;
	private static int CML_OPT_HEIGHT = 768;
	private static String CML_OPT_TYPE = "";
	private static String CML_OPT_TITLE = "";
	private static String CML_OPT_EANCODE = "";
	private static String CML_OPT_REGNR = "";

	// TCP server related variables go here
	private static AppServer mTcpServer;

	private static Long m_start_time = 0L;
	private static List<String> med_content = new ArrayList<String>();
	private static List<ArrayList<Long>> med_id = new ArrayList<ArrayList<Long>>();		// Define a list of ids... used to group products!
	private static List<Medication> med_search = new ArrayList<Medication>();
	private static List<Article> rose_search = new ArrayList<Article>();
	private static List<User> customer_search = new ArrayList<User>();
	private static List<Medication> med_title = new ArrayList<Medication>();
	private static List<Medication> list_of_favorites = new ArrayList<Medication>();
	private static List<Article> list_of_articles = new ArrayList<Article>();
	private static List<String> list_of_carts = new ArrayList<String>();
	private static List<User> list_of_gln_codes = new ArrayList<User>();
	private static Map<String, Medication> m_med_basket = new TreeMap<String, Medication>();
	private static Map<String, Article> m_shopping_basket = new LinkedHashMap<String, Article>();
	private static HashMap<String, User> m_user_map = null;
	private static HashMap<String, Address> m_address_map = null;
	// Ibsa
	private static TreeMap<String, Conditions> m_map_ibsa_conditions = null;
	private static Map<String, String> m_map_ibsa_glns = null;
	// Desitin
	private static TreeMap<String, TreeMap<String, Float>> m_map_desitin_conditions = null;
	// Rose
	private static Map<String, Article> m_comparison_basket = new LinkedHashMap<String, Article>();	
	private static Map<String, List<Article>> m_map_similar_articles = new HashMap<String, List<Article>>();
	
	private static HashSet<String> favorite_meds_set;
	private static DataStore favorite_data = null;
	private static String m_query_str = "";
	private static String m_customer_query_str = "";
	private static String m_customer_gln_code = "";
	private static UIState m_curr_uistate = new UIState("aips");
	private static String m_curr_regnr = "";
	private static int med_index = -1;
	private static int prev_med_index = -1;
	
	private static ProgressIndicator m_progress_indicator = new ProgressIndicator(32);
	private static MiddlePane m_middle_pane = null;
	private static WebPanel2 m_web_panel = null;
	private static String m_css_str = null;
	private static String m_jscript_str = null;
	private static MainSqlDb m_sqldb = null;
	private static RoseSqlDb m_rosedb = null;
	private static FastAccessData m_customerdb = null;
	private static UpdateDb m_maindb_update = null;
	private static boolean m_full_db_update = true;
	private static boolean m_mutex_update = false;
	private static InteractionsCart m_interactions_cart = null;
	private static ShoppingCart m_shopping_cart = null;
	private static ComparisonCart m_comparison_cart = null;
	private static boolean m_compare_show_all = true;
	// private static InteractionsDb m_interdb = null;
	private static List<String> m_section_str = null;
	private static String m_application_data_folder = null;
	private static Emailer m_emailer;
	private static SettingsPage m_settings_page = null;
	// List of med authors
	private static List<Author> m_list_of_authors = new ArrayList<Author>();

	// Panels
	private static ListPanel m_list_titles = null;
	private static ListPanel m_list_auths = null;
	private static ListPanel m_list_regnrs = null;
	private static ListPanel m_list_atccodes = null;
	private static ListPanel m_list_ingredients = null;
	private static ListPanel m_list_therapies = null;
	private static ListPanel m_list_customers = null;

	// Preferences
	private static Preferences m_prefs = null;
	private static boolean m_preferences_ok = false;

	// Colors
	private static Color m_toolbar_bg = new Color(240, 240, 240); 			// light gray
	private static Color m_but_color_bg = new Color(220, 220, 250); 		// blue-ish
	private static Color m_hover_but_color_bg = new Color(190, 190, 220);
	private static Color m_selected_but_color = new Color(240, 240, 240); 	// light gray
	private static Color m_list_selected_color = new Color(235, 235, 235); 	// light gray 2
	private static Color m_search_field_bg = new Color(230, 250, 250);		// green-ish

	private static ResourceBundle m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));

	private static Font m_custom_font = null;
	
	// 0: Präparat, 1: Inhaber, 2: Wirkstoff/ATC, 3: Reg. Nr., 4: Therapie, 5: Customer
	// -> {0, 1, 2, 3, 4, 5};
	private static final int NAME = 0;
	private static final int OWNER = 1;
	private static final int SUBSTANCE = 2;
	private static final int REGISTER = 3;
	private static final int THERAPY = 4;
	private static final int CUSTOMER = 5;
	private static int m_query_type = NAME;

	/**
	 * Adds an option into the command line parser
	 * @param optionName - option name
	 * @param description - option description
	 * @param hasValue - if set to true, --option=value, otherwise, --option is a boolean
	 * @param isMandatory - if set to true, option must be provided
	 */
	@SuppressWarnings("static-access")
	static void addOption(Options opts, String optionName, String description, boolean hasValue, boolean isMandatory) {
		OptionBuilder opt = OptionBuilder.withLongOpt(optionName);
		opt = opt.withDescription(description);
		if (hasValue)
			opt = opt.hasArg();
		if (isMandatory)
			opt = opt.isRequired();
		opts.addOption(opt.create());
	}

	static void commandLineParse(Options opts, String[] args) {
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(opts, args);
			if (cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("amikodesk", opts);
				System.exit(0);
			}
			if (cmd.hasOption("version")) {
				System.out.println("Version of amikodesk: "	+ Constants.APP_VERSION);
			}
			if (cmd.hasOption("port")) {
				int port = Integer.parseInt(cmd.getOptionValue("port"));
				if (port > 999 && port < 9999)
					CML_OPT_SERVER = true;
				System.out.print("Initializing TCP server... ");
				mTcpServer = new AppServer(port);
				// NOTE: Must be stopped at a certain point...
				new Thread(mTcpServer).start();
				System.out.println("done");
			}
			if (cmd.hasOption("width")) {
				int width = Integer.parseInt(cmd.getOptionValue("width"));
				if (width > 1024 && width <= 1920)
					CML_OPT_WIDTH = width;
			}
			if (cmd.hasOption("height")) {
				int height = Integer.parseInt(cmd.getOptionValue("height"));
				if (height > 768 && height <= 1200)
					CML_OPT_WIDTH = height;
			}
			if (cmd.hasOption("lang")) {
				if (cmd.getOptionValue("lang").equals("de")) {
					// Check if db exists
					File wfile = new File("./dbs/amiko_db_full_idx_de.db");
					if (!wfile.exists())
						System.out.println("> Error: amiko_db_full_idx_de.db not in directory ./dbs");
					Constants.DB_LANGUAGE = "DE";
				} else if (cmd.getOptionValue("lang").equals("fr")) {
					// Check if db exists
					File wfile = new File("./dbs/amiko_db_full_idx_fr.db");
					if (!wfile.exists())
						System.out.println("> Error: amiko_db_full_idx_fr.db not in directory ./dbs");
					Constants.DB_LANGUAGE = "FR";
				}
			}
			if (cmd.hasOption("type")) {
				String type = cmd.getOptionValue("type");
				if (type != null && !type.isEmpty())
					CML_OPT_TYPE = type;
			}
			if (cmd.hasOption("title")) {
				String title = cmd.getOptionValue("title");
				if (title != null && !title.isEmpty())
					CML_OPT_TITLE = title;
			}
			if (cmd.hasOption("eancode")) {
				String eancode = cmd.getOptionValue("eancode");
				if (eancode != null && !eancode.isEmpty())
					CML_OPT_EANCODE = eancode;
			}
			if (cmd.hasOption("regnr")) {
				String regnr = cmd.getOptionValue("regnr");
				if (regnr != null && !regnr.isEmpty())
					CML_OPT_REGNR = regnr;
			}
		} catch (ParseException e) {
			System.err.println("Parsing failed: " + e.getMessage());
		}
	}

	private static boolean commandLineOptionsProvided() {
		return (!CML_OPT_TYPE.isEmpty() && (!CML_OPT_TITLE.isEmpty()
				|| !CML_OPT_EANCODE.isEmpty() || !CML_OPT_REGNR.isEmpty() || CML_OPT_SERVER == true));
	}
	
	private static char getUserClass() {
		String uc[] = m_map_ibsa_glns.get(m_customer_gln_code).split(";");
		char user_class = uc[0].charAt(0);
		// Fallback!
		if (uc.length>1) 
			user_class = uc[1].charAt(0);
		return user_class;
	}
	
	/*
	 * MAIN function
	 */
	public static void main(String[] args) {

		// Initialize globales
		m_application_data_folder = Utilities.appDataFolder();
		favorite_meds_set = new HashSet<String>();
		favorite_data = new DataStore(m_application_data_folder);
		favorite_meds_set = favorite_data.load(); // HashSet containing registration numbers

		// Register toolkit
		Toolkit tk = Toolkit.getDefaultToolkit();
		tk.addAWTEventListener(WindowSaver.getInstance(m_application_data_folder), AWTEvent.WINDOW_EVENT_MASK);

		// Specify command line options
		Options options = new Options();
		addOption(options, "help", "print this message", false, false);
		addOption(options, "version", "print the version information and exit",	false, false);
		addOption(options, "port", "starts AmiKo-server at given port", true, false);
		addOption(options, "width", "sets window width", true, false);
		addOption(options, "height", "sets window height", true, false);
		addOption(options, "lang", "use given language", true, false);
		addOption(options, "type", "start light or full app", true, false);
		addOption(options, "title", "display medical info related to given title", true, false);
		addOption(options, "eancode", "display medical info related to given 13-digit ean-code", true, false);
		addOption(options, "regnr", "display medical info related to given 5-digit registration number", true, false);
		// Activate command line parser
		commandLineParse(options, args);

		// Initialize language files
		if (Utilities.appLanguage().equals("de"))
			m_rb = ResourceBundle.getBundle("amiko_de_CH", new Locale("de", "CH"));
		else if (Utilities.appLanguage().equals("fr"))
			m_rb = ResourceBundle.getBundle("amiko_fr_CH", new Locale("fr", "CH"));

		new Thread() {
			@Override
			public void run() {
				if (Utilities.appCustomization().equals("desitin")) {
					new SplashWindow(Constants.APP_NAME, 5000);
				} else if (Utilities.appCustomization().equals("meddrugs")) {
					new SplashWindow(Constants.APP_NAME, 5000);
				} else if (Utilities.appCustomization().equals("zurrose")) {
					new SplashWindow(Constants.APP_NAME, 3000);
				} else if (Utilities.appCustomization().equals("ibsa")) {
					new SplashWindow(Constants.APP_NAME, 5000);			
				}
			}
		}.start();
		
		// Load javascript
		String jscript_str = FileOps.readFromFile(Constants.JS_FOLDER + "main_callbacks.js");
		m_jscript_str = "<script language=\"javascript\">" + jscript_str + "</script>";

		// Load css style sheet
		m_css_str = "<style>" + FileOps.readFromFile(Constants.CSS_SHEET) + "</style>";

		// Load main database
		m_sqldb = new MainSqlDb();
		// Attempt to load alternative database. if db does not exist, load default database
		// These databases are NEVER zipped!
		if (m_sqldb.loadDBFromPath(m_application_data_folder + "\\" + Constants.DEFAULT_AMIKO_DB_BASE + Utilities.appLanguage() + ".db") == 0) {
			System.out.println("Loading default amiko database");
			if (Utilities.appLanguage().equals("de"))
				m_sqldb.loadDB("de");
			else if (Utilities.appLanguage().equals("fr"))
				m_sqldb.loadDB("fr");
		}

		// Load rose database
		if (Utilities.appCustomization().equals("zurrose")) {
			m_rosedb = new RoseSqlDb();
			if (m_rosedb.loadDBFromPath(m_application_data_folder + "\\" + Constants.ROSE_DB_NEW) == 0) {
				System.out.println("Loading default rose db");
				m_rosedb.loadDB();
			}
		}
		
		// Initialize update class
		m_maindb_update = new UpdateDb(m_sqldb);

		// Create shop folder in application data folder
		File wdir = new File(m_application_data_folder + "\\shop");
		if (!wdir.exists())
			wdir.mkdirs();
		
		// Load interaction cart
		m_interactions_cart = new InteractionsCart();
		
		// Preferences
		m_prefs = Preferences.userRoot().node(SettingsPage.class.getName());
		
		// Get gln code
		m_customer_gln_code = m_prefs.get("glncode", "7601000000000");

		// Load all modules-related files
		FileLoader file_loader = new FileLoader();
		
		if (Utilities.appCustomization().equals("ibsa")) {
			// Load shop related files for ibsa
			m_user_map = file_loader.loadGlnCodes("gln_codes.ser");		// maps gln -> user address
			m_list_of_authors = file_loader.loadAuthors();
			m_map_ibsa_conditions = file_loader.loadIbsaConditions();	// maps gln -> customer conditions
			m_map_ibsa_glns = file_loader.loadIbsaGlns();				// maps gln -> customer category	
			// Create shopping cart and load related files
			m_shopping_cart = new ShoppingIbsa();
			m_shopping_cart.setMaps(m_map_ibsa_glns, m_map_ibsa_conditions);
			m_emailer = new Emailer(m_rb);
		} else if (Utilities.appCustomization().equals("desitin")) {
			// Load shop related files for desitin			
			m_user_map = file_loader.loadGlnCodes("desitin_gln_codes.ser");	// maps gln -> user address
			m_list_of_authors = file_loader.loadAuthors();		
			m_map_desitin_conditions = file_loader.loadDesitinConditions();	// maps gln -> customer conditions	
			// Create shopping cart and load pertinent files
			m_shopping_cart = new ShoppingDesitin();
			if (m_shopping_cart!=null) {
				if (m_user_map!=null && m_user_map.containsKey(m_customer_gln_code+"S")) {
					User user = m_user_map.get(m_customer_gln_code+"S");	// using only m_customer_gln_code leads to nullpointer exception!
					m_shopping_cart.setUserCategory(user.category);
				}	
				if (m_map_desitin_conditions.containsKey(m_customer_gln_code)) {
					m_shopping_cart.setMap(m_map_desitin_conditions.get(m_customer_gln_code));
				}
			}
			m_emailer = new Emailer(m_rb);
		} else if (Utilities.appCustomization().equals("zurrose")) {
			// Load files
			m_user_map = file_loader.loadRoseUserMap("rose_conditions.ser");	
			// Create comparison cart and load related files
			m_comparison_cart = new ComparisonCart();
			// Create shopping cart
			m_shopping_cart = new ShoppingRose();
			if (m_shopping_cart!=null) {
				if (m_user_map.containsKey(m_customer_gln_code)) {
					LinkedHashMap<String, Float> rebate_map = m_user_map.get(m_customer_gln_code).rebate_map;
					LinkedHashMap<String, Float> expenses_map = m_user_map.get(m_customer_gln_code).expenses_map;
					m_shopping_cart.setMaps(rebate_map, expenses_map);
				}
			}
		}
				
		// Set default query type
		m_curr_uistate.setQueryType(m_query_type = NAME);
		
		// UIUtils.setPreferredLookAn dFeel();
		NativeInterface.open();
		NativeSwing.initialize();

		// Setup font size based on screen size
		if (!Utilities.appCustomization().equals("ibsa")) {
			UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Dialog", Font.PLAIN, 14));	
			UIManager.put("Label.font", new Font("Dialog", Font.PLAIN, 12));
			UIManager.put("CheckBox.font", new Font("Dialog", Font.PLAIN, 12));
			UIManager.put("Button.font", new Font("Dialog", Font.BOLD, 14));
			UIManager.put("ToggleButton.font", new Font("Dialog", Font.BOLD, 14));
			UIManager.put("Menu.font", new Font("Dialog", Font.PLAIN, 12));
			UIManager.put("MenuBar.font", new Font("Dialog", Font.PLAIN, 12));
			UIManager.put("MenuItem.font", new Font("Dialog", Font.PLAIN, 12));
			UIManager.put("ToolBar.font", new Font("Dialog", Font.PLAIN, 12));				
		} else {
			try {				
	            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	            // Create the font to use. Specify the size!
	            m_custom_font = Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\AvenirLTPro-Light.ttf"));            
	            // Register the font
	            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\AvenirLTPro-Light.ttf")));
				UIManager.getLookAndFeelDefaults().put("defaultFont", m_custom_font);
				UIManager.put("Label.font", m_custom_font.deriveFont(Font.PLAIN, 12));
				UIManager.put("CheckBox.font", m_custom_font.deriveFont(Font.PLAIN, 12));
				UIManager.put("Button.font", m_custom_font.deriveFont(Font.BOLD, 14));
				UIManager.put("ToggleButton.font", m_custom_font.deriveFont(Font.BOLD, 14));
				UIManager.put("Menu.font", m_custom_font.deriveFont(Font.PLAIN, 12));
				UIManager.put("MenuBar.font", m_custom_font.deriveFont(Font.PLAIN, 12));
				UIManager.put("MenuItem.font",m_custom_font.deriveFont(Font.PLAIN, 12));
				UIManager.put("ToolBar.font", m_custom_font.deriveFont(Font.PLAIN, 12));		
	            // setUIFont(new FontUIResource(customFont));
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch(FontFormatException e) {
	            e.printStackTrace();
	        }		
		}
		// Setup colors
		UIManager.put("ToggleButton.select", m_selected_but_color);
		
		// Schedule a job for the event-dispatching thread.
		// This job creates and shows this application's GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!commandLineOptionsProvided()) {
					System.out.println("No relevant command line options provided... creating full GUI");
					createAndShowFullGUI();
				} else if (CML_OPT_TYPE.equals("full")) {
					System.out.println("Creating full GUI");
					createAndShowFullGUI();
				} else if (CML_OPT_TYPE.equals("light")) {
					System.out.println("Creating light GUI");
					createAndShowLightGUI();
				}
			}
		});

		NativeInterface.runEventPump();
	}

	static class CheckListRenderer extends JCheckBox implements ListCellRenderer<Object> {

		final static Icon imgFavNotSelected = new ImageIcon(Constants.IMG_FOLDER + "28-star-gy.png");
		final static Icon imgFavSelected = new ImageIcon(Constants.IMG_FOLDER + "28-star-ye.png");
		final static Icon imgATCExists = new ImageIcon(Constants.IMG_FOLDER + "atc_exists_icon.png");
		final static Icon imgNoATC = new ImageIcon(Constants.IMG_FOLDER + "empty_icon_16.png");
		final static Icon imgCustomer = new ImageIcon(Constants.IMG_FOLDER + "customer_icon.png");
		final static Icon imgNoCustomer = new ImageIcon(Constants.IMG_FOLDER + "empty_icon_16.png");
		
		public CheckListRenderer() {
			setOpaque(true);
		}

		/*
		 * Method called when it's time to draw each cell Returns the specific
		 * rendering for that one cell of the JList (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
		 * .JList, java.lang.Object, int, boolean, boolean)
		 * 
		 * Note: Swing insists on accessing each item in the entire ListModel
		 * while getting it displayed on screen. Furthermore, after accessing
		 * all the items, Swing then re-accesses the first n number of items
		 * visible on screen (in the viewport, not off screen below).
		 */
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected, boolean hasFocus) {
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setText(value.toString());

			if (isSelected) {
				setBackground(m_list_selected_color);
				setForeground(list.getForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			if (!m_curr_uistate.isComparisonMode() && !m_curr_uistate.isCustomerSearchMode()) {
				// Extract registration number corresponding to index
				if (index < med_search.size()) {
					String regnrs = med_search.get(index).getRegnrs();
					if (favorite_meds_set.contains(regnrs))
						setIcon(imgFavSelected);
					else
						setIcon(imgFavNotSelected);
				}
			} else if (m_curr_uistate.isComparisonMode()){
				if (index < rose_search.size()) {
					String atc = rose_search.get(index).getAtcCode();
					if (!atc.equals("k.A."))
						setIcon(imgATCExists);
					else
						setIcon(imgNoATC);
				}
			} else if (m_curr_uistate.isCustomerSearchMode()) {
				if (index < customer_search.size()) {
					String gln_code = customer_search.get(index).gln_code;
					if (m_map_ibsa_glns.containsKey(gln_code))
						setIcon(imgCustomer);
					else
						setIcon(imgNoCustomer);
				}
			}
			
			// Set position of the star
			setVerticalTextPosition(SwingConstants.TOP);

			return this;
		}
	}

	static class CustomListModel extends AbstractListModel<String> {

		List<String> model = new ArrayList<>();

		public CustomListModel(List<String> lStr) {
			model.addAll(lStr);
		}

		public void addElement(String s) {
			model.add(s);
			this.fireContentsChanged(this, model.size() - 1, model.size() - 1);
		}

		@Override
		public int getSize() {
			return model.size();
		}

		@Override
		public String getElementAt(int index) {
			return model.get(index);
		}
	}

	/**
	 * This is the panel on the left side displaying the results of the search
	 * 
	 * @author Max
	 * 
	 */
	static class ListPanel extends JPanel implements ListSelectionListener, FocusListener {

		private JList<String> list = null;
		private JScrollPane jscroll = null;

		public ListPanel() {
			super(new BorderLayout());

			// String[] titles = med_title.toArray(new
			// String[med_title.size()]);
			// list = new JList<String>(titles);

			DefaultListModel<String> model = new DefaultListModel<String>();
			list = new JList<String>(model);
			list.setSelectedIndex(0);
			list.setCellRenderer(new CheckListRenderer());
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectionBackground(m_list_selected_color);
			list.setSelectionForeground(Color.BLACK);
			if (!Utilities.appCustomization().equals("ibsa"))
				list.setFont(new Font("Dialog", Font.PLAIN, 14));
			else
				list.setFont(m_custom_font.deriveFont(Font.PLAIN, 14));
			list.addListSelectionListener(this);

			// Implements "starring" mechanism (captures clicks)
			MouseListener mouseListener = new MouseAdapter() {
				public void mouseClicked(MouseEvent mouseEvent) {
					// JList theList = (JList) mouseEvent.getSource();
					if (!m_curr_uistate.isComparisonMode() && !m_curr_uistate.isCustomerSearchMode()) {
						if (mouseEvent.getClickCount()==1) {
							int index = list.locationToIndex(mouseEvent.getPoint());
							if (index>=0 && mouseEvent.getX()<32) {
								// Note: extracts a String... could be optimized to an array of ints in the future!
								String regnrs = med_search.get(index).getRegnrs();
								if (favorite_meds_set.contains(regnrs))
									favorite_meds_set.remove(regnrs);
								else
									favorite_meds_set.add(regnrs);
								favorite_data.save(favorite_meds_set);
								repaint();
							}
						}
					} 
				}
			};
			list.addMouseListener(mouseListener);

			JPanel listPanel = new JPanel(new BorderLayout());
			Font title_font = new Font("Dialog", Font.PLAIN, 14);
			if (Utilities.appCustomization().equals("ibsa"))
				title_font = m_custom_font.deriveFont(Font.PLAIN, 14);
			TitledBorder titledBorder = BorderFactory.createTitledBorder(null,
					m_rb.getString("result"), TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, title_font);
			listPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));

			// Add list to a scrolling panel
			jscroll = new JScrollPane(list);
			jscroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			listPanel.add(jscroll, BorderLayout.CENTER);
			add(listPanel, BorderLayout.CENTER);

			setFocusable(true);
			requestFocusInWindow();
		}

		/**
		 * Updates the data in the ListPanel
		 * 
		 * @param lStr
		 */
		public void update(List<String> lStr) {
			CustomListModel dlm = new CustomListModel(lStr);

			if (lStr.size() > BigCellNumber)
				list.setPrototypeCellValue(dlm.getElementAt(0));
			else {
				list.setPrototypeCellValue(null); // Does not work!
				list.setFixedCellHeight(-1);
			}

			list.setModel(dlm);
			jscroll.revalidate();
			jscroll.repaint();
		}

		/**
		 * Called any time the user selects an item from the list
		 */
		public void valueChanged(ListSelectionEvent e) {
			if (m_curr_uistate.isLoadCart())
				m_curr_uistate.restoreUseMode();
			if (e.getSource() == list && !e.getValueIsAdjusting()) {
				prev_med_index = med_index; // Store current index
				med_index = list.getSelectedIndex(); // Returns -1 if there is no selection
				/*
				 * if (med_index<0 && prev_med_index>=0)
				 * list.setSelectedIndex(prev_med_index);
				 */
				if (m_curr_uistate.isCustomerSearchMode())
					m_web_panel.updateCustomerInfo();			
				else if (m_curr_uistate.isInteractionsMode()) 	// Display interaction cart
					m_web_panel.updateInteractionsCart();
				else if (m_curr_uistate.isShoppingMode()) {		// Display shopping cart
					if (Utilities.isRoseShoppingApp()) 
						m_web_panel.updateRoseShoppingCart();
					else
						m_web_panel.updateListOfPackages();
				}
				else if (m_curr_uistate.isComparisonMode())
					m_web_panel.updateComparisonCart();
				else	
					m_web_panel.updateText();	// Display Fachinformation, default usage!
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
			this.requestFocus();
			System.out.println("focus gained");
		}

		@Override
		public void focusLost(FocusEvent e) {
			System.out.println("focus lost");
		}
	}

	/**
	 * This is the middle pane class
	 * 
	 * @author Max
	 * 
	 */
	static class MiddlePane extends JPanel implements ListSelectionListener {

		private JList<String> list = null;
		private JScrollPane jscroll = null;
		
		private boolean is_assort = false;

		public MiddlePane(String[] sec_titles) {
			super(new BorderLayout());

			list = new JList<String>(sec_titles);
			// list.setSelectedIndex(0);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectionBackground(Color.BLUE);
			list.setSelectionForeground(Color.WHITE);
			if (!Utilities.appCustomization().equals("ibsa"))
				list.setFont(new Font("Dialog", Font.PLAIN, 13));
			else
				list.setFont(m_custom_font.deriveFont(Font.PLAIN, 13));
			list.addListSelectionListener(this);

			JPanel listPanel = new JPanel(new BorderLayout());
			listPanel.setBorder(null);

			jscroll = new JScrollPane(list);
			jscroll.setBorder(null);
			jscroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			listPanel.add(jscroll, BorderLayout.CENTER);
			add(listPanel, BorderLayout.CENTER);
		}

		public void update(final String[] pane_entries) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (pane_entries!=null) {
						list.removeAll();
						list.setListData(pane_entries);
						if (pane_entries[0].equals(m_rb.getString("assortart")) || pane_entries[0].equals(m_rb.getString("negassort")))
							is_assort = true;
						else
							is_assort = false;
						if (m_curr_uistate.isInteractionsMode())
							m_section_str = Arrays.asList(pane_entries);
					}
				}
			});
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				int sel_index = list.getSelectedIndex();
				if (sel_index>=0) {
					if (!m_curr_uistate.isComparisonMode() && !m_curr_uistate.isShoppingMode()) {
						m_web_panel.moveToAnchor(m_section_str.get(sel_index));
					} else if (m_curr_uistate.isComparisonMode()) {
						// Do nothing
					} else if (m_curr_uistate.isShoppingMode()){
						if ((m_curr_uistate.isLoadCart() || m_query_type==CUSTOMER) && !m_curr_uistate.isAssortList()) {
							String cart_path = m_application_data_folder + "\\shop\\";
							if (!m_customer_gln_code.isEmpty() && m_query_type==CUSTOMER)
								cart_path += (m_customer_gln_code + "\\");										
							File file = new File(cart_path);
							if (file.exists() && file.isDirectory() && file.list().length>0) {
								// Load and deserialize m_shopping_basket
								String path = cart_path + list_of_carts.get(sel_index) + ".ser";
								byte[] serialized_bytes = FileOps.readBytesFromFile(path);
								if (serialized_bytes!=null) {
									m_shopping_basket = (LinkedHashMap<String, Article>) FileOps.deserialize(serialized_bytes);
									if (m_shopping_basket==null) {
										System.out.println("Shopping cart "	+ list_of_carts.get(sel_index) + ".ser is corrupted... deleting it!");
										file.delete();
										m_shopping_basket = new LinkedHashMap<String, Article>();
									}
									m_web_panel.updateShoppingHtml();
								}
							}
						} else {							
							if (list_of_articles.size()>0 && sel_index<=list_of_articles.size()) {	
								boolean updateBasket = false;
								Article article = null;
								if (is_assort) {
									if (sel_index>0)
										article = list_of_articles.get(sel_index-1);
									else
										return;
								} else
									article = list_of_articles.get(sel_index);
								//
								String ean_code = article.getEanCode();
								m_curr_regnr = ean_code;
																
	                            if (ean_code!=null) {	                                	                            		                            	                          	
									// If ean code is already in shopping basket...      
	                            	if (m_shopping_basket.containsKey(ean_code)) {
	                            		article = m_shopping_basket.get(ean_code);
	                            		article.incrementQuantity();
	                                    updateBasket = true;
	                            	} else {
	                            		// If categories exist, we can assume that the article is kosher
	                            		if (!article.getCategories().isEmpty()) {
	                            			updateBasket = true;
	                            		} else {							
	                            			// We have selected an assorted article which has only a very basic amount of info
											List<Medication> med_list = m_sqldb.searchEanCode(ean_code);
											assert(med_list.size()==1);
											// Assumption: one ean code corresponds to one med
											Medication m = med_list.get(0);
											// Get its packages
											String[] packages = m.getPackages().split("\n");
											if (packages!=null) {
												// user/customer categories are defined in aips2sqlite:glncodes.java
												String user_category = m_prefs.get("type", "arzt");
												// Loop through all packages and find the right one, add it to the basket
												for (String p : packages) {
													if (!p.isEmpty() && p.contains(ean_code)) {
														String[] entry = p.split("\\|");
														Article a = new Article(entry, m.getAuth());
														a.setRegnr(m.getRegnrs());
														article = a;
														if (a.isVisible(user_category) && a.hasPrice(user_category)) {
															a.setQuantity(1);
															updateBasket = true;
															break;	// Stop for loop to save time...
														}
													}
												}
											}
	                            		}
	                            	}

	                            	
	                            	// Remember regnr globally! Used when switching back to "Kompendium"
									m_curr_regnr = article.getRegnr();	 
									
	                            	if (updateBasket==true) {
                            			// We have selected a standard / non-assorted article
										m_shopping_basket.put(ean_code, article);
										// Update shopping basket
										m_shopping_cart.setShoppingBasket(m_shopping_basket);
										m_web_panel.updateShoppingHtml();
	                            	}
	                            }                        	
							}
						}
					}
				}
			}
		}
	}

	static class TreePanel extends JPanel implements TreeSelectionListener {

		private JTree jtree = null;
		private List<String> titles = null;
		private String[] entries = null;
		private JScrollPane jscroll = null;

		public TreePanel() {
			titles = new ArrayList<String>();
			for (int i = 0; i < med_title.size(); ++i) {
				titles.add(med_title.get(i).getTitle());
			}

			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Fach Infos (DE)");
			DefaultMutableTreeNode child;
			DefaultMutableTreeNode grandChild;
			for (char childStart = 'A'; childStart <= 'Z'; childStart++) {
				child = new DefaultMutableTreeNode(childStart);
				root.add(child);
				for (int grandChildIndex = 0; grandChildIndex < titles.size(); grandChildIndex++) {
					if (titles.get(grandChildIndex).startsWith(
							Character.toString(childStart))) {
						grandChild = new DefaultMutableTreeNode(titles.get(grandChildIndex));
						child.add(grandChild);
					}
				}
			}

			jtree = new JTree(root);
			jtree.addTreeSelectionListener(this);

			jscroll = new JScrollPane(jtree);
			// jscroll.setPreferredSize(new Dimension(320, 640));
			add(jscroll);
		}

		public void update(List<String> lStr, String name_tree_node) {
			entries = lStr.toArray(new String[lStr.size()]);

			DefaultTreeModel model = (DefaultTreeModel) jtree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
			root.removeAllChildren();
			DefaultMutableTreeNode child;
			DefaultMutableTreeNode grandChild;
			for (char childStart = 'A'; childStart <= 'Z'; childStart++) {
				child = new DefaultMutableTreeNode(childStart);
				root.add(child);
				for (int grandChildIndex = 0; grandChildIndex < entries.length; grandChildIndex++) {
					if (entries[grandChildIndex].startsWith(Character.toString(childStart))) {
						grandChild = new DefaultMutableTreeNode(entries[grandChildIndex]);
						child.add(grandChild);
					}
				}
			}

			model.reload(root);

			jscroll.revalidate();
			jscroll.repaint();
		}

		public void valueChanged(TreeSelectionEvent e) {
			// Find med_index
			for (int i = 0; i < entries.length; ++i) {
				if (entries[i].equals(jtree.getLastSelectedPathComponent().toString())) {
					med_index = i;
					break;
				}
			}
			m_web_panel.updateText();
		}
	}

	/**
	 * This is the web (html) panel class
	 * 
	 * @author Max
	 * 
	 */
	static class WebPanel2 extends JPanel {

		private JWebBrowser jWeb = null;
		private StringBuffer content_str = null;
		private TitledBorder titledBorder = null;
		private JPanel webBrowserPanel = null;
		
		public WebPanel2() {
			// YET another mega-hack ;)
			super(new BorderLayout());
			webBrowserPanel = new JPanel(new BorderLayout());
			Font title_font = new Font("Dialog", Font.PLAIN, 14);
			if (Utilities.appCustomization().equals("ibsa"))
				title_font = m_custom_font.deriveFont(Font.PLAIN, 14);
			titledBorder = BorderFactory.createTitledBorder(null, m_rb.getString("fachinfo"), 
					TitledBorder.DEFAULT_JUSTIFICATION,	TitledBorder.DEFAULT_POSITION, title_font);
			webBrowserPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));
			jWeb = new JWebBrowser(NSComponentOptions.destroyOnFinalization());

			/**
			 * Add function called by javascript This trick fools JWebBrowser
			 * lack of javascript return values (kinda cumbersome...)
			 */
			jWeb.registerFunction(new WebBrowserFunction("invokeJava") {
				@Override
				public Object invoke(JWebBrowser webBrowser, Object... args) {
					String msg = args[0].toString().trim();
					String row_key = args[1].toString().trim();		// Typically: a first ean code
					// Uncomment following line for debug purposes...
					System.out.println(getName() + " -> msg = " + msg + " / key = " + row_key);
					//
					if (m_curr_uistate.isInteractionsMode()) {
						if (msg.equals("delete_all"))
							m_med_basket.clear();
						else if (msg.equals("delete_row"))
							m_med_basket.remove(row_key);
						m_web_panel.updateInteractionsHtml();
					} else if (m_curr_uistate.isComparisonMode()) {
						if (msg.equals("sort_cart")) {
							int type = (int) Float.parseFloat(row_key);
							m_comparison_cart.sortCart(type, true);
							m_web_panel.updateComparisonCartHtml();
						} else if (msg.equals("show_all")) {
							m_compare_show_all = !m_compare_show_all;
							m_web_panel.updateComparisonCart();
						} else if (msg.equals("pharma_code")) {
							// Copy the clipboard
							StringSelection stringSelection = new StringSelection (row_key);
							Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
							clpbrd.setContents (stringSelection, null);
						} else if (msg.equals("upload_article")) {
							m_comparison_cart.updateUploadList(row_key);
							m_web_panel.updateComparisonCartHtml();
						} else if (msg.equals("upload_to_server")) {
							m_comparison_cart.uploadToServer();
						}
					} else if (m_curr_uistate.isShoppingMode()) {
						if (msg.equals("delete_all")) {
							m_shopping_basket.clear();
							m_map_similar_articles.clear();
							// Update shopping basket
							m_shopping_cart.saveWithIndex(m_shopping_basket);
							m_web_panel.updateShoppingHtml();
						} else if (msg.equals("delete_row")) {
							Article article = m_shopping_basket.get(row_key);
							article.setQuantity(1);
							m_shopping_basket.remove(row_key);
							m_map_similar_articles.remove(row_key);
							// Update shopping basket
							m_shopping_cart.saveWithIndex(m_shopping_basket);
							m_web_panel.updateShoppingHtml();
						} else if (msg.equals("show_all")) {
							if (Utilities.isRoseShoppingApp()) {
								m_compare_show_all = !m_compare_show_all;
								m_shopping_cart.setFilterState(m_compare_show_all);
								m_shopping_cart.updateMapSimilarArticles(m_map_similar_articles);
								m_web_panel.updateShoppingHtml();
							}
						} else if (msg.startsWith("select_article")) {
							if (Utilities.isRoseShoppingApp()) {
								String ean_to_be_selected = msg.replace("select_article", "");
								m_shopping_cart.updateSelectList(ean_to_be_selected + "-" + row_key);
								Article article = m_shopping_basket.get(row_key);
								updateShoppingCart(row_key, article);
								// m_web_panel.updateShoppingHtml();
 							}
						} else if (msg.startsWith("swap_articles")) {
							if (Utilities.isRoseShoppingApp()) {
								// This is the article that will be replaced!
								String ean_to_be_swapped = msg.replace("swap_articles", "");
								// Extract information about article to be swapped
								Article article_to_be_swapped = m_shopping_basket.get(ean_to_be_swapped);
								int quantity = 1;
								if (article_to_be_swapped!=null)
									quantity = article_to_be_swapped.getQuantity();
								// Search selected similar article in rose db
								List<Article> la = m_rosedb.searchEan(row_key);							
								Article sim_article = la.get(0);
								// This is its ean code
								String sim_ean = sim_article.getEanCode();
								sim_article.setQuantity(quantity);
								// Find index of article that will be swapped
								ArrayList<String> sim_pos = new ArrayList<String>(m_shopping_basket.keySet());
								int idx = sim_pos.indexOf(ean_to_be_swapped);
								assert(sim_ean.equals(row_key));
								// Generate temporary shopping basket 
								LinkedHashMap<String, Article> tmp_shopping_basket = new LinkedHashMap<String, Article>(m_shopping_basket);
								m_shopping_basket.clear();
								// Fill new map
								int index = 0;
								for (Map.Entry<String, Article> entry : tmp_shopping_basket.entrySet()) {
									if (index!=idx)
										m_shopping_basket.put(entry.getKey(), entry.getValue());
									else
										m_shopping_basket.put(sim_ean, sim_article);
									index++;
								}	
								// Find all similar articles
								LinkedList<Article> sa = listSimilarArticles(sim_article);
								if (sa!=null) {		
									if (m_map_similar_articles.containsKey(sim_ean))
										m_map_similar_articles.remove(sim_ean);
									// Check if article to be swapped is in list of similar articles, if not add.
									boolean in_list = false;
									for (Article a : sa) {
										if (a.getEanCode().equals(ean_to_be_swapped)) {
											in_list = true;
											break;
										}
									}
									if (!in_list)
										sa.add(article_to_be_swapped);
									m_map_similar_articles.put(sim_ean, sa);
									m_shopping_cart.updateMapSimilarArticles(m_map_similar_articles);
								}										
								// Update shopping basket
								m_shopping_cart.setShoppingBasket(m_shopping_basket);
								m_web_panel.updateShoppingHtml();
							}
						} else if (msg.startsWith("change_marge")) {
							int marge = Integer.parseInt(row_key.trim());
							if (marge >= 0) {
								m_shopping_cart.setMarginPercent(marge);
								// Loop through all medis and update
								for (Map.Entry<String, Article> entry : m_shopping_basket.entrySet()) {
									Article article = entry.getValue();
									if (!article.isSpecial()) {
										String ean_code = article.getEanCode();
										article.setMargin(marge / 100.0f);
										updateShoppingCart(ean_code, article);
									}
								}
							}
						} else if (msg.startsWith("change_qty")) {
							if (m_shopping_basket.containsKey(row_key)) {
								Article article = m_shopping_basket.get(row_key);
								int quantity = Integer.parseInt(msg.replaceAll("change_qty", "").trim());
								article.setQuantity(quantity);
								m_shopping_basket.put(row_key, article);
								// Update shopping basket
								m_shopping_cart.setShoppingBasket(m_shopping_basket);
								// m_web_panel.updateShoppingHtml();
								updateShoppingCart(row_key, article);
								// @maxl 20.Jan.2016 - the following line makes everything more dynamic
								updateShoppingHtml();
							}
						} else if (msg.startsWith("assort_list")) {
							m_curr_uistate.setUseMode("assortlist");
							Map<String, String> assorted_articles = m_shopping_cart.getAssortedArticles(row_key);
							list_of_articles.clear();
							for (Map.Entry<String, String> entry : assorted_articles.entrySet()) {
								Article article = new Article();
								article.setPackTitle(entry.getValue());
								article.setEanCode(entry.getKey());
								article.setQuantity(1);
								list_of_articles.add(article);
							}
							// Update mid pane entries
							List<String> list_of_packages = new ArrayList<String>();
							if (list_of_articles.size()>0) {
								list_of_packages.add(m_rb.getString("assortart"));								
								for (Article a : list_of_articles)
									list_of_packages.add(a.getPackTitle());
							} else {
								list_of_packages.add(m_rb.getString("negassort"));
							}
							String[] packages = list_of_packages.toArray(new String[list_of_packages.size()]);
							m_middle_pane.update(packages);
						} else if (msg.equals("load_cart")) {
							if (row_key.equals("0.0")) {
								// List all old shopping carts in the central pane
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										m_curr_uistate.setUseMode("loadcart");
										// List of all files in directory
										String cart_path = m_application_data_folder + "\\shop";
										if (!m_customer_gln_code.isEmpty() && m_query_type==CUSTOMER)
											cart_path += ("\\" + m_customer_gln_code);										
										list_of_carts = listCartsInFolder(cart_path);
										String[] file_str = list_of_carts.toArray(new String[list_of_carts.size()]);
										// Update middle pane
										m_middle_pane.update(file_str);
									}
								});
								m_web_panel.updateShoppingHtml();
							} else {
								// Save old cart with index = index
								m_shopping_cart.saveWithIndex(m_shopping_basket);
								// Load new cart
								int index = (int) (Float.parseFloat(row_key));
								m_shopping_basket.clear();
								m_map_similar_articles.clear();
								// m_shopping_cart.setShoppingBasket(m_shopping_basket);
								m_shopping_cart.setCartIndex(index);
								loadShoppingCartWithIndex(index);
							}
						} else if (msg.equals("create_pdf")) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									// Open file chooser
									JFileChooser fc = FileOps.getFileChooser(m_rb.getString("saveOrder"), ".pdf", "*.pdf");
									fc.setSelectedFile(new File(orderFileName()	+ ".pdf"));
									if (fc != null) {
										int r = fc.showSaveDialog(jWeb);
										if (r == JFileChooser.APPROVE_OPTION) {
											String filename = fc.getSelectedFile().getPath();
											SaveBasket sbasket = new SaveBasket(m_shopping_cart);
											sbasket.generatePdf(null, filename,	"all");
										}
									}
									m_web_panel.updateShoppingHtml();
								}
							});
							m_web_panel.updateShoppingHtml();
						} else if (msg.equals("create_csv")) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									// Open file chooser
									JFileChooser fc = FileOps.getFileChooser(m_rb.getString("saveOrder"), ".csv", "*.csv");
									fc.setSelectedFile(new File(orderFileName()	+ ".csv"));
									if (fc != null) {
										int r = fc.showSaveDialog(jWeb);
										if (r == JFileChooser.APPROVE_OPTION) {
											String filename = fc.getSelectedFile().getPath();
											SaveBasket sbasket = new SaveBasket(m_shopping_cart);
											sbasket.generateCsv(null, filename,	"all");
										}
									}
									m_web_panel.updateShoppingHtml();
								}
							});
							m_web_panel.updateShoppingHtml();
						} else if (msg.startsWith("change_shipping")) {
							// Extract shipping type (known types: AZ, BZ, A, B, and E)
							String shipping_type = msg.replace("change_shipping", "");
							// System.out.println(row_key + " -> " + shipping_type);
							updateCheckoutTable(row_key, shipping_type);
						} else if (msg.equals("check_out")) {
							// Get shipping and billing address (if they exist)		
							if (m_user_map!=null) {
								m_address_map = new HashMap<String, Address>();
								if (m_user_map.containsKey(m_customer_gln_code+"S"))
									m_address_map.put("S", new Address(m_user_map.get(m_customer_gln_code+"S")));
								if (m_user_map.containsKey(m_customer_gln_code+"B"))
									m_address_map.put("B", new Address(m_user_map.get(m_customer_gln_code+"B")));
								if (m_user_map.containsKey(m_customer_gln_code+"O"))
									m_address_map.put("O", new Address(m_user_map.get(m_customer_gln_code+"O")));	
							}
							// Save shopping basket
							m_shopping_cart.setCustomerGlnCode(m_customer_gln_code);
							m_shopping_cart.saveWithIndex(m_shopping_basket);
							// Set AGB checkbox
							m_shopping_cart.setAgbsAccepted(false);
							// Update...
							m_web_panel.showCheckoutHtml();
						} else if (msg.equals("change_address")) {
							// Change customer settings / address
							m_settings_page.displayCustomerSettings(row_key, m_customer_gln_code, m_address_map);							
						} else if (msg.equals("agbs_accepted")) {
							boolean a = Boolean.valueOf(row_key);
							m_shopping_cart.setAgbsAccepted(a);
						} else if (msg.equals("show_agbs")) {
							AmiKoDialogs ad = new AmiKoDialogs(Utilities.appLanguage(), Utilities.appCustomization());
							ad.AgbDialog();
						} else if (msg.equals("send_order")) {
							if (m_shopping_cart.getAgbsAccepted() && !m_emailer.isSending()) {
								m_shopping_cart.save(m_shopping_basket);
								SaveBasket sbasket = new SaveBasket(m_shopping_cart);
								// Update authors list with subtotals, vats and shipping costs
								m_list_of_authors = m_shopping_cart.updateAuthors(m_list_of_authors);
								sbasket.setAuthorList(m_list_of_authors);							
								// Information necessary only for user_id = 18
								m_emailer.setCustomer(m_customer_gln_code, m_address_map);
								m_emailer.sendAllOrders(m_list_of_authors, sbasket);
								m_web_panel.updateShoppingHtml();
							}
						}
					}

					if (msg.equals("add_to_shopping_cart")) {
						m_shopping_basket = m_shopping_cart.getShoppingBasket();
						// No cart, load default cart
						if (m_shopping_basket == null) {
							m_shopping_cart.loadWithIndex(-1);
							m_shopping_basket = m_shopping_cart.getShoppingBasket();
						}
						AmiKoDialogs sd = new AmiKoDialogs(Utilities.appLanguage(), Utilities.appCustomization());
						if (m_shopping_basket.containsKey(row_key)) {
							Article article = m_shopping_basket.get(row_key);
							article.incrementQuantity();
							m_shopping_basket.put(row_key, article);
							sd.ShoppingCartDialog(row_key, true, m_rb);
						} else {
							if (med_index >= 0) {
								// user/customer categories are defined in aips2sqlite:glncodes.java
								String user_category = m_prefs.get("type", "arzt");
								// Get full info on selected medication
								long row_id = med_id.get(med_index).get(0);
								Medication m = m_sqldb.getMediWithId(row_id);
								// Get its packages
								String[] packages = m.getPackages().split("\n");
								if (packages != null) {
									// Loop through all packages and find the right one, add it to the basket
									for (int i = 0; i < packages.length; ++i) {
										if (!packages[i].isEmpty() && packages[i].contains(row_key)) {
											String[] entry = packages[i].split("\\|");
											Article article = new Article(entry, m.getAuth());
											if (article.isVisible(user_category) && article.hasPrice(user_category)) {
												article.setQuantity(1);
												m_shopping_basket.put(row_key, article);
												sd.ShoppingCartDialog(row_key, true, m_rb);
											} else {
												sd.ShoppingCartDialog(row_key, false, m_rb);
											}
										}
									}
								}
							}
						}
						m_shopping_cart.setShoppingBasket(m_shopping_basket);
						// m_shopping_cart.printShoppingBasket();
					}
					return "true";
				}
			});

			jWeb.addWebBrowserListener(new WebBrowserAdapter() {
				@Override
				public void locationChanging(WebBrowserNavigationEvent event) {
					final String newResourceLocation = event.getNewResourceLocation();
					if (newResourceLocation.startsWith("https://github.com/") || 
							newResourceLocation.startsWith("http://evidentia.ch")) {
						// Launch default webbrowser
						if (Desktop.isDesktopSupported()) {
							Desktop desktop = Desktop.getDesktop();
							if (desktop.isSupported(Desktop.Action.BROWSE)) {
								try {
									desktop.browse(new URI(event.getNewResourceLocation()));
								} catch(Exception ex) {}
							}
						}
						event.consume();
					}
				}
			});
			
			/*
			jWeb.addMouseWheelListener(new MouseWheelListener() {
				@Override
			    public void mouseWheelMoved(MouseWheelEvent e) {
		            int notches = e.getWheelRotation();
		            System.out.println(notches);
			    }
			});
			*/
			
			/*
			 * JWebBrowserWindow jWebWindow =
			 * WebBrowserWindowFactory.create(jWeb);
			 * jWebWindow.setBarsVisible(false);
			 */
			
			jWeb.setBarsVisible(false);
			webBrowserPanel.add(jWeb, BorderLayout.CENTER);
			// jWeb.setMinimumSize(new Dimension(640, 640));
			// ---> jWeb.setPreferredSize(new Dimension(640, 600));
			// jWeb.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			add(webBrowserPanel, BorderLayout.CENTER);
		}

		
		/*---------------------------------------------------------------------------
		 * KOMPENDIUM
		 */
		public void setTitle(String title) {
			titledBorder.setTitle(title);
			webBrowserPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));
		}

		public void moveToAnchor(String anchor) {
			anchor = anchor.replaceAll("<html>", "").replaceAll("</html>", "")
					.replaceAll(" &rarr; ", "-"); // Spaces before and after of &rarr; are important...
			jWeb.executeJavascript("document.getElementById('" + anchor	+ "').scrollIntoView(true);");
		}

		public void updateSectionTitles(Medication m) {
			// Get section titles
			String[] titles = m.getSectionTitles().split(";");
			// Use abbreviations...
			Locale locale = null;
			String[] section_titles = null;
			if (Utilities.appLanguage().equals("de")) {
				locale = Locale.GERMAN;
				section_titles = Constants.SectionTitle_DE;
			} else if (Utilities.appLanguage().equals("fr")) {
				locale = Locale.FRENCH;
				section_titles = Constants.SectionTitle_FR;
			}
			for (int i = 0; i < titles.length; ++i) {
				for (String s : section_titles) {
					String titleA = titles[i].replaceAll(" ", "");
					String titleB = m.getTitle().replaceAll(" ", "");
					if (titleA.toLowerCase(locale).contains(titleB.toLowerCase(locale))) {
						if (titles[i].contains("®"))
							titles[i] = titles[i].substring(0, titles[i].indexOf("®") + 1);
						else
							titles[i] = titles[i].split(" ")[0].replaceAll("/-", "");
						break;
					} else if (titles[i].toLowerCase(locale).contains(s.toLowerCase(locale))) {
						titles[i] = s;
						break;
					}
				}
			}
			m_middle_pane.update(titles);
		}

		public void updateText() {
			// Set right panel title
			m_web_panel.setTitle(m_rb.getString("fachinfo"));	
			if (med_index >= 0 && med_index < med_id.size()) {
				// Get full info on selected medication
				long row_id = med_id.get(med_index).get(0);
				Medication m = m_sqldb.getMediWithId(row_id);
				// Get section ids
				if (m.getSectionIds()!=null) {
					String[] sections = m.getSectionIds().split(",");
					m_section_str = Arrays.asList(sections);
					// Update section titles
					updateSectionTitles(m);
					// Get FI content
					content_str = new StringBuffer(m.getContent());
					// DateFormat df = new SimpleDateFormat("dd.MM.yy");
					String _amiko_str = Constants.APP_NAME + " - Datenstand AIPS Swissmedic " + Constants.GEN_DATE;
					content_str = content_str.insert(content_str.indexOf("<head>"), "<title>" + _amiko_str + "</title>");
					content_str = content_str.insert(content_str.indexOf("</head>"), m_jscript_str + m_css_str);
					// Enable javascript
					jWeb.setJavascriptEnabled(true);
					
					if (CML_OPT_SERVER == false) {
						try {
							// Currently preferred solution, html saved in C:/Users/ ... folder
							String path_html = System.getProperty("user.home") + "/" + Constants.APP_NAME + "/htmls/";
							String _title = m.getTitle();
							String file_name = _title.replaceAll("[®,/;.]", "_") + ".html";
							FileOps.writeToFile(content_str.toString(),	path_html, file_name, "UTF-16");
							jWeb.navigate("file:///" + path_html + file_name);
						} catch (IOException e) {
							// Fallback solution (used to be preferred implementation)
							jWeb.setHTMLContent(content_str.toString());
						}
					} else {
						// Original fallback solution works well and is fast...
						jWeb.setHTMLContent(content_str.toString());
					}
					
					jWeb.setVisible(true);
				} else {
					m_web_panel.emptyPage();
				}
			} else {
				m_web_panel.noclickPage();
			}
		}
		
		/*---------------------------------------------------------------------------
		 * SHOPPING
		 */
		public String orderFileName() {
			String gln_code = m_prefs.get("glncode", "7610000000000");
			DateTime dT = new DateTime();
			DateTimeFormatter fmt = DateTimeFormat.forPattern("ddMMyyyy'T'HHmmss");
			return (gln_code + "_" + fmt.print(dT));
		}

		public List<String> listCartsInFolder(String folder) {
			// List of all files in directory
			List<String> l_carts = new ArrayList<String>();
			l_carts.clear();	// Clear buffer, just to be on the safe side
			File path = new File(folder);
			if (path.exists()) {				
				File[] files = path.listFiles();
				for (File file : files) {
					if (Utilities.appLanguage().equals("de")) {
						if (file.isFile() && file.getName().startsWith("WK") && file.getName().endsWith(".ser")) {
							String f = file.getName();
							l_carts.add(f.substring(0, f.lastIndexOf(".")));
						}
					} else if (Utilities.appLanguage().equals("fr")) {
						if (file.isFile() && file.getName().startsWith("PA") && file.getName().endsWith(".ser")) {
							String f = file.getName();
							l_carts.add(f.substring(0, f.lastIndexOf(".")));
						}
					}
				}
				// Zeno-style sorting of the old shopping carts
				if (l_carts.size()>0)
					Collections.reverse(l_carts);
			}
			if (l_carts.size()==0)
				l_carts.add(m_rb.getString("noOrders"));
			return l_carts;
		}
		
		public void loadShoppingCartWithIndex(final int n) {
			if (m_shopping_basket==null || m_shopping_basket.size()==0)
				m_shopping_basket = m_shopping_cart.loadWithIndex(n);
			if (Utilities.isRoseShoppingApp()) {
				// Update list of similar articles 
				List<String> keys = new ArrayList<String>(m_shopping_basket.keySet());
				for (String k : keys) {
					Article article = m_shopping_basket.get(k);
					String ean = article.getEanCode();
					LinkedList<Article> la = listSimilarArticles(article);
					if (la!=null) {
						// Check if ean code is already part of the map...
						if (!m_map_similar_articles.containsKey(ean)) {
							m_map_similar_articles.put(ean, la);
							m_shopping_cart.updateMapSimilarArticles(m_map_similar_articles);
						}
					}				
				}
				// Update list of selected articles (only main articles are selected by default)
				ArrayList<String> eans = new ArrayList<String>(m_shopping_basket.keySet());
				for (String ean : eans)
					m_shopping_cart.updateSelectList(ean);
			} 
			updateShoppingHtml();
		}

		public void updateShoppingCart(String ean_code, Article article) {
			// Update shopping cart for ean code
			updateShoppingCartRow(ean_code, article);
			// Update shopping cart table for assorted articles
			if (Utilities.appCustomization().equals("ibsa")) {
				List<String> ean_codes_assorts = m_shopping_cart.getAssortList(ean_code);
				if (ean_codes_assorts != null) {
					for (String ean : ean_codes_assorts) {
						if (m_shopping_basket.containsKey(ean)) {
							Article a = m_shopping_basket.get(ean);
							updateShoppingCartRow(ean, a);
						}
					}
				}
			}
			updateShoppingCartTotals();
		}
		
		public void updateRoseShoppingCart() {
			if (med_index>=0) {
				for (Long row_id : med_id.get(med_index)) {
					Article article = m_rosedb.getArticleWithId(row_id);
           			// We have selected a standard / non-assorted article
					String ean = article.getEanCode();
					m_shopping_basket.put(ean, article);
					// Update shopping basket
					m_shopping_cart.setShoppingBasket(m_shopping_basket);					
					// Update list of selecte articles
					m_shopping_cart.updateSelectList(ean);
					// Update list of similar articles only for last insert article
					LinkedList<Article> la = listSimilarArticles(article);
					if (la!=null) {
						// Check if ean code is already part of the map...
						if (!m_map_similar_articles.containsKey(ean)) {
							m_map_similar_articles.put(ean, la);
							m_shopping_cart.updateMapSimilarArticles(m_map_similar_articles);
						}
					}
				}				
				m_web_panel.updateShoppingHtml();
			}
		}
		
		public void updateShoppingHtml() {
			// Retrieve main html
			String html_str = m_shopping_cart.updateShoppingCartHtml(m_shopping_basket);
			// Update html
			jWeb.setJavascriptEnabled(true);
			jWeb.setHTMLContent(html_str);
			jWeb.setVisible(true);
		}

		public void showCheckoutHtml() {
			// Retrieve main html
			String html_str = m_shopping_cart.checkoutHtml(m_address_map);
			// Update html
			jWeb.setJavascriptEnabled(true);
			jWeb.setHTMLContent(html_str);
			jWeb.setVisible(true);
		}
		
		public void updateShoppingCartRow(String row_key, Article article) {
			String js = m_shopping_cart.getRowUpdateJS(row_key, article);
			jWeb.executeJavascript(js);
		}

		public void updateShoppingCartTotals() {
			String js = m_shopping_cart.getTotalsUpdateJS();
			jWeb.executeJavascript(js);
		}

		public void updateCheckoutTable(String row_key, String shipping_type) {
			String js = m_shopping_cart.getCheckoutUpdateJS(row_key, shipping_type);
			jWeb.executeJavascript(js);
		}

		public void updateListOfPackages() {
			List<String> list_of_packages = new ArrayList<String>();
			String[] packages = { m_rb.getString("packs") };
			if (m_curr_uistate.isShoppingMode()) {
				// user/customer categories are defined in aips2sqlite:glncodes.java
				String user_category = m_prefs.get("type", "arzt");
				if (med_index < med_id.size() && med_index >= 0) {
					// Get full info on selected medication			
					if (Utilities.isRoseShoppingApp()) {
						/*
						for (Long row_id : med_id.get(med_index)) {
							Article article = m_rosedb.getArticleWithId(row_id);
							list_of_packages.add(article.getPackTitle().trim() + " [" + article.getExfactoryPrice() + "]");
						}
						*/						
					} else {
						list_of_articles.clear();
						// Loop through all indexes listed in med_id (typically there is only one entry)
						for (Long row_id : med_id.get(med_index)) {
							Medication m = m_sqldb.getMediWithId(row_id);
							// Get packages and author
							packages = m.getPackages().split("\n");
							// Extract the information that is shown to the user
							if (packages!=null) {
								for (String p : packages) {
									if (!p.isEmpty()) {
										String[] entry = p.split("\\|");
										Article article = new Article(entry, m.getAuth());
										// Set regnr							
										article.setRegnr(m.getRegnrs());
										if (article.isVisible(user_category) && article.hasPrice(user_category)) {
											list_of_articles.add(article);
											// Get user class
											if (Utilities.appCustomization().equals("ibsa")) {
												char user_class = ' ';
		  										if (m_map_ibsa_glns.containsKey(m_customer_gln_code))
													user_class = getUserClass();
												// Check if article has free samples, use user class (A-,B-,C-Kunde)
												if (article.hasFreeSamples(user_class + "-" + user_category))
													list_of_packages.add(article.getPackTitle().trim() + " [" + article.getPrice(user_category) + ", M]");
												else 
													list_of_packages.add(article.getPackTitle().trim() + " [" + article.getPrice(user_category) + "]");
											} else if (Utilities.appCustomization().equals("desitin")) {
												// 'user_category' is only relevant for "ibsa"
												list_of_packages.add(article.getPackTitle().trim() + " [" + article.getExfactoryPrice() + "]");
											}
										}
									}
								}
							}
						}
					}
				}
			}
			// Update section titles
			if (list_of_packages.size() == 0)
				list_of_packages.add(m_rb.getString("nopacks"));
			packages = list_of_packages.toArray(new String[list_of_packages.size()]);
			m_middle_pane.update(packages);
		}
		
		public LinkedList<Article> listSimilarArticles(Article article) {
			LinkedList<Article> list_a = new LinkedList<Article>();
			String atc_code = article.getAtcCode();	
			String size = article.getPackSize();
			String unit = article.getPackUnit();
			if (!atc_code.equals("k.A.")) {
				for (Article a : m_rosedb.searchATC(atc_code)) {
					if (!a.getAtcCode().equals("k.A.")) {
						String s = a.getPackSize().toLowerCase();
						String u = a.getPackUnit().toLowerCase();		
						if (!article.isOffMarket()) {
							if (!a.isOffMarket()) {
								// Make sure that articles added to the list are NOT off-the-market
								if ((size.contains(s) || s.contains(size)) && (unit.contains(u) || u.contains(unit)) )
									list_a.add(a);					
							}
						} else {
							// If the main article is off the market, get some replacements...
							u = u.replaceAll("[^A-Za-z]","");
							unit = unit.replaceAll("[^A-Za-z]","");
							// System.out.println(a.getPackTitle() + " -> " + a.getAvailability() + " | " + s + "=" + size + " | " + u + "=" + unit);
							if (u.equals(unit) && s.equals(size) && !a.isOffMarket())
								list_a.add(a);
						}
					}
				}
			}
			// If "Ersatzartikel" exists, add it to list
			String replace_pharma_code = article.getReplacePharma();	
			if (replace_pharma_code!=null && !replace_pharma_code.isEmpty()) {
				// Check if article is already in list
				for (Article a : list_a) {
					if (a.getPharmaCode().equals(replace_pharma_code))
						return list_a;
				}				
				List<Article> replace_article = m_rosedb.searchEan(replace_pharma_code);
				if (replace_article.size()>0)
					list_a.add(replace_article.get(0));
			}
			return list_a;
		}
		
		/*---------------------------------------------------------------------------
		 * INTERACTIONS
		 */
		public void updateInteractionsCart() {
			// Set right panel title
			m_web_panel.setTitle(m_rb.getString("medbasket"));
			// Display interactions in the web panel
			if (med_index >= 0 && med_index < med_id.size()) {
				// Get full info on selected medication
				long row_id = med_id.get(med_index).get(0);
				Medication m = m_sqldb.getMediWithId(row_id);
				// Add med to basket if not already in basket
				String title = m.getTitle().trim();
				if (title.length() > 30)
					title = title.substring(0, 30) + "...";
				if (!m_med_basket.containsKey(title))
					m_med_basket.put(title, m);
				updateInteractionsHtml();
			} else {			
				// Medikamentenkorb ist leer
				updateInteractionsHtml();
			}
		}

		public void updateInteractionsHtml() {
			// Retrieve main html
			String html_str = m_interactions_cart.updateHtml(m_med_basket);
			// Retrieve section titles
			m_middle_pane.update(m_interactions_cart.sectionTitles());
			// Update html
			jWeb.setJavascriptEnabled(true);
			jWeb.setHTMLContent(html_str);
			jWeb.setVisible(true);
		}
		
		/*---------------------------------------------------------------------------
		 * PREISVERGLEICH
		 */
		public void updateComparisonCart() {
			if (med_index >= 0 && list_of_articles.size() > 0) {
				Article article = list_of_articles.get(med_index);
				String atc_code = article.getAtcCode();				
				if (m_compare_show_all==true) {
					fillComparisonBasket(atc_code);
				} else {
					String size = article.getPackSize();
					String unit = article.getPackUnit();
					fillComparisonBasket(atc_code, size.toLowerCase(), unit.toLowerCase());
				}
				m_web_panel.updateComparisonCartHtml();
			}
		}
		
		public void fillComparisonBasket(String atc_code) {
			if (atc_code != null && atc_code.matches("^[a-zA-Z0-9]*$")) {
				m_comparison_basket.clear();
				for (Article a : m_rosedb.searchATC(atc_code)) {
					m_comparison_basket.put(a.getEanCode(), a);
				}
				// Sort everything
				m_comparison_cart.setComparisonBasket(m_comparison_basket);
				m_comparison_cart.sortCart(0, false);
			}
		}

		public void fillComparisonBasket(String atc_code, String size, String unit) {
			if (size.equals("0") || unit.equals("0"))
				fillComparisonBasket(atc_code);
			// 
			if (atc_code != null && atc_code.matches("^[a-zA-Z0-9]*$")) {
				m_comparison_basket.clear();
				for (Article a : m_rosedb.searchATC(atc_code)) {
					String s = a.getPackSize().toLowerCase();
					String u = a.getPackUnit().toLowerCase();					
					if ((size.contains(s) || s.contains(size)) && (unit.contains(u) || u.contains(unit)) )
						m_comparison_basket.put(a.getEanCode(), a);
				}
				// Sort everything
				m_comparison_cart.setComparisonBasket(m_comparison_basket);
				m_comparison_cart.sortCart(0, false);
			}
		}
		
		public void updateComparisonCartHtml() {
			// Retrieve main html
			String html_str = m_comparison_cart.updateComparisonCartHtml();
			// Update html
			jWeb.setJavascriptEnabled(true);
			jWeb.setHTMLContent(html_str);
			jWeb.setVisible(true);
		}
		
		/*--------------------------------------------------------------------------- 
		 * CUSTOMER_INFO
		 */
		public void updateCustomerInfo() {
			if (med_index>=0 && list_of_gln_codes.size()>0) {
				// Get customer info
				User customer = list_of_gln_codes.get(med_index);
				String category = customer.category;
				m_prefs.put("type", category.toLowerCase());
				m_customer_gln_code = customer.gln_code;
				// 
				if (m_map_ibsa_glns.containsKey(m_customer_gln_code)) {
					char user_class = getUserClass();
					category = user_class + "-" + category;					
				}
				
				if (!customer.last_name.isEmpty()) {
					m_web_panel.setTitle(m_rb.getString("shoppingCart") + " von " + customer.first_name + " " + customer.last_name 
							+ " [" + m_customer_gln_code + ", " + category + "]");
				} else if (!customer.name1.isEmpty()) {
					m_web_panel.setTitle(m_rb.getString("shoppingCart") + " von " + customer.name1 + " " + customer.name2 
							+ " [" + m_customer_gln_code + ", " + category + "]");
				} else {
					m_web_panel.setTitle(m_rb.getString("shoppingCart") + " [" + m_customer_gln_code + "]");
				}
				// Save customer query str
				m_customer_query_str = m_query_str;				
				// Update web panel
				if (m_shopping_cart!=null) {
					m_shopping_basket.clear();
					m_map_similar_articles.clear();
					m_shopping_cart.setCustomerGlnCode(m_customer_gln_code);
					m_shopping_cart.saveWithIndex(m_shopping_basket);
					m_web_panel.updateShoppingHtml();
				}
				// List shopping carts					
				list_of_carts = listCartsInFolder(m_application_data_folder + "\\shop\\" + m_customer_gln_code);
				String[] carts_str = list_of_carts.toArray(new String[list_of_carts.size()]);				
				// ... and update the middle pane
				m_middle_pane.update(carts_str);					
			}
		}
		
		/**
		 * 
		 */
		public void emptyPage() {
			// Update html
			jWeb.setJavascriptEnabled(true);
			jWeb.setHTMLContent("<html><body style=\"font-family:Verdana; font-size:0.9em;\">" + m_rb.getString("nofachinfo") + "</body></html>");
			jWeb.setVisible(true);
		}

		public void noclickPage() {
			// Update html
			jWeb.setJavascriptEnabled(true);
			jWeb.setHTMLContent("<html><body style=\"font-family:Verdana; font-size:0.9em;\">" + m_rb.getString("noclick") + "</body></html>");
			jWeb.setVisible(true);
		}
		
		public void dispose() {
			// Dispose native peer
			jWeb.disposeNativePeer();
			// Close socket connection
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mTcpServer.stop();
				}
			});
			// jWeb = null;
		}

		public void print() {
			jWeb.print(true);
		}		
	}

	static class TextPanel extends JPanel {

		private JTextArea jText = null;

		public TextPanel() {
			jText = new JTextArea("");
			jText.setEditable(false);
			jText.setLineWrap(false);
			JScrollPane jScroll = new JScrollPane(jText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			jScroll.setPreferredSize(new Dimension(704, 800));
			add(jScroll);
		}

		public void updateText() {
			if (med_index >= 0) {
				jText.setText(med_content.get(med_index));
				jText.setCaretPosition(0);
			}
		}
	}

	static class WebPanel extends JPanel {

		private JEditorPane jep = null;
		private HTMLEditorKit kit = null;

		public WebPanel() {
			jep = new JEditorPane();
			jep.setEditable(false);
			jep.setContentType("text/html");
			jep.setText("");

			kit = new HTMLEditorKit();
			jep.setEditorKit(kit);

			StyleSheet style_sheet = kit.getStyleSheet();
			String css_str = "";
			try {
				css_str = FileOps.readFromFileFast(Constants.HTML_FILES + "amiko_stylesheet.css", StandardCharsets.UTF_8);
				System.out.println(css_str);
			} catch (IOException e) {
				e.printStackTrace();
			}
			style_sheet.addRule(css_str);
			javax.swing.text.Document doc = kit.createDefaultDocument();
			jep.setDocument(doc);

			JScrollPane jScroll = new JScrollPane(jep);
			jScroll.setPreferredSize(new Dimension(704, 800));
			add(jScroll);
		}

		public void updateText() {
			if (med_index >= 0) {
				jep.setText(med_content.get(med_index));
				jep.setCaretPosition(0);
			}
		}
	}

	static class RoundJTextField extends JTextField {

		private Shape shape;

		public RoundJTextField(String s) {
			super(s);
		}

		public RoundJTextField(int size) {
			super(size);
			setOpaque(false); // As suggested by @AVD in comment.
		}

		protected void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
			super.paintComponent(g);
		}

		protected void paintBorder(Graphics g) {
			g.setColor(getForeground());
			g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
		}

		public boolean contains(int x, int y) {
			if (shape == null || !shape.getBounds().equals(getBounds())) {
				shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 15, 15);
			}
			return shape.contains(x, y);
		}
	}

	static class SearchField extends JTextField implements FocusListener {

		private String hint;
		private Icon icon;
		private Insets insets;

		public SearchField(final String hint) {
			super(hint);
			super.addFocusListener(this);
			if (!Utilities.appCustomization().equals("ibsa"))
				this.setFont(new Font("Dialog", Font.PLAIN, 14));
			else
				this.setFont(m_custom_font.deriveFont(Font.PLAIN, 14));
			this.hint = hint;
			this.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
			this.setBackground(m_search_field_bg);
			this.setEditable(true);

			this.icon = new ImageIcon(Constants.IMG_FOLDER + "mag_glass_16x16.png");
			Border border = UIManager.getBorder("TextField.border");
			insets = border.getBorderInsets(this);
			setBorder(border); // new CompoundBorder(border, empty));
		}

		public void setHint(String hint) {
			this.hint = hint;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int textX = 2;
			if (icon != null) {
				int iconWidth = icon.getIconWidth();
				int iconHeight = icon.getIconHeight();
				int x = insets.left + 3; 	// Icon's x coordinate
				textX = x + iconWidth + 2; 	// This is the x where text should start
				int y = (this.getHeight() - iconHeight) / 2;
				icon.paintIcon(this, g, x, y);
			}
			setMargin(new Insets(2, textX, 2, 2));
		}

		@Override
		public void focusGained(FocusEvent e) {
			super.setText(hint);
			super.setCaretPosition(0);									
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (this.getText().isEmpty()) {
				super.setText("");
			}
		}

		@Override
		public String getText() {
			String typed = super.getText();		
			if (typed.endsWith(hint) && typed.length()>hint.length()) {
				typed = typed.substring(0,1);
				super.setText(typed);
			} else if (typed.length()==0) {		
				super.setText(hint);
				super.setCaretPosition(0);						
			}
			return typed;
			// return typed.equals(hint) ? "" : typed;
		}

		@Override
		public void setText(final String t) {	
			super.setText(t);
		}
	}

	private static void createAndShowLightGUI() {
		// Create and setup window
		final JFrame jframe = new JFrame(Constants.APP_NAME);
		int min_width = CML_OPT_WIDTH;
		int min_height = CML_OPT_HEIGHT;

		jframe.setPreferredSize(new Dimension(min_width, min_height));
		jframe.setMinimumSize(new Dimension(min_width, min_height));
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - min_width) / 2;
		int y = (screen.height - min_height) / 2;
		jframe.setBounds(x, y, min_width, min_height);

		// Action listeners
		jframe.addWindowListener(new WindowListener() {
			// Use WindowAdapter!
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
				m_web_panel.dispose();
				Runtime.getRuntime().exit(0);
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});

		// Container
		final Container container = jframe.getContentPane();
		container.setBackground(Color.WHITE);
		container.setLayout(new BorderLayout());

		// ==== Light panel ====
		JPanel light_panel = new JPanel();
		light_panel.setBackground(Color.WHITE);
		light_panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(2, 2, 2, 2);

		// ---- Section titles ----
		m_middle_pane = null;
		if (Utilities.appLanguage().equals("de")) {
			m_middle_pane = new MiddlePane(Constants.SectionTitle_DE);
		} else if (Utilities.appLanguage().equals("fr")) {
			m_middle_pane = new MiddlePane(Constants.SectionTitle_FR);
		}
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 8;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(m_section_titles, gbc);
		if (m_middle_pane != null)
			light_panel.add(m_middle_pane, gbc);

		// ---- Fachinformation ----
		m_web_panel = new WebPanel2();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.gridheight = 20;
		gbc.weightx = 2.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.EAST;
		// --> container.add(m_web_panel, gbc);
		light_panel.add(m_web_panel, gbc);

		// ---- Add panel to main container ----
		container.add(light_panel, BorderLayout.CENTER);

		// Display window
		jframe.pack();
		// jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// jframe.setAlwaysOnTop(true);
		jframe.setVisible(true);

		// If command line options are provided start app with a particular
		// title or eancode
		if (commandLineOptionsProvided()) {
			final JToggleButton but_dummy = new JToggleButton("dummy_button");
			if (!CML_OPT_TITLE.isEmpty())
				startAppWithTitle(but_dummy);
			else if (!CML_OPT_EANCODE.isEmpty())
				startAppWithEancode(but_dummy);
			else if (!CML_OPT_REGNR.isEmpty())
				startAppWithRegnr(but_dummy);
			else if (CML_OPT_SERVER == true) {
				// Start thread that reads data from TCP server
				Thread server_thread = new Thread() {
					public void run() {
						while (true) {
							String tcpServerInput = "";
							// Wait until new data is available from input stream
							// Note: the TCP client defines the update rate!
							// System.out.print("Waiting for input...");
							while ((tcpServerInput = mTcpServer.getInput())
									.isEmpty())
								;
							/*
							 * Important note: we use invokeLater to post a "job" to Swing, which will then be run on the
							 * event dispatch thread at Swing's next convenience. Failing to do so will freeze the main thread.
							 */

							// Detect type of search (t=title, e=eancode, r=regnr)
							char typeOfSearch = tcpServerInput.charAt(0);
							if (typeOfSearch == 't') {
								// Extract title from received string
								CML_OPT_TITLE = tcpServerInput.substring(2);
								// System.out.println(" title -> " +
								// CML_OPT_TITLE);
								// Post a "job" to Swing, which will be run on
								// the event dispatch thread
								// at its next convenience.
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										startAppWithTitle(but_dummy);
									}
								});
							} else if (typeOfSearch == 'e') {
								// Extract ean code from received string
								CML_OPT_EANCODE = tcpServerInput.substring(2);
								// System.out.println(" eancode -> " +
								// CML_OPT_EANCODE);
								// Post a "job" to Swing, which will be run on
								// the event dispatch thread
								// at its next convenience.
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										startAppWithEancode(but_dummy);
									}
								});
							} else if (typeOfSearch == 'r') {
								// Extract registration number from received
								// string
								CML_OPT_REGNR = tcpServerInput.substring(2);
								// System.out.println(" regnr -> " +
								// CML_OPT_REGNR);
								// Post a "job" to Swing, which will be run on
								// the event dispatch thread
								// at its next convenience.
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										startAppWithRegnr(but_dummy);
									}
								});
							}
						}
					}
				};
				server_thread.start();
			}
		}
	}

	private static void setupButton(JToggleButton button, String toolTipText, String rolloverImg, String selectedImg) {
		if (!Utilities.appCustomization().equals("ibsa"))
			button.setFont(new Font("Dialog", Font.PLAIN, 11));
		else
			button.setFont(m_custom_font.deriveFont(Font.PLAIN, 11));
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setText(toolTipText);
		button.setRolloverIcon(new ImageIcon(Constants.IMG_FOLDER + rolloverImg));
		button.setSelectedIcon(new ImageIcon(Constants.IMG_FOLDER + selectedImg));
		button.setBackground(m_selected_but_color);
		button.setToolTipText(toolTipText);
		// Remove border
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setMargin(new Insets(0, 0, 0, 0));
		// Set adequate size
		// button.setPreferredSize(new Dimension(64, 32));
		// Set hand cursor
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private static void setupToggleButton(final JToggleButton button) {
		button.setBackground(m_but_color_bg);
		button.setFocusPainted(false);
		button.setBorder(new CompoundBorder(new LineBorder(m_but_color_bg),
				new EmptyBorder(0, 3, 0, 0)));
		button.setBorderPainted(false);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		// Set hand cursor
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		// 
		button.setRolloverEnabled(true);
		button.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        button.setBackground(m_hover_but_color_bg);
		    }
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        button.setBackground(m_but_color_bg);
		    }
		});
	}

	private static boolean isOperator(int id) {
		// Change user id
		int user_id = m_prefs.getInt("user", 17);
		if (user_id==id)
			return true;
		return false;
	}
	
	private static void startBrowser(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException e) {
				// TODO:
			} catch (URISyntaxException r) {
				// TODO:
			}
		}
	}
	
	private static void startEmailClient(String email, String subject) {
		if (Desktop.isDesktopSupported()) {
			try {
				URI mail_to_uri = URI.create("mailto:" + email + "?subject=" + subject);
				Desktop.getDesktop().mail(mail_to_uri);
			} catch (IOException e) {
				// TODO:
			}
		} else {
			AmiKoDialogs cd = new AmiKoDialogs(Utilities.appLanguage(), Utilities.appCustomization());
			cd.ContactDialog();
		}
	}
	
	private static void createAndShowFullGUI() {
		// Create and setup window
		String user_name = m_prefs.get("name", "");
		if (!user_name.isEmpty())
			user_name = " - " + user_name.trim();		
		String email_addr = m_prefs.get("emailadresse", "");
		if (!email_addr.isEmpty())
			user_name += " / " + email_addr.trim();		
		final JFrame jframe = new JFrame(Constants.APP_NAME + user_name);		
		if (Utilities.appCustomization().equals("ibsa"))
			jframe.setFont(m_custom_font.deriveFont(Font.PLAIN, 12));
		jframe.setName(Constants.APP_NAME + ".main");	

		int min_width = CML_OPT_WIDTH;
		int min_height = CML_OPT_HEIGHT;
		jframe.setPreferredSize(new Dimension(min_width, min_height));
		jframe.setMinimumSize(new Dimension(min_width, min_height));
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - min_width) / 2;
		int y = (screen.height - min_height) / 2;
		jframe.setBounds(x, y, min_width, min_height);

		// Set application icon
		if (Utilities.appCustomization().equals("ywesee")) {
			ImageIcon img = new ImageIcon(Constants.AMIKO_ICON);
			jframe.setIconImage(img.getImage());
		} else if (Utilities.appCustomization().equals("desitin")) {
			ImageIcon img = new ImageIcon(Constants.DESITIN_ICON);
			jframe.setIconImage(img.getImage());
		} else if (Utilities.appCustomization().equals("meddrugs")) {
			ImageIcon img = new ImageIcon(Constants.MEDDRUGS_ICON);
			jframe.setIconImage(img.getImage());
		} else if (Utilities.appCustomization().equals("zurrose")) {
			ImageIcon img = new ImageIcon(Constants.AMIKO_ICON);
			jframe.setIconImage(img.getImage());
		} else if (Utilities.appCustomization().equals("ibsa")) {
			ImageIcon img = new ImageIcon(Constants.IBSA_ICON);
			jframe.setIconImage(img.getImage());
		}
		
		// ------ Setup menubar ------
		JMenuBar menu_bar = new JMenuBar();
		// menu_bar.add(Box.createHorizontalGlue()); // --> aligns menu items to the right!
		// --- Menu "Datei" ---
		JMenu datei_menu = new JMenu(m_rb.getString("datei"));
		menu_bar.add(datei_menu);
		JMenuItem print_item = new JMenuItem(m_rb.getString("print") + "...");
		JMenuItem settings_item = new JMenuItem(m_rb.getString("settings") + "...");
		JMenuItem quit_item = new JMenuItem(m_rb.getString("beenden"));
		datei_menu.add(print_item);
		datei_menu.addSeparator();
		datei_menu.add(settings_item);
		datei_menu.addSeparator();
		datei_menu.add(quit_item);

		// --- Menu "Aktualisieren" ---
		JMenu update_menu = new JMenu(m_rb.getString("update"));
		menu_bar.add(update_menu);
		final JMenuItem updatedb_item = new JMenuItem("Aktualisieren via Internet...");
		updatedb_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		JMenuItem choosedb_item = new JMenuItem("Aktualisieren via Datei...");
		update_menu.add(updatedb_item);
		update_menu.add(choosedb_item);
		if (Utilities.appLanguage().equals("fr")) {
			updatedb_item.setText("Télécharger la banque de données...");
			updatedb_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
			choosedb_item.setText("Ajourner la banque de données...");
		}

		// --- Menu "Hilfe" ---
		JMenu hilfe_menu = new JMenu(m_rb.getString("help"));
		menu_bar.add(hilfe_menu);
		JMenuItem about_item = new JMenuItem("Über " + Constants.APP_NAME + "...");
		JMenuItem ywesee_item = new JMenuItem(Constants.APP_NAME + " im Internet");
		if (Utilities.appCustomization().equals("meddrugs"))
			ywesee_item.setText("med-drugs im Internet");
		JMenuItem report_item = new JMenuItem("Error Report...");
		JMenuItem contact_item = new JMenuItem("Kontakt...");

		if (Utilities.appLanguage().equals("fr")) {
			// Extrawunsch med-drugs
			if (Utilities.appCustomization().equals("meddrugs"))
				about_item.setText(Constants.APP_NAME);
			else
				about_item.setText("A propos de " + Constants.APP_NAME + "...");
			contact_item.setText("Contact...");
			if (Utilities.appCustomization().equals("meddrugs"))
				ywesee_item.setText("med-drugs sur Internet");
			else
				ywesee_item.setText(Constants.APP_NAME + " sur Internet");
			report_item.setText("Rapport d'erreur...");
		}
		hilfe_menu.add(about_item);
		hilfe_menu.add(ywesee_item);
		hilfe_menu.addSeparator();
		hilfe_menu.add(report_item);
		hilfe_menu.addSeparator();
		hilfe_menu.add(contact_item);

		// --- Menu "Abonnieren" (only for ywesee) ---
		JMenu subscribe_menu = new JMenu(m_rb.getString("subscribe"));
		if (Utilities.appCustomization().equals("ywesee")) {
			menu_bar.add(subscribe_menu);
		}

		// --- Menu "Neue Datenbank verfügbar!"
		final JMenu db_push_menu = new JMenu(m_rb.getString("newDb"));	
		menu_bar.add(db_push_menu);
		JMenuItem db_version_item = new JMenuItem("");
		db_push_menu.add(db_version_item);
		// Hide menu
		db_push_menu.setVisible(false);
		
		jframe.setJMenuBar(menu_bar);

		// ------ Setup toolbar ------
		JToolBar toolBar = new JToolBar("Database");
		toolBar.setPreferredSize(new Dimension(jframe.getWidth(), 64));
		final JToggleButton selectAipsButton = new JToggleButton(
				new ImageIcon( Constants.IMG_FOLDER + "aips32x32_bright.png"));
		final JToggleButton selectFavoritesButton = new JToggleButton(
				new ImageIcon(Constants.IMG_FOLDER + "favorites32x32_bright.png"));
		final JToggleButton selectInteractionsButton = new JToggleButton(
				new ImageIcon(Constants.IMG_FOLDER + "interactions32x32_bright.png"));
		final JToggleButton selectShoppingCartButton = new JToggleButton(
				new ImageIcon(Constants.IMG_FOLDER + "shoppingcart32x32_bright.png"));
		final JToggleButton selectComparisonCartButton = new JToggleButton(
				new ImageIcon(Constants.IMG_FOLDER + "comparisoncart32x32_bright.png"));

		final JToggleButton list_of_buttons[] = { selectAipsButton,	selectFavoritesButton, selectInteractionsButton,
				selectShoppingCartButton, selectComparisonCartButton };

		if (Utilities.appLanguage().equals("de")) {
			setupButton(selectAipsButton, "Kompendium", "aips32x32_gray.png", "aips32x32_dark.png");
			setupButton(selectFavoritesButton, "Favoriten",	"favorites32x32_gray.png", "favorites32x32_dark.png");
			setupButton(selectInteractionsButton, "Interaktionen", "interactions32x32_gray.png", "interactions32x32_dark.png");
			setupButton(selectShoppingCartButton, "Shop", "shoppingcart32x32_gray.png", "shoppingcart32x32_dark.png");
			setupButton(selectComparisonCartButton, "Preisvergleich", "comparisoncart32x32_gray.png", "comparisoncart32x32_dark.png");
		} else if (Utilities.appLanguage().equals("fr")) {
			setupButton(selectAipsButton, "Compendium", "aips32x32_gray.png", "aips32x32_dark.png");
			setupButton(selectFavoritesButton, "Favorites", "favorites32x32_gray.png", "favorites32x32_dark.png");
			setupButton(selectInteractionsButton, "Interactions", "interactions32x32_gray.png", "interactions32x32_dark.png");
			setupButton(selectShoppingCartButton, "Panier", "shoppingcart32x32_gray.png", "shoppingcart32x32_dark.png");
			setupButton(selectComparisonCartButton, "Preisvergleich", "comparisoncart32x32_gray.png", "comparisoncart32x32_dark.png");
		}

		// Add to toolbar and set up
		toolBar.setBackground(m_toolbar_bg);
		//
		toolBar.add(selectAipsButton);
		toolBar.addSeparator();
		toolBar.add(selectFavoritesButton);
		toolBar.addSeparator();
		toolBar.add(selectInteractionsButton);
		// Customizations
		if (Utilities.appCustomization().equals("ibsa") 
				|| Utilities.appCustomization().equals("desitin")
				|| Utilities.appCustomization().equals("zurrose")) {		
			toolBar.addSeparator();
			toolBar.add(selectShoppingCartButton);
		}
		if (Utilities.appCustomization().equals("zurrose")) {
			toolBar.addSeparator();
			toolBar.add(selectComparisonCartButton);
		}
		// Progress indicator (not working...)
		toolBar.addSeparator();
		toolBar.add(m_progress_indicator);
		// Add image on the right
		if (Utilities.appCustomization().equals("ibsa") 
				|| Utilities.appCustomization().equals("desitin")) {			
			JLabel jImageLabel = null;
			if (Utilities.appCustomization().equals("ibsa"))
				jImageLabel = new JLabel(new ImageIcon(Constants.IMG_FOLDER + "ibsa_image.png"));
			else if (Utilities.appCustomization().equals("desitin"))
				jImageLabel = new JLabel(new ImageIcon(Constants.IMG_FOLDER + "desitin_image.png"));
			jImageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			toolBar.add(Box.createHorizontalGlue());
			toolBar.add(jImageLabel);
			jImageLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent me)  
				{  
					String service_address = "";
					if (Utilities.appCustomization().equals("ibsa"))
						service_address = "service@ibsa.ch";
					else if (Utilities.appCustomization().equals("desitin"))
						service_address = "info@desitin.ch";
					if (Desktop.isDesktopSupported()) {
						try {
							URI mail_to_uri = URI.create("mailto:" + service_address + "?subject=AmiKo%20Desktop%20IBSA%20Feedback");
							Desktop.getDesktop().mail(mail_to_uri);
						} catch (IOException e) {
							// TODO:
						}
					} else {
						AmiKoDialogs cd = new AmiKoDialogs(Utilities.appLanguage(), Utilities.appCustomization());
						cd.ContactDialog();
					}
				}
			});
		}
		//
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		
		// ------ Setup settingspage ------
		m_settings_page = new SettingsPage(jframe, m_rb, m_user_map);
		m_settings_page.initUserSettings();
		m_settings_page.initCustomerSettings();
		// Retrieve gln codes for fast access
		if (Utilities.appCustomization().equals("ibsa")) {
			m_customerdb = m_settings_page.getUserDb();
		}
		
		jframe.addWindowListener(new WindowListener() {
			// Use WindowAdapter!
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
				m_web_panel.dispose();
				Runtime.getRuntime().exit(0);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// Save shopping cart
				if (m_shopping_cart!=null) {
					m_shopping_cart.saveWithIndex(m_shopping_basket);
				}
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});
		print_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				m_web_panel.print();
			}
		});
		settings_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				m_settings_page.displayUserSettings();
			}
		});
		quit_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					// Save shopping cart
					m_shopping_cart.saveWithIndex(m_shopping_basket);
					// Save settings
					WindowSaver.saveSettings();
					m_web_panel.dispose();
					Runtime.getRuntime().exit(0);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		subscribe_menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent event) {
				if (Utilities.appCustomization().equals("ywesee"))
					startBrowser("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3UM84Z6WLFKZE");
			}

			@Override
			public void menuDeselected(MenuEvent event) {
				// do nothing
			}

			@Override
			public void menuCanceled(MenuEvent event) {
				// do nothing
			}
		});
		contact_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (Utilities.appCustomization().equals("ywesee")) {
					startEmailClient("zdavatz@ywesee.com", "AmiKo%20Desktop%20Feedback");
				} else if (Utilities.appCustomization().equals("desitin")) {
					startEmailClient("info@desitin.ch", "AmiKo%20Desktop%20Desitin%20Feedback");
				} else if (Utilities.appCustomization().equals("meddrugs")) {
					startEmailClient("med-drugs@just-medical.com", "med-drugs%20desktop%20Feedback");
				} else if (Utilities.appCustomization().equals("zurrose")) {
					startBrowser("www.zurrose.ch/amiko");
				} else if (Utilities.appCustomization().equals("ibsa")) {
					startEmailClient("service@ibsa.ch", "AmiKo%20Desktop%20IBSA%20Feedback");
				}
			}
		});
		report_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Check first m_application_folder otherwise resort to
				// pre-installed report
				String report_file = m_application_data_folder + "\\" + Constants.DEFAULT_AMIKO_REPORT_BASE
						+ Utilities.appLanguage() + ".html";
				if (!(new File(report_file)).exists())
					report_file = System.getProperty("user.dir") + "/dbs/" + Constants.DEFAULT_AMIKO_REPORT_BASE
							+ Utilities.appLanguage() + ".html";
				// Open report file in browser
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new File(report_file).toURI());
					} catch (IOException e) {
						// TODO:
					}
				}
			}
		});
		ywesee_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (Utilities.appCustomization().equals("ywesee") || Utilities.appCustomization().equals("ibsa")) {
					startBrowser("http://www.ywesee.com/AmiKo/Desktop");
				} else if (Utilities.appCustomization().equals("desitin")) {
					startBrowser("http://www.desitin.ch/produkte/arzneimittel-kompendium-apps/");
				} else if (Utilities.appCustomization().equals("meddrugs")) {
					if (Utilities.appLanguage().equals("de"))
						startBrowser("http://www.med-drugs.ch");
					else if (Utilities.appLanguage().equals("fr"))
						startBrowser("http://www.med-drugs.ch/index.cfm?&newlang=fr");
				} else if (Utilities.appCustomization().equals("zurrose")) {
					startBrowser("www.zurrose.ch/amiko");
				}
			}
		});
		about_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				AmiKoDialogs ad = new AmiKoDialogs(Utilities.appLanguage(), Utilities.appCustomization());
				ad.AboutDialog();
			}
		});
		updatedb_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (m_mutex_update==false) {
					m_mutex_update = true;
					String db_file = m_maindb_update.doIt(jframe, Utilities.appLanguage(), Utilities.appCustomization(), m_application_data_folder, m_full_db_update);
					// ... and update time
					if (m_full_db_update==true) {
						DateTime dT = new DateTime();
						m_prefs.put("updateTime", dT.now().toString());		
					}
					//
					if (!db_file.isEmpty()) {
						// Save db path (can't hurt)
						WindowSaver.setDbPath(db_file);
					}
				}
			}
		});
		choosedb_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String db_file = m_maindb_update.chooseFromFile(jframe, Utilities.appLanguage(), Utilities.appCustomization(), m_application_data_folder);
				// ... and update time
				DateTime dT = new DateTime();
				m_prefs.put("updateTime", dT.now().toString());				
				//
				if (!db_file.isEmpty()) {
					// Save db path (can't hurt)
					WindowSaver.setDbPath(db_file);
				}
			}
		});
		db_version_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updatedb_item.doClick();
			}
		});
		
		// Container
		final Container container = jframe.getContentPane();
		container.setBackground(Color.WHITE);
		container.setLayout(new BorderLayout());

		// ==== Toolbar =====
		container.add(toolBar, BorderLayout.NORTH);

		// ==== Left panel ====
		JPanel left_panel = new JPanel();
		left_panel.setBackground(Color.WHITE);
		left_panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(2, 2, 2, 2);

		// ---- Search field ----
		final SearchField searchField = new SearchField(m_rb.getString("butSearch") + " " + m_rb.getString("butTitle"));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(searchField, gbc);
		left_panel.add(searchField, gbc);

		// ---- Buttons ----
		// Names
		String l_title = m_rb.getString("butTitle");
		String l_author = m_rb.getString("butAuthor");
		String l_atccode = m_rb.getString("butAtccode");
		String l_regnr = m_rb.getString("butRegnr");
		String l_ingredient = m_rb.getString("butIngred");
		String l_therapy = m_rb.getString("butTherapy");
		String l_search = m_rb.getString("butSearch");
		String l_customer = m_rb.getString("butCustomer");

		ButtonGroup bg = new ButtonGroup();

		final JToggleButton but_title = new JToggleButton(l_title);
		setupToggleButton(but_title);
		bg.add(but_title);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_title, gbc);
		left_panel.add(but_title, gbc);

		final JToggleButton but_auth = new JToggleButton(l_author);
		setupToggleButton(but_auth);
		bg.add(but_auth);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_auth, gbc);
		left_panel.add(but_auth, gbc);

		final JToggleButton but_atccode = new JToggleButton(l_atccode);
		setupToggleButton(but_atccode);
		bg.add(but_atccode);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_atccode, gbc);
		left_panel.add(but_atccode, gbc);

		final JToggleButton but_regnr = new JToggleButton(l_regnr);
		setupToggleButton(but_regnr);
		bg.add(but_regnr);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_regnr, gbc);
		left_panel.add(but_regnr, gbc);

		final JToggleButton but_therapy = new JToggleButton(l_therapy);
		setupToggleButton(but_therapy);
		bg.add(but_therapy);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_therapy, gbc);
		left_panel.add(but_therapy, gbc);

		final JToggleButton but_customer = new JToggleButton(l_customer);
		setupToggleButton(but_customer);
		bg.add(but_customer);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_customer, gbc);
		left_panel.add(but_customer, gbc);

		// Default: inititalize but title 
		but_title.doClick();
		
		// Configure according to user id
		boolean but_customer_enabled = isOperator(18) && m_curr_uistate.isShoppingMode();
		but_customer.setVisible(but_customer_enabled);
		but_customer.setEnabled(but_customer_enabled);
		boolean but_comparison_enabled = isOperator(19);
		selectComparisonCartButton.setVisible(but_comparison_enabled);
		selectComparisonCartButton.setEnabled(but_comparison_enabled);
		
		// ---- Card layout ----
		final CardLayout cardl = new CardLayout();
		cardl.setHgap(-4); // HACK to make things look better!!
		final JPanel p_results = new JPanel(cardl);
		m_list_titles = new ListPanel();
		m_list_auths = new ListPanel();
		m_list_regnrs = new ListPanel();
		m_list_atccodes = new ListPanel();
		m_list_ingredients = new ListPanel();
		m_list_therapies = new ListPanel();
		m_list_customers = new ListPanel();
		// Contraints
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 10;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		//
		p_results.add(m_list_titles, l_title);
		p_results.add(m_list_auths, l_author);
		p_results.add(m_list_regnrs, l_regnr);
		p_results.add(m_list_atccodes, l_atccode);
		p_results.add(m_list_ingredients, l_ingredient);
		p_results.add(m_list_therapies, l_therapy);
		p_results.add(m_list_customers, l_customer);

		// --> container.add(p_results, gbc);
		left_panel.add(p_results, gbc);
		left_panel.setBorder(null);
		// First card to show
		cardl.show(p_results, l_title);

		// ==== Right panel ====
		JPanel right_panel = new JPanel();
		right_panel.setBackground(Color.WHITE);
		right_panel.setLayout(new GridBagLayout());

		// ---- Section titles ----
		m_middle_pane = null;
		if (Utilities.appLanguage().equals("de"))
			m_middle_pane = new MiddlePane(Constants.SectionTitle_DE);
		else if (Utilities.appLanguage().equals("fr"))
			m_middle_pane = new MiddlePane(Constants.SectionTitle_FR);
		m_middle_pane.setMinimumSize(new Dimension(150, 150));
		m_middle_pane.setMaximumSize(new Dimension(320, 1000));

		// ---- Fachinformation ----
		m_web_panel = new WebPanel2();
		m_web_panel.setMinimumSize(new Dimension(320, 150));		
		
		// Add JSplitPane on the RIGHT
		final int Divider_location = 150;
		final int Divider_size = 10;
		final JSplitPane split_pane_right = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, m_middle_pane, m_web_panel );
		split_pane_right.setOneTouchExpandable(true);
		split_pane_right.setDividerLocation(Divider_location);
		split_pane_right.setDividerSize(Divider_size);

		// Add JSplitPane on the LEFT
		JSplitPane split_pane_left = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, left_panel, split_pane_right /* right_panel */ );
		split_pane_left.setOneTouchExpandable(true);
		split_pane_left.setDividerLocation(320); // Sets the pane divider location
		split_pane_left.setDividerSize(Divider_size);
		container.add(split_pane_left, BorderLayout.CENTER);

		// Add status bar on the bottom
		JPanel statusPanel = new JPanel();
		statusPanel.setPreferredSize(new Dimension(jframe.getWidth(), 16));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		container.add(statusPanel, BorderLayout.SOUTH);

		final JLabel m_status_label = new JLabel("");
		m_status_label.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(m_status_label);

		// Add mouse listener
		searchField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				searchField.setText("");
			}
		});

		final String final_title = l_title;
		final String final_author = l_author;
		final String final_atccode = l_atccode;
		final String final_regnr = l_regnr;
		final String final_therapy = l_therapy;
		final String final_customer = l_customer;		
		final String final_search = l_search;

		// Internal class that implements switching between buttons
		final class Toggle {
			public void toggleButton(JToggleButton jbn) {
				for (int i=0; i<list_of_buttons.length; ++i) {
					if (jbn==list_of_buttons[i])
						list_of_buttons[i].setSelected(true);
					else
						list_of_buttons[i].setSelected(false);
				}
			}
		};		
		
		// ------ Add toolbar action listeners ------
		selectAipsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Toggle().toggleButton(selectAipsButton);
				// Set state 'aips'
				if (!m_curr_uistate.getUseMode().equals("aips")) {
					m_curr_uistate.setUseMode("aips");
					m_curr_uistate.setQueryType(m_query_type=NAME);
					// Hide "Kunde" button
					but_customer.setEnabled(false);
					but_customer.setVisible(false);					
					// Show middle pane
					split_pane_right.setDividerSize(Divider_size);
					split_pane_right.setDividerLocation(Divider_location);
					m_middle_pane.setVisible(true);
					//
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							m_start_time = System.currentTimeMillis();							
							m_query_str = searchField.getText();
							int num_hits = 0;
							if (m_curr_uistate.getPrevUseMode().equals("shopping")) {							
								med_search = m_sqldb.searchRegNr(m_curr_regnr);
								sRegNr();
								but_title.doClick();
								cardl.show(p_results, final_title);
								num_hits = med_search.size();
								if (num_hits>0)
									med_index = 0;	// Choose first hit
							} else {
								num_hits = retrieveAipsSearchResults(true);
							}
							m_status_label.setText(med_search.size() + " Suchresultate in " 
									+ (System.currentTimeMillis() - m_start_time) / 1000.0f + " Sek.");
							if (med_index < 0 && prev_med_index >= 0)
								med_index = prev_med_index;
							m_web_panel.updateText();
							if (num_hits==0)
								m_web_panel.emptyPage();
						}
					});
				}
			}
		});
		selectFavoritesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Toggle().toggleButton(selectFavoritesButton);
				// Set state 'favorites'
				if (!m_curr_uistate.getUseMode().equals("favorites")) {
					m_curr_uistate.setUseMode("favorites");
					// Hide "Kunde" button
					but_customer.setEnabled(false);
					but_customer.setVisible(false);
					// Show middle pane
					split_pane_right.setDividerSize(Divider_size);
					split_pane_right.setDividerLocation(Divider_location);
					m_middle_pane.setVisible(true);				
					//
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							m_start_time = System.currentTimeMillis();
							// m_query_str = searchField.getText();
							// Clear the search container
							med_search.clear();
							for (String regnr : favorite_meds_set) {
								List<Medication> meds = m_sqldb.searchRegNr(regnr);
								if (!meds.isEmpty()) { // Add med database ID
									med_search.add(meds.get(0));
								}
							}
							// Sort list of meds
							Collections.sort(med_search, new Comparator<Medication>() {
								@Override
								public int compare(final Medication m1,	final Medication m2) {
									return m1.getTitle().compareTo(m2.getTitle());
								}
							});

							sTitle();
							cardl.show(p_results, final_title);

							m_status_label.setText(med_search.size() + " Suchresultate in "
									+ (System.currentTimeMillis() - m_start_time) / 1000.0f + " Sek.");
						}
					});
				}
			}
		});
		selectInteractionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Toggle().toggleButton(selectInteractionsButton);
				// Set state 'interactions'
				if (!m_curr_uistate.getUseMode().equals("interactions")) {
					m_curr_uistate.setUseMode("interactions");
					// Hide "Kunde" button
					but_customer.setEnabled(false);
					but_customer.setVisible(false);
					// Show middle pane
					split_pane_right.setDividerSize(Divider_size);
					split_pane_right.setDividerLocation(Divider_location);
					m_middle_pane.setVisible(true);				
					//
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							m_query_str = searchField.getText();
							retrieveAipsSearchResults(false);
							// Switch to interaction mode
							m_web_panel.updateInteractionsCart();
							m_web_panel.repaint();
							m_web_panel.validate();
						}
					});
				}
			}
		});
		selectShoppingCartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String email_adr = m_prefs.get("emailadresse", "");
				if (email_adr!=null && email_adr.length()>2)	// Two chars is the minimum length for an email address
					m_preferences_ok = true;
				if (m_preferences_ok) {
					m_preferences_ok = false;	// Check always
					new Toggle().toggleButton(selectShoppingCartButton);
					// Set state 'shopping'
					if (!m_curr_uistate.getUseMode().equals("shopping")) {
						m_curr_uistate.setUseMode("shopping");
						// Enable customer button
						boolean but_customer_enabled = isOperator(18);
						but_customer.setEnabled(but_customer_enabled);
						but_customer.setVisible(but_customer_enabled);
						// Show middle pane
						split_pane_right.setDividerSize(Divider_size);
						split_pane_right.setDividerLocation(Divider_location);
						m_middle_pane.setVisible(true);
						// Set right panel title
						m_web_panel.setTitle(m_rb.getString("shoppingCart"));
						// Switch to shopping cart
						int index = 1;
						if (m_shopping_cart != null) {
							m_shopping_cart.setCustomerGlnCode(m_customer_gln_code);
							index = m_shopping_cart.getCartIndex();
							m_web_panel.loadShoppingCartWithIndex(index);
							// m_shopping_cart.printShoppingBasket();
						}						
						// m_web_panel.updateShoppingHtml();
						m_web_panel.updateListOfPackages();
						// If button is clicked again, then show only ibsa or desitin products						
						m_curr_uistate.setQueryType(m_query_type=OWNER);
						if (Utilities.appCustomization().equals("ibsa")) {
							med_search = m_sqldb.searchAuth("ibsa");
							but_auth.doClick();
						} else if (Utilities.appCustomization().equals("desitin")) {				
							med_search = m_sqldb.searchAuth("desitin");
							but_auth.doClick();
						} else {
							if (Utilities.isRoseShoppingApp()) {
								// Hide middle pane
								m_middle_pane.setVisible(false);
								split_pane_right.setDividerLocation(0);							
								split_pane_right.setDividerSize(0);
								//
								rose_search = m_rosedb.searchTitle("");
							} else
								med_search = m_sqldb.searchTitle("");
							but_title.doClick();
						}
					} 
				} else {
					selectShoppingCartButton.setSelected(false);
					m_settings_page.displayUserSettings();
				}
			}
		});
		selectComparisonCartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Toggle().toggleButton(selectComparisonCartButton);
				// Set state 'comparison'
				if (!m_curr_uistate.getUseMode().equals("comparison")) {
					m_curr_uistate.setUseMode("comparison");
					// Hide "Kunde" button
					but_customer.setEnabled(false);
					but_customer.setVisible(false);
					// Hide middle pane
					m_middle_pane.setVisible(false);
					split_pane_right.setDividerLocation(0);							
					split_pane_right.setDividerSize(0);
					//
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							m_start_time = System.currentTimeMillis();
							// Set right panel title
							m_web_panel.setTitle(getTitle("priceComp"));	
							if (med_index>=0) {
								if (med_id!=null && med_index<med_id.size()) {
									long row_id = med_id.get(med_index).get(0);
									Medication m = m_sqldb.getMediWithId(row_id);
									String atc_code = m.getAtcCode();
									if (atc_code != null) {
										String atc = atc_code.split(";")[0];
										m_web_panel.fillComparisonBasket(atc);
										m_web_panel.updateComparisonCartHtml();
									}
								}
							}
							// Update pane on the left
							retrieveAipsSearchResults(false);
							
							m_status_label.setText(rose_search.size() + " Suchresultate in "
									+ (System.currentTimeMillis() - m_start_time) / 1000.0f + " Sek.");
						}
					});
				}
			}
		});

		// ------ Add keylistener to text field (type as you go feature) ------
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) { // keyReleased(KeyEvent e)
				// invokeLater potentially in the wrong place... more testing
				// required
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (m_curr_uistate.isLoadCart())
							m_curr_uistate.restoreUseMode();
						m_start_time = System.currentTimeMillis();
						m_query_str = searchField.getText();
						// Queries for SQLite DB
						if (!m_query_str.isEmpty()) {
							if (m_query_type == NAME) {
								if (m_curr_uistate.isComparisonMode() || m_curr_uistate.isRoseShoppingMode()) {
									rose_search = m_rosedb.searchTitle(m_query_str);
								} else {
									med_search = m_sqldb.searchTitle(m_query_str);
									if (m_curr_uistate.databaseUsed().equals("favorites"))
										retrieveFavorites();
								}
								sTitle();
								cardl.show(p_results, final_title);
							} else if (m_query_type == OWNER) {
								if (m_curr_uistate.isComparisonMode() || m_curr_uistate.isRoseShoppingMode()) {
									rose_search = m_rosedb.searchSupplier(m_query_str);
								} else {
									med_search = m_sqldb.searchAuth(m_query_str);
									if (m_curr_uistate.databaseUsed().equals("favorites"))
										retrieveFavorites();
								}
								sAuth();
								cardl.show(p_results, final_author);
							} else if (m_query_type == SUBSTANCE) {
								if (m_curr_uistate.isComparisonMode() || m_curr_uistate.isRoseShoppingMode()) {
									rose_search = m_rosedb.searchATC(m_query_str);
								} else {
									med_search = m_sqldb.searchATC(m_query_str);
									if (m_curr_uistate.databaseUsed().equals("favorites"))
										retrieveFavorites();
								}
								sATC();
								cardl.show(p_results, final_atccode);
							} else if (m_query_type == REGISTER) {
								if (m_curr_uistate.isComparisonMode() || m_curr_uistate.isRoseShoppingMode()) {
									rose_search = m_rosedb.searchEan(m_query_str);
								} else {
									med_search = m_sqldb.searchRegNr(m_query_str);
									if (m_curr_uistate.databaseUsed().equals("favorites"))
										retrieveFavorites();
								}
								sRegNr();
								cardl.show(p_results, final_regnr);
							} else if (m_query_type == THERAPY) {
								if (m_curr_uistate.isComparisonMode() || m_curr_uistate.isRoseShoppingMode()) {
									rose_search = m_rosedb.searchTherapy(m_query_str);
								} else {
									med_search = m_sqldb.searchApplication(m_query_str);
									if (m_curr_uistate.databaseUsed().equals("favorites"))
										retrieveFavorites();
								}
								sTherapy();
								cardl.show(p_results, final_therapy);
							} else if (m_query_type == CUSTOMER) {
								customer_search = m_customerdb.searchUser(m_query_str);
								sCustomer();
								cardl.show(p_results, final_customer);
							} else {
								// do nothing
							}
							int num_hits = 0;									
							if (m_curr_uistate.isComparisonMode() || m_curr_uistate.isRoseShoppingMode())
								num_hits = rose_search.size();
							else if (m_query_type==5)
								num_hits = customer_search.size();
							else
								num_hits = med_search.size();
							m_status_label.setText(num_hits + " Suchresultate in " + (System.currentTimeMillis() - m_start_time) / 1000.0f + " Sek.");								

						}
					}
				});
			}
		});

		// Add actionlisteners
		but_title.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (m_curr_uistate.isLoadCart())
					m_curr_uistate.restoreUseMode();
				searchField.setHint(final_search + " " + final_title);
				searchField.requestFocus();				
				m_curr_uistate.setQueryType(m_query_type = 0);
				sTitle();
				cardl.show(p_results, final_title);
			}
		});
		but_auth.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (m_curr_uistate.isLoadCart())
					m_curr_uistate.restoreUseMode();
				searchField.setHint(final_search + " " + final_author);
				searchField.requestFocus();				
				m_curr_uistate.setQueryType(m_query_type = 1);
				sAuth();
				cardl.show(p_results, final_author);
			}
		});
		but_atccode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (m_curr_uistate.isLoadCart())
					m_curr_uistate.restoreUseMode();
				searchField.setHint(final_search + " " + final_atccode);
				searchField.requestFocus();				
				m_curr_uistate.setQueryType(m_query_type = 2);
				sATC();
				cardl.show(p_results, final_atccode);
			}
		});
		but_regnr.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (m_curr_uistate.isLoadCart())
					m_curr_uistate.restoreUseMode();
				searchField.setHint(final_search + " " + final_regnr);
				searchField.requestFocus();
				m_curr_uistate.setQueryType(m_query_type = 3);
				sRegNr();
				cardl.show(p_results, final_regnr);
			}
		});
		but_therapy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (m_curr_uistate.isLoadCart())
					m_curr_uistate.restoreUseMode();
				searchField.setHint(final_search + " " + final_therapy);
				searchField.requestFocus();				
				m_curr_uistate.setQueryType(m_query_type = 4);
				sTherapy();
				cardl.show(p_results, final_therapy);
			}
		});
		but_customer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				searchField.setHint(final_search + " " + final_customer);
				searchField.requestFocus();
				m_curr_uistate.setQueryType(m_query_type = 5);
				customer_search = m_customerdb.searchUser(m_customer_query_str);
				sCustomer();
				cardl.show(p_results, final_customer);				
			}
		});
		
		// Display window
		jframe.pack();
		// jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// jframe.setAlwaysOnTop(true);
		jframe.setVisible(true);

		// Check if user has selected an alternative database
		/*
		 * NOTE: 21/11/2013: This solution is put on ice. Favored is a solution
		 * where the database selected by the user is saved in a default folder
		 * (see variable "m_application_data_folder")
		 */
		/*
		 * try { WindowSaver.loadSettings(jframe); String database_path =
		 * WindowSaver.getDbPath(); if (database_path!=null)
		 * m_sqldb.loadDBFromPath(database_path); } catch(IOException e) {
		 * e.printStackTrace(); }
		 */
		// Load AIPS database
		selectAipsButton.setSelected(true);
		selectFavoritesButton.setSelected(false);
		m_curr_uistate.setUseMode("aips");
		med_search = m_sqldb.searchTitle("");
		sTitle(); // Used instead of sTitle (which is slow)
		cardl.show(p_results, final_title);

		/**
		 * Observers
		 */
		// Attach observer to 'settingspage'
		m_settings_page.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				if (arg instanceof String) {
					System.out.println(arg);
					// Change title
					String user_name = m_prefs.get("name", "");				
					if (!user_name.isEmpty())
						user_name = " - " + user_name.trim();		
					String email_addr = m_prefs.get("emailadresse", "");
					if (!email_addr.isEmpty())
						user_name += " / " + email_addr.trim();
					jframe.setTitle(Constants.APP_NAME + user_name);
					// Get gln code
					m_customer_gln_code = m_prefs.get("glncode", "7610000000000");
					// Change layout according to user id
					boolean but_customer_enabled = isOperator(18) && m_curr_uistate.isShoppingMode();
					but_customer.setVisible(but_customer_enabled);
					but_customer.setEnabled(but_customer_enabled);
					//
					boolean but_comparison_enabled = isOperator(19);
					selectComparisonCartButton.setVisible(but_comparison_enabled);
					selectComparisonCartButton.setEnabled(but_comparison_enabled);							
					// Refresh shopping if user has changed...
					if (m_shopping_cart!=null) {
						// Refresh some stuff
						m_shopping_basket.clear();
						m_map_similar_articles.clear();
						m_shopping_cart.setCustomerGlnCode(m_customer_gln_code);
						m_shopping_cart.saveWithIndex(m_shopping_basket);
						// Desitin
						if (Utilities.appCustomization().equals("desitin")) {
							String ext_gln_code = m_customer_gln_code+"S";
							if (m_user_map.containsKey(ext_gln_code)) {
								User user = m_user_map.get(ext_gln_code);	// using only m_customer_gln_code leads to nullpointer exception!
								m_shopping_cart.setUserCategory(user.category);
							}														
							if (m_map_desitin_conditions.containsKey(m_customer_gln_code)) {
								m_shopping_cart.setMap(m_map_desitin_conditions.get(m_customer_gln_code));
							}
						}
						m_web_panel.updateShoppingHtml();
					}
					// Go back to compendium search mode
					new Toggle().toggleButton(selectAipsButton);
					m_curr_uistate.setUseMode("aips");
					m_curr_uistate.setQueryType(m_query_type=NAME);
					med_search = m_sqldb.searchTitle("");
					sTitle(); // Used instead of sTitle (which is slow)
					but_title.doClick();
					cardl.show(p_results, final_title);			
					// Clean middle pane
					m_web_panel.updateListOfPackages();
					m_web_panel.noclickPage();
				} else if (arg instanceof Address) {
					// Update address
					Address addr = (Address)arg;
					// No need to initialize new HashMap...
					if (m_address_map!=null) {
						m_address_map.put(addr.type, addr);
					}
					// Update checkout page
					m_web_panel.showCheckoutHtml();
				}
			}
		});
		
		// Attach observer to 'm_update'
		m_maindb_update.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				System.out.println(arg);
				// Reset flag
				m_full_db_update = true;
				m_mutex_update = false;				
				db_push_menu.setVisible(false);
				// Refresh some stuff after update
				if (Utilities.appCustomization().equals("ibsa") || Utilities.appCustomization().equals("ywesee")) {
					FileLoader file_loader = new FileLoader();
					m_user_map = file_loader.loadGlnCodes("gln_codes.ser");				
					m_list_of_authors = file_loader.loadAuthors();
					m_emailer.loadAccess();
					//
					if (m_shopping_cart!=null) {
						m_map_ibsa_conditions = file_loader.loadIbsaConditions();
						m_map_ibsa_glns = file_loader.loadIbsaGlns();
						m_shopping_cart.setMaps(m_map_ibsa_glns, m_map_ibsa_conditions);
					}
				} else if (Utilities.appCustomization().equals("desitin")) {
					FileLoader file_loader = new FileLoader();
					m_user_map = file_loader.loadGlnCodes("desitin_gln_codes.ser");				
					m_list_of_authors = file_loader.loadAuthors();
					m_emailer.loadAccess();
					//
					if (m_shopping_cart!=null) {
						if (m_user_map.containsKey(m_customer_gln_code+"S")) {
							User user = m_user_map.get(m_customer_gln_code+"S");	// using only m_customer_gln_code leads to nullpointer exception!
							m_shopping_cart.setUserCategory(user.category);
						}														
						m_map_desitin_conditions = file_loader.loadDesitinConditions();
						if (m_map_desitin_conditions.containsKey(m_customer_gln_code)) {
							m_shopping_cart.setMap(m_map_desitin_conditions.get(m_customer_gln_code));
						}					
					}
				} else if (Utilities.appCustomization().equals("zurrose")) {
					// Load files
					FileLoader file_loader = new FileLoader();
					m_user_map = file_loader.loadRoseUserMap("rose_conditions.ser");
					// Create shopping cart
					if (m_shopping_cart!=null) {
						m_shopping_cart.loadFiles();
						if (m_user_map.containsKey(m_customer_gln_code)) {
							LinkedHashMap<String, Float> rebate_map = m_user_map.get(m_customer_gln_code).rebate_map;
							LinkedHashMap<String, Float> expenses_map = m_user_map.get(m_customer_gln_code).expenses_map;
							m_shopping_cart.setMaps(rebate_map, expenses_map);
						}
					}
				}
				// Empty shopping basket
				if (m_curr_uistate.isShoppingMode()) {
					m_shopping_basket.clear();
					m_map_similar_articles.clear();
					m_customer_gln_code = "";
					m_shopping_cart.saveWithIndex(m_shopping_basket);
					m_web_panel.updateShoppingHtml();
				}
				if (m_curr_uistate.isComparisonMode())
					m_web_panel.setTitle(getTitle("priceComp"));
			}
		});

		// Attach observer to 'm_emailer'
		if (m_emailer!=null) {
			m_emailer.addObserver(new Observer() {
				@Override
				public void update(Observable o, Object arg) {
					System.out.println(arg);
					// Empty shopping basket
					m_shopping_basket.clear();
					m_map_similar_articles.clear();				
					m_shopping_cart.saveWithIndex(m_shopping_basket);
					m_web_panel.updateShoppingHtml();
				}
			});
		}
		
		// Attach observer to "m_comparison_cart"
		if (m_comparison_cart!=null) {
			m_comparison_cart.addObserver(new Observer() {
				@Override
				public void update(Observable o, Object arg) {
					System.out.println(arg);
					m_web_panel.setTitle(getTitle("priceComp"));
					m_comparison_cart.clearUploadList();
					m_web_panel.updateComparisonCartHtml();			
					new AmiKoDialogs(Utilities.appLanguage(), Utilities.appCustomization()).UploadDialog((String)arg);
				}			
			});	
		}
		
		// If command line options are provided start app with a particular title or eancode
		if (commandLineOptionsProvided()) {
			if (!CML_OPT_TITLE.isEmpty())
				startAppWithTitle(but_title);
			else if (!CML_OPT_EANCODE.isEmpty())
				startAppWithEancode(but_regnr);
			else if (!CML_OPT_REGNR.isEmpty())
				startAppWithRegnr(but_regnr);
		}
		
		// Check if new database is online!
		if (Utilities.isInternetReachable()) {
			String db_url = "http://pillbox.oddb.org/amiko_db_full_idx_" + Utilities.appLanguage() + ".zip";			
			String local_file = Utilities.appDataFolder() + "/" + Constants.DEFAULT_AMIKO_DB_BASE + Utilities.appLanguage() + ".db.zip";
			System.out.println(local_file);
			try {
				// Retrieve file length and date of remote file 
				URL uri = new URL(db_url);
				URLConnection ucon = uri.openConnection();
				long size_remote_db_bytes = Long.parseLong(ucon.getHeaderField("Content-Length"));
				long date = ucon.getDate();
				// Local file size
				long size_local_db_bytes = (new File(local_file)).length();
				// Comparison
				long diff_bytes = size_remote_db_bytes - size_local_db_bytes;
				System.out.println("Size difference local and remote db = " + diff_bytes);
				if (diff_bytes!=0) {
					db_push_menu.setVisible(true);
					if (Utilities.appLanguage().equals("de"))
						db_version_item.setText("Version vom " + new SimpleDateFormat("dd.MM.yyyy").format(new Date(date)) + " runterladen...");
					else if (Utilities.appLanguage().equals("fr"))
						db_version_item.setText("Installez la version du " + new SimpleDateFormat("dd.MM.yyyy").format(new Date(date)) + "...");						
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		// Start timer
		Timer global_timer = new Timer();
		// Time checks all 2 minutes (120'000 milliseconds)
		global_timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				checkIfUpdateRequired(updatedb_item);
			}
		}, 2 * 60 * 1000, 2 * 60 * 1000);
	}

	static String getTitle(String key) {
		String updateTime = m_prefs.get("updateTime", "nie");
		DateTime uT = new DateTime(updateTime);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
		String title = m_rb.getString(key) + " (aktualisiert am " + fmt.print(uT) + ")";
		return title;
	}
	
	static int retrieveAipsSearchResults(boolean simple) {
		switch (m_curr_uistate.getQueryType()) {
		case 0:
			if (!simple) {
				if (!m_curr_uistate.isComparisonMode() && !Utilities.isRoseShoppingApp())
					med_search = m_sqldb.searchTitle(m_query_str);
				else
					rose_search = m_rosedb.searchTitle(m_query_str);
			}
			sTitle();
			break;
		case 1:
			if (!simple) {
				if (!m_curr_uistate.isComparisonMode() && !Utilities.isRoseShoppingApp())
					med_search = m_sqldb.searchAuth(m_query_str);
				else
					rose_search = m_rosedb.searchSupplier(m_query_str);
			}
			sAuth();
			break;
		case 2:
			if (!simple) {
				if (!m_curr_uistate.isComparisonMode() && !Utilities.isRoseShoppingApp())	
					med_search = m_sqldb.searchATC(m_query_str);
				else
					rose_search = m_rosedb.searchATC(m_query_str);
			}
			sATC();
			break;
		case 3:
			if (!simple) {
				if (!m_curr_uistate.isComparisonMode() && !Utilities.isRoseShoppingApp())
					med_search = m_sqldb.searchRegNr(m_query_str);
				else
					rose_search = m_rosedb.searchEan(m_query_str);
			}
			sRegNr();
			break;
		case 4:
			if (!simple) {
				if (!m_curr_uistate.isComparisonMode() && !Utilities.isRoseShoppingApp())	
					med_search = m_sqldb.searchIngredient(m_query_str);
				else
					rose_search = m_rosedb.searchTherapy(m_query_str);
			}
			sIngredient();
			break;
		default:
			break;
		}
		if (!simple)
			return med_search.size();
		else
			return rose_search.size();
	}

	static void retrieveFavorites() {
		// Select only subset, remove the rest
		// TODO: optimize!! too slow
		list_of_favorites.clear();
		for (Medication m : med_search) {
			if (favorite_meds_set.contains(m.getRegnrs()))
				list_of_favorites.add(m);
		}
		med_search.clear();
		for (Medication f : list_of_favorites)
			med_search.add(f);
	}

	static void checkIfUpdateRequired(JMenuItem update_item) {
		// Get current date
		DateTime dT = new DateTime();
		// Get stored date
		String updateTime = m_prefs.get("updateTime", dT.now().toString());
		DateTime uT = new DateTime(updateTime);
		// Seconds diffSec = Seconds.secondsBetween(uT, dT);
		Minutes diffMin = Minutes.minutesBetween(uT, dT);
		// Do this only when the application is freshly installed
		int timeDiff = diffMin.getMinutes();
		/*
		if (timeDiff == 0)
			m_prefs.put("updateTime", dT.now().toString());
		*/		
		// First check if everything needs to be updated...
		switch(m_prefs.getInt("update", 0)) {
		case 0: // Manual
			// do nothing
			break;
		case 1: // Daily
			if (timeDiff > 60 * 24) {
				m_full_db_update = true;
				update_item.doClick();
			}
			break;
		case 2: // Weekly
			if (timeDiff > 60 * 24 * 7) {
				m_full_db_update = true;
				update_item.doClick();
			}
			break;
		case 3: // Monthly
			if (timeDiff > 60 * 24 * 30) {
				m_full_db_update = true;
				update_item.doClick();
			}
			break;
		default:
			break;
		}
		
		// else proceed with the Preisvergleich-only update
		switch(m_prefs.getInt("update-comp", 0)) {
		case 0: // Manual
			// do nothing
			break;
		case 1: // Half-hourly
			if (timeDiff % 30 == 0) {
				m_full_db_update = false;
				update_item.doClick();
			}
			break;
		case 2: // Hourly
			if (timeDiff % 60 == 0) {
				m_full_db_update = false;
				update_item.doClick();
			}
			break;
		case 3: // Half-daily
			if (timeDiff % (4 * 60) == 0) {
				m_full_db_update = false;
				update_item.doClick();
			}
			break;
		default:
			break;
		}
	}

	static void startAppWithTitle(JToggleButton but_title) {
		m_query_str = CML_OPT_TITLE;
		med_search = m_sqldb.searchTitle(m_query_str);
		// Check first if search delivers any result
		if (med_search.size() > 0) {
			if (CML_OPT_TYPE.equals("full"))
				but_title.doClick();
			else if (CML_OPT_TYPE.equals("light")) {
				m_query_type = CUSTOMER;
				med_id.clear();
				for (int i = 0; i < med_search.size(); ++i) {
					Medication ms = med_search.get(i);
					ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
					med_id.add(al);
				}
			}
			med_index = 0;
			m_web_panel.updateText();
		}
	}

	static void startAppWithEancode(JToggleButton but_regnr) {
		// Check if delivered eancode is "kosher"
		if (CML_OPT_EANCODE.length() == 13
				&& CML_OPT_EANCODE.indexOf("7680") == 0) {
			// Extract 5-digit registration number
			m_query_str = CML_OPT_EANCODE.substring(4, 9);
			med_search = m_sqldb.searchRegNr(m_query_str);
			// Check first if search delivers any result
			if (med_search.size() > 0) {
				if (CML_OPT_TYPE.equals("full"))
					but_regnr.doClick();
				else if (CML_OPT_TYPE.equals("light")) {
					m_query_type = REGISTER;
					med_id.clear();
					for (int i = 0; i < med_search.size(); ++i) {
						Medication ms = med_search.get(i);
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
						med_id.add(al);
					}
				}
				med_index = 0;
				m_web_panel.updateText();
			}
		} else {
			System.out.println("> Error: Wrong EAN code");
		}
	}

	static void startAppWithRegnr(JToggleButton but_regnr) {
		// Simple check, should be improved...
		if (CML_OPT_REGNR.length() == 5) {
			m_query_str = CML_OPT_REGNR;
			med_search = m_sqldb.searchRegNr(m_query_str);
			// Check first if search delivers any result
			if (med_search.size() > 0) {
				if (CML_OPT_TYPE.equals("full"))
					but_regnr.doClick();
				else if (CML_OPT_TYPE.equals("light")) {
					m_query_type = REGISTER;
					med_id.clear();
					for (int i = 0; i < med_search.size(); ++i) {
						Medication ms = med_search.get(i);
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
						med_id.add(al);
					}
				}
				med_index = 0;
				m_web_panel.updateText();
			}
		} else {
			System.out.println("> Error: Wrong registration number");
		}
	}

	static Map<Product, ArrayList<Long>> pp_list_of_products() {
		// Map title to id in the sqlite database
		Map<Product, ArrayList<Long>> map_id = new TreeMap<Product, ArrayList<Long>>();
		// Preprocessing: generate map of unique names to ids					
		for (Medication ms : med_search) {
			ArrayList<Long> list_of_ids = new ArrayList<Long>();						
			// Use to group title if it exists
			Product product = new Product();
			product.title = ms.getAddInfo();
			product.group_title = ms.getAddInfo();		
			product.author = ms.getAuth();
			product.regnrs = ms.getRegnrs();
			// If there is a group title, use it! group_title = title
			if (product.group_title!=null && !product.group_title.isEmpty()) {
				if (map_id.containsKey(product))
					list_of_ids = map_id.get(product);
				list_of_ids.add(ms.getId());
				map_id.put(product, list_of_ids);
			} else {	
				// No group title available... group_title = empty string
				list_of_ids.add(ms.getId());
				product.title = ms.getTitle();						
				map_id.put(product, list_of_ids);
			}
		}
		return map_id;
	}
	
	static void sTitle() {
		List<String> m = new ArrayList<String>();		
		if (!m_curr_uistate.isComparisonMode()) {
			med_id.clear();
			Pattern p_red = Pattern.compile(".*O]");
			Pattern p_green = Pattern.compile(".*G]");		
			
			if (m_curr_uistate.isSearchMode()) {	// Kompendium and favorites
				if (med_search.size() < BigCellNumber) {
					for (Medication ms : med_search) {
						String pack_info_str = "";
						Scanner pack_str_scanner = new Scanner(ms.getPackInfo());
						while (pack_str_scanner.hasNextLine()) {
							String pack_str_line = pack_str_scanner.nextLine();
							Matcher m_red = p_red.matcher(pack_str_line);
							Matcher m_green = p_green.matcher(pack_str_line);
							if (m_red.find())
								pack_info_str += "<font color=red>" + pack_str_line	+ "</font><br>";
							else if (m_green.find())
								pack_info_str += "<font color=green>" + pack_str_line + "</font><br>";
							else
								pack_info_str += "<font color=gray>" + pack_str_line + "</font><br>";
						}								
						pack_str_scanner.close();				
						m.add("<html><b>" + ms.getTitle() + "</b><br><font size=-1>" + pack_info_str + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
						med_id.add(al);
					}
				} else {
					for (Medication ms : med_search) {				
						m.add("<html><body style=\"width: 1024px;\"><b>" + ms.getTitle() + "</b></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
						med_id.add(al);
					}
				}
			} else if (!m_curr_uistate.isSearchMode()) {
				if (m_curr_uistate.isShoppingMode()) {	// Shopping
					if (Utilities.isRoseShoppingApp()) {
						for (Article as : rose_search) {
							m.add("<html><body style='width: 1024px;'><b>"+ as.getPackTitle() + "</b><br>"
									+ "<font color=gray size=-1>Lager: " + as.getItemsOnStock() 
									+ " (CHF " + as.getCleanExfactoryPrice() + ")" + "</font></html>");
							ArrayList<Long> al = new ArrayList<>(Arrays.asList(as.getId()));
							med_id.add(al);
						}
					} else {
						// Map title to id in the sqlite database
						Map<Product, ArrayList<Long>> map_of_products = pp_list_of_products();
						// Now loop again to compose the mid pane entries
						for (Map.Entry<Product, ArrayList<Long>> entry : map_of_products.entrySet()) {
							Product product = entry.getKey();
							ArrayList<Long> al = entry.getValue();
							if (product.group_title==null || product.group_title.isEmpty())
								m.add("<html><body style=\"width: 1024px;\"><b>" + product.title + "</b></html>");
							else
								m.add("<html><body style=\"width: 1024px;\"><b>" + product.group_title + "</b></html>");
							med_id.add(al);
						}
					}
				} else {	// Interactions
					for (Medication ms : med_search) {
						m.add("<html><body style=\"width: 1024px;\"><b>" + ms.getTitle() + "</b></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
						med_id.add(al);
					}
				}
			}
		} else { 	// Comparison
			list_of_articles.clear();
			for (Article as : rose_search) {
				list_of_articles.add(as);
				m.add("<html><body style='width: 1024px;'><b>"+ as.getPackTitle() + "</b><br>"
						+ "<font color=gray size=-1>Lager: " + as.getItemsOnStock() 
						+ " (CHF " + as.getCleanExfactoryPrice() + ")" + "</font></html>");
			}
		}
		m_list_titles.update(m);
	}

	static void sAuth() {
		List<String> m = new ArrayList<String>();
		if (!m_curr_uistate.isComparisonMode()) {	// Kompendium, favorites, interactions
			med_id.clear();
			if (!m_curr_uistate.isShoppingMode()) {
				if (med_search.size() < BigCellNumber) {
					for (Medication ms : med_search) {
						m.add("<html><b>" + ms.getTitle() + "</b><br>" 
								+ "<font color=gray size=-1>" + ms.getAuth() + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
						med_id.add(al);
					}
				} else {
					for (Medication ms : med_search) {
						m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle() + "</b><br>"
								+ "<font color=gray size=-1>" + ms.getAuth() + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));					
						med_id.add(al);
					}
				}
			} else {	// Shopping
				if (Utilities.isRoseShoppingApp()) {
					for (Article as : rose_search) {
						m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
								+ "</b><br><font color=gray size=-1>" + as.getSupplier() + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(as.getId()));
						med_id.add(al);
					}
				} else {
					// Map title to id in the sqlite database
					Map<Product, ArrayList<Long>> map_id = pp_list_of_products();
					// Now loop again to compose the mid pane entries
					for (Map.Entry<Product, ArrayList<Long>> entry : map_id.entrySet()) {
						Product product = entry.getKey();
						ArrayList<Long> al = entry.getValue();
						if (product.group_title==null || product.group_title.isEmpty()) {
							m.add("<html><body style='width: 1024px;'><b>" + product.title + "</b><br>"
									+ "<font color=gray size=-1>" + product.author + "</font></html>");
						} else {
							m.add("<html><body style='width: 1024px;'><b>" + product.group_title + "</b><br>"
									+ "<font color=gray size=-1>" + product.author + "</font></html>");
						}
						med_id.add(al);
					}
				}
			}
		} else {	// Comparison
			list_of_articles.clear();
			for (int i=0; i<rose_search.size(); ++i) {
				Article as = rose_search.get(i);
				list_of_articles.add(as);
				m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
						+ "</b><br><font color=gray size=-1>" + as.getSupplier() + "</font></html>");
			}
		}
		m_list_auths.update(m);
	}

	static void sATC() {
		List<String> m = new ArrayList<String>();
		if (!m_curr_uistate.isComparisonMode()) {
			med_id.clear();
			if (!m_curr_uistate.isShoppingMode()) {
				// Kompendium, favorites, interactions and shopping
				if (med_search.size() < BigCellNumber) {
					for (int i=0; i<med_search.size(); ++i) {
						Medication ms = med_search.get(i);
						if (ms.getAtcCode() != null) {
							String[] m_code = ms.getAtcCode().split(";");
							String atc_code_str = "";
							String atc_title_str = "";
							if (m_code.length > 1) {
								atc_code_str = m_code[0];
								atc_title_str = m_code[1];
							}							
							if (ms.getAtcClass() != null) {
								String[] m_class = ms.getAtcClass().split(";");
								String atc_class_str = "";

								if (m_class.length == 1) {
									m.add("<html><b>" + ms.getTitle() 
											+ "</b><br><font color=gray size=-1>" + atc_code_str + " - " + atc_title_str
											+ "<br>" + m_class[0] + "</font></html>");									
								} else if (m_class.length == 2) { // *** Ver.<1.2.4
									atc_class_str = m_class[1];
									m.add("<html><b>" + ms.getTitle() 
											+ "</b><br><font color=gray size=-1>" + atc_code_str + " - " + atc_title_str
											+ "<br>" + atc_class_str + "</font></html>");
								} else if (m_class.length == 3) { // *** Ver. 1.2.4 and above
									atc_class_str = "";
									String[] atc_class_l4_and_l5 = m_class[2].split("#");
									if (atc_class_l4_and_l5.length > 0)
										atc_class_str = atc_class_l4_and_l5[atc_class_l4_and_l5.length - 1];
									m.add("<html><b>" + ms.getTitle() + "</b><br>"
											+ "<font color=gray size=-1>" + atc_code_str + " - " + atc_title_str
											+ "<br>" + atc_class_str + "<br>"
											+ m_class[1] + "</font></html>");
								}
							} else {
								m.add("<html><b>" + ms.getTitle() 
										+ "</b><br><font color=gray size=-1>" + atc_code_str + " - " + atc_title_str
										+ "<br>k.A.</font></html>");
							}
							ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
							med_id.add(al);
						}
					}
				} else {
					for (Medication ms : med_search) {
						if (ms.getAtcCode() != null) {
							String[] m_code = ms.getAtcCode().split(";");
							String atc_code_str = "";
							String atc_title_str = "";
							if (m_code.length > 1) {
								atc_code_str = m_code[0];
								atc_title_str = m_code[1];
							}
							m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle()
									+ "</b><br><font color=gray size=-1>" + atc_code_str + " - " + atc_title_str + "</font></html>");
							ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
							med_id.add(al);
						}
					}
				}
			} else {
				if (Utilities.isRoseShoppingApp()) {
					for (Article as : rose_search) {
						m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
								+ "</b><br><font color=gray size=-1>" + as.getAtcCode() + " - " + as.getAtcClass() + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(as.getId()));
						med_id.add(al);
					}
				}
			}
		} else {
			// Comparison
			list_of_articles.clear();
			for (Article as : rose_search) {
				list_of_articles.add(as);
				m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
						+ "</b><br><font color=gray size=-1>" + as.getAtcCode() + " - " + as.getAtcClass() + "</font></html>");
			}
		}
		m_list_atccodes.update(m);
	}

	static void sRegNr() {
		List<String> m = new ArrayList<String>();
		if (!m_curr_uistate.isComparisonMode()) {
			med_id.clear();
			if (!m_curr_uistate.isShoppingMode()) {
				// Kompendium, favorites, interactions and shopping
				if (med_search.size() < BigCellNumber) {
					for (Medication ms : med_search) {
						m.add("<html><b>" + ms.getTitle() + "</b><br>" 
								+ "<font color=gray size=-1>" + ms.getRegnrs() + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));
						med_id.add(al);
					}
				} else {
					for (Medication ms : med_search) {
						m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle()
								+ "</b><br><font color=gray size=-1>" + ms.getRegnrs() + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));					
						med_id.add(al);
					}
				}
			} else {
				if (Utilities.isRoseShoppingApp()) {
					for (Article as : rose_search) {
						m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
								+ "</b><br><font color=gray size=-1>" + as.getPharmaCode() + " (" + as.getEanCode() + ")</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(as.getId()));
						med_id.add(al);
					}
				}
			}
		} else {
			// Comparison
			list_of_articles.clear();
			for (Article as : rose_search) {
				list_of_articles.add(as);
				m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
						+ "</b><br><font color=gray size=-1>" + as.getPharmaCode() + " (" + as.getEanCode() + ")</font></html>");
			}
		}
		m_list_regnrs.update(m);
	}

	static void sTherapy() {
		List<String> m = new ArrayList<String>();
		if (!m_curr_uistate.isComparisonMode()) {
			med_id.clear();
			if (!m_curr_uistate.isShoppingMode()) {
				// Kompendium, favorites, interactions and shopping
				if (med_search.size() < BigCellNumber) {
					for (Medication ms : med_search) {
						if (ms.getApplication() != null) {
							String application_str = ms.getApplication().replaceAll("\n", "<p>");
							application_str = ms.getApplication().replaceAll(";", "<p>");
							m.add("<html><b>" + ms.getTitle()
									+ "</b><br><font color=gray size=-1>" + application_str + "</font></html>");
							ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));						
							med_id.add(al);
						}
					}
				} else {
					for (Medication ms : med_search) {
						if (ms.getApplication() != null) {
							String application_str = ms.getApplication().replaceAll(";", " / ");
							m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle()
									+ "</b><br><font color=gray size=-1>" + application_str + "</font></html>");
							ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));						
							med_id.add(al);
						}
					}
				}
			} else {
				if (Utilities.isRoseShoppingApp()) {
					for (Article as : rose_search) {
						m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
								+ "</b><br><font color=gray size=-1>" + as.getTherapyCode() + " - " + as.getAtcClass() + "</font></html>");
						ArrayList<Long> al = new ArrayList<>(Arrays.asList(as.getId()));
						med_id.add(al);
					}
				}
			}
		} else {
			// Comparison
			list_of_articles.clear();
			for (Article as : rose_search) {
				list_of_articles.add(as);
				m.add("<html><body style='width: 1024px;'><b>" + as.getPackTitle()
						+ "</b><br><font color=gray size=-1>" + as.getTherapyCode() + " - " + as.getAtcClass() + "</font></html>");
			}
		}
		m_list_therapies.update(m);
	}

	static void sCustomer() {
		List<String> m = new ArrayList<String>();
		list_of_gln_codes.clear();
		for (User s : customer_search) {
			String info_str = "";
			if (!s.last_name.isEmpty()) {
				info_str = "<html><body style=\"width:1024px;\"><b>" + s.first_name + " " + s.last_name + "</b><br>";
				if (!s.name1.isEmpty())					
					info_str += "<font color=gray size=-1>" + s.name1 + "</font><br>";						
				else
					info_str += "<font color=gray size=-1>" + "-" + "</font><br>";										
				info_str += "<font color=gray size=-1>" + s.zip + " " + s.city + "</font></body></html>";
			} else if (!s.name1.isEmpty()){
				info_str = "<html><body style=\"width:1024px;\"><b>" + s.name1 + "</b><br>";
				info_str += "<font color=gray size=-1>" + s.street + "</font><br>";
				info_str += "<font color=gray size=-1>" + s.zip + " " + s.city + "</font></body></html>";
			}
			m.add(info_str);
			list_of_gln_codes.add(s);
		}
		m_list_customers.update(m);
	}

	static void sIngredient() {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		if (med_search.size() < BigCellNumber) {
			for (int i = 0; i < med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><b>" + ms.getSubstances()
						+ "</b><br><font color=gray size=-1>" + ms.getTitle() + "</font></html>");
				ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));				
				med_id.add(al);
			}
		} else {
			for (int i = 0; i < med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><body style='width: 1024px;'><b>" + ms.getSubstances()
						+ "</b><br><font color=gray size=-1>" + ms.getTitle() + "</font></html>");
				ArrayList<Long> al = new ArrayList<>(Arrays.asList(ms.getId()));				
				med_id.add(al);
			}
		}
		m_list_ingredients.update(m);
	}
}
