package org.riskfirst.tweetprint.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.PUBLIC_ONLY)
public class OrderDetails {
	
	public long tweetId;
	public CardType cardType;
	public Style style;
	public Arrangement arrangement;
	public boolean responseTweet;
	public String message;
	public long x;
	public long y;
	public long width;
	public long height;
	
	public OrderDetails(long tweetId, CardType cardType, Style style, Arrangement arrangement, boolean responseTweet,
			String message, long x, long y, long width, long height) {
		super();
		this.tweetId = tweetId;
		this.cardType = cardType;
		this.style = style;
		this.arrangement = arrangement;
		this.responseTweet = responseTweet;
		this.message = message;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public OrderDetails() {
		super();
	}
	
}
