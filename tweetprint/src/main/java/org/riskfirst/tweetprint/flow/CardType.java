package org.riskfirst.tweetprint.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum CardType {

	POST_CARD, GREETINGS_CARD;
	
	@JsonIgnore
	public String getProdigiSku() {
		switch (this) {
		case POST_CARD:
			return "CLASSIC-POST-GLOS-6X4";
		case GREETINGS_CARD:
			return "GLOBAL-GRE-MOH-7X5-DIR";
		default:
			throw new IllegalStateException("No SKU for card type "+this);
		}
	}
	
	public float getWidthHeightRatio() {
		switch (this) {
		case POST_CARD:
			return 6f/4f;
		case GREETINGS_CARD:
			return 7f/5f;
		default:
			throw new IllegalStateException("No ratio for card type "+this);
		}
	}
	
	public int getWidthPx() {
		switch (this) {
		case POST_CARD: 
			return 3588;
		case GREETINGS_CARD:
			return 6118; 		
		default:
			throw new IllegalStateException("No width for card type "+this);
		}
		
	}
	
	public int getHeightPx() {
		switch (this) {
		case POST_CARD: 
			return 1287;
		case GREETINGS_CARD:
			return 2161;
		default:
			throw new IllegalStateException("No height for card type "+this);
		}
	}
	
	public int getPricePence() {
		switch (this) {
		case POST_CARD: 
			return 20;
		case GREETINGS_CARD:
			return 80;
		default:
			throw new IllegalStateException("No price for card type "+this);
		}
	}
}