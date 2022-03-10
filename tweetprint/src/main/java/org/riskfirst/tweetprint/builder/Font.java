package org.riskfirst.tweetprint.builder;

public enum Font {
	
	SERIF("noto-serif-display/NotoSerifDisplay.css", "Noto Serif Display"),
	SANS("noto-sans/NotoSans.css", "Noto Sans"),
	SCRIPT("dancing-script/DancingScript.css", "Dancing Script");

	
	
	public final String cssLocation;
	public final String text;
	
	
	private Font(String cssLocation, String name) {
		this.cssLocation = cssLocation;
		this.text = name;
	}
	
	
	
}

