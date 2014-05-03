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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Toolkit;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
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
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
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

import chrriis.dj.nativeswing.NSComponentOptions;
import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserFunction;

public class AMiKoDesk {
	
	// Important Constants
	private static final int BigCellNumber = 512;	
	
	private static String DB_LANGUAGE = "";
	
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
	private static final String DEFAULT_AMIKO_DB_BASE = "amiko_db_full_idx_";
	private static final String DEFAULT_AMIKO_REPORT_BASE = "amiko_report_";
	private static final String DEFAULT_INTERACTION_DB_BASE = "drug_interactions_idx_";
	private static final String DEFAULT_INTERACTION_CSV_BASE = "drug_interactions_csv_";
	private static final String IMG_FOLDER = "./images/";	
	private static final String HTML_FILES = "./fis/fi_de_html/";
	private static final String CSS_SHEET = "./css/amiko_stylesheet.css";
	private static final String INTERACTIONS_SHEET = "./css/interactions_css.css";
	private static final String JS_FOLDER = "./jscripts/";
	private static List<String> med_content = new ArrayList<String>();
	private static List<Long> med_id = new ArrayList<Long>();
	private static List<Medication> med_search = new ArrayList<Medication>();
	private static List<Medication> med_title = new ArrayList<Medication>();
	private static List<Medication> list_of_favorites = new ArrayList<Medication>();
	private static Map<String, Medication> m_med_basket = new TreeMap<String, Medication>();
	private static HashSet<String> favorite_meds_set;
	private static DataStore favorite_data = null;
	private static String m_database_used = "aips";
	private static boolean m_seek_interactions = false;
	private static ProgressIndicator m_progress_indicator = new ProgressIndicator(32);
	
	// German section title abbreviations
	private static final String[] SectionTitle_DE = {"Zusammensetzung", "Galenische Form", "Kontraindikationen", 
		"Indikationen", "Dosierung/Anwendung", "Vorsichtsmassnahmen", "Interaktionen", "Schwangerschaft", 
		"Fahrtüchtigkeit", "Unerwünschte Wirk.", "Überdosierung", "Eig./Wirkung", "Kinetik", "Präklinik", 
		"Sonstige Hinweise", "Zulassungsnummer", "Packungen", "Inhaberin", "Stand der Information"};	
	// French section title abbrevations
	private static final String[] SectionTitle_FR = {"Composition", "Forme galénique", "Contre-indications", 
		"Indications", "Posologie", "Précautions", "Interactions", "Grossesse/All.", 
		"Conduite", "Effets indésir.", "Surdosage", "Propriétés/Effets", "Cinétique", "Préclinique", 
		"Remarques", "Numéro d'autorisation", "Présentation", "Titulaire", "Mise à jour"};	
	
	private static int med_index = -1;
	private static IndexPanel m_section_titles = null;
	private static WebPanel2 m_web_panel = null;
	private static String m_css_str = null;
	private static String m_css_interactions_str = null;
	private static String m_js_deleterow_str = null;
	private static String m_query_str = null;
	private static SqlDatabase m_sqldb = null;
	// private static InteractionsDb m_interdb = null;
	private static Map<String, String> m_interactions_map = null;
	private static List<String> m_section_str = null;
	private static String m_application_data_folder = null;
	
	// Panels
	private static ListPanel m_list_titles = null;
	private static ListPanel m_list_auths = null;
	private static ListPanel m_list_regnrs = null;
	private static ListPanel m_list_atccodes = null;
	private static ListPanel m_list_ingredients = null;
	private static ListPanel m_list_therapies = null;

	// Colors
	private static Color m_but_color = new Color(220,225,255);
	
	// 0: Präparat, 1: Inhaber, 2: ATC Code, 3: Reg. Nr., 4: Wirkstoff, 5: Therapie
	// -> {0, 1, 2, 3, 4, 5, 6};
	private static int m_query_type = 0;
	
	/**
	 * Adds an option into the command line parser
	 * @param optionName - the option name
	 * @param description - option descriptiuon
	 * @param hasValue - if set to true, --option=value, otherwise, --option is a boolean
	 * @param isMandatory - if set to true, the option must be provided.
	 */
	@SuppressWarnings("static-access")
	
