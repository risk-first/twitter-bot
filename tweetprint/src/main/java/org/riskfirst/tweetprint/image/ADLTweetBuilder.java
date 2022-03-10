package org.riskfirst.tweetprint.image;

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
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.constants.XMLConstants;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetV2.MediaEntityV2;
import io.github.redouane59.twitter.dto.tweet.TweetV2.UrlEntityV2;
import io.github.redouane59.twitter.dto.tweet.entities.MediaEntity;
import io.github.redouane59.twitter.dto.tweet.entities.UrlEntity;

public class ADLTweetBuilder {

	private final String twemojiBase = "https://twemoji.maxcdn.com/v/13.1.0/svg/";
	private final String twemojiSuffix = ".svg";

	private static double round(double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    return (double) Math.round(value * scale) / scale;
	}

	public Document convertTweetsToAdl(List<Tweet> tweets) throws Exception { 
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document d = db.newDocument();
		Element diagram = d.createElementNS(ADL_NAMESPACE, "diagram");
		diagram.setAttributeNS(XSL_TEMPLATE_NAMESPACE, "xslt:template", "/public/templates/tweet/tweet-template.xsl");
		diagram.setAttributeNS(Kite9Namespaces.SVG_NAMESPACE, "svg:version", "1.0");
		
		Element frame = d.createElementNS(ADL_NAMESPACE, "frame");
		diagram.appendChild(frame);
		
		IntStream.range(0, tweets.size())
			.mapToObj(i -> convertTweet(tweets.get(i), i == tweets.size() - 1, d))
			.forEach(t -> frame.appendChild(t));
		
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
		Element textarea = convertEmojis(tweet, "wraparea", status.getText());
		
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
				removeText(textarea, mev2.getKey());
				textarea.setTextContent(textarea.getTextContent().replace(mev2.getKey(), ""));
			}
		}
	}

	private void removeText(Node n, String key) {
		if (n instanceof Element) {
			Element e = (Element) n;
			for (int i = 0; i < e.getChildNodes().getLength(); i++) {
				removeText(e.getChildNodes().item(i), key);
			}
		} else if (n instanceof Text) {
			Text t = (Text) n;
			t.setTextContent(t.getTextContent().replace(key, ""));
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
				removeText(textarea, mev2.getUrl());
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

}