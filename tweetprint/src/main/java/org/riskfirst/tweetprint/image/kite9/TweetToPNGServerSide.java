package org.riskfirst.tweetprint.image.kite9;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.kite9.diagram.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@Controller
public class TweetToPNGServerSide extends HtmlImageGenerator {

	public static void main(String[] args) throws Exception {
		Twitter twitter = TwitterFactory.getSingleton();
		ResponseList<Status> tweets = twitter.lookup(1490201135327723521l);
		
		System.out.println(new ObjectMapper()
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(tweets.get(0)));
	}
	
	@GetMapping(produces = MediaType.IMAGE_PNG_VALUE, path="/render-png/{tweetId}")
	public void respond(HttpServletResponse response, @PathVariable("tweetId") long tweetId, RequestEntity<?> request) throws Exception {
//		Twitter twitter = TwitterFactory.getSingleton();
//		ResponseList<Status> tweets = twitter.lookup(tweetId);
		
		String adl = StreamUtils.copyToString(
			TweetToPNGServerSide.class.getResourceAsStream("/static/public/templates/tweet/example.adl"),
			StandardCharsets.UTF_8);
		
		convertToPng(adl, response, request.getUrl().toString());
		
	}

	private void convertToPng(String adl, HttpServletResponse response, String uri) {
		try {
			Kite9PNGTranscoder svgt = new Kite9PNGTranscoder();
			//svgt.getTranscodingHints().put(Kite9SVGTranscoder.KEY_ENCAPSULATING, true);
			
			TranscoderInput input = new TranscoderInput(new StringReader(adl));
			input.setURI(uri);
				
			TranscoderOutput pngOutput = new TranscoderOutput(response.getOutputStream());
			
//			TranscoderOutput output = new TranscoderOutput();
//			svgt.transcode(input, output);
			
//			PNGTranscoder png = new PNGTranscoder() {
//	
//				@Override
//				protected UserAgent createUserAgent() {
//					return svgt.getUserAgent();
//				}
//				
//			};
//	
//			png.setTranscodingHints(svgt.getTranscodingHints());
//			Document document = output.getDocument();
//			document.setDocumentURI(uri);
//			TranscoderInput pngInput = new TranscoderInput(document);
//			pngInput.setURI(uri);
		
			svgt.transcode(input, pngOutput);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't convert to png", e);
		}
	}
}
