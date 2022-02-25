package org.riskfirst.tweetprint.image.kite9;

import static org.kite9.diagram.dom.ns.Kite9Namespaces.ADL_NAMESPACE;
import static org.kite9.diagram.dom.ns.Kite9Namespaces.XSL_TEMPLATE_NAMESPACE;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.constants.XMLConstants;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.kite9.diagram.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetV2.MediaEntityV2;
import io.github.redouane59.twitter.dto.tweet.TweetV2.UrlEntityV2;
import io.github.redouane59.twitter.dto.tweet.entities.MediaEntity;
import io.github.redouane59.twitter.dto.tweet.entities.UrlEntity;

/**
 * Some examples:  1496069070281752583  - embedded image
 * 1496069070281752583 - embedded site
 * 1494209900632854528 - emojis + small media
 * 
 * @author rob@kite9.com
 *
 */
@Controller
public class TweetToPNGServerSide {
	
	private final String twemojiBase = "https://twemoji.maxcdn.com/v/13.1.0/svg/";
	private final String twemojiSuffix = ".svg";
	
	private final TwitterClient tc;

	public TweetToPNGServerSide(TwitterClient tc) {
		super();
		this.tc = tc;
	}

	protected Document convertTweetsToAdl(List<Tweet> tweets) throws Exception { 
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document d = db.newDocument();
		Element diagram = d.createElementNS(ADL_NAMESPACE, "diagram");
		diagram.setAttributeNS(XSL_TEMPLATE_NAMESPACE, "xslt:template", "/public/templates/tweet/tweet-template.xsl");
		diagram.setAttributeNS(Kite9Namespaces.SVG_NAMESPACE, "svg:version", "1.0");
		
		IntStream.range(0, tweets.size())
			.mapToObj(i -> convertTweet(tweets.get(i), i == tweets.size() - 1, d))
			.forEach(t -> diagram.appendChild(t));
		
		d.appendChild(diagram);
		
		return d;
	}
	
	private Element convertTweet(Tweet status, boolean isLast, Document d) {
		Element tweet = d.createElementNS(ADL_NAMESPACE, "tweet");
		if (isLast) {
			tweet.setAttribute("class", "big");
		}
		
		tweet.setAttribute("href", status.getUser().getProfileImageUrl());
		convertEmojis(tweet, "displayName", status.getUser().getDisplayedName());
		convertEmojis(tweet, "screenName", status.getUser().getName());
		tweet.setAttribute("date", convertDateShort(status.getCreatedAt()));
		tweet.setAttribute("longDate", convertDateLong(status.getCreatedAt()));
		tweet.setAttribute("likes", convertSocialCount(status.getLikeCount()));
		tweet.setAttribute("comments", convertSocialCount(status.getReplyCount()));
		tweet.setAttribute("retweets", convertSocialCount(status.getRetweetCount()));
		tweet.setAttribute("quoteTweets", convertSocialCount(status.getQuoteCount()));
		tweet.setAttribute("source", removeMarkup(status.getSource()));
		tweet.setAttribute("reply", ""+(status.getInReplyToStatusId() != null));
		Element textarea = convertEmojis(tweet, "textarea", status.getText());
		
		addMediaEntities(status, d, tweet, textarea);
		addEntities(status, d, tweet, textarea);
		
		return tweet;
	}
	
	private String justHostName(String url) throws URISyntaxException {
		return new URI(url).getHost();
	}

	private void addMediaEntities(Tweet status, Document d, Element tweet, Element textarea) {
		if (status.getMedia() != null) {
			for (MediaEntity me : status.getMedia()) {
				MediaEntityV2 mev2 = (MediaEntityV2) me;
				
				Element media = d.createElementNS(ADL_NAMESPACE, "media");
				media.setAttribute("width", ""+mev2.getWidth());
				media.setAttribute("height", ""+mev2.getHeight());
				media.setAttribute("href", me.getMediaUrl());
				tweet.appendChild(media);
				textarea.setTextContent(textarea.getTextContent().replace(mev2.getKey(), ""));
			}
		}
	}
	
	private void addEntities(Tweet status, Document d, Element tweet, Element textarea) {
		if (status.getEntities() != null) {
			for (UrlEntity me : status.getEntities().getUrls()) {
				UrlEntityV2 mev2 = (UrlEntityV2) me;
				
				try {
					Element media = d.createElementNS(ADL_NAMESPACE, "media");
					media.setAttribute("site", justHostName(mev2.getUnwoundedUrl()));
					media.setAttribute("description", mev2.getDescription());
					media.setAttribute("title", mev2.getTitle());
					loadPreviewData(mev2.getUnwoundedUrl(), media);
					tweet.appendChild(media);
				} catch (Exception e) {
					// give up silently if we can't get the media
					e.printStackTrace();
				}
				textarea.setTextContent(textarea.getTextContent().replace(mev2.getUrl(), ""));
			}
		}
	}
	
	private void loadPreviewData(String url, Element media) throws MalformedURLException, IOException {
		org.jsoup.nodes.Document d = Jsoup.parse(new URL(url), 2000);
		Elements metaTags = d.getElementsByTag("meta");
		String width="1280";
		String height = "640";
		
		if (!metaTags.select("[property='og:image']").isEmpty()) {
			String imageUrl = metaTags.select("[property='og:image']").first().attr("content");
			Elements foundWidth = metaTags.select("[property='og:image:width']");
			Elements foundHeight = metaTags.select("[property='og:image:height']");
			if ((foundWidth.size() > 0)  && (foundHeight.size() > 0)) {
				width = foundWidth.first().attr("content");
				height = foundHeight.first().attr("content");
			}
			
			media.setAttribute("width", width); //mev2.getWidth());
			media.setAttribute("height", height); //+mev2.getHeight());
			media.setAttribute("href", imageUrl);
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

	private String convertDateLong(LocalDateTime d) {
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern("hh:mm a · MMM dd, yyyy");
		return sdf.format(d);
	}

			
			
	private String convertDateShort(LocalDateTime d) {
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd MMM yyyy");
		return sdf.format(d);
	}
	
	private Element convertEmojis(Element parent, String tagName, String s) {
		Document ownerDocument = parent.getOwnerDocument();
		Element converted = ownerDocument.createElementNS(ADL_NAMESPACE, tagName);
		parent.appendChild(converted);
		
		EmojiToMarkupParser.parseInto(s, converted, uc -> {
			Element image = ownerDocument.createElementNS(Kite9Namespaces.SVG_NAMESPACE, "svg:image");
			String code = uc.getEmoji().getHtmlHexadecimal().substring(3).replace(";","");
			String url = twemojiBase + code + twemojiSuffix;
			image.setAttributeNS(XMLConstants.XLINK_NAMESPACE_URI, "xlink:href", url);
			image.setAttribute("width", "20");
			image.setAttribute("height", "20");
			return image;		
		});

		return converted;
	}
	
	
	

	@GetMapping(produces = MediaType.IMAGE_PNG_VALUE, path="/render-png")
	public void respond(HttpServletResponse response, @RequestParam(required = true, name="tweetId") String tweetId, RequestEntity<?> request) throws Exception {
		Tweet status = tc.getTweet(tweetId);
		
		
		Document adlIn = convertTweetsToAdl(Collections.singletonList(status));
		
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
