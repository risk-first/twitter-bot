package org.riskfirst.tweetprint.image;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.kite9.diagram.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.riskfirst.tweetprint.builder.OrderDetails;
import org.w3c.dom.Document;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;

public class Kite9ImageBuilder implements ImageBuilder {
	
	private final TwitterClient tc;
	private final ADLTweetBuilder tweetBuilder;
	private final String baseUrl;

	public Kite9ImageBuilder(TwitterClient tc, ADLTweetBuilder tweetBuilder, String baseUrl) {
		super();
		this.tc = tc;
		this.tweetBuilder = tweetBuilder;
		this.baseUrl = baseUrl;
	}
	
	protected byte[] produceImage(Document d, float widthPx, float heightPx, String uri) {
		try {
			Kite9PNGTranscoder svgt = new Kite9PNGTranscoder();	
			svgt.addTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING, true);
			svgt.addTranscodingHint(Kite9SVGTranscoder.KEY_WIDTH, widthPx);
			svgt.addTranscodingHint(Kite9SVGTranscoder.KEY_HEIGHT, heightPx);
			TranscoderInput input = new TranscoderInput(d);
			input.setURI(uri);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream((int) (widthPx * heightPx * 5f));
			TranscoderOutput output= new TranscoderOutput(baos);

			svgt.transcode(input, output);
			return baos.toByteArray();
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't convert to svg", e);
		}
	}
	
	@Override
	public byte[] produceTweetImage(OrderDetails order, float widthPx, float heightPx) throws RuntimeException {
		Tweet status = tc.getTweet(""+order.tweetId);
		Document adlIn = tweetBuilder.convertTweetsToAdl(Collections.singletonList(status));
		return produceImage(adlIn, widthPx, heightPx, baseUrl);
	}

	@Override
	public byte[] produceMessageImage(OrderDetails od, float widthPx, float heightPx) throws RuntimeException {
		Document adlIn = tweetBuilder.convertMessageToAdl(od.message, od.address, od.font, od.cardType);
		return produceImage(adlIn, widthPx, heightPx, baseUrl);
	}

}
