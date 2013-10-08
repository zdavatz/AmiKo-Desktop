package com.maxl.java.amikodesk;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
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
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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

public class AMiKoDesk {

	private static final boolean DEBUG = true;
	
	private static final String AMIKO_NAME = "AmiKo Desktop";
	private static final String COMED_NAME = "CoMed Desktop";	
	private static final String AMIKO_DESITIN_NAME = "AmiKo Desktop Desitin";
	private static final String COMED_DESITIN_NAME = "CoMed Desktop Desitin";
	private static final String AMIKO_MEDDRUGS_NAME = "med-drugs desktop";
	private static final String COMED_MEDDRUGS_NAME = "med-drugs-fr desktop";
	private static final String AMIKO_ZURROSE_NAME = "AmiKo Desktop ZR";
	private static final String COMED_ZURROSE_NAME = "CoMed Desktop ZR";
	
	private static final String AMIKO_ICON = "./icons/amiko_icon.png";
	private static final String DESITIN_ICON = "./icons/desitin_icon.png";
	private static final String DESITIN_LOGO = "./images/desitin_logo.png";
	private static final String MEDDRUGS_ICON = "./icons/meddrugs_icon.png";
	private static final String MEDDRUGS_LOGO = "./images/meddrugs_logo.png";
	private static final String ZURROSE_ICON = "./icons/amiko_icon.png";
	
	// -->> Note: uncomment name of app to compile!
	private static final String APP_NAME = AMIKO_NAME;
	// private static final String APP_NAME = COMED_NAME;
	// private static final String APP_NAME = AMIKO_DESITIN_NAME;
	// private static final String APP_NAME = COMED_DESITIN_NAME;
	// private static final String APP_NAME = AMIKO_MEDDRUGS_NAME;
	// private static final String APP_NAME = COMED_MEDDRUGS_NAME;
	// private static final String APP_NAME = AMIKO_ZURROSE_NAME;
	// private static final String APP_NAME = COMED_ZURROSE_NAME;
	private static final String VERSION = "1.1.0 (32-bit)";	
	private static final String GEN_DATE = "30.09.2013";
	
	private static Long m_start_time = 0L;
	private static final String HTML_FILES = "./fis/fi_de_html/";
	private static final String CSS_SHEET = "./css/amiko_stylesheet.css";
	private static List<String> med_content = new ArrayList<String>();
	private static List<Long> med_id = new ArrayList<Long>();
	private static List<Medication> med_search = new ArrayList<Medication>();
	private static List<Medication> med_title = new ArrayList<Medication>();

	private static String SectionTitle_DE[] = {"Zusammensetzung", "Galenische Form", "Indikationen", "Dosierung", "Kontraindikationen",
		"Vorsichtmass.", "Interaktionen", "Schwangerschaft", "Fahrtüchtigkeit", "UAW", "Überdosierung", 
		"Eig./Wirkung", "Kinetik", "Präklinik", "Sonstige Hinweise", "Packungen", "Firma", "Stand"};
	private static String SectionTitle_FR[] = {"Composition", "Forme galénique", "Indications", "Posologie", "Contre-indic.", 
		"Précautions", "Interactions", "Grossesse/All.", "Conduite", "Effets indésir.",	"Surdosage", 
		"Propriétés/Effets", "Cinétique", "Préclinique", "Remarques", "Présentation", "Titulaire", "Mise à jour"};
	
