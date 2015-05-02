package com.maxl.java.amikodesk;

public class Product implements Comparable<Product> {
	public String title = "";
	public String group_title;
	public String author = "";
	public String regnrs = "";

	@Override
	public int compareTo(Product p) {
		return new AlphanumComp().compare(this.title, p.title);
	}
}