package org.riskfirst.tweetprint.builder;

public enum CardType {
	

	POST_CARD("CLASSIC-POST-GLOS-6X4", 6f/4f, "noun-postcard-1130482.svg", "Post Card", 3588 / 2, 1287), 
	GREETINGS_CARD("GLOBAL-GRE-MOH-7X5-DIR", 6f/5f, "noun-greeting-card-4384989.svg", "Greetings Card", 6118, 2161);
	
	public final String sku;
	public final float ratio;
	public final String image;
	public final String text;
	public final float width;
	public final float height;
	
	private CardType(String sku, float ratio, String image, String text, float width, float height) {
		this.sku = sku;
		this.ratio = ratio;
		this.image = image;
		this.text = text;
		this.width = width;
		this.height = height;
	}
}