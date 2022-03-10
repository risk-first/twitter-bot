package org.riskfirst.tweetprint.builder;

public enum Arrangement {

	LANDSCAPE("noun-landscape-3271398.svg", "Landscape"), 
	PORTRAIT("noun-portrait-3271395.svg", "Portrait");
	
	public final String image;
	public final String text;
	
	private Arrangement(String image, String text) {
		this.image = image;
		this.text = text;
	}

	
}
