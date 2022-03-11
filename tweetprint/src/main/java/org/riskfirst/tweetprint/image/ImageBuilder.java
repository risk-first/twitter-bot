package org.riskfirst.tweetprint.image;

import org.riskfirst.tweetprint.builder.OrderDetails;

public interface ImageBuilder {
	
	public byte[] produceTweetImage(OrderDetails od, float widthPx, float heightPx) throws RuntimeException;
	
	public byte[] produceMessageImage(OrderDetails od, float widthPx, float heightPx) throws RuntimeException;

}
