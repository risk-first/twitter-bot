package org.riskfirst.tweetprint.flow;

import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Controller
public class FlowController {

	ObjectMapper om = new ObjectMapper();
	
	
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
	

	@PostMapping("/addressPreview")
	public ModelAndView loadTweet(
			@RequestParam("tweetId") long tweetId,
			@RequestParam("cardType") CardType cardType,
			@RequestParam("style") Style style,
			@RequestParam("arrangement") Arrangement arrangement,
			@RequestParam(required = false, name="response") boolean responseTweet,
			@RequestParam("message") String message,
			@RequestParam("x") long x,
			@RequestParam("y") long y,
			@RequestParam("width") long width,
			@RequestParam("height") long height) throws Exception {
		OrderDetails od = new OrderDetails(tweetId, cardType, style, arrangement, responseTweet, message, x, y, width, height);
		ModelAndView out = new ModelAndView("preview");
		out.addObject("tweetUrl", "http://robs-pro:8080/render-png?tweetId=1496845845404479490");
		String base64 = Base64.getEncoder().encodeToString(om.writeValueAsBytes(od));
		out.addObject("orderDetails", base64);
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
