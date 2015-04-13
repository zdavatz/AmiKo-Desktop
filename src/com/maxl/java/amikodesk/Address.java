package com.maxl.java.amikodesk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Address implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	public String idealeId = "";
	public String xprisId = "";
	public String title = "";
	public String fname = "";
	public String lname = "";
	public String name1 = "";
	public String name2 = "";
	public String name3 = "";
	public String street = "";
	public String zip = "";
	public String city = "";
	public String email = "";
	public String phone = "";
	public boolean isHuman = true;
	
	public Address() {
		// Struct
	}
	
	// Ideale customer ID and Xpris customer ID are nil
	public String getAsLongString(String time_stamp, String addr_type, String gln_code) {
		String addr_str = time_stamp + "|" + addr_type + "|" + gln_code + "|" 
				+ idealeId + "|" + xprisId + "|"
				+ title + "|" + fname + "|" + lname + "|"
				+ name1 + "|" + name2 + "|" + name3 + "|" 
				+ street + "|" + zip + "|" + city + "|" 
				+ phone + "||" + email;	// No fax!
		
		return addr_str;
	}
	
	public String getAsClassicString(String addr_type) {
		String addr_str = title + "\n" 
			+ fname + " " + lname + "\n";
		if (!name1.isEmpty())
			addr_str += name1 + ", "; 
		if (!name2.isEmpty())
			addr_str += name2 + ", ";
		if (!name3.isEmpty())
			addr_str += name3;
		if (!name1.isEmpty() || !name2.isEmpty() || !name3.isEmpty())
			addr_str += "\n";    		    		
		if (addr_str.substring(addr_str.length()-3, addr_str.length()-1).equals(", "))
			addr_str = addr_str.substring(0, addr_str.length()-3) + "\n";
		addr_str += street + "\n" 
			+ zip + " " + city + "\n";

    	// Anything smaller than 10 characters cannto be an address!
    	if (addr_str.length()<10) {
    		if (addr_type.equals("S"))
    			addr_str = "Keine Lieferadresse";
    		else if (addr_type.equals("B"))
    			addr_str = "Keine Rechnungsadresse";
    		else if (addr_type.equals("O"))
    			addr_str = "Keine Bestelladresse";    		
    	}
		
		return addr_str;
	}
	
	/**
	 * Always treat de-serialization as a full-blown constructor, by validating
	 * the final state of the de-serialized object.
	 */
	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		// always perform the default de-serialization first
		ois.defaultReadObject();
	}

	/**
	 * This is the default implementation of writeObject. Customise if necessary.
	 */
	private void writeObject(ObjectOutputStream oos)
			throws IOException {
		// perform the default serialization for all non-transient, non-static fields
		oos.defaultWriteObject();
	}
}
