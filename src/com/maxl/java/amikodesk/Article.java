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
	private String pack_title;
	private String pack_size;
	private String pack_unit;
	private String exfactory_price;
	private String public_price;
	private String additional_info;
	private String ean_code;
	private String pharma_code;
	private String author;
	private String dropdown_str;
	private float margin = -1.0f;	// <0.0f -> not initialized
	private float buying_price = 0.0f;
	private float selling_price = 0.0f;;
	private int quantity = 1;
	private int assorted_quantity = 1;
	private int draufgabe = 0;
	private float cash_rebate = 0.0f; // [%]
	private int onstock;
	private boolean special = false;
	
	public Article(String[] entry) {
		if (entry!=null) {
			if (Utilities.appLanguage().equals("de")) {
				ean_code = pharma_code = pack_title = pack_size 
					= pack_unit = public_price = exfactory_price = additional_info = "k.A.";
			} else if (Utilities.appLanguage().equals("fr")) {
				ean_code = pharma_code = pack_title = pack_size 
						= pack_unit = public_price = exfactory_price = additional_info = "p.c.";
			}
			if (entry.length>7) {
				if (!entry[0].isEmpty())
					pack_title = entry[0];
				if (!entry[1].isEmpty())
					pack_size = entry[1];
				if (!entry[2].isEmpty())
					pack_unit = entry[2];
				if (!entry[3].isEmpty())
					public_price = entry[3];
				if (!entry[4].isEmpty())
					exfactory_price = entry[4];
				if (!entry[5].isEmpty())				
					additional_info = entry[5];				
				if (!entry[6].isEmpty())
					ean_code = entry[6];
				if (!entry[7].isEmpty())
					pharma_code = entry[7];
			}
			quantity = 1;
		}
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
	
	public String getDropDownStr() {
		return dropdown_str;
	}
	
	public void setDropDownStr(String dropdown_str) {
		this.dropdown_str = dropdown_str;
	}
	
	public boolean isSpecial() {
		return special;
	}
	
	public void setSpecial(boolean special) {
		this.special = special;
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
		else
			price = "k.A.";
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
			price = "k.A.";
		return price;
	}
	
	public float getPublicPriceAsFloat() {
		float public_as_float = 0.0f;
		String price_pruned = public_price.replaceAll("[^\\d.]", "");
		if (!price_pruned.isEmpty() && !price_pruned.equals("..")) {
			public_as_float = Float.parseFloat(price_pruned);
		}						
		return public_as_float;	
	}
	
	public float getTotPublicPrice() {
		return quantity*getPublicPriceAsFloat();
	}
	
	/**
	 * Buying price (what the doctor/pharmacy pays, defined by drug company)
	*/	
	public float getBuyingPrice() {					
		return buying_price;
	}
	
	public float getTotBuyingPrice() {
		return quantity*buying_price;
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
		return (quantity+draufgabe)*selling_price;
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
		this.cash_rebate = cash_rebate;
	}
	
	/**
	 * Calculates  "barrabatt"
	 * @return
	 */
	public float getCashRebate() {
		/*
		if (quantity+draufgabe>0) {
			float buying_rebated = tot_buying_price/(quantity+draufgabe);
			if (buying_rebated>0.0f) {
				float diff = buying_price-buying_rebated;
				if (diff>0.0f)
					return (int)(0.5f+100.0f*(diff/buying_price));
				else 
					return 0;
			}
		}
		*/
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
	
	public String getAdditionalInfo() {
		return additional_info;
	}	
	
	public void setAdditionalInfo(String additional_info) {
		this.additional_info = additional_info;
	}
	
	
    public String getCategories() {
    	String cat = "";    	
    	String[] c = additional_info.split(",");
    	for (int i=0; i<3; ++i) {
    		if (!c[i].isEmpty() && c[i].trim().matches("^[a-zA-Z]+$")) {
    			cat += (c[i].trim() + ", ");
    		}
    	}
    	if (cat.length()>2)
    		return cat.substring(0,cat.length()-2);
    	return "";
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
}
