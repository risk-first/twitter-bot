package org.riskfirst.hackernews;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Finds occasions where people have tweeted hacker news articles, and replies to them
 * with the comments from hacker news.  
 * 
 * Won't reply to the same person within a day.
 * 
 * Now reduced to 14 stories to hopefully avoid the problem of 
 * being banned.  I really have no idea why this is happening.
 * 
 * @author rob@kite9.com
 *
 */
public class HackerNews {

	
	private static final Client BUILD = ClientBuilder.newBuilder()
			.register(JacksonFeature.class)
			.build();
	
	public static int tweetsPerStory = 2;
	public static int maxStories = 12;
	public static int maxReplies = 10;
	public static int replyTimeout = 1 * 24 * 60 * 60;
		
	public static Map<String, Long> lastReplies;
 	
	
	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.load(new FileReader(new File("hncomment.properties")));
		tweetsPerStory = Integer.parseInt((String) props.getOrDefault("tweetsPerStory", ""+tweetsPerStory));
		maxStories = Integer.parseInt((String) props.getOrDefault("maxStories", ""+maxStories));
		maxReplies = Integer.parseInt((String) props.getOrDefault("maxReplies", ""+maxReplies));
		replyTimeout = Integer.parseInt((String) props.getOrDefault("replyTimeout", ""+replyTimeout));
		
		// load up the top 20 hacker news articles
		Twitter twitter = TwitterFactory.getSingleton();
		
		loadLastReplies();
		
		withRandomStories(s -> searchForStory(s, twitter));
	}
	
	
	private static void loadLastReplies() throws Exception {
		long oneDayAgo = Instant.now().minus(replyTimeout, ChronoUnit.SECONDS).toEpochMilli();
		
		File rep = new File("replies.json");
		if (rep.exists()) {
			lastReplies = new ObjectMapper().readValue(rep, new TypeReference<Map<String, Long>>() {});
			
			lastReplies = lastReplies.entrySet().stream()
				.filter(e -> e.getValue() > oneDayAgo)
				.collect(Collectors.toMap(e-> e.getKey(), e -> e.getValue()));
			
		} else {
			lastReplies = new HashMap<String, Long>();
		}
		
		System.out.println("Not replying to: ("+lastReplies.size()+") "+lastReplies.keySet());
	}
	
	private static void storeLastReplies() throws Exception {
		File rep = new File("replies.json");
		new ObjectMapper().writeValue(rep, lastReplies);
	}


	public static void searchForStory(Story s, Twitter t) {
		try {
			
			String url = s.getUrl();
			
			if (url == null) 
				return;

			if (!hasBeenDone(s, t)) {
				System.out.println("Found story: "+url+"  with "+s.getScore()+" points ");
				List<Story> comments = getGoodComments(s);
				
				Query q = new Query("\""+url+"\" +exclude:retweets");
				
				// limit replies
				q.setCount(maxReplies);
				
				// todo - add the last hour/whatever
				QueryResult qr = t.search(q);
				sendReplies(s, t, comments, qr.getTweets());
			
				try {
					// wait 15s before sending the next tweet
					Thread.sleep(15000);
				} catch (InterruptedException e) {
				}
			} else {
				System.out.println("Already done: "+url);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void sendReplies(Story s, Twitter t, List<Story> comments, List<Status> tweetsOfStory) throws Exception {
		int c = 0;
				
		for (int i=0; i<tweetsOfStory.size(); i++) {
			Status tw = tweetsOfStory.get(i);
			if (!lastReplies.containsKey(tw.getUser().getScreenName())) {
				Story comment = comments.get(c);
				c++;
				if (!replyWithSummary(s, comment, tw, t)) { 
					System.out.println("Rate limited?");
					return;
				}
				if (c>=comments.size()) {
					System.out.println("Out Of Comments");
					return;
				}
			}
			
		}
	}
	
	
	private static List<Story> getGoodComments(Story in) {
		return in.getKids().stream()
			.distinct()
			.map(l -> getStory(l))
			.limit(tweetsPerStory)
			.collect(Collectors.toList());
	}
	
	private static String createCommentsUrl(Story s) {
		return "https://news.ycombinator.com/item?id="+s.getId();
	}
	
	
	private static boolean replyWithSummary(Story s, Story reply, Status tw, Twitter t) throws Exception {
		try {
			String html = reply.getText();
			
			if (html != null) {
			
				// twitter can only handle first few characters...
				String screenName = tw.getUser().getScreenName();
				String prefix = "@" + screenName + "\n";
				
				String urlForComments = "\n\nContinues on HN: " + createCommentsUrl(s);
				int excerptLength = 280 - urlForComments.length() - 10 - prefix.length();
				
				String text = convertToText(html);
				String excerpt = (text.length() < excerptLength) ? "\""+text+"\"" : "\""+text.substring(0, excerptLength)+"... \"";
				String completeText = prefix + excerpt+urlForComments;
				
				StatusUpdate su = new StatusUpdate(completeText);
				System.out.println("Posting: "+completeText.substring(0, 40));
				
				su.setInReplyToStatusId(tw.getId());
				Status done = t.updateStatus(su);
				System.out.println(done.getId()+" "+done.getRateLimitStatus()+" "+done.getText().replace("\n"," ").substring(0, 20));
				lastReplies.put(screenName, Instant.now().toEpochMilli());
				storeLastReplies();
				return (done.getRateLimitStatus()== null);
			
			} else {
				return false;
			}
		
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}

	}


	private static String convertToText(String text) {
		return Jsoup.parse(text.replace("<p>", "<p>\n")).wholeText();					 
	}


	private static boolean hasBeenDone(Story s, Twitter t) throws TwitterException {
		QueryResult qr = t.search(new Query("\""+createCommentsUrl(s)+"\" (from:HNCommentsBot OR from:risk_first)"));
		
		return qr.getTweets().size() > 0;
	}


	public static void withRandomStories(Consumer<Story> storyConsumer) {
		WebTarget wt = BUILD.target("https://hacker-news.firebaseio.com");
		WebTarget topStories = wt.path("v0/topstories.json");
		GenericType<List<Long>> gt = new GenericType<List<Long>>() {};
		List<Long> ll = topStories.request(MediaType.APPLICATION_JSON).get(gt);
		Collections.shuffle(ll);
		ll = ll.subList(0, maxStories);
		
		ll.stream()
			.map(id -> getStory(id))
			.filter(s -> "story".equals(s.getType()))
			.filter(s -> s.getDescendants() > 0)
			.filter(s -> s.getScore() > 10)
			.forEach(storyConsumer);
	}
	
	public static Story getStory(long id) {
		WebTarget wt = BUILD.target("https://hacker-news.firebaseio.com/v0/item/"+id+".json");
		Story out = wt.request().get(Story.class);
		return out;
	}
}
