package org.riskfirst.tweetprint.builder;

public enum Style {

	BIG("Big Tweet"), 
	REGULAR("Regular Tweet"), 
	MINIMAL("Minimal   (No likes)"), 
	MEDIA("Media Only");
	
	public final String text;

	private Style(String text) {
		this.text = text;
	}

	
}
