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

public class Author {

	private String name;
	private String company;
	private String email;
	private String email_cc;
	private String address;	
	private String salutation;
	private String s;	
	private String l;
	private String p;
	private String o;
	private float subtotal_CHF;
	private float vat25_CHF;
	private float vat80_CHF;
	private float shipping_CHF;
	private float additional_discount;
	private String shipping_type;
	
	public Author() {
		//
	}
	
	public Author(Author another) {
		this.name = another.name;
		this.company = another.company;
		this.email = another.email;
		this.email_cc = another.email_cc;
		this.address = another.address;
		this.salutation = another.salutation;
		this.s = another.s;	
		this.l = another.l;
		this.p = another.p;
		this.o = another.o;		
		this.subtotal_CHF = another.subtotal_CHF;
		this.vat25_CHF = another.vat25_CHF;
		this.vat80_CHF = another.vat80_CHF;			
		this.shipping_CHF = another.shipping_CHF;
		this.shipping_type = another.shipping_type;
	}
	
	boolean isAuthor(String n) {
		return n.equals(name);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		if (name.length()>7)
			return name.toLowerCase().trim().substring(0, 7);
		else
			return name.toLowerCase().trim();
	}
	
	public void setCompany(String company) {
		this.company = company;
	}
	
	public String getCompany() {
		return company;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmailCC(String email_cc) {
		this.email_cc = email_cc;
	}
	
	public String getEmailCC() {
		return email_cc;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}
	
	public String getSalutation() {
		return salutation;
	}

	public void setS(String s) {
		this.s = s;
	}
	
	public String getS() {
		return this.s;
	}
	
	public void setL(String l) {
		this.l = l;
	}
	
	public String getL() {
		return this.l;
	}
	
	public void setP(String p) {
		this.p = p;
	}
	
	public String getP() {
		return this.p;
	}

	public void setO(String o) {
		this.o = o;
	}
	
	public String getO() {
		return this.o;
	}

	public void setCosts(float subtotal, float vat25, float vat80, float shipping, String type) {
		subtotal_CHF = subtotal;
		vat25_CHF = vat25;
		vat80_CHF = vat80;
		shipping_CHF = shipping;
		shipping_type = type;
	}
	
	public float getSubtotal() {
		return subtotal_CHF;
	}
	
	public float getAdditionalDiscount() {
		return additional_discount;
	}
	
	public void setAdditionalDiscount(float add_discount) {
		additional_discount = add_discount;
	}
	
	public float getShippingCosts() {
		return shipping_CHF;
	}	
	
	public String getShippingType() {
		return shipping_type;
	}
	
	public float getVat25() {
		return vat25_CHF;
	}
	
	public float getVat80() {
		return vat80_CHF;
	}
}
