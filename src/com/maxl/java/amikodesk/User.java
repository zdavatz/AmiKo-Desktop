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

public class User {
	private String full_address;
	private String gln_code;
	private String name_1;	// or family name
	private String name_2;	// or first name
	private String street;
	private String post_code;
	private String city;
	private String country;
	private String type;
	private String category;
	private boolean is_human;	// human/corporation
	private boolean dispensation_permit;
	private boolean anaesthesia_permit;

	public String getFullAddress() {
		return full_address;
	}
	
	public void setFullAddress(String full_address) {
		this.full_address = full_address;
	}	
	
	public String getGlnCode() {
		return gln_code;
	}
	
	public void setGlnCode(String gln_code) {
		this.gln_code = gln_code;
	}
	
	public String getName1() {
		return name_1;
	}
	
	public void setName1(String name_1) {
		this.name_1 = name_1;
	}
	
	public String getName2() {
		return name_2;
	}
	
	public void setName2(String name_2) {
		this.name_2 = name_2;
	}
	
	public String getStreet() {
		return street;
	}
	
	public void setStreet(String street) {
		this.street = street;
	}
	
	public String getPostCode() {
		return post_code;
	}
	
	public void setPostCode(String post_code) {
		this.post_code = post_code;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getType() {
		return type;
	}		
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public boolean isHuman() {
		return is_human;
	}
	
	public void setHuman(boolean is_human) {
		this.is_human = is_human;
	}
	
	public boolean getDispensationPermit() {
		return dispensation_permit;
	}
	
	public void setDispensationPermit(boolean dispensation_permit) {
		this.dispensation_permit = dispensation_permit;
	}

	public boolean getAnaesthesiaPermit() {
		return anaesthesia_permit;
	}
	
	public void setAnaesthesiaPermit(boolean anaesthesia_permit) {
		this.anaesthesia_permit = anaesthesia_permit;
	}
}
