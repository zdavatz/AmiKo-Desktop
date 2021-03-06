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

public class Article implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String pack_title;
	private String pack_size;
	private String pack_unit;
	private String pack_galen;
	private String active_substance;
	private String atc_code;
	private String atc_class;
	private String exfactory_price = "";
	private String public_price = "";
	private String fap_price = "";
	private String fep_price = "";
	private String value_added_tax = "";
	private String additional_info;
	private String regnr = "";
	private String ean_code = "";
	private String pharma_code = "";
	private String therapy_code = "";
	private String author = "";
	private String supplier = "";
	private String availability = "";
	private String dropdown_str;
	private String author_code = "";
	private String replace_ean_code = "";
	private String replace_pharma_code = "";
	private String flags = "";
	private float margin = -1.0f;	// <0.0f -> not initialized
	private float buying_price = 0.0f;
	private float selling_price = 0.0f;;
	private int quantity = 1;
	private int assorted_quantity = 0;
	private int draufgabe = 0;
	private float cash_rebate = 0.0f; // [%]
	private int onstock;
	private int likes;
	private int visible;
	private int free_samples;
	
	public Article() {
		//
	}
	
	public Article(String[] entry, String author) {
		if (entry!=null) {
			if (Utilities.appLanguage().equals("de")) {
				ean_code = pharma_code = pack_title = pack_size 
						= pack_unit = public_price = exfactory_price 
						= additional_info = "k.A.";
			} else if (Utilities.appLanguage().equals("fr")) {
				ean_code = pharma_code = pack_title = pack_size 
						= pack_unit = public_price = exfactory_price 
						= additional_info = "n.s.";
			}
			if (author!=null && !author.isEmpty())
				this.author = author;
			// efp + "|" + pup + "|" + fap + "|" + fep + "|" + vat
			if (entry.length>10) {
				if (!entry[0].isEmpty())
					pack_title = entry[0];
				if (!entry[1].isEmpty())
					pack_size = entry[1];
				if (!entry[2].isEmpty())
					pack_unit = entry[2];				
				if (!entry[3].isEmpty())
					exfactory_price = entry[3];
				if (!entry[4].isEmpty())
					public_price = entry[4];
				if (!entry[5].isEmpty())
					fap_price = entry[5];
				if (!entry[6].isEmpty())
					fep_price = entry[6];							
				if (!entry[7].isEmpty())
					value_added_tax = entry[7];
				if (!entry[8].isEmpty())				
					additional_info = entry[8];				
				if (!entry[9].isEmpty())
					ean_code = entry[9];
				if (!entry[10].isEmpty())
					pharma_code = entry[10];
				if (entry.length>11) {
					if (!entry[11].isEmpty()) {
						visible = Integer.parseInt(entry[11]);
					}
				}
				if (entry.length>12) {
					if (!entry[12].isEmpty())
						free_samples = Integer.parseInt(entry[12]);
				}
			}
			quantity = 1;
		}
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getPackTitle() {
		return pack_title;
	}

	public void setPackTitle(String pack_title) {
		this.pack_title = pack_title;
	}
	
	public String getPackSize() {
		return pack_size;
	}

	public void setPackSize(String pack_size) {
		this.pack_size = pack_size;
	}
	
	public String getPackUnit() {
		return pack_unit;
	}

	public void setPackUnit(String pack_unit) {
		this.pack_unit = pack_unit;
	}
	
	public String getPackGalen() {
		return pack_galen;
	}
	
	public void setPackGalen(String pack_galen) {
		this.pack_galen = pack_galen;
	}

	public void setRegnr(String regnr) {
		this.regnr = regnr;
	}
	
	public String getRegnr() {
		return regnr;
	}
	
	public String getEanCode() {
		return ean_code;
	}
	
	public void setEanCode(String ean_code) {
		this.ean_code = ean_code;
	}
	
	public String getPharmaCode() {
		return pharma_code;
	}

	public void setPharmaCode(String pharma_code) {
		this.pharma_code = pharma_code;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getSupplier() {
		return supplier;
	}
	
	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}
	
	public String getCode() {
		return author_code;
	}
	
	public void setCode(String code) {
		this.author_code = code;
	}
	
	public String getActiveSubstance() {
		return active_substance;
	}
	
	public void setActiveSubstance(String active_substance) {
		this.active_substance = active_substance;
	}
		
	public String getAtcCode() {
		return atc_code;
	}
	
	public void setAtcCode(String atc_code) {
		this.atc_code = atc_code;
	}

	public String getAtcClass() {
		return atc_class;
	}
	
	public void setAtcClass(String atc_class) {
		this.atc_class = atc_class;
	}
	
	public String getTherapyCode() {
		return therapy_code;
	}
	
	public void setTherapyCode(String therapy_code) {
		this.therapy_code = therapy_code;
	}
	
	public String getDropDownStr() {
		return dropdown_str;
	}
	
	public void setDropDownStr(String dropdown_str) {
		this.dropdown_str = dropdown_str;
	}	
	
	public String getAdditionalInfo() {
		return additional_info;
	}	
	
	public void setAdditionalInfo(String additional_info) {
		this.additional_info = additional_info;
	}
		
    public String getCategories() {
    	String cat = "";    	
    	if (additional_info!=null) {
	    	String[] c = additional_info.split(",");
	    	for (int i=0; i<c.length; ++i) {
	    		if (!c[i].isEmpty() && c[i].trim().matches("^[a-zA-Z]+$")) {
	    			cat += (c[i].trim() + ", ");
	    		}
	    	}
	    	if (cat.length()>2)
	    		return cat.substring(0,cat.length()-2);
	    	return "";
    	}
    	return "";
    }
    
    /*
		user/customer categories are defined in aips2sqlite:GlnCodes.java
			arzt, apotheke, drogerie, spital, grossist
     */
    public boolean isVisible(String user_category) {
    	// For non-ibsa article visible = 0xff
    	if (user_category.equals("arzt") || user_category.equals("apotheke")) {
    		return ((visible & 0x08)>0);
    	} else if (user_category.equals("drogerie")) {
    		String swissmedic_cat = getCategories();
    		if (!swissmedic_cat.isEmpty()) {
    			String s[] = swissmedic_cat.split(",");
    			if (s.length>0)
    				swissmedic_cat = s[0].trim();
    		}
    		return ((visible & 0x04)>0 
    				&& (swissmedic_cat.equals("D") || swissmedic_cat.equals("E") || swissmedic_cat.equals("CE")));
    	} else if (user_category.equals("spital")) {
    		return ((visible & 0x02)>0);
    	} else if (user_category.equals("grossist")) {
    		return ((visible & 0x01)>0);    	
    	} else { // always true for all other categories
    		return true;
    	}
    }

    /*
		user/customer categories are defined in aips2sqlite:ShoppingCart.java
     */
    public boolean hasFreeSamples(String user_category) {
    	if (user_category.equals("B-arzt"))
    		return ((free_samples & 0x0001)>0);
    	else if (user_category.equals("A-arzt"))
    		return ((free_samples & 0x0002)>0);
    	else if (user_category.equals("B-apotheke"))
    		return ((free_samples & 0x0004)>0);
    	else if (user_category.equals("A-apotheke"))
    		return ((free_samples & 0x0008)>0);
       	else if (user_category.equals("B-drogerie"))
    		return ((free_samples & 0x0010)>0);
    	else if (user_category.equals("A-drogerie"))
    		return ((free_samples & 0x0020)>0);
       	else if (user_category.equals("C-spital"))
    		return ((free_samples & 0x0040)>0);
       	else if (user_category.equals("B-spital"))
    		return ((free_samples & 0x0080)>0);
    	else if (user_category.equals("A-spital"))
    		return ((free_samples & 0x0100)>0);   	
    	// for all other cases
    	return false;
    }
    
	public boolean isSpecial() {		
		if (additional_info!=null)
			return (additional_info.contains("SL") || additional_info.contains("LS"));
		return false;
	}
	
	public float getVat() {
		if (value_added_tax!=null && !value_added_tax.isEmpty())
			return Float.parseFloat(value_added_tax);
		else
			return 2.5f;
	}
	
	/**
	 * Price dependent on customer category
	 */
	public String getPrice(String user_category) {
		if (author.toLowerCase().contains("ibsa")) {
			if (user_category.equals("spital")) {
				if (fap_price.isEmpty())
					return exfactory_price;
				return fap_price;
			} else {
				if (fep_price.isEmpty())
					return exfactory_price;
				return fep_price;
			}
		}
		return exfactory_price;
	}
	
	/**
	 * Does the article have a price?
	 */
	public boolean hasPrice(String user_category) {
		String no_info_str = "k.A.";
		if (Utilities.appLanguage().equals("fr"))
			no_info_str = "n.s.";
		if (author.toLowerCase().contains("ibsa")) {
			if (user_category.equals("spital")) {
				if (fap_price.isEmpty())
					return !exfactory_price.contains(no_info_str);
				return !fap_price.contains(no_info_str);
			} else {
				if (fep_price.isEmpty())
					return !exfactory_price.contains(no_info_str);
				return !fep_price.contains(no_info_str);
			}
		}
		return !exfactory_price.contains(no_info_str);
	}
	
	/**
	 * Exfactory price
	*/
	public String getExfactoryPrice() {
		return exfactory_price;
	}

	public void setExfactoryPrice(String exfactory_price) {
		this.exfactory_price = exfactory_price;
	}
	
	public String getCleanExfactoryPrice() {
		String price = exfactory_price;
		String price_pruned = price.replaceAll("[^\\d.]", "");
		if (!price_pruned.isEmpty() && !price_pruned.equals(".."))
			price = price_pruned;
		else {
			if (Utilities.appLanguage().equals("de"))
				price = "k.A.";
			else if (Utilities.appLanguage().equals("fr"))
				price = "n.s.";
		}
		return price;
	}
	
	public float getExfactoryPriceAsFloat() {
		float exfacto_as_float = 0.0f;
		String price_pruned = exfactory_price.replaceAll("[^\\d.]", "");
		if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {
			exfacto_as_float = Float.parseFloat(price_pruned);
		}						
		return exfacto_as_float;
	}

	public float getTotExfactoryPrice() {
		return quantity*getExfactoryPriceAsFloat();
	}
	
	/**
	 * Public price
	*/	
	public String getPublicPrice() {
		return public_price;
	}	
	
	public void setPublicPrice(String public_price) {
		this.public_price = public_price;
	}	
	
	public String getCleanPublicPrice() {
		String price = public_price;
		String price_pruned = price.replaceAll("[^\\d.]", "");
		if (!price_pruned.isEmpty() && !price_pruned.equals(".."))
			price = price_pruned;
		else
			if (Utilities.appLanguage().equals("de"))
				price = "k.A.";
			else if (Utilities.appLanguage().equals("fr"))
				price = "n.s.";
		return price;
	}
	
	public float getPublicPriceAsFloat() {
		float public_as_float = 0.0f;		
		String price_pruned = public_price.replaceAll("[^\\d.]", "");
		if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {
			public_as_float = Float.parseFloat(price_pruned);
		} else {
			public_as_float = getExfactoryPriceAsFloat() * 1.80f;
		}
		return public_as_float;	
	}
	
	public float getTotPublicPrice() {
		return quantity*getPublicPriceAsFloat();
	}
	
	/**
	 * FAP
	 */
	public String getFapPrice() {
		return this.fap_price;
	}
	
	/**
	 * FEP
	 */
	public String getFepPrice() {
		return this.fep_price;
	}
		
	/**
	 * Buying price (what the doctor/pharmacy pays, defined by drug company)
	*/	
	public float getBuyingPrice(float cr) {					
		return buying_price*(1.0f-cr/100.0f);
	}
	
	public float getTotBuyingPrice(float cr) {
		return quantity*buying_price*(1.0f-cr/100.0f);
	}

	public void setBuyingPrice(float buying_price) {
		if (margin>=0.0f)
			selling_price = (1.0f+margin)*buying_price;
		else	// default
			selling_price = 1.8f*buying_price;
		this.buying_price = buying_price;
	}
	
	/**
	 * Selling price = consumer price (what the consumer pays, defined by doctor/pharmacy)
	*/		
	public float getSellingPrice() {
		if (margin>=0.0f)
			selling_price = (1.0f+margin)*buying_price;
		else	// default
			selling_price = 1.8f*buying_price;
		return selling_price;
	}

	public float getTotSellingPrice() {
		// Update selling price first... need to take into account the margin.
		float s = getSellingPrice();
		return (quantity+draufgabe)*s;
	}
		
	/**
	 * Calculates the profit
	 * @return
	 */
	public int getProfit(float tot_buying_price, float tot_selling_price) {
		if (tot_buying_price>0.0f)
			return (int)(0.5f+(tot_selling_price/tot_buying_price-1.0f)*100.0f);
		return 0;
	}

	public void setCashRebate(float cash_rebate) {
		if (cash_rebate>0)
			draufgabe = 0;
		this.cash_rebate = cash_rebate;
	}
	
	/**
	 * Calculates  "barrabatt"
	 * @return
	 */
	public float getCashRebate() {
		if ((cash_rebate>=0.0f && cash_rebate<0.01f) || (cash_rebate<=0.0f && cash_rebate>-0.01f))
			cash_rebate = 0.0f;
		if (draufgabe>0)
			return (100.0f*(float)draufgabe/(draufgabe+quantity));
		else 
			return cash_rebate;
	}
	
	public void setMargin(float margin) {
		this.margin = margin;
	}
	
	public float getMargin() {
		return margin;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public void incrementQuantity() {
		quantity++;
	}
	
	public void decrementQuantity() {
		quantity--;
		if (quantity<0)
			quantity = 0;
	}
	
	public int getAssortedQuantity() {
		return assorted_quantity;
	}
	
	public void setAssortedQuantity(int assorted_quantity) {
		this.assorted_quantity = assorted_quantity;
	}
	
	public int getDraufgabe() {
		return draufgabe;
	}
	
	public void setDraufgabe(int draufgabe) {
		if (draufgabe>0)
			this.draufgabe = draufgabe;
		else
			this.draufgabe = 0;
	}
	
	public void setItemsOnStock(int onstock) {
		this.onstock = onstock;
	}
	
	public int getItemsOnStock() {
		return onstock;
	}
	
	public void setLikes(int likes) {
		this.likes = likes;
	}
	
	public int getLikes() {
		return likes;
	}
	
	public void setAvailability(String availability) {
		this.availability = availability;
	}
	
	public String getAvailability() {
		return availability;
	}
	
	public boolean isOffMarket() {
		return availability.equals("-1");
	}
	
	public void setReplaceEan(String ean) {
		this.replace_ean_code = ean;
	}
	
	public String getReplaceEan() {
		return replace_ean_code;
	}
	
	public void setReplacePharma(String pharma) {
		this.replace_pharma_code = pharma;
	}
	
	public String getReplacePharma() {
		return replace_pharma_code;
	}
	
	public void setFlags(String flags) {
		this.flags = flags;
	}
	
	public String getFlags() {
		return flags;
	}
}