	private static int med_index = 0;
	private static WebPanel2 m_web_panel = null;
	private static String m_css_str = null;
	private static String m_query_str = null;
	private static SqlDatabase m_sqldb = null;
	private static List<String> m_section_str = null;
	private static List<String> m_section_ids = null;
	
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
				formatter.printHelp("aips2xml", opts);
				System.exit(0);
			}
			if (cmd.hasOption("version")) {
				System.out.println("Version of aips2xml: " + VERSION);
			}
		} catch(ParseException e) {
			System.err.println("Parsing failed: " + e.getMessage());
		}
	}			
	
	private static String appLanguage() {
		if (APP_NAME.equals(AMIKO_NAME) || APP_NAME.equals(AMIKO_DESITIN_NAME) 
				|| APP_NAME.equals(AMIKO_MEDDRUGS_NAME) || APP_NAME.equals(AMIKO_ZURROSE_NAME)) {
			return "de";
		} else if (APP_NAME.equals(COMED_NAME) || APP_NAME.equals(COMED_DESITIN_NAME) 
				|| APP_NAME.equals(COMED_MEDDRUGS_NAME) || APP_NAME.equals(COMED_ZURROSE_NAME)) {
			return "fr";
		}
		return "";
	}

	private static String appCustomization() {
		if (APP_NAME.equals(AMIKO_NAME) || APP_NAME.equals(COMED_NAME)) {
			return "ywesee";
		} else if (APP_NAME.equals(AMIKO_DESITIN_NAME) || APP_NAME.equals(COMED_DESITIN_NAME)) {
			return "desitin";
		} else if (APP_NAME.equals(AMIKO_MEDDRUGS_NAME) || APP_NAME.equals(COMED_MEDDRUGS_NAME)) {
			return "meddrugs";
		} else if (APP_NAME.equals(AMIKO_ZURROSE_NAME) || APP_NAME.equals(COMED_ZURROSE_NAME)) {
			return "zurrose";
		}
		return "";
	}
	
	public static void main(String[] args) {		
	
		if (appCustomization().equals("desitin")) {
			SplashWindow splash = new SplashWindow(APP_NAME, 7500);
		} else if (appCustomization().equals("meddrugs")) {
			SplashWindow splash = new SplashWindow(APP_NAME, 7500);
		} else if (appCustomization().equals("zurrose")) {
			SplashWindow splash = new SplashWindow(APP_NAME, 5000);
		}
		// Load css style sheet
		m_css_str = "<style>" + readFromFile(CSS_SHEET) + "</style>";
		
		// Load database		
		m_sqldb = new SqlDatabase();
		if (appLanguage().equals("de"))
			m_sqldb.loadDB("de");
		else if (appLanguage().equals("fr"))
			m_sqldb.loadDB("fr");
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
        
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				createAndShowGUI();
			}
		});

		NativeInterface.runEventPump();
	}	
	
	static class ListPanel extends JPanel implements ListSelectionListener, FocusListener {
		
		private JList<String> list = null;
		private JScrollPane jscroll = null;
		
		public ListPanel() {
			super(new BorderLayout());
			
			String[] titles = med_title.toArray(new String[med_title.size()]);
			list = new JList<String>(titles);
					
			list.setSelectedIndex(0);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectionBackground(new Color(240,240,240));
			list.setSelectionForeground(Color.BLACK);
			list.setFont(new Font("Dialog", Font.PLAIN, 14));
			list.addListSelectionListener(this);
			
			JPanel listPanel = new JPanel(new BorderLayout());
			if (appLanguage().equals("de")) {
		        TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Suchresultat", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				listPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));
			} else if (appLanguage().equals("fr")) {
		        TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Résultat de la Recherche", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				listPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));			
			}
			jscroll = new JScrollPane(list);
			listPanel.add(jscroll, BorderLayout.CENTER);
			add(listPanel, BorderLayout.CENTER);
			
			setFocusable(true);
			requestFocusInWindow();
		}
		
		public void update(List<String> lStr) {
			String[] m_lStr = lStr.toArray(new String[lStr.size()]);
			list.setListData(m_lStr);
			
			jscroll.revalidate();
			jscroll.repaint();
		}
		
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				med_index = list.getSelectedIndex();
				m_web_panel.updateText();
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
		
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				int sel_index = list.getSelectedIndex();
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
		
		public WebPanel2() {
			// YET another mega-hack ;)
			super(new BorderLayout());
			JPanel webBrowserPanel = new JPanel(new BorderLayout());
			if (appLanguage().equals("de")) {				
		        TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Fachinformation", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				webBrowserPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));			
			} else if (appLanguage().equals("fr")) {
		        TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Notice Infopro", 
		        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
		        		new Font("Dialog", Font.PLAIN, 14));
				webBrowserPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));
			}
			jWeb = new JWebBrowser(NSComponentOptions.destroyOnFinalization());
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
		
		public void moveToAnchor(String anchor) {	
			jWeb.executeJavascript("document.getElementById('" + anchor + "').scrollIntoView(true);");
		}
		
		public void updateText() {
			if (med_index>=0) {
				Medication m =  m_sqldb.getMediWithId(med_id.get(med_index));
				String[] sections = m.getSectionIds().split(",");
	    		m_section_str = Arrays.asList(sections);
				String[] ids = m.getSectionIds().split(",");
	    		m_section_ids = Arrays.asList(ids);	
				content_str = new StringBuffer(m.getContent());	
				// DateFormat df = new SimpleDateFormat("dd.MM.yy");
				String _amiko_str = APP_NAME + " - Datenstand AIPS Swissmedic " + GEN_DATE;
				content_str = content_str.insert(content_str.indexOf("<head>"), "<title>" + _amiko_str + "</title>");
				content_str = content_str.insert(content_str.indexOf("</head>"), m_css_str);
				jWeb.setJavascriptEnabled(true);
				
				try {
					// Currently preferred solution, html saved in C:/Users/ ... folder
					String path_html = System.getProperty ("user.home") + "/" + APP_NAME +"/htmls/";
					String _title = m.getTitle();					
					String file_name = _title.replaceAll("[®,/;.]","_") + ".html";
					writeToFile(content_str.toString(), path_html, file_name);
					jWeb.navigate("file:///" + path_html + file_name);
				} catch(IOException e) {
					// Fallback solution (used to be preferred implementation)
					jWeb.setHTMLContent(content_str.toString());
				}
				
				/*
				PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
				attr_set.add(new Copies(2));
				attr_set.add(Sides.DUPLEX);
				PrintService[] service = PrinterJob.lookupPrintServices();
				if (service.length==0)
					System.out.println("no printing services available");
				for (int i=0; i<service.length; ++i)
					System.out.println(service[i].getName());
				DocPrintJob job = service[0].createPrintJob(); 
				*/				    
				
				/*
				System.out.println("--> " + jWeb.getNativeComponent().getName());
			    int instanceID = ObjectRegistry.getInstance().add(this);
			    String resourcePath = WebServer.getDefaultWebServer().getDynamicContentURL(WebBrowserObject.class.getName(), "html/" + instanceID);
			    System.out.println(instanceID + ": " + resourcePath);
			    String lUrl = WebServer.getDefaultWebServer().getURLPrefix(); // e.g. http://127.0.0.1:4716
			    System.out.println(lUrl);
				System.out.println(jWeb.getBrowserType() + ", version: " + jWeb.getBrowserVersion());
				System.out.println(jWeb.getResourceLocation() + " / " + jWeb.getName() + " / " + jWeb.getPageTitle());
				*/
				
				jWeb.setVisible(true);
			}
		}
		
		public void dispose() {
			jWeb.disposeNativePeer();
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
			jText.setText(med_content.get(med_index));
			jText.setCaretPosition(0);
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
			jep.setText(med_content.get(med_index));
			jep.setCaretPosition(0);
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
	
	static class AboutDialog extends JDialog {

	    public AboutDialog() {
	        initUI();
	    }

	    public final void initUI() {

	        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	        add(Box.createRigidArea(new Dimension(0, 10)));

	        ImageIcon icon = null;
	        if (appCustomization().equals("ywesee")) {
	        	icon = new ImageIcon(AMIKO_ICON);
	        } else if (appCustomization().equals("desitin")) {
		        icon = new ImageIcon(DESITIN_ICON);
	        } else if (appCustomization().equals("meddrugs")) {
	        	icon = new ImageIcon(MEDDRUGS_ICON);
	        } else if (appCustomization().equals("zurrose")) {
	        	icon = new ImageIcon(ZURROSE_ICON);
	        }
	        Image img = icon.getImage();
		    Image scaled_img = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
		    if (appCustomization().equals("meddrugs"))
		    	scaled_img = img.getScaledInstance(128, 128,  java.awt.Image.SCALE_SMOOTH);
		    icon = new ImageIcon(scaled_img);
		    JLabel label = new JLabel(icon);
		    label.setAlignmentX(0.5f);
		    label.setBorder(new EmptyBorder(5,5,5,5));
		    add(label);
	        
	        add(Box.createRigidArea(new Dimension(0, 10)));

			DateFormat df = new SimpleDateFormat("dd.MM.yy");
			String date_str = GEN_DATE; // df.format(new Date());
			
			try {
				final URI uri = new URI("https://play.google.com/store/apps/details?id=com.ywesee.amiko.de&hl=en");
				class OpenUrlAction implements ActionListener {
					@Override
					public void actionPerformed(ActionEvent e) {
						open(uri);
					}
					private void open(URI uri) {
						if (Desktop.isDesktopSupported()) {
							try {
								Desktop.getDesktop().browse(uri);	
							} catch (IOException e) {
								// TODO:
							}
						}
					}
				}
				
		        JButton info = new JButton();
		        String sponsoring = "";
		        if (appLanguage().equals("de")) {
		        	if (appCustomization().equals("ywesee")) {
		        		sponsoring = "<br>" +
		        				"<br><a href=\"\">AMiKo / CoMed</a>" +
		        				"<br>Arzneimittel-Kompendium für Android" +
		        				"<br>";
		        	} else if (appCustomization().equals("desitin")) {
			        	sponsoring = "<br>" +
			        			"<br>Unterstützt durch Desitin Pharma GmbH" +
			        			"<br>";
			        } else if (appCustomization().equals("meddrugs")) {
			        	//
			        } else if (appCustomization().equals("zurrose")) {
			        	//
			        }
		        	
			        info.setText(
				        "<html><center><b>" + APP_NAME + "</b><br><br>" +
		        		"Arzneimittel-Kompendium für Windows PC<br>" +
		        		"Version " + VERSION + "<br>" + 
		        		date_str + "<br>" +
		        		"Lizenz: GPLv3.0<br><br>" +
		        		"Konzept: Zeno R.R. Davatz<br>" +
		        		"Entwicklung: Dr. Max Lungarella<br>" +
		        		sponsoring +
				        "</center></html>");
		        } else if (appLanguage().equals("fr")) {
		        	if (appCustomization().equals("ywesee")) {
		        		sponsoring = "<br>" +
		        				"<br><a href=\"\">AMiKo / CoMed</a>" +
		        				"<br>Compedium des Médicaments pour Android" +
		        				"<br>";
		        	} else if (appCustomization().equals("desitin")) {
			        	sponsoring = "<br>" +
			        			"<br>Supporteè par Desitin Pharma GmbH" +
			        			"<br>";
			        } else if (appCustomization().equals("meddrugs")) {
			        	//
			        } else if (appCustomization().equals("zurrose")) {
			        	//
			        }
		        	info.setText(
					        "<html><center><br>" + APP_NAME + "</b><br><br>" +
			        		"Compendium des Médicaments Suisse pour Windows<br>" +
			        		"Version " + VERSION + "<br>" + 
			        		date_str + "<br>" +
			        		"Licence: GPLv3.0<br><br>" +
			        		"Concept: Zeno R.R. Davatz<br>" +
			        		"Développement: Dr. Max Lungarella<br>" +
			        		sponsoring +
					        "</center></html>");
		        }
		        info.setFont(new Font("Dialog", Font.PLAIN, 14));
		        info.setAlignmentX(0.5f);
		        info.setBackground(Color.WHITE);
		        info.setOpaque(false);
		        info.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		        info.setBorderPainted(false);
		        info.setFocusPainted(false);
		        info.setContentAreaFilled(false);
		        info.setToolTipText(uri.toString());
		        info.addActionListener(new OpenUrlAction());
		        add(info);		        
			} catch(URISyntaxException r) {
				// TODO:
			}
			
	        if (appCustomization().equals("desitin")) {
		        add(Box.createRigidArea(new Dimension(0, 10)));
	        	icon = new ImageIcon(DESITIN_LOGO);
	        	img = icon.getImage();
	        	scaled_img = img.getScaledInstance(128, 64, java.awt.Image.SCALE_SMOOTH);
	        	icon = new ImageIcon(scaled_img);
	        	label = new JLabel(icon);
	        	label.setAlignmentX(0.5f);
	        	label.setBorder(new EmptyBorder(5,5,5,5));
	        	add(label);
	        } 
			
	        add(Box.createRigidArea(new Dimension(0, 40)));

	        JButton but_close = new JButton("OK");
	        but_close.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent event) {
	                dispose();
	            }
	        });
	        but_close.setAlignmentX(0.5f);
	        add(but_close);

	        this.setModalityType(ModalityType.APPLICATION_MODAL);
        	this.setTitle("About " + APP_NAME);
	        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	        this.setLocationRelativeTo(null);
	        this.setSize(360,400);
	        if (appCustomization().equals("desitin"))
	        	this.setSize(360, 500);
	        else if (appCustomization().equals("meddrugs"))
	        	this.setSize(360, 450);
	        this.setResizable(false);
	    }
	}	
	
	static class ContactDialog extends JDialog {
		
		ContactDialog() {
	        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	        add(Box.createRigidArea(new Dimension(0, 10)));

	        ImageIcon icon = null;
	        if (appCustomization().equals("ywesee")) {
	        	icon = new ImageIcon(AMIKO_ICON);
	        } else if (appCustomization().equals("desitin")) {
		        icon = new ImageIcon(DESITIN_ICON);
	        } else if (appCustomization().equals("meddrugs")) {
	        	icon = new ImageIcon(MEDDRUGS_ICON);
	        } else if (appCustomization().equals("zurrose")) {
	        	icon = new ImageIcon(ZURROSE_ICON);
	        }
	        Image img = icon.getImage();
		    Image scaled_img = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
		    icon = new ImageIcon(scaled_img);
		    JLabel label = new JLabel(icon);
		    label.setAlignmentX(0.5f);
		    label.setBorder(new EmptyBorder(5,5,5,5));
		    add(label);
	        
	        add(Box.createRigidArea(new Dimension(0, 10)));

			JButton info = new JButton();
			if (appLanguage().equals("de")) {
				info.setText(
						"<html><center>" +
				        "Kontaktieren Sie bitte Zeno Davatz<br>" +
				        "E-Mail-Adresse: zdavatz@ywesee.com<br>" +
				        "</center></html>");
			} else if (appLanguage().equals("fr")) {
				info.setText(
						"<html><center>" +
				        " S'il vous plait nous contacter au<br>" +
				        "zdavatz@ywesee.com<br>" +
				        "</center></html>");
			}
			info.setFont(new Font("Dialog", Font.PLAIN, 14));
		    info.setAlignmentX(0.5f);
		    info.setBackground(Color.WHITE);
		    info.setOpaque(false);
		    info.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		    info.setBorderPainted(false);
		    info.setFocusPainted(false);
		    info.setContentAreaFilled(false);
		    add(info);
			
	        add(Box.createRigidArea(new Dimension(0, 30)));
		    
			JButton but_close = new JButton("OK");
			but_close.setSize(new Dimension(48,12));
			but_close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					dispose();
				}
			});
	        but_close.setAlignmentX(0.5f);
			add(but_close);
        
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			this.setTitle("Update");
			this.setLocationRelativeTo(null);
			this.setSize(320, 240);
			this.setResizable(false);
		}
	}
	
	private static void createAndShowGUI() {
		// Create and setup window
		final JFrame jframe = new JFrame(APP_NAME);
        int min_width = 1024;
        int min_height = 768;
        jframe.setPreferredSize(new Dimension(min_width, min_height));
		jframe.setMinimumSize(new Dimension(min_width, min_height));
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width-min_width)/2;
        int y = (screen.height-min_height)/2;
        jframe.setBounds(x,y,min_width,min_height);
        
		// Set application icon
        if (appCustomization().equals("ywesee")) {
			ImageIcon img = new ImageIcon("./icons/amiko_icon.png");
			jframe.setIconImage(img.getImage());
        } else if (appCustomization().equals("desitin")) {
			ImageIcon img = new ImageIcon("./icons/desitin_icon.png");
			jframe.setIconImage(img.getImage());
        } else if (appCustomization().equals("meddrugs")) {
			ImageIcon img = new ImageIcon("./icons/meddrugs_icon.png");
			jframe.setIconImage(img.getImage());        	
        } else if (appCustomization().equals("zurrose")) {
			ImageIcon img = new ImageIcon("./icons/amiko_icon.png");
			jframe.setIconImage(img.getImage());        	
        }

		// Setup menubar
		JMenuBar menu_bar = new JMenuBar();	
		// menu_bar.add(Box.createHorizontalGlue());	// --> aligns menu items to the right!
		// Main menus
		JMenu datei_menu = new JMenu("Datei");
		if (appLanguage().equals("fr"))
			datei_menu.setText("Fichier");
		menu_bar.add(datei_menu);		
		JMenu hilfe_menu = new JMenu("Hilfe");
		if (appLanguage().equals("fr"))
			hilfe_menu.setText("Aide");
		menu_bar.add(hilfe_menu);
		JMenu update_menu = new JMenu("Abonnieren");
		if (appLanguage().equals("fr"))
			update_menu.setText("Abonnement");
		if (appCustomization().equals("desitin")) {
			update_menu.setText("Update");
			if (appLanguage().equals("fr"))
				update_menu.setText("Mise à jour");
		}
		menu_bar.add(update_menu);
		// Menu items
		JMenuItem print_item = new JMenuItem("Drucken...");
		JMenuItem quit_item = new JMenuItem("Beenden");
		if (appLanguage().equals("fr")) {
			print_item.setText("Imprimer");		
			quit_item.setText("Terminer");
		}
		datei_menu.add(print_item);
		datei_menu.addSeparator();
		datei_menu.add(quit_item);			
		JMenuItem ywesee_item = new JMenuItem(APP_NAME + " im Internet");
		JMenuItem about_item = new JMenuItem("Info zu " + APP_NAME);		
		if (appLanguage().equals("fr")) {
			ywesee_item.setText(APP_NAME + " sur Internet");
			about_item.setText("A propos de " + APP_NAME);
		}
		hilfe_menu.add(ywesee_item);
		hilfe_menu.add(about_item);
		jframe.setJMenuBar(menu_bar);
		
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
		print_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				m_web_panel.print();
			}
		});
		quit_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				m_web_panel.dispose();
				Runtime.getRuntime().exit(0);
			}
		});
		update_menu.addMenuListener(new MenuListener() {
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
				} else if (appCustomization().equals("desitin")) {
					if (Desktop.isDesktopSupported()) {
						try {
							URI mail_to_uri = URI.create("mailto:info@desitin.ch?subject=AmiKo%20Desktop%20Desitin%20Update");
							Desktop.getDesktop().mail(mail_to_uri);
						} catch (IOException e) {
							// TODO:
						}
					} else {
						ContactDialog cd = new ContactDialog();
						cd.setVisible(true);
					}
				} else if (appCustomization().equals("meddrugs")) {
					if (Desktop.isDesktopSupported()) {
						try {
							URI mail_to_uri = URI.create("mailto:mo@just-medical.com?subject=med-drugs%20desktop%20Update");
							Desktop.getDesktop().mail(mail_to_uri);
						} catch (IOException e) {
							// TODO:
						}
					} else {
						ContactDialog cd = new ContactDialog();
						cd.setVisible(true);
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
			@Override
			public void menuDeselected(MenuEvent event) {
				// do nothing
			}
			@Override
			public void menuCanceled(MenuEvent event) {
				// do nothing
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
				} else if (appCustomization().equals("meddrugs")) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI("http://www.med-drugs.ch"));							
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
				AboutDialog ad = new AboutDialog();
				ad.setVisible(true);
			}
		});
		
		// Container
		final Container container = jframe.getContentPane();
		container.setBackground(Color.WHITE);
		container.setLayout(new BorderLayout());
		
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
		IndexPanel m_section_titles = null;
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
		
		// -> searchField.addActionListener(new ActionListener() {
		// Add keylistener to text field (type as you go feature)
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {		
			/*
			// -> Check if return key was hit
			@Override
			public void actionPerformed(ActionEvent ae) {
			*/
				m_start_time = System.currentTimeMillis();
				m_query_str = searchField.getText();
				if (!m_query_str.isEmpty()) {
					if (m_query_type==0) {
						med_search = m_sqldb.searchTitle(m_query_str);						
						sTitle(m_query_str);
						cardl.show(p_results, final_title);
					} else if (m_query_type==1) {
						med_search = m_sqldb.searchAuth(m_query_str);
						sAuth(m_query_str);
						cardl.show(p_results, final_author);
					} else if (m_query_type==2) {
						med_search = m_sqldb.searchATC(m_query_str);
						sATC(m_query_str);
						cardl.show(p_results, final_atccode);
					} else if (m_query_type==3) {
						med_search = m_sqldb.searchRegNr(m_query_str);
						sRegNr(m_query_str);
						cardl.show(p_results, final_regnr);					
					} else if (m_query_type==4) {
						med_search = m_sqldb.searchIngredient(m_query_str);
						sIngredient(m_query_str);
						cardl.show(p_results, final_ingredient);	
					} else if (m_query_type==5) {
						med_search = m_sqldb.searchApplication(m_query_str);
						sTherapy(m_query_str);
						cardl.show(p_results, final_therapy);	
					} else {
						//
					}
					m_status_label.setText(med_search.size() + " Suchresultate in " + 
							(System.currentTimeMillis()-m_start_time)/1000.0f + " Sek.");
				}
				if (DEBUG)
					System.out.println("Time for search in [sec]: " + (System.currentTimeMillis()-m_start_time)/1000.0f);
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
	}	
	
	static void sTitle(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		Pattern p_red = Pattern.compile(".*O]");
		Pattern p_green = Pattern.compile(".*G]");
		for (int i=0; i<med_search.size(); ++i) {
			Medication ms = med_search.get(i);
			Scanner pack_str_scanner = new Scanner(ms.getPackInfo());
			String pack_info_str = "";
			while (pack_str_scanner.hasNextLine()) {
				String pack_str_line = pack_str_scanner.nextLine();
				Matcher m_red = p_red.matcher(pack_str_line);
				Matcher m_green = p_green.matcher(pack_str_line);							
				if (m_red.find())
					pack_info_str += "<font color=red>" + pack_str_line + "</font><br>";					
				else if (m_green.find())
					pack_info_str += "<font color=green>" + pack_str_line + "</font><br>";
				else
					pack_info_str += "<font color=gray>" + pack_str_line + "</font><br>";
			}
			pack_str_scanner.close();
			m.add("<html><b>" + ms.getTitle() + "</b><br><font size=-1>" + pack_info_str +"</font></html>");
			med_id.add(ms.getId());
		}
		m_list_titles.update(m);
	}

	static void sAuth(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		for (int i=0; i<med_search.size(); ++i) {
			Medication ms = med_search.get(i);
			m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + ms.getAuth() + "</font></html>");
			med_id.add(ms.getId());
		}
		m_list_auths.update(m);
	}
	
	static void sATC(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		for (int i=0; i<med_search.size(); ++i) {
			Medication ms = med_search.get(i);
			String atc_code_str = ms.getAtcCode().replaceAll(";", " - ");
			m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + atc_code_str + "</font></html>");
			med_id.add(ms.getId());
		}										
		m_list_atccodes.update(m);
	}

	static void sRegNr(String query_str) {
		med_id.clear();
		List<String> m = new ArrayList<String>();
		for (int i=0; i<med_search.size(); ++i) {
			Medication ms = med_search.get(i);
			m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + ms.getRegnrs()+"</font></html>");
			med_id.add(ms.getId());
		}												
		m_list_regnrs.update(m);
	}
	
	static void sIngredient(String query_str) {
		med_id.clear();					
		List<String> m = new ArrayList<String>();
		for (int i=0; i<med_search.size(); ++i) {
			Medication ms = med_search.get(i);
			m.add("<html><b>" + ms.getSubstances() + "</b><br><font color=gray size=-1>" + ms.getTitle()+"</font></html>");
			med_id.add(ms.getId());
		}						
		m_list_ingredients.update(m);
	}
	
	static void sTherapy(String query_str) {
		med_id.clear();					
		List<String> m = new ArrayList<String>();
		for (int i=0; i<med_search.size(); ++i) {
			Medication ms = med_search.get(i);
			String application_str = ms.getApplication().replaceAll("\n", "<p>");
			application_str = ms.getApplication().replaceAll(";", "<p>");
			m.add("<html><b>" + ms.getTitle() + "</b><br><font color=gray size=-1>" + application_str + "</font></html>");
			med_id.add(ms.getId());
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
