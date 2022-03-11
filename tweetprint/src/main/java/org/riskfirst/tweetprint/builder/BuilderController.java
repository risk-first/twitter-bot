package org.riskfirst.tweetprint.builder;

import java.io.IOException;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Controller
public class BuilderController {

	
	
	@GetMapping("/loadTweet")
	public ModelAndView loadTweet(@RequestParam("url") String url) {
		long tweetId = extractTweetId(url);
		OrderDetails order = new OrderDetails();
		order.tweetId = tweetId;
		ModelAndView out = new ModelAndView("builder/format");
		out.addObject("order", order);
		
		if (tweetId < 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
		return out;
	}
	
	@GetMapping("/preview/{data}")
	public ModelAndView previewGet(@PathVariable("data") String data) throws Exception {
		OrderDetails order = decodeOrder(data);
		ModelAndView out = new ModelAndView("builder/preview");
		out.addObject("order", order);
		out.addObject("data", data);
		return out;
	}

	private static ObjectMapper ORDER_MAPPER = new ObjectMapper();

	public static OrderDetails decodeOrder(String data) throws IOException, JsonParseException, JsonMappingException {
		byte[] base64 = Base64.getDecoder().decode(data);
		OrderDetails order = ORDER_MAPPER.readValue(base64, OrderDetails.class);
		return order;
	}
	
	public static String encodeOrder(OrderDetails od) throws JsonProcessingException {
		String base64 = Base64.getEncoder().encodeToString(ORDER_MAPPER.writeValueAsBytes(od));
		return base64;
	}


	/**
	 * Handles the post from the format page, and turns it into a preview url
	 */
	@PostMapping("/preview")
	public RedirectView previewPost(
			@RequestParam("tweetId") long tweetId,
			@RequestParam("type") CardType cardType,
			@RequestParam("style") Style style,
			@RequestParam("arrangement") Arrangement arrangement,
			@RequestParam(required = false, name="response") boolean responseTweet,
			@RequestParam("message") String message,
			@RequestParam(defaultValue = OrderDetails.PLACEHOLDER_ADDRESS, name="address") String address,
			@RequestParam("font") Font font) throws Exception {
		OrderDetails od = new OrderDetails(tweetId, cardType, style, arrangement, responseTweet, message, address, font);
		String base64 = encodeOrder(od);
		return new RedirectView("/preview/"+base64);
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