	static void addOption(Options opts, String optionName, String description, boolean hasValue, boolean isMandatory) {
		OptionBuilder opt = OptionBuilder.withLongOpt(optionName);
		opt = opt.withDescription(description);
		if (hasValue) 
			opt = opt.hasArg();
		if(isMandatory) 
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
				System.out.println("Version of amikodesk: " + Constants.APP_VERSION);
			}
			if (cmd.hasOption("port")) {
				int port = Integer.parseInt(cmd.getOptionValue("port"));
				if (port>999 && port<9999)
					CML_OPT_SERVER = true;
				System.out.print("Initializing TCP server... ");					
				mTcpServer = new AppServer(port);
				// NOTE: Must be stopped at a certain point...				
				new Thread(mTcpServer).start();
				System.out.println("done");
			}
			if (cmd.hasOption("width")) {
				int width = Integer.parseInt(cmd.getOptionValue("width"));
				if (width>1024 && width<=1920)
					CML_OPT_WIDTH = width;					
			}
			if (cmd.hasOption("height")) {
				int height = Integer.parseInt(cmd.getOptionValue("height"));
				if (height>768 && height<=1200)
					CML_OPT_WIDTH = height;									
			}
			if (cmd.hasOption("lang")) {
				if (cmd.getOptionValue("lang").equals("de")) {
					// Check if db exists
					File wfile = new File("./dbs/amiko_db_full_idx_de.db");
					if (!wfile.exists())
						System.out.println("> Error: amiko_db_full_idx_de.db not in directory ./dbs");
					DB_LANGUAGE = "DE"; 
				}
				else if (cmd.getOptionValue("lang").equals("fr")) {
					// Check if db exists
					File wfile = new File("./dbs/amiko_db_full_idx_fr.db");
					if (!wfile.exists())
						System.out.println("> Error: amiko_db_full_idx_fr.db not in directory ./dbs");
					DB_LANGUAGE = "FR";								
				}
			}
			if (cmd.hasOption("type")) {
				String type = cmd.getOptionValue("type");
				if (type!=null && !type.isEmpty())
					CML_OPT_TYPE = type;
			}
			if (cmd.hasOption("title")) {
				String title = cmd.getOptionValue("title");
				if (title!=null && !title.isEmpty())
					CML_OPT_TITLE = title;
			}
			if (cmd.hasOption("eancode")) {
				String eancode = cmd.getOptionValue("eancode");
				if (eancode!=null && !eancode.isEmpty())
					CML_OPT_EANCODE = eancode;
			}
			if (cmd.hasOption("regnr")) {
				String regnr = cmd.getOptionValue("regnr");
				if (regnr!=null && !regnr.isEmpty())
					CML_OPT_REGNR = regnr;
			}
		} catch(ParseException e) {
			System.err.println("Parsing failed: " + e.getMessage());
		}
	}			
	
	private static boolean commandLineOptionsProvided() {
		return (!CML_OPT_TYPE.isEmpty() && (
				!CML_OPT_TITLE.isEmpty() || !CML_OPT_EANCODE.isEmpty() || !CML_OPT_REGNR.isEmpty() || 
				CML_OPT_SERVER==true));
	}

	private static String appLanguage() {
		if (DB_LANGUAGE.equals("DE"))
			return "de";
		else if (DB_LANGUAGE.equals("FR"))
			return "fr";
		else if (Constants.APP_NAME.equals(Constants.AMIKO_NAME) || Constants.APP_NAME.equals(Constants.AMIKO_DESITIN_NAME) 
				|| Constants.APP_NAME.equals(Constants.AMIKO_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.AMIKO_ZURROSE_NAME)) {
			return "de";
		} else if (Constants.APP_NAME.equals(Constants.COMED_NAME) || Constants.APP_NAME.equals(Constants.COMED_DESITIN_NAME) 
				|| Constants.APP_NAME.equals(Constants.COMED_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.COMED_ZURROSE_NAME)) {
			return "fr";
		}
		return "";
	}

	private static String appCustomization() {		
		if (Constants.APP_NAME.equals(Constants.AMIKO_NAME) || Constants.APP_NAME.equals(Constants.COMED_NAME)) {
			return "ywesee";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_DESITIN_NAME) || Constants.APP_NAME.equals(Constants.COMED_DESITIN_NAME)) {
			return "desitin";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.COMED_MEDDRUGS_NAME)) {
			return "meddrugs";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_ZURROSE_NAME) || Constants.APP_NAME.equals(Constants.COMED_ZURROSE_NAME)) {
			return "zurrose";
		}
		return "";
	}
	
	public static void main(String[] args) {		
			
		// Initialize globales
		m_application_data_folder = System.getenv("APPDATA") + "\\Ywesee\\" + Constants.APP_NAME;
       	favorite_meds_set = new HashSet<String>();		
		favorite_data = new DataStore(m_application_data_folder);
		favorite_meds_set = favorite_data.load();	// HashSet containing registration numbers

		// Register toolkit
		Toolkit tk = Toolkit.getDefaultToolkit();
        tk.addAWTEventListener(WindowSaver.getInstance(m_application_data_folder), AWTEvent.WINDOW_EVENT_MASK);		
		
		// Specify command line options
		Options options = new Options();
		addOption(options, "help", "print this message", false, false );
		addOption(options, "version", "print the version information and exit", false, false);
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
		
		if (appCustomization().equals("desitin")) {
			new SplashWindow(Constants.APP_NAME, 5000);
		} else if (appCustomization().equals("meddrugs")) {
			new SplashWindow(Constants.APP_NAME, 5000);
		} else if (appCustomization().equals("zurrose")) {
			new SplashWindow(Constants.APP_NAME, 3000);
		}
		// Load css style sheet
		m_css_str = "<style>" + readFromFile(CSS_SHEET) + "</style>";
		// Load interactions css style sheet
		m_css_interactions_str = "<style>" + readFromFile(INTERACTIONS_SHEET) + "</style>";
		// Load delete row javascript
		m_js_deleterow_str = readFromFile(JS_FOLDER + "deleterow.js");
		
		// Load main database
		m_sqldb = new SqlDatabase();
		// Attempt to load alternative database. if db does not exist, load default database
		// These databases are NEVER zipped!
		if (m_sqldb.loadDBFromPath(m_application_data_folder + "\\" + DEFAULT_AMIKO_DB_BASE + appLanguage() + ".db")==0) {
			System.out.println("Loading default database");
			if (appLanguage().equals("de"))
				m_sqldb.loadDB("de");
			else if (appLanguage().equals("fr"))
				m_sqldb.loadDB("fr");
		}
		// Load drug interaction sqlite database
		/*
		m_interdb = new InteractionsDb();
		if (m_interdb.loadDBFromPath(m_application_data_folder + "\\" + DEFAULT_INTERACTION_DB_BASE + appLanguage() + ".db")==0) {
			System.out.println("Loading default interactions database");
			if (appLanguage().equals("de"))
				m_interdb.loadDB("de");
			else if (appLanguage().equals("fr"))
				m_interdb.loadDB("fr");
		}
		*/
		// Load drug interactions csv file 
		m_interactions_map = readFromCsvToMap(m_application_data_folder + "\\" + DEFAULT_INTERACTION_CSV_BASE + appLanguage() + ".csv");
		if (m_interactions_map==null) {
			System.out.println("Loading default drug interactions csv file");
			m_interactions_map = readFromCsvToMap("./dbs/" + DEFAULT_INTERACTION_CSV_BASE + appLanguage() + ".csv");
		}
		// UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();
		NativeSwing.initialize();

        // Setup font size based on screen size  
        UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Dialog", Font.PLAIN, 14));
		UIManager.put("Label.font", new Font("Dialog", Font.PLAIN, 14));  
        UIManager.put("Button.font", new Font("Dialog", Font.BOLD, 14));
        UIManager.put("Menu.font", new Font("Dialog", Font.PLAIN, 14));
        UIManager.put("MenuBar.font", new Font("Dialog", Font.PLAIN, 14));
        UIManager.put("MenuItem.font", new Font("Dialog", Font.PLAIN, 14));
        UIManager.put("ToolBar.font", new Font("Dialog", Font.PLAIN, 12));
        UIManager.put("ToggleButton.font", new Font("Dialog", Font.PLAIN, 12));
        UIManager.put("ToggleButton.select", new Color(240,240,240));
        
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				if (!commandLineOptionsProvided()) {
					System.out.println("No relevant command line options provided... creating full GUI");
					createAndShowFullGUI();
				}
				else if (CML_OPT_TYPE.equals("full")) {
					System.out.println("Creating full GUI");
					createAndShowFullGUI();
				}
				else if (CML_OPT_TYPE.equals("light")) {
					System.out.println("Creating light GUI");
					createAndShowLightGUI();
				}
			}
		});

		NativeInterface.runEventPump();
	}	
	
	static class CheckListRenderer extends JCheckBox implements ListCellRenderer<Object> {
		
		final static Icon imgFavNotSelected = new ImageIcon(IMG_FOLDER+"28-star-gy.png");
		final static Icon imgFavSelected = new ImageIcon(IMG_FOLDER+"28-star-ye.png");

		public CheckListRenderer() {
			setOpaque(true);
		}
		
		/* Method called when it's time to draw each cell
		 * Returns the specific rendering for that one cell of the JList
		 * (non-Javadoc)
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 * 
		 * Note: Swing insists on accessing each item in the entire ListModel while getting it displayed on screen. 
		 * Furthermore, after accessing all the items, Swing then re-accesses the first n number of items visible on screen 
		 * (in the viewport, not off screen below).
		 */				
		public Component getListCellRendererComponent(JList<?> list, Object value, 
				int index, boolean isSelected, boolean hasFocus)
		{	
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setText(value.toString());	
			
			// System.out.println("getListCellRenderer");
	
			if (isSelected) {
				setBackground(new Color(230,230,230));
				setForeground(list.getForeground());				
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			// Extract registration number corresponding to index
			String regnrs = med_search.get(index).getRegnrs();
			if (favorite_meds_set.contains(regnrs)) 
				setIcon(imgFavSelected);
			else				
				setIcon(imgFavNotSelected);
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
			this.fireContentsChanged(this, model.size()-1, model.size()-1);
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
	 * Panel on the left side displaying the results of the search
	 * @author Max
	 *
	 */
	static class ListPanel extends JPanel implements ListSelectionListener, FocusListener {
		
		private JList<String> list = null;
		private JScrollPane jscroll = null;
		
		public ListPanel() {
			super(new BorderLayout());
			
			// String[] titles = med_title.toArray(new String[med_title.size()]);
			// list = new JList<String>(titles);	
			
		    DefaultListModel<String> model = new DefaultListModel<String>();
			list = new JList<String>(model);
			list.setSelectedIndex(0);
			list.setCellRenderer(new CheckListRenderer());
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectionBackground(new Color(240,240,240));
			list.setSelectionForeground(Color.BLACK);
			list.setFont(new Font("Dialog", Font.PLAIN, 14));
			list.addListSelectionListener(this);
					
		    MouseListener mouseListener = new MouseAdapter() {
		        public void mouseClicked(MouseEvent mouseEvent) {
		        	// JList theList = (JList) mouseEvent.getSource();
		        	if (mouseEvent.getClickCount() == 1) {
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
		    };
		    list.addMouseListener(mouseListener);			
			
			JPanel listPanel = new JPanel(new BorderLayout());
			if (appLanguage().equals("de")) {
		        TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Suchresultat", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				listPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));
			} else if (appLanguage().equals("fr")) {
		        TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Résultat de la recherche", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				listPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));			
			}
			
			// Add the list to a scrolling panel
			jscroll = new JScrollPane(list);
			listPanel.add(jscroll, BorderLayout.CENTER);
			add(listPanel, BorderLayout.CENTER);
			
			setFocusable(true);
			requestFocusInWindow();
		}
		
		/**
		 * Updates the data in the ListPanel
		 * @param lStr
		 */
		public void update(List<String> lStr) {
			CustomListModel dlm = new CustomListModel(lStr);
			
			if (lStr.size()>BigCellNumber)
				list.setPrototypeCellValue(dlm.getElementAt(0));
			else {
				list.setPrototypeCellValue(null);		// Does not work!
				list.setFixedCellHeight(-1);
			}
			
			// System.out.println("update");
			list.setModel(dlm);
			jscroll.revalidate();
			jscroll.repaint();	
		}
		
		/**
		 * Called when somebody clicks on the ListPanel
		 */
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				med_index = list.getSelectedIndex();
				if (!m_seek_interactions) {
					// Display Fachinfos in the web panel
					m_web_panel.updateText();
				} else {
					m_web_panel.doInteractions();
				}
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
	
	static class IndexPanel extends JPanel implements ListSelectionListener {
		
		private JList<String> list = null;
		
		public IndexPanel(String[] sec_titles) {			
			super(new BorderLayout());
			
			JPanel indexPanel = new JPanel(new BorderLayout());
			// indexPanel.setBorder(BorderFactory.createTitledBorder("Index"));
			indexPanel.setBorder(null);
			list = new JList<String>(sec_titles);
			// list.setSelectedIndex(0);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectionBackground(Color.BLUE);
			list.setSelectionForeground(Color.WHITE);
			list.setFont(new Font("Dialog", Font.PLAIN, 13));
			list.addListSelectionListener(this);
			
			indexPanel.add(list, BorderLayout.CENTER);
			add(indexPanel, BorderLayout.CENTER);
		}	
		
		public void updatePanel(String[] sec_titles) {
			final String[] titles = sec_titles;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {	
					if (titles!=null) {
						list.removeAll();
						list.setListData(titles);
					}
				}
			});
		}
		
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				int sel_index = list.getSelectedIndex();
				if (sel_index>=0)
					m_web_panel.moveToAnchor(m_section_str.get(sel_index));
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
			for (int i=0; i<med_title.size(); ++i) {
				titles.add(med_title.get(i).getTitle());
			}
			
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Fach Infos (DE)");
			DefaultMutableTreeNode child;
			DefaultMutableTreeNode grandChild;
			for (char childStart='A'; childStart<='Z'; childStart++) {
				child = new DefaultMutableTreeNode(childStart);
				root.add(child);
				for (int grandChildIndex=0; grandChildIndex<titles.size(); grandChildIndex++) {
					if (titles.get(grandChildIndex).startsWith(Character.toString(childStart))) {
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
			
			DefaultTreeModel model = (DefaultTreeModel)jtree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();			
			root.removeAllChildren();
			DefaultMutableTreeNode child;
			DefaultMutableTreeNode grandChild;
			for (char childStart='A'; childStart<='Z'; childStart++) {
				child = new DefaultMutableTreeNode(childStart);
				root.add(child);
				for (int grandChildIndex=0; grandChildIndex<entries.length; grandChildIndex++) {
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
			for (int i=0; i<entries.length; ++i) {
				if (entries[i].equals(jtree.getLastSelectedPathComponent().toString())) {
					med_index = i;
					break;
				}
			}
			m_web_panel.updateText();
		}
	}
	
	static class WebPanel2 extends JPanel {
		
		private JWebBrowser jWeb = null;
		private StringBuffer content_str = null;
		private TitledBorder titledBorder = null;
		private JPanel webBrowserPanel = null;
		
		public WebPanel2() {
			// YET another mega-hack ;)
			super(new BorderLayout());
			webBrowserPanel = new JPanel(new BorderLayout());
			if (appLanguage().equals("de")) {				
		        titledBorder = BorderFactory.createTitledBorder(null, "Fachinformation", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				webBrowserPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));			
			} else if (appLanguage().equals("fr")) {
		        titledBorder = BorderFactory.createTitledBorder(null, "Notice Infopro", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				webBrowserPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));
			}
			jWeb = new JWebBrowser(NSComponentOptions.destroyOnFinalization());
			
			/**
			 * Add function called by javascript
			 * This trick fools JWebBrowser lack of javascript return values (kinda cumbersome...)
			 */
			jWeb.registerFunction(new WebBrowserFunction("invokeJava") {
				@Override
				public Object invoke(JWebBrowser webBrowser, Object... args) {
					// int row = (int)Float.parseFloat(args[0].toString());
					String row_key = args[0].toString().trim();
					// System.out.println(getName() + " -> key = " + row_key + " / num rows = " + args[1]);
					if (row_key.equals("Delete all"))
						m_med_basket.clear();
					else
						m_med_basket.remove(row_key);
					m_web_panel.updateInteractionHtml();
					return "true";
				}
			});

			/*
			JWebBrowserWindow jWebWindow = WebBrowserWindowFactory.create(jWeb);
			jWebWindow.setBarsVisible(false);
			*/			
			jWeb.setBarsVisible(false);
			webBrowserPanel.add(jWeb, BorderLayout.CENTER);
			// jWeb.setMinimumSize(new Dimension(640, 640));
			// ---> jWeb.setPreferredSize(new Dimension(640, 600));
			// jWeb.setBorder(BorderFactory.createLineBorder(Color.GRAY));			
			add(webBrowserPanel, BorderLayout.CENTER);
		}
		
		public void setTitle(String title) {
			titledBorder.setTitle(title);
			webBrowserPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));				
		}
		
		public void moveToAnchor(String anchor) {
			anchor = anchor.replaceAll("<html>", "").replaceAll("</html>", "").replaceAll(" &rarr; ", "-");	// Spaces before and after of &rarr; are important...
			// System.out.println(anchor);
			jWeb.executeJavascript("document.getElementById('" + anchor + "').scrollIntoView(true);");
		}
		
		public void updateSectionTitles(Medication m) {
			// Get section titles
			String[] titles = m.getSectionTitles().split(";");
			// Use abbreviations...
			Locale locale = null;
			String[] section_titles = null;
			if (appLanguage().equals("de")) {
				locale = Locale.GERMAN;
				section_titles = SectionTitle_DE;
			} else if (appLanguage().equals("fr")) {
				locale = Locale.FRENCH;
				section_titles = SectionTitle_FR;
			}
			for (int i=0; i<titles.length; ++i) {					
	    		for (String s : section_titles) {
	    			String titleA = titles[i].replaceAll(" ", "");
	    			String titleB = m.getTitle().replaceAll(" ", "");
	    			if (titleA.toLowerCase(locale).contains(titleB.toLowerCase(locale))) {
	    				if (titles[i].contains("®"))
	    					titles[i] = titles[i].substring(0,titles[i].indexOf("®")+1);
	    				else
	    					titles[i] = titles[i].split(" ")[0].replaceAll("/-", "");
	    				break;
	    			}
	    			else if (titles[i].toLowerCase(locale).contains(s.toLowerCase(locale))) {
	    				titles[i] = s;
	    				break;
	    			}
	    		}
			}
			m_section_titles.updatePanel(titles);
		}
		
		public void updateText() {
			if (med_index>=0) {
				Medication m =  m_sqldb.getMediWithId(med_id.get(med_index));
				// Get section ids
				String[] sections = m.getSectionIds().split(",");
				m_section_str = Arrays.asList(sections);
				// Get FI content
				content_str = new StringBuffer(m.getContent());

				// Update section titles		
				updateSectionTitles(m);				
				
				// DateFormat df = new SimpleDateFormat("dd.MM.yy");
				String _amiko_str = Constants.APP_NAME + " - Datenstand AIPS Swissmedic " + Constants.GEN_DATE;
				content_str = content_str.insert(content_str.indexOf("<head>"), "<title>" + _amiko_str + "</title>");
				content_str = content_str.insert(content_str.indexOf("</head>"), m_css_str);
				jWeb.setJavascriptEnabled(true);
				
				if (CML_OPT_SERVER==false) {
					try {
						// Currently preferred solution, html saved in C:/Users/ ... folder
						String path_html = System.getProperty ("user.home") + "/" + Constants.APP_NAME +"/htmls/";
						String _title = m.getTitle();					
						String file_name = _title.replaceAll("[®,/;.]","_") + ".html";
						writeToFile(content_str.toString(), path_html, file_name);
						jWeb.navigate("file:///" + path_html + file_name);
					} catch(IOException e) {
						// Fallback solution (used to be preferred implementation)
						jWeb.setHTMLContent(content_str.toString());
					}
				} else {
					// Original fallback solution works well and is fast...
					jWeb.setHTMLContent(content_str.toString());					
				}
				
				jWeb.setVisible(true);
			}
		}
		
		private String addColorLegend() {
			String legend;
		    /*
		     Risikoklassen
		     -------------
			     A: Keine Massnahmen notwendig (grün)
			     B: Vorsichtsmassnahmen empfohlen (gelb)
			     C: Regelmässige Überwachung (orange)
			     D: Kombination vermeiden (pinky)
			     X: Kontraindiziert (hellrot)
			     0: Keine Angaben (grau)
		    */
			legend = "<table id=\"Legende\" width=\"100%25\">";
			legend += "<tr><td bgcolor=\"#caff70\"></td>" +
					"<td>A</td>" +
					"<td>Keine Massnahmen notwendig</td></tr>";
			legend += "<tr><td bgcolor=\"#ffec8b\"></td>" +
					"<td>B</td>" +
					"<td>Vorsichtsmassnahmen empfohlen</td></tr>";
			legend += "<tr><td bgcolor=\"#ffb90f\"></td>" +
					"<td>C</td>" +
					"<td>Regelmässige Überwachung</td></tr>";
			legend += "<tr><td bgcolor=\"#ff82ab\"></td>" +
					"<td>D</td>" +
					"<td>Kombination vermeiden</td></tr>";
			legend += "<tr><td bgcolor=\"#ff6a6a\"></td>" +
					"<td>X</td>" +
					"<td>Kontraindiziert</td></tr>";		
			legend += "<tr><td bgcolor=\"#dddddd\"></td>" +
					"<td>0</td>" +
					"<td>Keine Angaben</td></tr>";				
			legend += "</table>";
			
			return legend;
		}
		
		public void updateInteractionHtml() {
			// Redisplay selected meds
			String basket_html_str = "<table id=\"Interaktionen\" width=\"100%25\">";
			String delete_all_button_str = "";
			String interactions_html_str = "";
			String top_note_html_str = "";
			String legend_html_str = "";
			String bottom_note_html_str = "";
			String atc_code1 = "";
			String atc_code2 = "";
			String name1 = "";
			String[] m_code1 = null;
			String[] m_code2 = null;
			int med_counter = 1;
						
			// Build interaction basket table
			if (m_med_basket.size()>0) {
				for (Map.Entry<String, Medication> entry1 : m_med_basket.entrySet()) {
					m_code1 = entry1.getValue().getAtcCode().split(";");
					atc_code1 = "k.A.";
					name1 = "k.A.";
					if (m_code1.length>1) {
						atc_code1 = m_code1[0];
						name1 = m_code1[1];
					}
					basket_html_str += "<tr>";
					basket_html_str += "<td>" + med_counter + "</td>"
							+ "<td>" + entry1.getKey() + " </td> " 
							+ "<td>" + atc_code1 + "</td>"
							+ "<td>" + name1 + "</td>"
							+ "<td align=\"right\">" + "<input type=\"button\" value=\"löschen\" onclick=\"deleteRow('Interaktionen',this)\" />" + "</td>";
					basket_html_str += "</tr>";
					med_counter++;					
				}
				basket_html_str += "</table>";
				// Medikamentenkorb löschen
				delete_all_button_str = "<div id=\"Delete_all\"><input type=\"button\" value=\"alle löschen\" onclick=\"deleteRow('Delete_all',this)\" /></div>";				
			} else {
				// Medikamentenkorb ist leer
				basket_html_str = "<div>Ihr Medikamentenkorb ist leer.<br><br></div>";
			}

			// Build list of interactions
			m_section_str = new ArrayList<String>();
			// Add table to section titles
			m_section_str.add("Interaktionen");
			if (med_counter>1) {
				for (Map.Entry<String, Medication> entry1 : m_med_basket.entrySet()) {
					m_code1 = entry1.getValue().getAtcCode().split(";");
					if (m_code1.length>1) {
						// Get ATC code of first drug, make sure to get the first in the list (the second one is not used)
						atc_code1 = m_code1[0].split(",")[0];
						for (Map.Entry<String, Medication> entry2 : m_med_basket.entrySet()) {
							m_code2 = entry2.getValue().getAtcCode().split(";");
							if (m_code2.length>1) {
								// Get ATC code of second drug
								atc_code2 = m_code2[0];						
								if (atc_code1!=null && atc_code2!=null && !atc_code1.equals(atc_code2)) {
									// Get html interaction content from drug interactions map
									String inter = m_interactions_map.get(atc_code1 + "-" + atc_code2);
									if (inter!=null) {
										inter = inter.replaceAll(atc_code1, entry1.getKey());
										inter = inter.replaceAll(atc_code2, entry2.getKey());
										interactions_html_str += (inter + "");
										// Add title to section title list
										if (!inter.isEmpty())
											m_section_str.add("<html>" + entry1.getKey() + " &rarr; " + entry2.getKey() + "</html>");
									}
									/*
									// Get html interaction content from interaction database
									List<String> interactions = m_interdb.searchATC(atc_code1, atc_code2);
									for (String inter : interactions) {
										inter = inter.replaceAll(atc_code1, entry1.getKey());
										inter = inter.replaceAll(atc_code2, entry2.getKey());
										interactions_html_str += (inter + "");
										// Add title to section title list
										if (!inter.isEmpty())
											m_section_str.add("<html>" + entry1.getKey() + " &rarr; " + entry2.getKey() + "</html>");
									}
									*/
								}
							}
						}
					}
				}
			}
			
			if (m_med_basket.size()>0 && m_section_str.size()<2) {
				// Add note to indicate that there are no interactions
				top_note_html_str = "<p class=\"paragraph0\">Werden keine Interaktionen angezeigt, sind z.Z. keine Interaktionen bekannt.</p><br><br>";			
			} else if (m_med_basket.size()>0 && m_section_str.size()>1) {
				// Add color legend
				legend_html_str = addColorLegend();				
				// Add legend to section titles
				m_section_str.add("Legende");	
			}
			bottom_note_html_str += "<p class=\"footnote\">1. Datenquelle: Public Domain Daten von EPha.ch.</p> " +
					"<p class=\"footnote\">2. Unterstützt durch:  IBSA Institut Biochimique SA.</p>";
			
			String jscript_str = "<script> language=\"javascript\">" + m_js_deleterow_str + "</script>";
			String html_str = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + jscript_str + m_css_interactions_str + "</head><body><div id=\"interactions\">" 
					+ basket_html_str + delete_all_button_str + "<br><br>" + top_note_html_str
					+ interactions_html_str + "<br>" + legend_html_str + "<br>" + bottom_note_html_str + "</body></div></html>";
			
			// Update section titles
			String[] titles = m_section_str.toArray(new String[m_section_str.size()]);
			m_section_titles.updatePanel(titles);

			// Update html
			jWeb.setJavascriptEnabled(true);
			jWeb.setHTMLContent(html_str);
			jWeb.setVisible(true);
		}
		
		public void doInteractions() {
			// Display interactions in the web panel
			if (med_index>=0) {
				// Get medication which was clicked on...
				Medication m = m_sqldb.getMediWithId(med_id.get(med_index));
				// Add med to basket if not already in basket 
				String title = m.getTitle().trim();
				if (title.length()>30)
					title = title.substring(0, 30) +"...";
				if (!m_med_basket.containsKey(title))
					m_med_basket.put(title, m);
				updateInteractionHtml();
			} else {
				// Medikamentenkorb ist leer
				updateInteractionHtml();				
			}
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
			JScrollPane jScroll = new JScrollPane(jText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			jScroll.setPreferredSize(new Dimension(704, 800));			
			add(jScroll);
		}
		
		public void updateText() {
			if (med_index>=0) {
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
				css_str = readFromFileFast(HTML_FILES + "amiko_stylesheet.css", StandardCharsets.UTF_8);
				System.out.println(css_str);
			} catch(IOException e) {
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
			if (med_index>=0) {
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
	         g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
	         super.paintComponent(g);
	    }
	    protected void paintBorder(Graphics g) {
	         g.setColor(getForeground());
	         g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
	    }
	    public boolean contains(int x, int y) {
	         if (shape == null || !shape.getBounds().equals(getBounds())) {
	             shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 15, 15);
	         }
	         return shape.contains(x, y);
	    }
	}
	
	static class SearchField extends JTextField implements FocusListener {

	    private final String hint;
		private Icon icon;
		private Insets insets;
		
	    public SearchField(final String hint) {
	        super(hint);
	        super.addFocusListener(this);	        
			this.setFont(new Font("Dialog", Font.PLAIN, 14));
	        this.hint = hint;      
	        /*
	        this.setBackground(new Color(240,240,240));
	        TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Suche", 
	        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
	        		new Font("Dialog", Font.PLAIN, 14));
	        this.setBorder(BorderFactory.createTitledBorder(titledBorder));
	        */
	        // this.setBorder(BorderFactory.createLineBorder(new Color(220,250,250)));
	        // this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	        this.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
	        this.setBackground(new Color(220,250,250));
	        this.setEditable(true);
	        
	        this.icon = new ImageIcon(IMG_FOLDER+"mag_glass_16x16.png");
	        Border border = UIManager.getBorder("TextField.border");
	        insets = border.getBorderInsets(this);
	        
	        Border empty = new EmptyBorder(0, 0, 0, 0);
	        setBorder(new CompoundBorder(border, empty));	        
	    }

	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);	 
	        int textX = 2;	 
	        if (icon!=null) {
	            int iconWidth = icon.getIconWidth();
	            int iconHeight = icon.getIconHeight();
	            int x = insets.left + 3;		// icon's x coordinate
	            textX = x + iconWidth + 2; 		// this is the x where text should start
	            int y = (this.getHeight() - iconHeight)/2;
	            icon.paintIcon(this, g, x, y);
	        }	 
	        setMargin(new Insets(2, textX, 2, 2));		        
	    }    
	    
	    @Override
	    public void focusGained(FocusEvent e) {
	    	if(!this.getText().isEmpty()) {
	            super.setText("");
	        }
	        this.setEditable(true);
	        this.requestFocus();
	    }
	    
	    @Override
	    public void focusLost(FocusEvent e) {
	        if(this.getText().isEmpty()) {
	            super.setText(hint);
	        }
	    }
	    
	    @Override
	    public String getText() {
	        String typed = super.getText();
	        return typed.equals(hint) ? "" : typed;
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
        int x = (screen.width-min_width)/2;
        int y = (screen.height-min_height)/2;
        jframe.setBounds(x,y,min_width,min_height);		
        
		// Action listeners
		jframe.addWindowListener(new WindowListener() {
			// Use WindowAdapter!
	        @Override 
	        public void windowOpened(WindowEvent e) {}
	        @Override 
	        public void windowClosed(WindowEvent e) {
				m_web_panel.dispose();
				Runtime.getRuntime().exit(0);	        	
	        }
	        @Override
	        public void windowClosing(WindowEvent e) {}
	        @Override 
	        public void windowIconified(WindowEvent e) {}
	        @Override 
	        public void windowDeiconified(WindowEvent e) {}
	        @Override 
	        public void windowActivated(WindowEvent e) {}
	        @Override 
	        public void windowDeactivated(WindowEvent e) {}
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
		gbc.insets = new Insets(2,2,2,2);		
		
		// ---- Section titles ----
		m_section_titles = null;
		if (appLanguage().equals("de")) {
			m_section_titles = new IndexPanel(SectionTitle_DE);	
		} else if (appLanguage().equals("fr")) {
			m_section_titles = new IndexPanel(SectionTitle_FR);				
		}			
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 8;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(m_section_titles, gbc);
		if (m_section_titles!=null)
			light_panel.add(m_section_titles, gbc);
		
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
		// jframe.setAlwaysOnTop(true);
		jframe.setVisible(true);
		//jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);			
		
		// If command line options are provided start app with a particular title or eancode
		if (commandLineOptionsProvided()) {
			final JButton but_dummy = new JButton("dummy_button");
			if (!CML_OPT_TITLE.isEmpty())
				startAppWithTitle(but_dummy);
			else if (!CML_OPT_EANCODE.isEmpty())
				startAppWithEancode(but_dummy);
			else if (!CML_OPT_REGNR.isEmpty())
				startAppWithRegnr(but_dummy);
			else if (CML_OPT_SERVER==true) {
				// Start thread that reads data from TCP server
				Thread server_thread = new Thread() {
					public void run() {
						while (true) {
							String tcpServerInput = "";
							// Wait until new data is available from input stream
							// Note: the TCP client defines the update rate!
							// System.out.print("Waiting for input...");							
							while ((tcpServerInput = mTcpServer.getInput()).isEmpty());
								/*
								 * Important note: we use invokeLater to post a "job" to Swing, which will then be run 
								 * on the event dispatch thread at Swing's next convenience. Failing to do so will freeze
								 * the main thread.
								 */								

								// Detect type of search (t=title, e=eancode, r=regnr)								
								char typeOfSearch = tcpServerInput.charAt(0);							
								if (typeOfSearch=='t') {
									// Extract title from received string
									CML_OPT_TITLE = tcpServerInput.substring(2);
									// System.out.println(" title -> " + CML_OPT_TITLE);
									// Post a "job" to Swing, which will be run on the event dispatch thread 
									// at its next convenience.
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {									
											startAppWithTitle(but_dummy);
										}
									});
								} else if (typeOfSearch=='e') {
									// Extract ean code from received string
									CML_OPT_EANCODE = tcpServerInput.substring(2);
									// System.out.println(" eancode -> " + CML_OPT_EANCODE);
									// Post a "job" to Swing, which will be run on the event dispatch thread 
									// at its next convenience.
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {									
											startAppWithEancode(but_dummy);
										}
									});
								} else if (typeOfSearch=='r') {
									// Extract registration number from received string
									CML_OPT_REGNR = tcpServerInput.substring(2);
									// System.out.println(" regnr -> " + CML_OPT_REGNR);
									// Post a "job" to Swing, which will be run on the event dispatch thread 
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
		
	private static void createAndShowFullGUI() {
		// Create and setup window
		final JFrame jframe = new JFrame(Constants.APP_NAME);
		jframe.setName(Constants.APP_NAME + ".main");
				
		int min_width = CML_OPT_WIDTH;
        int min_height = CML_OPT_HEIGHT;       
        jframe.setPreferredSize(new Dimension(min_width, min_height));
		jframe.setMinimumSize(new Dimension(min_width, min_height));
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width-min_width)/2;
        int y = (screen.height-min_height)/2;
        jframe.setBounds(x,y,min_width,min_height);
        
		// Set application icon
        if (appCustomization().equals("ywesee")) {
			ImageIcon img = new ImageIcon(Constants.AMIKO_ICON);
			jframe.setIconImage(img.getImage());
        } else if (appCustomization().equals("desitin")) {
			ImageIcon img = new ImageIcon(Constants.DESITIN_ICON);
			jframe.setIconImage(img.getImage());
        } else if (appCustomization().equals("meddrugs")) {
			ImageIcon img = new ImageIcon(Constants.MEDDRUGS_ICON);
			jframe.setIconImage(img.getImage());        	
        } else if (appCustomization().equals("zurrose")) {
			ImageIcon img = new ImageIcon(Constants.AMIKO_ICON);
			jframe.setIconImage(img.getImage());        	
        }

		// ------ Setup menubar ------
		JMenuBar menu_bar = new JMenuBar();	
		// menu_bar.add(Box.createHorizontalGlue());	// --> aligns menu items to the right!
		// -- Menu "Datei" --
		JMenu datei_menu = new JMenu("Datei");
		if (appLanguage().equals("fr"))
			datei_menu.setText("Fichier");
		menu_bar.add(datei_menu);			
		JMenuItem print_item = new JMenuItem("Drucken...");
		JMenuItem quit_item = new JMenuItem("Beenden");
		if (appLanguage().equals("fr")) {
			print_item.setText("Imprimer");
			quit_item.setText("Terminer");
		}
		datei_menu.add(print_item);
		datei_menu.addSeparator();
		datei_menu.add(quit_item);		
		// -- Menu "Hilfe" --
		JMenu hilfe_menu = new JMenu("Support");
		if (appLanguage().equals("fr"))
			hilfe_menu.setText("Aide");
		menu_bar.add(hilfe_menu);
		
		JMenuItem ywesee_item = new JMenuItem(Constants.APP_NAME + " im Internet");
		if (appCustomization().equals("meddrugs"))
			ywesee_item.setText("med-drugs im Internet");
		JMenuItem report_item = new JMenuItem("Error Report");
		JMenuItem about_item = new JMenuItem("Info zu " + Constants.APP_NAME);		
		JMenuItem contact_item = new JMenuItem("Kontakt");
		
		if (appLanguage().equals("fr")) {			
			if (appCustomization().equals("meddrugs"))
				ywesee_item.setText("med-drugs sur Internet");
			else
				ywesee_item.setText(Constants.APP_NAME + " sur Internet");
			report_item.setText("Error Report");
			// Extrawunsch med-drugs
			if (appCustomization().equals("meddrugs"))
				about_item.setText(Constants.APP_NAME);
			else
				about_item.setText("A propos de " + Constants.APP_NAME);
			contact_item.setText("Contact");			
		}		
		hilfe_menu.add(report_item);
		hilfe_menu.addSeparator();
		hilfe_menu.add(about_item);
		hilfe_menu.add(ywesee_item);		
		hilfe_menu.addSeparator();
		hilfe_menu.add(contact_item);
		// -- Menu "Aktualisieren" --
		JMenu update_menu = new JMenu("Update");
		if (appLanguage().equals("fr"))
			update_menu.setText("Mise à jour");
		menu_bar.add(update_menu);
		JMenuItem updatedb_item = new JMenuItem("Update via Internet");
		JMenuItem choosedb_item = new JMenuItem("Update via Datei");
		update_menu.add(updatedb_item);
		update_menu.add(choosedb_item);
		if (appLanguage().equals("fr")) {
			updatedb_item.setText("Télécharger la banque de données");
			choosedb_item.setText("Ajourner la banque de données");			
		}
		// Menu "Abonnieren" (only for ywesee)
		JMenu subscribe_menu = new JMenu("Abonnieren");
		if (appLanguage().equals("fr"))
			subscribe_menu.setText("Abonnement");		
		if (appCustomization().equals("ywesee")) {
			menu_bar.add(subscribe_menu);
		}		
		
		jframe.setJMenuBar(menu_bar);
		
		// ------ Setup toolbar ------
		JToolBar toolBar = new JToolBar("Database");
		toolBar.setPreferredSize(new Dimension(jframe.getWidth(), 64));
		final JToggleButton selectAipsButton = new JToggleButton(new ImageIcon(IMG_FOLDER+"aips32x32_bright.png"));
		final JToggleButton selectFavoritesButton = new JToggleButton(new ImageIcon(IMG_FOLDER+"favorites32x32_bright.png"));
		final JToggleButton selectInteractionsButton = new JToggleButton(new ImageIcon(IMG_FOLDER+"interactions32x32_bright.png"));
		
		selectAipsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
	    selectAipsButton.setHorizontalTextPosition(SwingConstants.CENTER);
		selectAipsButton.setText("AIPS");
		selectAipsButton.setRolloverIcon(new ImageIcon(IMG_FOLDER+"aips32x32_gray.png"));
		selectAipsButton.setSelectedIcon(new ImageIcon(IMG_FOLDER+"aips32x32_dark.png"));
		selectAipsButton.setBackground(new Color(240,240,240));
		selectAipsButton.setToolTipText("AIPS");
		selectFavoritesButton.setVerticalTextPosition(SwingConstants.BOTTOM);
	    selectFavoritesButton.setHorizontalTextPosition(SwingConstants.CENTER);
		selectFavoritesButton.setText("Favorites");
		selectFavoritesButton.setRolloverIcon(new ImageIcon(IMG_FOLDER+"favorites32x32_gray.png"));
		selectFavoritesButton.setSelectedIcon(new ImageIcon(IMG_FOLDER+"favorites32x32_dark.png"));
		selectFavoritesButton.setBackground(new Color(240,240,240));
		selectFavoritesButton.setToolTipText("Favorites");
		selectInteractionsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
	    selectInteractionsButton.setHorizontalTextPosition(SwingConstants.CENTER);
		selectInteractionsButton.setText("Interactions");
		selectInteractionsButton.setRolloverIcon(new ImageIcon(IMG_FOLDER+"interactions32x32_gray.png"));
		selectInteractionsButton.setSelectedIcon(new ImageIcon(IMG_FOLDER+"interactions32x32_dark.png"));
		selectInteractionsButton.setBackground(new Color(240,240,240));
		selectInteractionsButton.setToolTipText("Interactions");

		// Remove border
		Border emptyBorder = BorderFactory.createEmptyBorder();
		selectAipsButton.setBorder(emptyBorder);
		selectFavoritesButton.setBorder(emptyBorder);
		selectInteractionsButton.setBorder(emptyBorder);
		// Set adequate size
		selectAipsButton.setPreferredSize(new Dimension(32,32));
		selectFavoritesButton.setPreferredSize(new Dimension(32,32));
		selectInteractionsButton.setPreferredSize(new Dimension(32,32));
		// Add to toolbar and set up
		toolBar.setBackground(new Color(240,240,240));
		toolBar.add(selectAipsButton);
		toolBar.addSeparator();
		toolBar.add(selectFavoritesButton);
		toolBar.addSeparator();
		toolBar.add(selectInteractionsButton);
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		// Progress indicator (not working...)
		toolBar.addSeparator(new Dimension(32,32));
		toolBar.add(m_progress_indicator);				
		
		jframe.addWindowListener(new WindowListener() {
			// Use WindowAdapter!
	        @Override 
	        public void windowOpened(WindowEvent e) {}
	        @Override 
	        public void windowClosed(WindowEvent e) {
				m_web_panel.dispose();
				Runtime.getRuntime().exit(0);	        	
	        }
	        @Override
	        public void windowClosing(WindowEvent e) {}
	        @Override 
	        public void windowIconified(WindowEvent e) {}
	        @Override 
	        public void windowDeiconified(WindowEvent e) {}
	        @Override 
	        public void windowActivated(WindowEvent e) {}
	        @Override 
	        public void windowDeactivated(WindowEvent e) {}
		});
		print_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				m_web_panel.print();
			}
		});
		quit_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Save settings
				try {
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
				if (appCustomization().equals("ywesee")) {
					if (Desktop.isDesktopSupported()) {				
						try {
							Desktop.getDesktop().browse(new URI("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3UM84Z6WLFKZE"));							
						} catch (IOException e) {
							// TODO:
						} catch (URISyntaxException r) {
							// TODO:
						}
					}
				}
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
				if (appCustomization().equals("ywesee")) {
					if (Desktop.isDesktopSupported()) {
						try {
							URI mail_to_uri = URI.create("mailto:zdavatz@ywesee.com?subject=AmiKo%20Desktop%20Feedback");
							Desktop.getDesktop().mail(mail_to_uri);
						} catch (IOException e) {
							// TODO:
						}
					} else {
						AmiKoDialogs cd = new AmiKoDialogs(appLanguage(), appCustomization());
						cd.ContactDialog();
					}
				} else if (appCustomization().equals("desitin")) {
					if (Desktop.isDesktopSupported()) {
						try {
							URI mail_to_uri = URI.create("mailto:info@desitin.ch?subject=AmiKo%20Desktop%20Desitin%20Feedback");
							Desktop.getDesktop().mail(mail_to_uri);
						} catch (IOException e) {
							// TODO:
						}
					} else {
						AmiKoDialogs cd = new AmiKoDialogs(appLanguage(), appCustomization());
						cd.ContactDialog();
					}
				} else if (appCustomization().equals("meddrugs")) {
					if (Desktop.isDesktopSupported()) {
						try {
							URI mail_to_uri = URI.create("mailto:med-drugs@just-medical.com?subject=med-drugs%20desktop%20Feedback");
							Desktop.getDesktop().mail(mail_to_uri);
						} catch (IOException e) {
							// TODO:
						}
					} else {
						AmiKoDialogs cd = new AmiKoDialogs(appLanguage(), appCustomization());
						cd.ContactDialog();
					}
				} else if (appCustomization().equals("zurrose")) {
					if (Desktop.isDesktopSupported()) {				
						try {
							Desktop.getDesktop().browse(new URI("www.zurrose.ch/amiko"));							
						} catch (IOException e) {
							// TODO:
						} catch (URISyntaxException r) {
							// TODO:
						}
					}
				}
			}
		});
		report_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Check first m_application_folder otherwise resort to pre-installed report
				String report_file = m_application_data_folder + "\\" + DEFAULT_AMIKO_REPORT_BASE + appLanguage() + ".html";
				if (!(new File(report_file)).exists())
					report_file = System.getProperty("user.dir") + "/dbs/" + DEFAULT_AMIKO_REPORT_BASE + appLanguage() + ".html";
				// Open report file in browser
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new File(report_file).toURI());							
					} 	catch (IOException e) {
						// TODO:
					}
				}
			}
		});
		ywesee_item.addActionListener(new ActionListener() {
			@Override			
			public void actionPerformed(ActionEvent event) {
				if (appCustomization().equals("ywesee")) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI("http://www.ywesee.com/AmiKo/Desktop"));							
						} 	catch (IOException e) {
							// TODO:
						} catch (URISyntaxException r) {
						// TODO:
						}
					}
				} else if (appCustomization().equals("desitin")) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI("http://www.desitin.ch/produkte/arzneimittel-kompendium-apps/"));							
						} 	catch (IOException e) {
							// TODO:
						} catch (URISyntaxException r) {
						// TODO:
						}
					}				
				} else if (appCustomization().equals("meddrugs")) {
					if (Desktop.isDesktopSupported()) {
						try {
							if (appLanguage().equals("de"))
								Desktop.getDesktop().browse(new URI("http://www.med-drugs.ch"));
							else if (appLanguage().equals("fr"))
								Desktop.getDesktop().browse(new URI("http://www.med-drugs.ch/index.cfm?&newlang=fr"));
						} 	catch (IOException e) {
							// TODO:
						} catch (URISyntaxException r) {
						// TODO:
						}
					}				
				} else if (appCustomization().equals("zurrose")) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI("www.zurrose.ch/amiko"));							
						} 	catch (IOException e) {
							// TODO:
						} catch (URISyntaxException r) {
						// TODO:
						}
					}					
				}
			}
		});	
		about_item.addActionListener(new ActionListener() {
			@Override			
			public void actionPerformed(ActionEvent event) {
				AmiKoDialogs ad = new AmiKoDialogs(appLanguage(), appCustomization());
				ad.AboutDialog();
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
		gbc.insets = new Insets(2,2,2,2);
		
		// ---- Search field ----
		final SearchField searchField = new SearchField("Suche Präparat");
		if (appLanguage().equals("fr"))
			searchField.setText("Recherche Specialité");			
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(searchField, gbc);
		left_panel.add(searchField, gbc);
		
		// ---- Buttons ----
		// Names
		String l_title = "Präparat";
		String l_author = "Inhaberin";
		String l_atccode = "ATC Code";
		String l_regnr = "Zulassungsnummer";
		String l_ingredient = "Wirkstoff";
		String l_therapy = "Therapie";
		String l_search = "Suche";
		
		if (appLanguage().equals("fr")) {
			l_title = "Spécialité";
			l_author = "Titulaire";
			l_atccode = "Code ATC";
			l_regnr = "Nombre Enregistration";
			l_ingredient = "Principe Active";
			l_therapy = "Thérapie";
			l_search = "Recherche";
		}
		
		JButton but_title = new JButton(l_title);
		but_title.setBackground(m_but_color);
		but_title.setBorder(new CompoundBorder(
				new LineBorder(m_but_color), new EmptyBorder(0,3,0,0)));
		but_title.setHorizontalAlignment(SwingConstants.LEFT);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_title, gbc);
		left_panel.add(but_title, gbc);
		
		JButton but_auth = new JButton(l_author);
		but_auth.setBackground(m_but_color);
		but_auth.setBorder(new CompoundBorder(
				new LineBorder(m_but_color), new EmptyBorder(0,3,0,0)));
		but_auth.setHorizontalAlignment(SwingConstants.LEFT);		
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_auth, gbc);
		left_panel.add(but_auth, gbc);
		
		JButton but_atccode = new JButton(l_atccode);
		but_atccode.setBackground(m_but_color);
		but_atccode.setBorder(new CompoundBorder(
				new LineBorder(m_but_color), new EmptyBorder(0,3,0,0)));
		but_atccode.setHorizontalAlignment(SwingConstants.LEFT);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_atccode, gbc);
		left_panel.add(but_atccode, gbc);
		
		JButton but_regnr = new JButton(l_regnr);
		but_regnr.setBackground(m_but_color);
		but_regnr.setBorder(new CompoundBorder(
				new LineBorder(m_but_color), new EmptyBorder(0,3,0,0)));
		but_regnr.setHorizontalAlignment(SwingConstants.LEFT);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_regnr, gbc);
		left_panel.add(but_regnr, gbc);
		
		JButton but_ingredients = new JButton(l_ingredient);
		but_ingredients.setBackground(m_but_color);		
		but_ingredients.setBorder(new CompoundBorder(
				new LineBorder(m_but_color), new EmptyBorder(0,3,0,0)));
		but_ingredients.setHorizontalAlignment(SwingConstants.LEFT);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_ingredients, gbc);	
		left_panel.add(but_ingredients, gbc);
		
		JButton but_therapy = new JButton(l_therapy);
		but_therapy.setBackground(m_but_color);		
		but_therapy.setBorder(new CompoundBorder(
				new LineBorder(m_but_color), new EmptyBorder(0,3,0,0)));
		but_therapy.setHorizontalAlignment(SwingConstants.LEFT);
		gbc.gridx = 0;
		gbc.gridy += 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(but_therapy, gbc);
		left_panel.add(but_therapy, gbc);
		
		// ---- Card layout ----
		final CardLayout cardl = new CardLayout();
		cardl.setHgap(-4);	// HACK to make things look better!!
		final JPanel p_results = new JPanel(cardl);
		m_list_titles = new ListPanel();
		m_list_auths = new ListPanel();
		m_list_regnrs = new ListPanel();
		m_list_atccodes = new ListPanel();
		m_list_ingredients = new ListPanel();
		m_list_therapies = new ListPanel();				
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
		
		// --> container.add(p_results, gbc);
		left_panel.add(p_results, gbc);
		// First card to show
		cardl.show(p_results, l_title);

		// ==== Right panel ====
		JPanel right_panel = new JPanel();
		right_panel.setBackground(Color.WHITE);
		right_panel.setLayout(new GridBagLayout());
		
		// ---- Section titles ----
		m_section_titles = null;
		if (appLanguage().equals("de")) {
			m_section_titles = new IndexPanel(SectionTitle_DE);	
		} else if (appLanguage().equals("fr")) {
			m_section_titles = new IndexPanel(SectionTitle_FR);				
		}			
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 8;
		gbc.weightx = gbc.weighty = 0.0;
		// --> container.add(m_section_titles, gbc);
		if (m_section_titles!=null)
			right_panel.add(m_section_titles, gbc);
		
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
		right_panel.add(m_web_panel, gbc);
				
		// Add JSplitPane
		JSplitPane split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_panel, right_panel);
		split_pane.setOneTouchExpandable(true);
		split_pane.setDividerLocation(320);	// Sets the pane divider location
		left_panel.setBorder(null);
		right_panel.setBorder(null);
		container.add(split_pane, BorderLayout.CENTER);
		
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
		final String final_ingredient = l_ingredient;
		final String final_therapy = l_therapy;
		final String final_search = l_search;	
		
		// ------ Add toolbar action listeners ------
		selectAipsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectAipsButton.setSelected(true);
				selectFavoritesButton.setSelected(false);
				selectInteractionsButton.setSelected(false);
				m_database_used = "aips";
				m_seek_interactions = false;
				// Set right panel title
				if (appLanguage().equals("de"))
					m_web_panel.setTitle("Fachinformation");
				else if (appLanguage().equals("fr"))
					m_web_panel.setTitle("Notice Infopro");
				
				m_start_time = System.currentTimeMillis();
				m_query_str = searchField.getText();				
				med_search = m_sqldb.searchTitle(m_query_str);
				sTitle("");	// Used instead of sTitle (which is slow)
				cardl.show(p_results, final_title);	

				m_status_label.setText(med_search.size() + " Suchresultate in " + 
						(System.currentTimeMillis()-m_start_time)/1000.0f + " Sek.");

				m_web_panel.updateText();
			}
		});
		selectFavoritesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectAipsButton.setSelected(false);
				selectFavoritesButton.setSelected(true);
				selectInteractionsButton.setSelected(false);
				m_database_used = "favorites";
				m_seek_interactions = false;
				// Set right panel title
				if (appLanguage().equals("de"))
					m_web_panel.setTitle("Fachinformation");
				else if (appLanguage().equals("fr"))
					m_web_panel.setTitle("Notice Infopro");
				
				m_start_time = System.currentTimeMillis();
				// m_query_str = searchField.getText();				
				// Clear the search container
				med_search.clear();
				for (String regnr : favorite_meds_set) {
					List<Medication> meds = m_sqldb.searchRegNr(regnr);
					if (!meds.isEmpty())
						med_search.add(meds.get(0));
				}
				// Sort list of meds
				Collections.sort(med_search, new Comparator<Medication>() {
					@Override
					public int compare(final Medication m1, final Medication m2) {
						return m1.getTitle().compareTo(m2.getTitle());
					}
				});
				
				sTitle("");		// "" argument unused
				cardl.show(p_results, final_title);
			
				m_status_label.setText(med_search.size() + " Suchresultate in " + 
						(System.currentTimeMillis()-m_start_time)/1000.0f + " Sek.");
			}
		});
		selectInteractionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectAipsButton.setSelected(false);
				selectFavoritesButton.setSelected(false);
				selectInteractionsButton.setSelected(true);
				m_database_used = "aips";
				m_seek_interactions = true;
				// Set right panel title
				if (appLanguage().equals("de"))
					m_web_panel.setTitle("Medikamentenkorb");
				else if (appLanguage().equals("fr"))
					m_web_panel.setTitle("Médicaments sélectionnées");
				// Switch to interaction mode
				m_web_panel.doInteractions();
			}
		});
		
		// ------ Add keylistener to text field (type as you go feature) ------
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {	// keyReleased(KeyEvent e)
				//invokeLater potentially in the wrong place... more testing required
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {							
						m_start_time = System.currentTimeMillis();
						m_query_str = searchField.getText();
						// Queries for SQLite DB
						if (!m_query_str.isEmpty()) {
							if (m_query_type==0) {
								med_search = m_sqldb.searchTitle(m_query_str);
								if (m_database_used.equals("favorites"))
									retrieveFavorites();
								sTitle(m_query_str);
								cardl.show(p_results, final_title);
							} else if (m_query_type==1) {
								med_search = m_sqldb.searchAuth(m_query_str);
								if (m_database_used.equals("favorites"))
									retrieveFavorites();
								sAuth(m_query_str);
								cardl.show(p_results, final_author);
							} else if (m_query_type==2) {
								med_search = m_sqldb.searchATC(m_query_str);
								if (m_database_used.equals("favorites"))
									retrieveFavorites();
								sATC(m_query_str);
								cardl.show(p_results, final_atccode);
							} else if (m_query_type==3) {
								med_search = m_sqldb.searchRegNr(m_query_str);
								if (m_database_used.equals("favorites"))
									retrieveFavorites();
								sRegNr(m_query_str);
								cardl.show(p_results, final_regnr);					
							} else if (m_query_type==4) {
								med_search = m_sqldb.searchIngredient(m_query_str);
								if (m_database_used.equals("favorites"))
									retrieveFavorites();
								sIngredient(m_query_str);
								cardl.show(p_results, final_ingredient);	
							} else if (m_query_type==5) {
								med_search = m_sqldb.searchApplication(m_query_str);
								if (m_database_used.equals("favorites"))
									retrieveFavorites();
								sTherapy(m_query_str);
								cardl.show(p_results, final_therapy);	
							} else {
								// do nothing
							}
							m_status_label.setText(med_search.size() + " Suchresultate in " + 
									(System.currentTimeMillis()-m_start_time)/1000.0f + " Sek.");
						}
						/*
						if (DEBUG)
							System.out.println("Time for search in [sec]: " + (System.currentTimeMillis()-m_start_time)/1000.0f);
							*/
					}
				});
			}				
		});
		
		// Add actionlisteners
		but_title.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				searchField.setText(final_search + " " + final_title);
				m_query_type = 0;
				sTitle(m_query_str);
				cardl.show(p_results, final_title);
			}
		});
		but_auth.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				searchField.setText(final_search + " " + final_author);
				m_query_type = 1;
				sAuth(m_query_str);
				cardl.show(p_results, final_author);
			}
		});		
		but_atccode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				searchField.setText(final_search + " " + final_atccode);
				m_query_type = 2;
				sATC(m_query_str);
				cardl.show(p_results, final_atccode);
			}
		});
		but_regnr.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				searchField.setText(final_search + " " + final_regnr);
				m_query_type = 3;
				sRegNr(m_query_str);
				cardl.show(p_results, final_regnr);
			}
		});
		but_ingredients.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				searchField.setText(final_search + " " + final_ingredient);
				m_query_type = 4;
				sIngredient(m_query_str);
				cardl.show(p_results, final_ingredient);
			}
		});
		but_therapy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				searchField.setText(final_search + " " + final_therapy);
				m_query_type = 5;
				sTherapy(m_query_str);
				cardl.show(p_results, final_therapy);
			}
		});
		
		// Display window
		jframe.pack();
		// jframe.setAlwaysOnTop(true);
		jframe.setVisible(true);
		//jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	

		// Check if user has selected an alternative database
		/*  NOTE:
			21/11/2013: This solution is put on ice. Favored is a solution
			where the database selected by the user is saved in a default folder
			(see variable "m_application_data_folder")
		*/
		/*		
		try {
			WindowSaver.loadSettings(jframe);
			String database_path = WindowSaver.getDbPath();
			if (database_path!=null)
				m_sqldb.loadDBFromPath(database_path);
		} catch(IOException e) {
			e.printStackTrace();
		}
		*/
		// Load AIPS database
		selectAipsButton.setSelected(true);
		selectFavoritesButton.setSelected(false);
		m_database_used = "aips";
		med_search = m_sqldb.searchTitle("");
		sTitle("");	// Used instead of sTitle (which is slow)
		cardl.show(p_results, final_title);	

		// Add menu item listeners	
		updatedb_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String db_file = m_sqldb.updateDB(jframe, appLanguage(), appCustomization(), m_application_data_folder);
				if (!db_file.isEmpty()) {
					// Save db path (can't hurt)
					WindowSaver.setDbPath(db_file);				
				}
			}
		});
		
		choosedb_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String db_file = m_sqldb.chooseDB(jframe, appLanguage(), m_application_data_folder);
				if (!db_file.isEmpty()) {
					// Save db path (can't hurt)
					WindowSaver.setDbPath(db_file);				
				}
			}
		});		
		
		// Attach observer to 'm_sqldb'
		m_sqldb.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				System.out.println(arg);
				// Refresh search results
				selectAipsButton.setSelected(true);
				selectFavoritesButton.setSelected(false);
				m_database_used = "aips";
				med_search = m_sqldb.searchTitle("");
				sTitle("");	// Used instead of sTitle (which is slow)
				cardl.show(p_results, final_title);						
			}
		});
		
		// If command line options are provided start app with a particular title or eancode
		if (commandLineOptionsProvided()) {
			if (!CML_OPT_TITLE.isEmpty())
				startAppWithTitle(but_title);
			else if (!CML_OPT_EANCODE.isEmpty())
				startAppWithEancode(but_regnr);
			else if (!CML_OPT_REGNR.isEmpty())
				startAppWithRegnr(but_regnr);
		}
	}	
	
	static void retrieveFavorites() 
	{
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
	
	static void startAppWithTitle(JButton but_title)
	{
		m_query_str = CML_OPT_TITLE;
		med_search = m_sqldb.searchTitle(m_query_str);
		// Check first if search delivers any result
		if (med_search.size()>0) {
			if (CML_OPT_TYPE.equals("full"))
				but_title.doClick();
			else if (CML_OPT_TYPE.equals("light")) {
				m_query_type = 0;
				med_id.clear();
				for (int i=0; i<med_search.size(); ++i) {
					Medication ms = med_search.get(i);
					med_id.add(ms.getId());
				}
			}
			med_index = 0;
			m_web_panel.updateText();						
		}
	}
	
	static void startAppWithEancode(JButton but_regnr)
	{
		// Check if delivered eancode is "kosher"
		if (CML_OPT_EANCODE.length()==13 && CML_OPT_EANCODE.indexOf("7680")==0) {
			// Extract 5-digit registration number			
			m_query_str = CML_OPT_EANCODE.substring(4, 9);
			med_search = m_sqldb.searchRegNr(m_query_str);
			// Check first if search delivers any result			
			if (med_search.size()>0) {
				if (CML_OPT_TYPE.equals("full"))
					but_regnr.doClick();
				else if (CML_OPT_TYPE.equals("light")) {
					m_query_type = 3;
					med_id.clear();
					for (int i=0; i<med_search.size(); ++i) {
						Medication ms = med_search.get(i);
						med_id.add(ms.getId());
					}									
				}
				med_index = 0;
				m_web_panel.updateText();
			}
		} else {
			System.out.println("> Error: Wrong EAN code");
		}
	}
	
	static void startAppWithRegnr(JButton but_regnr)
	{
		// Simple check, should be improved...
		if (CML_OPT_REGNR.length()==5) {
			m_query_str = CML_OPT_REGNR;
			med_search = m_sqldb.searchRegNr(m_query_str);
			// Check first if search delivers any result			
			if (med_search.size()>0) {
				if (CML_OPT_TYPE.equals("full"))
					but_regnr.doClick();
				else if (CML_OPT_TYPE.equals("light")) {
					m_query_type = 3;
					med_id.clear();
					for (int i=0; i<med_search.size(); ++i) {
						Medication ms = med_search.get(i);
						med_id.add(ms.getId());
					}					
				}
				med_index = 0;
				m_web_panel.updateText();
			}
		} else {
			System.out.println("> Error: Wrong registration number");
		}
	}

	static void sTitle(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		Pattern p_red = Pattern.compile(".*O]");
		Pattern p_green = Pattern.compile(".*G]");
		if (med_search.size()<BigCellNumber && !m_seek_interactions) {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
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
				m.add("<html><b>" + ms.getTitle() + "</b><br><font size=-1>"
						+ pack_info_str + "</font></html>");
				med_id.add(ms.getId());
			}
		} else {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle() + "</b></html>");
				med_id.add(ms.getId());
			}
		}

		m_list_titles.update(m);
	}
	
	static void sAuth(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		if (med_search.size()<BigCellNumber) {		
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + ms.getAuth() + "</font></html>");
				med_id.add(ms.getId());
			}
		} else {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + ms.getAuth() + "</font></html>");
				med_id.add(ms.getId());
			}
		}
		m_list_auths.update(m);
	}
	
	static void sATC(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		if (med_search.size()<BigCellNumber) {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);			
				// String atc_code_str = ms.getAtcCode().replaceAll(";", " - ");
				
				String[] m_code = ms.getAtcCode().split(";");
				String atc_code_str = "";
				String atc_title_str = "";
				if (m_code.length>1) {
					atc_code_str = m_code[0];
					atc_title_str = m_code[1];
				}
				
				String[] m_class = ms.getAtcClass().split(";");			
				String atc_class_str = "";
				if (m_class.length==2)
					atc_class_str = m_class[1];
				else if (m_class.length==3)
					atc_class_str = m_class[2];
				m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + atc_code_str + " - " 
					+ atc_title_str + "<br>" + atc_class_str + "</font></html>");
				med_id.add(ms.getId());
			}
		} else {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				String[] m_code = ms.getAtcCode().split(";");
				String atc_code_str = "";
				String atc_title_str = "";
				if (m_code.length>1) {
					atc_code_str = m_code[0];
					atc_title_str = m_code[1];
				}				
				m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + atc_code_str + " - " 
						+ atc_title_str + "</font></html>");
					med_id.add(ms.getId());
			}
		}
		m_list_atccodes.update(m);
	}

	static void sRegNr(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		if (med_search.size()<BigCellNumber) {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + ms.getRegnrs()+"</font></html>");
				med_id.add(ms.getId());
			}
		} else {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + ms.getRegnrs()+"</font></html>");
				med_id.add(ms.getId());	
			}
		}
		m_list_regnrs.update(m);
	}
	
	/**
	 * Nicely formats search for ingredient ("Wirkstoff")
	 * @param query_str
	 */
	static void sIngredient(String query_str) {
		med_id.clear();					
		List<String> m = new ArrayList<String>();
		if (med_search.size()<BigCellNumber) {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><b>" + ms.getSubstances() + "</b><br><font color=gray size=-1>" + ms.getTitle()+"</font></html>");
				med_id.add(ms.getId());
			}
		} else {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				m.add("<html><body style='width: 1024px;'><b>" + ms.getSubstances() + "</b><br><font color=gray size=-1>" + ms.getTitle()+"</font></html>");
				med_id.add(ms.getId());
			}
		}
		m_list_ingredients.update(m);
	}
	
	static void sTherapy(String query_str) {
		med_id.clear();					
		List<String> m = new ArrayList<String>();
		if (med_search.size()<BigCellNumber) {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				String application_str = ms.getApplication().replaceAll("\n", "<p>");
				application_str = ms.getApplication().replaceAll(";", "<p>");
				m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + application_str + "</font></html>");
				med_id.add(ms.getId());
			}
		} else {
			for (int i=0; i<med_search.size(); ++i) {
				Medication ms = med_search.get(i);
				String application_str = ms.getApplication().replaceAll(";", " / ");
				m.add("<html><body style='width: 1024px;'><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + application_str + "</font></html>");
				med_id.add(ms.getId());		
			}
		}
		m_list_therapies.update(m);
	}
	
	static String readFromFileFast(String filename, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(filename));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	static String readFromFile(String filename) {
		String file_str = "";		
        try {
        	FileInputStream fis = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                file_str += (line + "\n");
            }
            br.close();
        }
        catch (Exception e) {
        	System.err.println(">> Error in reading file");        	
        }
        
		return file_str;	
	}
	
	static Map<String,String> readFromCsvToMap(String filename) {
		Map<String, String> map = new TreeMap<String, String>();
		try {
			File file = new File(filename);
			if (!file.exists()) 
				return null;
			FileInputStream fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				String token[] = line.split("\\|\\|");
				map.put(token[0] + "-" + token[1], token[2]);
			}
			br.close();
		} catch (Exception e) {
			System.err.println(">> Error in reading csv file");
		}
		
		return map;
	}
	
	static void writeToFile(String string_to_write, String dir_name, String file_name) 
			throws IOException {
       	File wdir = new File(dir_name);
       	if (!wdir.exists())
       		wdir.mkdirs();
		File wfile = new File(dir_name+file_name);
		if (!wfile.exists())
			wfile.createNewFile();
		// FileWriter fw = new FileWriter(wfile.getAbsoluteFile());
		// Used to be UTF-8 --> does not work (@maxl: 08/Jun/2013)
       	CharsetEncoder encoder = Charset.forName("UTF-16").newEncoder();
       	encoder.onMalformedInput(CodingErrorAction.REPORT);
       	encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(wfile.getAbsoluteFile()), encoder);
		BufferedWriter bw = new BufferedWriter(osw);      			
		bw.write(string_to_write);
		bw.close();
	}		
}
