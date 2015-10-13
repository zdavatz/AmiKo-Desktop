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

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities.EscapeMode;

public class Utilities {

	static public String appDataFolder() {
		return (System.getenv("APPDATA") + "\\Ywesee\\" + Constants.APP_NAME);
	}
	
	static public String appLanguage() {
		if (Constants.DB_LANGUAGE.equals("DE"))
			return "de";
		else if (Constants.DB_LANGUAGE.equals("FR"))
			return "fr";
		else if (Constants.APP_NAME.equals(Constants.AMIKO_NAME) || Constants.APP_NAME.equals(Constants.AMIKO_DESITIN_NAME) 
				|| Constants.APP_NAME.equals(Constants.AMIKO_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.AMIKO_ZURROSE_NAME)
				|| Constants.APP_NAME.equals(Constants.AMIKO_IBSA_NAME)) {
			return "de";
		} else if (Constants.APP_NAME.equals(Constants.COMED_NAME) || Constants.APP_NAME.equals(Constants.COMED_DESITIN_NAME) 
				|| Constants.APP_NAME.equals(Constants.COMED_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.COMED_ZURROSE_NAME)
				|| Constants.APP_NAME.equals(Constants.COMED_IBSA_NAME)) {
			return "fr";
		}
		return "";
	}

	static public String appCustomization() {		
		if (Constants.APP_NAME.equals(Constants.AMIKO_NAME) || Constants.APP_NAME.equals(Constants.COMED_NAME)) {
			return "ywesee";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_DESITIN_NAME) || Constants.APP_NAME.equals(Constants.COMED_DESITIN_NAME)) {
			return "desitin";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_MEDDRUGS_NAME) || Constants.APP_NAME.equals(Constants.COMED_MEDDRUGS_NAME)) {
			return "meddrugs";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_ZURROSE_NAME) || Constants.APP_NAME.equals(Constants.COMED_ZURROSE_NAME)) {
			return "zurrose";
		} else if (Constants.APP_NAME.equals(Constants.AMIKO_IBSA_NAME) || Constants.APP_NAME.equals(Constants.COMED_IBSA_NAME)) {
			return "ibsa";
		}
		return "";
	}
	
	static public boolean showFullSettings() {
		return (appCustomization().equals("ibsa") || appCustomization().equals("zurrose") 
				|| appCustomization().equals("ywesee") || appCustomization().equals("desitin"));
	}
	
	static public String prettyFormat(float value) {
		return String.format("%,.2f", value);
	}
	
	static public boolean isInternetReachable() {
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
	
	static public String prettyHtml(String str) {
		org.jsoup.nodes.Document mDoc = Jsoup.parse(str);
		
		mDoc.outputSettings().escapeMode(EscapeMode.xhtml);
		mDoc.outputSettings().prettyPrint(true);
		mDoc.outputSettings().indentAmount(2);
		
		return mDoc.toString();
	}
	
	static public void setUIFont(FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
	    while (keys.hasMoreElements()) {
	    	Object key = keys.nextElement();
	        Object value = UIManager.get(key);
	        if (value instanceof FontUIResource) {
	        	FontUIResource orig = (FontUIResource) value;
	            Font font = new Font(f.getFontName(), orig.getStyle(), orig.getSize());
	            UIManager.put(key, new FontUIResource(font));
	        }
	    }
	}
	
   static void recursivelySetFonts(Component comp, Font font) {
        comp.setFont(font);
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            for(int j=0, ub=cont.getComponentCount(); j<ub; ++j)
                recursivelySetFonts(cont.getComponent(j), font);
        }
    }
}
