package org.riskfirst.tweetprint.builder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.PUBLIC_ONLY)
public class OrderDetails {
	
	public static final String PLACEHOLDER_ADDRESS = "To Someone,\n Their Address\nGoes\nHERE\nABC 123";
	
	public long tweetId;
	public CardType cardType = CardType.POSTCARD;
	public Style style = Style.BIG;
	public Arrangement arrangement = Arrangement.PORTRAIT;
	public boolean responseTweet = true;
	public String message = "Dear Fred,\n\nHope you are well.\n\nWeather awful.\n\nWish you were here.\n\nLove Jim";
	public String address = PLACEHOLDER_ADDRESS; 
	public Font font = Font.SANS;
	
	public OrderDetails(long tweetId, CardType cardType, Style style, Arrangement arrangement, boolean responseTweet,
			String message, String address, Font font) {
		super();
		this.tweetId = tweetId;
		this.cardType = cardType;
		this.style = style;
		this.arrangement = arrangement;
		this.responseTweet = responseTweet;
		this.message = message;
		this.address = address;
		this.font = font;
	}

	public OrderDetails() {
		super();
	}
	
}
