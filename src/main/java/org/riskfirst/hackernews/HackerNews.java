package org.riskfirst.hackernews;

import java.util.List;
import java.util.function.Consumer;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class HackerNews {

	
	public static void main(String[] args) {
		
		// load up the top 20 hacker news articles
		Twitter twitter = TwitterFactory.getSingleton();

		withTop20Urls(s -> searchForStory(s, twitter));
		getStory(234324);
	}
	
	
	public static void searchForStory(Story s, Twitter t) {
		try {
			String url = s.getUrl();
			System.out.println("Found story: "+url+"  with "+s.getScore()+" points ");
			Query q = new Query("\""+url+"\" +exclude:retweets");
			
			// reply to at most 8 posts of this story.
			q.setCount(8);
			
			// todo - add the last hour/whatever
			QueryResult qr = t.search(q);
						
			for (Status tw: qr.getTweets()) {
				if (!hasReplied(tw, t)) { 
					replyWithSummary(s, tw, t);
				}
			}
			
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	private static void replyWithSummary(Story s, Status tw, Twitter t) throws TwitterException {
		// find the first comment on the story
		Long firstDescendant = s.getKids().get(0);
		Story reply = getStory(firstDescendant);
		String html = reply.getText();
		
		// twitter can only handle first few characters...
		String prefix = "@" + tw.getUser().getScreenName() + "\n";
		
		String urlForComments = "\n\nContinues on HN: https://news.ycombinator.com/item?id="+s.getId();
		int excerptLength = 280 - urlForComments.length() - 10 - prefix.length();
		
		String text = convertToText(html);
		String excerpt = (text.length() < excerptLength) ? "\""+text+"\"" : "\""+text.substring(0, excerptLength)+"... \"";
		String completeText = prefix + excerpt+urlForComments;
		
		StatusUpdate su = new StatusUpdate(completeText);
		su.setInReplyToStatusId(tw.getId());
		Status done = t.updateStatus(su);
		System.out.println(done.getId());
	}


	private static String convertToText(String text) {
		return Jsoup.parse(text.replace("<p>", "<p>\n")).wholeText();					 
	}


	private static boolean hasReplied(Status tw, Twitter t) throws TwitterException {
		QueryResult qr = t.search(new Query("conversation_id:"+tw.getId()+" from:risk_first"));
		
		return qr.getTweets().size() > 0;
	}


	public static void withTop20Urls(Consumer<Story> storyConsumer) {
		WebTarget wt = ClientBuilder.newBuilder().build().target("https://hacker-news.firebaseio.com");
		WebTarget topStories = wt.path("v0/topstories.json");
		GenericType<List<Long>> gt = new GenericType<List<Long>>() {};
		List<Long> ll = topStories.request(MediaType.APPLICATION_JSON).get(gt);
		
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
