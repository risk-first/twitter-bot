package org.riskfirst.tweetprint.image;

import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.kite9.diagram.dom.XMLHelper;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.redouane59.twitter.TwitterClient;

/**
 * Some examples:  1496069070281752583  - embedded image
 * 1496069070281752583 - embedded site
 * 1494209900632854528 - emojis + small media
 * 
 * @author rob@kite9.com
 *
 */
public class TweetToPNGServerSide extends ADLTweetBuilder {
	
	public TweetToPNGServerSide(TwitterClient tc) {
		super(tc);
	}

	@GetMapping(produces = MediaType.IMAGE_PNG_VALUE, path="/print-quality")
	public void printQuality(
			HttpServletResponse response, 
			@RequestParam(required = true, name="payload") String payload, 
			RequestEntity<?> request) throws Exception {
//		Tweet status = tc.getTweet(tweetId);
//		
//		
//		Document adlIn = convertTweetsToAdl(Collections.singletonList(status));
//		
//		System.out.println(new XMLHelper().toXML(adlIn, false));
//		
//		convertToPng(adlIn, response, request.getUrl().toString());
	}


	@GetMapping(produces = MediaType.IMAGE_PNG_VALUE, path="/print-quality")
	public void printQuality(
			HttpServletResponse response, 
			@RequestParam(required = true, name="payload") String payload, 
			RequestEntity<?> request) throws Exception {
//		Tweet status = tc.getTweet(tweetId);
//		
//		
//		Document adlIn = convertTweetsToAdl(Collections.singletonList(status));
//		
//		System.out.println(new XMLHelper().toXML(adlIn, false));
//		
//		convertToPng(adlIn, response, request.getUrl().toString());
	}
}
