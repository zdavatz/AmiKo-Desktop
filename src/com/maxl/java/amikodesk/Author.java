package com.maxl.java.amikodesk;

public class Author {

	private String name;
	private String email;
	private String email_cc;
	private String address;	
	private String salutation;
	
	public Author() {
		//
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
	
}
