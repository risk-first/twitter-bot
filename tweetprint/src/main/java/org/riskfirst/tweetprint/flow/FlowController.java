package org.riskfirst.tweetprint.flow;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class FlowController {

	@GetMapping("/loadTweet")
	public ModelAndView loadTweet(@RequestParam("url") String url) {
		long tweetId = extractTweetId(url);
		ModelAndView out = new ModelAndView("loadTweet");
		out.addObject("tweetId", tweetId);
		
		if (tweetId < 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
		return out;
	}
	

	@GetMapping("/addressPreview")
	public ModelAndView loadTweet(
			@RequestParam("tweetId") long tweetId,
			@RequestParam("cardType") CardType cardType,
			@RequestParam("style") Style style,
			@RequestParam("arrangement") Arrangement arrangement,
			@RequestParam("response") boolean responseTweet,
			@RequestParam("message") String message,
			@RequestParam("x") long x,
			@RequestParam("y") long y,
			@RequestParam("width") long width,
			@RequestParam("height") long height) {
	
		
		ModelAndView out = new ModelAndView("addressPreview");
		return out;
	}
	

	private long extractTweetId(String url) {
		if (url.contains("/status/")) {
			url = url.substring(url.lastIndexOf("/status/")+8);
		}
		
		if (url.contains("?")) {
			url = url.substring(0, url.indexOf("?"));
		}
		
		long tweetId = Long.parseLong(url);
		return tweetId;
	}
	
	
	
	
}
