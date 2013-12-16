package com.maxl.java.amikodesk;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

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
			String sponsoring = "";
			if (mAppLang.equals("de")) {
				if (mAppCustom.equals("ywesee")) {
					sponsoring = "<br>" +
							"<br><a href=\"\">AmiKo / CoMed</a>" +
							"<br>Arzneimittel-Kompendium für Android" +
							"<br>";
				} else if (mAppCustom.equals("desitin")) {
					sponsoring = "<br>" +
							"<br>Unterstützt durch Desitin Pharma GmbH" +
							"<br>";
				} else if (mAppCustom.equals("meddrugs")) {
					//
				} else if (mAppCustom.equals("zurrose")) {
					//
				}

				info.setText(
						"<html><center><b>" + Constants.APP_NAME + "</b><br><br>" +
								"Arzneimittel-Kompendium für Windows PC<br>" +
								"Version " + Constants.APP_VERSION + "<br>" + 
								Constants.GEN_DATE + "<br>" +
								"Lizenz: GPLv3.0<br><br>" +
								"Konzept: Zeno R.R. Davatz<br>" +
								"Entwicklung: Dr. Max Lungarella<br>" +
								sponsoring +
						"</center></html>");
			} else if (mAppLang.equals("fr")) {
				if (mAppCustom.equals("ywesee")) {
					sponsoring = "<br>" +
							"<br><a href=\"\">AmiKo / CoMed</a>" +
							"<br>Compedium des Médicaments pour Android" +
							"<br>";
				} else if (mAppCustom.equals("desitin")) {
					sponsoring = "<br>" +
							"<br>Supporteé par Desitin Pharma GmbH" +
							"<br>";
				} else if (mAppCustom.equals("meddrugs")) {
					//
				} else if (mAppCustom.equals("zurrose")) {
					//
				}
				info.setText(
						"<html><center><br>" + Constants.APP_NAME + "</b><br><br>" +
								"Compendium des Médicaments Suisse pour Windows<br>" +
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
}
