package org.riskfirst.tweetprint.builder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.PUBLIC_ONLY)
public class OrderDetails {
	
	public long tweetId;
	public CardType cardType = CardType.POST_CARD;
	public Style style = Style.BIG;
	public Arrangement arrangement = Arrangement.PORTRAIT;
	public boolean responseTweet = true;
	public String message = "Dear Fred,\n\nHope you are well.\n\nWeather awful.\n\nWish you were here.\n\nLove Jim";
	public Font font = Font.SANS;
	
	public OrderDetails(long tweetId, CardType cardType, Style style, Arrangement arrangement, boolean responseTweet,
			String message, Font font) {
		super();
		this.tweetId = tweetId;
		this.cardType = cardType;
		this.style = style;
		this.arrangement = arrangement;
		this.responseTweet = responseTweet;
		this.message = message;
		this.font = font;
	}

	public OrderDetails() {
		super();
	}
	
}
