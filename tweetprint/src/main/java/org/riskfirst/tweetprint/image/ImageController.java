package org.riskfirst.tweetprint.image;

import java.awt.image.RenderedImage;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.riskfirst.tweetprint.builder.Arrangement;
import org.riskfirst.tweetprint.builder.BuilderController;
import org.riskfirst.tweetprint.builder.CardType;
import org.riskfirst.tweetprint.builder.Font;
import org.riskfirst.tweetprint.builder.OrderDetails;
import org.riskfirst.tweetprint.builder.Style;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Some examples:  1496069070281752583  - embedded image
 * 1496069070281752583 - embedded site
 * 1494209900632854528 - emojis + small media
 * 
 * @author rob@kite9.com
 *
 */
@Controller
public class ImageController {
		
	private final ImageBuilder imageBuilder;

	public ImageController(ImageBuilder ib) {
		super();
		this.imageBuilder = ib;
	}
	
	@GetMapping(produces = "image/png", path="/image-preview/{order}")
	public void previewTweetImage(
			HttpServletResponse response, 
			@PathVariable("order") String base64,
			RequestEntity<?> request) throws Exception {
	
		OrderDetails order = BuilderController.decodeOrder(base64);
		replyWithTweetPng(response, order);
	}
	
	@GetMapping(produces = "image/png", path="/image-preview.png")
	public void previewTweetImage(
			HttpServletResponse response, 
			@RequestParam(required = true, name="tweetId") long tweetId, 
			@RequestParam(defaultValue = "POSTCARD", name="type") CardType type,
			@RequestParam(defaultValue = "BIG", name="style") Style style, 
			@RequestParam(defaultValue = "PORTRAIT", name="arrangement") Arrangement arrangement,
			@RequestParam(defaultValue = "false", name="responseTweet") boolean responseTweet,
			RequestEntity<?> request) throws Exception {
	
		OrderDetails od = new OrderDetails();
		od.tweetId = tweetId;
		od.cardType = type;
		od.style = style;
		od.arrangement = arrangement;
		od.responseTweet = responseTweet;
		
		replyWithTweetPng(response, od);
	}

	private void replyWithTweetPng(HttpServletResponse response, OrderDetails od)
			throws Exception {
		float width, height;
		if (od.arrangement == Arrangement.PORTRAIT) {
			height = 600;
			width = height / od.cardType.ratio;
		} else {
			width = 600;
			height = width / od.cardType.ratio;
		}

		byte[] image = imageBuilder.produceTweetImage(od, width, height);
		replyWithPng(response, image);
	}
	
	@GetMapping(produces = "image/png", path="/message-preview/{order}")
	public void previewMessageImage(
			HttpServletResponse response, 
			@PathVariable("order") String base64,
			RequestEntity<?> request) throws Exception {
	
		OrderDetails order = BuilderController.decodeOrder(base64);
		replyWithMessagePng(response, order);
	}
	
	@GetMapping(produces = "image/png", path="/message-preview.png")
	public void previewMessage(
			HttpServletResponse response, 
			@RequestParam(defaultValue = "Your Message Here...", name="message") String message,
			@RequestParam(defaultValue = OrderDetails.PLACEHOLDER_ADDRESS, name="address") String address,
			@RequestParam(defaultValue = "POSTCARD", name="type") CardType type,
			@RequestParam(defaultValue = "SERIF", name="font") Font font,
			RequestEntity<?> request) throws Exception {
		OrderDetails od = new OrderDetails();
		od.address = address;
		od.message = message;
		od.cardType = type;
		od.font = font;
		replyWithMessagePng(response, od);
	}
	
	private void replyWithMessagePng(HttpServletResponse response, OrderDetails od)
			throws Exception {
		float width = 600;
		float height = width / od.cardType.ratio;

		byte[] image = imageBuilder.produceMessageImage(od, width, height);
		replyWithPng(response, image);
	}
	
	private void replyWithPng(HttpServletResponse response, byte[] out) {
		try {
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/png");
			StreamUtils.copy(out, response.getOutputStream());
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't convert to svg", e);
		}
	}
	
	
	@GetMapping(produces = "image/png", path="/composite/{order}")
	public void compositeImage(
			HttpServletResponse response, 
			@PathVariable("order") String base64,
			RequestEntity<?> request) throws Exception {
		
		OrderDetails order = BuilderController.decodeOrder(base64);
		
		RenderedImage out = order.cardType.cf.performCompositing(order, imageBuilder);
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/png");

		ImageIO.write(out, "PNG", response.getOutputStream());
	}

}
