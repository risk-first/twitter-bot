package org.riskfirst.hackernews;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.jsoup.Jsoup;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class HackerNews {

	
	public static final int TWEETS_PER_STORY = 2;
	public static final int MAX_STORIES = 20;
	
	public static void main(String[] args) {
		
		// load up the top 20 hacker news articles
		Twitter twitter = TwitterFactory.getSingleton();

		withTop20Urls(s -> searchForStory(s, twitter));
		getStory(234324);
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
				q.setCount(Math.min(comments.size(), MAX_STORIES));
				
				// todo - add the last hour/whatever
				QueryResult qr = t.search(q);
						
				for (int i=0; i<qr.getTweets().size(); i++) {
					Status tw = qr.getTweets().get(i);
					Story comment = comments.get(i);
					if (!replyWithSummary(s, comment, tw, t)) { 
						System.out.println("Rate limited?");
						return;
					}
				}
			
			} else {
				System.out.println("Already done: "+url);
			}
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	
	private static List<Story> getGoodComments(Story in) {
		return in.getKids().stream()
			.map(l -> getStory(l))
			.limit(TWEETS_PER_STORY)
			.collect(Collectors.toList());
	}
	
	private static String createCommentsUrl(Story s) {
		return "https://news.ycombinator.com/item?id="+s.getId();
	}
	
	
	private static boolean replyWithSummary(Story s, Story reply, Status tw, Twitter t) throws TwitterException {
		try {
			String html = reply.getText();
			
			// twitter can only handle first few characters...
			String prefix = "@" + tw.getUser().getScreenName() + "\n";
			
			String urlForComments = "\n\nContinues on HN: " + createCommentsUrl(s);
			int excerptLength = 280 - urlForComments.length() - 10 - prefix.length();
			
			String text = convertToText(html);
			String excerpt = (text.length() < excerptLength) ? "\""+text+"\"" : "\""+text.substring(0, excerptLength)+"... \"";
			String completeText = prefix + excerpt+urlForComments;
			
			StatusUpdate su = new StatusUpdate(completeText);
			su.setInReplyToStatusId(tw.getId());
			Status done = t.updateStatus(su);
			System.out.println(done.getId()+" "+done.getRateLimitStatus());
			return (done.getRateLimitStatus()== null);
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


	public static void withTop20Urls(Consumer<Story> storyConsumer) {
		WebTarget wt = ClientBuilder.newBuilder().build().target("https://hacker-news.firebaseio.com");
		WebTarget topStories = wt.path("v0/topstories.json");
		GenericType<List<Long>> gt = new GenericType<List<Long>>() {};
		List<Long> ll = topStories.request(MediaType.APPLICATION_JSON).get(gt);
		ll = ll.subList(0, MAX_STORIES);
		
		ll.stream()
			.map(id -> getStory(id))
			.filter(s -> "story".equals(s.getType()))
			.filter(s -> s.getDescendants() > 0)
			.filter(s -> s.getScore() > 10)
			.forEach(storyConsumer);
	}
	
	public static Story getStory(long id) {
		WebTarget wt = ClientBuilder.newClient().target("https://hacker-news.firebaseio.com/v0/item/"+id+".json");
		Story out = wt.request().get(Story.class);
		return out;
	}
}
