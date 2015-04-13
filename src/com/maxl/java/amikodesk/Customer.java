package com.maxl.java.amikodesk;

public class Customer implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	String gln_code = "";
	String addr_type = "";	// S: shipping, B: billing, O: Office	
	String type = "";		// arzt, spital, drogerie, ...
	String title = "";
	String first_name = "";	
	String last_name = "";	
	String name1 = "";		// company name 1
	String name2 = "";		// company name 2
	String name3 = "";		// company name 3
	String street = "";		// street / pobox
	String number = "";
	String zip = "";
	String city = "";
	String phone = "";
	String fax = "";
	String email = "";
	String selbst_disp = "";
	String bet_mittel = "";
	
	Customer() {
		// Struct-like class... 'nough said.
	}
}

