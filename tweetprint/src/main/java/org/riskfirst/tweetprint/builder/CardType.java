package org.riskfirst.tweetprint.builder;

import org.riskfirst.tweetprint.image.CompositeFunction;
import org.riskfirst.tweetprint.image.compositing.GreetingsCardCompositeFunction;
import org.riskfirst.tweetprint.image.compositing.PostCardCompositeFunction;

public enum CardType {
	

	POSTCARD("CLASSIC-POST-GLOS-6X4", 6f/4f, "noun-postcard-1130482.svg", "Post Card", 3588 / 2, 1287, PostCardCompositeFunction.INSTANCE), 
	GREETINGSCARD("GLOBAL-GRE-MOH-7X5-DIR", 7f/5f, "noun-greeting-card-4384989.svg", "Greetings Card", 2161, 6118/4, GreetingsCardCompositeFunction.INSTANCE);
	
	public final String sku;
	public final float ratio;
	public final String image;
	public final String text;
	public final float width;
	public final float height;
	public final CompositeFunction cf; 
	
	private CardType(String sku, float ratio, String image, String text, float width, float height, CompositeFunction cf) {
		this.sku = sku;
		this.ratio = ratio;
		this.image = image;
		this.text = text;
		this.width = width;
		this.height = height;
		this.cf = cf;
	}
	
	
	
}