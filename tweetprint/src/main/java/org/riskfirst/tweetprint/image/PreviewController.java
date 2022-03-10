package org.riskfirst.tweetprint.image;

import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.kite9.diagram.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.XMLHelper;
import org.riskfirst.tweetprint.builder.Arrangement;
import org.riskfirst.tweetprint.builder.CardType;
import org.riskfirst.tweetprint.builder.Style;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;

/**
 * Some examples:  1496069070281752583  - embedded image
 * 1496069070281752583 - embedded site
 * 1494209900632854528 - emojis + small media
 * 
 * @author rob@kite9.com
 *
 */
@Controller
public class PreviewController {
	
	private final TwitterClient tc;
	private ADLTweetBuilder tweetBuilder = new ADLTweetBuilder();

	public PreviewController(TwitterClient tc) {
		super();
		this.tc = tc;
	}
	
	@GetMapping(produces = "image/png", path="/image-preview.png")
	public void preview(
			HttpServletResponse response, 
			@RequestParam(required = true, name="tweetId") String tweetId, 
			@RequestParam(defaultValue = "POST_CARD", name="type") CardType type,
			@RequestParam(defaultValue = "BIG", name="style") Style style, 
			@RequestParam(defaultValue = "PORTRAIT", name="arrangement") Arrangement arrangement,
			@RequestParam(defaultValue = "false", name="includeReplied") boolean includeReplied,
			RequestEntity<?> request) throws Exception {
	
		Tweet status = tc.getTweet(tweetId);
		
		convertTweet(response, type, style, arrangement, request, status);
	}

	private void convertTweet(HttpServletResponse response, CardType type, Style style, Arrangement arrangement,
			RequestEntity<?> request, Tweet status) throws Exception {
		float width, height;
		if (arrangement == Arrangement.PORTRAIT) {
			width = 600;
			height = width * type.ratio;
		} else {
			width = 600;
			height = width / type.ratio;
		}
		
		Document adlIn = tweetBuilder.convertTweetsToAdl(Collections.singletonList(status));
		
		System.out.println(new XMLHelper().toXML(adlIn, false));
		
		convertToPng(adlIn, response, request.getUrl().toString(), style, width, height);
	}

	private void convertToPng(Document adlIn, HttpServletResponse response, String uri, Style style, float widthPx, float heightPx) {
		try {
			Kite9PNGTranscoder svgt = new Kite9PNGTranscoder();	
			svgt.addTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING, true);
			svgt.addTranscodingHint(Kite9SVGTranscoder.KEY_WIDTH, widthPx);
			svgt.addTranscodingHint(Kite9SVGTranscoder.KEY_HEIGHT, heightPx);
			svgt.getVariables().put("style", style.name().toLowerCase());
			TranscoderInput input = new TranscoderInput(adlIn);
			input.setURI(uri);
			TranscoderOutput pngOutput = new TranscoderOutput(response.getOutputStream());
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/png");
			svgt.transcode(input, pngOutput);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't convert to svg", e);
		}
	}

}
