package org.riskfirst.tweetprint.image.kite9;

import static org.kite9.diagram.dom.ns.Kite9Namespaces.ADL_NAMESPACE;
import static org.kite9.diagram.dom.ns.Kite9Namespaces.XSL_TEMPLATE_NAMESPACE;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.jsoup.Jsoup;
import org.kite9.diagram.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.MediaEntity;
import twitter4j.MediaEntity.Size;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@Controller
public class TweetToPNGServerSide {
	

	public static void main(String[] args) throws Exception {
		Twitter twitter = TwitterFactory.getSingleton();
		ResponseList<Status> tweets = twitter.lookup(1490201135327723521l);
		
		System.out.println(new ObjectMapper()
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(tweets.get(0)));
	}
	
	protected Document convertTweetsToAdl(List<Status> tweets) throws Exception { 
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document d = db.newDocument();
		Element diagram = d.createElementNS(ADL_NAMESPACE, "diagram");
		diagram.setAttributeNS(XSL_TEMPLATE_NAMESPACE, "xslt:template", "/public/templates/tweet/tweet-template.xsl");
		
		IntStream.range(0, tweets.size())
			.mapToObj(i -> convertTweet(tweets.get(i), i == tweets.size() - 1, d))
			.forEach(t -> diagram.appendChild(t));
		
		d.appendChild(diagram);
		
		return d;
	}
	
	private Element convertTweet(Status status, boolean isLast, Document d) {
		Element tweet = d.createElementNS(ADL_NAMESPACE, "tweet");
		if (isLast) {
			tweet.setAttribute("class", "big");
		}
		
		tweet.setAttribute("href", status.getUser().getOriginalProfileImageURLHttps());
		tweet.setAttribute("displayName", convertEmojis(status.getUser().getName()));
		tweet.setAttribute("screenName", convertEmojis(status.getUser().getScreenName()));
		tweet.setAttribute("date", convertDateShort(status.getCreatedAt()));
		tweet.setAttribute("longDate", convertDateLong(status.getCreatedAt()));
		tweet.setAttribute("likes", convertSocialCount(status.getFavoriteCount()));
//		tweet.setAttribute("comments", convertSocialCount(status.getc()));
		tweet.setAttribute("retweets", convertSocialCount(status.getRetweetCount()));
//		tweet.setAttribute("quoteTweets", convertSocialCount(status.getRetweetCount()));
		tweet.setAttribute("source", removeMarkup(status.getSource()));
		tweet.setAttribute("reply", ""+(status.getInReplyToStatusId() != 0));
		
		Element textarea = d.createElementNS(ADL_NAMESPACE, "textarea");
		String text = status.getText();
		textarea.setTextContent(convertEmojis(text));
		tweet.appendChild(textarea);
		
		addMediaEntities(status, d, tweet, textarea);
		
		return tweet;
	}

	private void addMediaEntities(Status status, Document d, Element tweet, Element textarea) {
		for (MediaEntity me : status.getMediaEntities()) {
			Element media = d.createElementNS(ADL_NAMESPACE, "media");
			Map<Integer, Size> sizes = me.getSizes();
			Size large = sizes.get(Size.LARGE);
			media.setAttribute("width", ""+large.getWidth());
			media.setAttribute("height", ""+large.getHeight());
			media.setAttribute("href", me.getMediaURL());
			media.setAttribute("site", "bobby");
			media.setAttribute("title", "sometitle");
			media.setAttribute("description", me.getExtAltText());
			tweet.appendChild(media);
			textarea.setTextContent(textarea.getTextContent().replace(me.getText(), ""));
		}
	}

	private String removeMarkup(String s) {
		return Jsoup.parse(s).text();
	}

	private static double round (double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    return (double) Math.round(value * scale) / scale;
	}
				
	private static String convertSocialCount(int am) {
		DecimalFormat df=new DecimalFormat("#,###.##");
		if (am > 10_0000_000) {
			// 12M
			double f = round(am / 1_000_000f, 0);
			return df.format(f)+"M";
		}
		if (am > 5_0000_000) {
			// 5.2M
			double f = round(am / 1_000_000f, 1);
			return df.format(f)+"M";
		} else if (am > 12_000) {
			// 3,546K, 23K
			double f = round(am / 1_000f, 0);
			return df.format(f)+"K";
		} else {
			return df.format(am);
		}
	}

	private String convertDateLong(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a · MMM dd, yyyy");
		return sdf.format(d);
	}

			
			
	private String convertDateShort(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		return sdf.format(d);
	}

	private String convertEmojis(String s) {
		return s;
	}

	@GetMapping(produces = MediaType.IMAGE_PNG_VALUE, path="/render-png/{tweetId}")
	public void respond(HttpServletResponse response, @PathVariable("tweetId") long tweetId, RequestEntity<?> request) throws Exception {
		Twitter twitter = TwitterFactory.getSingleton();
		ResponseList<Status> tweets = twitter.lookup(tweetId);
		
		Document adlIn = convertTweetsToAdl(tweets);
		
		System.out.println(new XMLHelper().toXML(adlIn, false));
		
		convertToPng(adlIn, response, request.getUrl().toString());
	}

	private void convertToPng(Document adlIn, HttpServletResponse response, String uri) {
		try {
			Kite9PNGTranscoder svgt = new Kite9PNGTranscoder();			
			TranscoderInput input = new TranscoderInput(adlIn);
			input.setURI(uri);
			TranscoderOutput pngOutput = new TranscoderOutput(response.getOutputStream());
			svgt.transcode(input, pngOutput);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't convert to png", e);
		}
	}
}
