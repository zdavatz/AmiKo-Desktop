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
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import chrriis.dj.nativeswing.NSComponentOptions;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class AmiKoDialogs extends JDialog {

	private String mAppLang;
	private String mAppCustom;

	public AmiKoDialogs(String app_lang, String app_custom) {
		mAppLang = app_lang;
		mAppCustom = app_custom;
	}

	public void AboutDialog() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		add(Box.createRigidArea(new Dimension(0, 10)));

		ImageIcon icon = null;
		if (mAppCustom.equals("ywesee")) {
			icon = new ImageIcon(Constants.AMIKO_ICON);
		} else if (mAppCustom.equals("desitin")) {
			icon = new ImageIcon(Constants.DESITIN_ICON);
		} else if (mAppCustom.equals("meddrugs")) {
			icon = new ImageIcon(Constants.MEDDRUGS_ICON);
		} else if (mAppCustom.equals("zurrose")) {
			icon = new ImageIcon(Constants.ZURROSE_ICON);
		}
		Image img = icon.getImage();
		Image scaled_img = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
		if (mAppCustom.equals("meddrugs"))
			scaled_img = img.getScaledInstance(128, 128,  java.awt.Image.SCALE_SMOOTH);
		icon = new ImageIcon(scaled_img);
		JLabel label = new JLabel(icon);
		label.setAlignmentX(0.5f);
		label.setBorder(new EmptyBorder(5,5,5,5));
		add(label);

		add(Box.createRigidArea(new Dimension(0, 10)));

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
			String app_text = "";
			String sponsoring = "";
			if (mAppLang.equals("de")) {
				app_text = "Arzneimittel-Kompendium für Windows<br>";
				if (mAppCustom.equals("ywesee")) {
					app_text = "Arzneimittel-Kompendium für Windows<br>";					
				} else if (mAppCustom.equals("desitin")) {
					sponsoring = "<br>" +
							"<br>Unterstützt durch Desitin Pharma GmbH" +
							"<br>";
				} else if (mAppCustom.equals("meddrugs")) {
					app_text = "Schweizer Medikamenten-Enzyklopädie für Windows<br>";
				} else if (mAppCustom.equals("zurrose")) {
					//
				}

				info.setText(
						"<html><center><b>" + Constants.APP_NAME + "</b><br><br>" +
								app_text +
								"Version " + Constants.APP_VERSION + "<br>" + 
								Constants.GEN_DATE + "<br>" +
								"Lizenz: GPLv3.0<br><br>" +
								"Konzept: Zeno R.R. Davatz<br>" +
								"Entwicklung: Dr. Max Lungarella<br>" +
								sponsoring +
						"</center></html>");
			} else if (mAppLang.equals("fr")) {
				app_text = "Compendium des Médicaments Suisse pour Windows<br>";
				if (mAppCustom.equals("ywesee")) {
					app_text = "Compendium des Médicaments Suisse pour Windows<br>";
				} else if (mAppCustom.equals("desitin")) {
					sponsoring = "<br>" +
							"<br>Supporteé par Desitin Pharma GmbH" +
							"<br>";
				} else if (mAppCustom.equals("meddrugs")) {
					app_text = "Encyclopédie des médicaments de la Suisse<br>";
				} else if (mAppCustom.equals("zurrose")) {
					//
				}
				info.setText(
						"<html><center><br>" + Constants.APP_NAME + "</b><br><br>" +
								app_text +
								"Version " + Constants.APP_VERSION + "<br>" + 
								Constants.GEN_DATE + "<br>" +
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

		if (mAppCustom.equals("desitin")) {
			add(Box.createRigidArea(new Dimension(0, 10)));
			icon = new ImageIcon(Constants.DESITIN_LOGO);
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
		this.setTitle("About " + Constants.APP_NAME);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setSize(360,400);
		if (mAppCustom.equals("desitin"))
			this.setSize(360, 540);
		else if (mAppCustom.equals("meddrugs"))
			this.setSize(360, 450);
		this.setResizable(false);
		
		this.setVisible(true);
	}

	public void ContactDialog() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		add(Box.createRigidArea(new Dimension(0, 10)));

		ImageIcon icon = null;
		if (mAppCustom.equals("ywesee")) {
			icon = new ImageIcon(Constants.AMIKO_ICON);
		} else if (mAppCustom.equals("desitin")) {
			icon = new ImageIcon(Constants.DESITIN_ICON);
		} else if (mAppCustom.equals("meddrugs")) {
			icon = new ImageIcon(Constants.MEDDRUGS_ICON);
		} else if (mAppCustom.equals("zurrose")) {
			icon = new ImageIcon(Constants.ZURROSE_ICON);
		}
		Image img = icon.getImage();
		Image scaled_img = img.getScaledInstance(64, 64,
				java.awt.Image.SCALE_SMOOTH);
		icon = new ImageIcon(scaled_img);
		JLabel label = new JLabel(icon);
		label.setAlignmentX(0.5f);
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(label);

		add(Box.createRigidArea(new Dimension(0, 10)));

		JButton info = new JButton();
		if (mAppLang.equals("de")) {
			info.setText("<html><center>"
					+ "Kontaktieren Sie bitte Zeno Davatz<br>"
					+ "E-Mail-Adresse: zdavatz@ywesee.com<br>"
					+ "</center></html>");
		} else if (mAppLang.equals("fr")) {
			info.setText("<html><center>"
					+ " S'il vous plait nous contacter au<br>"
					+ "zdavatz@ywesee.com<br>" + "</center></html>");
		}
		info.setFont(new Font("Dialog", Font.PLAIN, 14));
		info.setAlignmentX(0.5f);
		info.setBackground(Color.WHITE);
		info.setOpaque(false);
		info.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		info.setBorderPainted(false);
		info.setFocusPainted(false);
		info.setContentAreaFilled(false);
		add(info);

		add(Box.createRigidArea(new Dimension(0, 30)));

		JButton but_close = new JButton("OK");
		but_close.setSize(new Dimension(48, 12));
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
		
		this.setVisible(true);
	}
	
	public void NoInternetDialog() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		add(Box.createRigidArea(new Dimension(0, 10)));

		ImageIcon icon = null;
		if (mAppCustom.equals("ywesee")) {
			icon = new ImageIcon(Constants.AMIKO_ICON);
		} else if (mAppCustom.equals("desitin")) {
			icon = new ImageIcon(Constants.DESITIN_ICON);
		} else if (mAppCustom.equals("meddrugs")) {
			icon = new ImageIcon(Constants.MEDDRUGS_ICON);
		} else if (mAppCustom.equals("zurrose")) {
			icon = new ImageIcon(Constants.ZURROSE_ICON);
		}
		Image img = icon.getImage();
		Image scaled_img = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
		icon = new ImageIcon(scaled_img);
		JLabel label = new JLabel(icon);
		label.setAlignmentX(0.5f);
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(label);

		add(Box.createRigidArea(new Dimension(0, 10)));

		JButton info = new JButton();
		if (mAppLang.equals("de")) {
			info.setText("<html><center>"
					+ "Für die Aktualisierung brauchen Sie<br>"
					+ "eine aktive Internetverbindung.<br>"
					+ "</center></html>");
		} else if (mAppLang.equals("fr")) {
			info.setText("<html><center>"
					+ "Pour la mise à jour vous devez disposer<br>"
					+ "d’une connexion Internet active.<br>" 
					+ "</center></html>");
		}
		info.setFont(new Font("Dialog", Font.PLAIN, 14));
		info.setAlignmentX(0.5f);
		info.setBackground(Color.WHITE);
		info.setOpaque(false);
		info.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		info.setBorderPainted(false);
		info.setFocusPainted(false);
		info.setContentAreaFilled(false);
		add(info);

		add(Box.createRigidArea(new Dimension(0, 30)));

		JButton but_close = new JButton("OK");
		but_close.setSize(new Dimension(48, 12));
		but_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		but_close.setAlignmentX(0.5f);
		add(but_close);

		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setTitle("Keine Internetverbindung");
		this.setLocationRelativeTo(null);
		this.setSize(320, 240);
		this.setResizable(false);
		
		this.setVisible(true);
	}
	
	public void ShoppingCartDialog(String ean, boolean to_shopping_cart, ResourceBundle rb) {
        final JDialog dialog = new JDialog(this, rb.getString("shoppingCart"), true);
		JPanel panel = new JPanel();
		JLabel label = new JLabel();	
		int display_time = 2000;		
		int dialog_width = 340;
		int dialog_height = 60;
		if (to_shopping_cart) {
			label.setText("<html><b>" + ean + "</b> " + rb.getString("addCart") + "</html>");
		} else {
			label.setText("<html>" + rb.getString("noPrice") + "<br>" + rb.getString("callAuth") + "</html>");
			display_time = 4000;
			dialog_width = 340;
			dialog_height = 80;
		}
			
        Timer timer = new Timer(display_time, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        
		panel = new JPanel(new BorderLayout(5, 5));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

		dialog.getContentPane().add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setModal(false);
		dialog.setSize(dialog_width, dialog_height);
		dialog.setResizable(false);
		dialog.setAlwaysOnTop(true);
        dialog.setVisible(true); 	// If modal, application will pause here
	}
	
	public void AgbDialog() {		
		String title = "Allgemeine Geschäftsbedingungen";
		if (Utilities.appLanguage().equals("fr"))
			title = "Conditions générales";		
        final JDialog dialog = new JDialog(this, title, true);		
		
        JWebBrowser jAgb = new JWebBrowser(NSComponentOptions.destroyOnFinalization());
		jAgb.setBarsVisible(false);	
		// Default AGB text
		String agb_html = "<html>No AGBs...</html>";
		String agb_file = Utilities.appDataFolder() + "/" + Constants.AGBS_HTML + Utilities.appLanguage() + ".html";
		if ((new File(agb_file)).exists())
			agb_html = FileOps.readFromFile(agb_file);
		else 
			agb_html = FileOps.readFromFile("./shop/agbs_" + Utilities.appLanguage() + ".html");				
		jAgb.setHTMLContent("<html>" + agb_html + "</html>");
		jAgb.setVisible(true);
		
		dialog.getContentPane().add(jAgb);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setModal(false);
		dialog.setSize(640, 640);
		dialog.setResizable(false);
		dialog.setAlwaysOnTop(true);
        dialog.setVisible(true); 	// If modal, application will pause here
	}
	
	public void UploadDialog(String msg) {
        final JDialog dialog = new JDialog(this, "Preisvergleich", true);
		JPanel panel = new JPanel();
		JLabel label = new JLabel();	
		int display_time = 3000;		
		int dialog_width = 340;
		int dialog_height = 60;
		label.setText("<html>" + msg + "</html>");
			
        Timer timer = new Timer(display_time, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        
		panel = new JPanel(new BorderLayout(5, 5));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

		dialog.getContentPane().add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setModal(false);
		dialog.setSize(dialog_width, dialog_height);
		dialog.setResizable(false);
		dialog.setAlwaysOnTop(true);
        dialog.setVisible(true); 	// If modal, application will pause here
	}
}
