package org.riskfirst.twitter;

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.riskfirst.Article;
import org.riskfirst.ArticleLoader;
import org.riskfirst.twitter.unused.FollowerRetweetSource;
import org.riskfirst.twitter.unused.SavedSearchRetweetSource;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class Tweeter {

    static Properties props = new Properties();
    static String riskFirstWikiDir;

	public static void main(String[] args) throws Exception {
		props.load(new FileReader(new File("tweeter.properties")));
		riskFirstWikiDir = props.getProperty("dir", "../website");
		
		URI baseURI = new URI(props.getProperty("baseURI", "https://riskfirst.org/"));
		
		System.out.println("baseURI: "+baseURI);

		Twitter twitter = TwitterFactory.getSingleton();
		List<Article> allArticles = new ArticleLoader().loadArticles(riskFirstWikiDir);
		List<Article> articles = allArticles.stream().filter(a -> !tweetsArticle(a)).collect(Collectors.toList());
		List<String> tags = Arrays.asList(props.getProperty("tags").split(","));
		
		System.out.println("Tags in play: "+tags);
		
		List<StatusUpdate> potentialTweets = new ArrayList<>();
		List<Long> retweets;
		List<Long> potentialRetweets = new ArrayList<>();
		
		collectTweets(baseURI, articles, potentialTweets, tags);

		RetweetSource followerSource = new FollowerRetweetSource(twitter);
		retweets = followerSource.getRandomTweets(amount("follow", amount("follow", 2)));
		potentialRetweets.addAll(retweets);

		RetweetSource searchSource = new SavedSearchRetweetSource(twitter);
		retweets = searchSource.getRandomTweets(amount("searches", amount("search", 2)));
		potentialRetweets.addAll(retweets);
		
		for (StatusUpdate statusUpdate : potentialTweets) {
			try {
				System.out.println("Tweeting: "+statusUpdate);
				twitter.updateStatus(statusUpdate);
			} catch (Exception e) {
				System.err.println("Couldn't tweet: "+statusUpdate);
				e.printStackTrace();
			}
			Thread.sleep(amount("delay", 1000));
		}	
		
		for (Long l : potentialRetweets) {
			try {
				System.out.println("Retweeting: "+l);
				twitter.retweetStatus(l);
			} catch (Exception e) {
				System.err.println("Couldn't re-tweet: "+l);
				e.printStackTrace();
			}
			Thread.sleep(amount("delay", 1000));
		}	
	}

	private static int amount(String prop, int i) {
		return Integer.parseInt(props.getProperty(prop, ""+i));
	}

	public static void collectTweets(URI baseURI, List<Article> articles, List<StatusUpdate> potentialTweets, List<String> tags) {
		List<StatusUpdate> tweets;
		TweetSource imageTweetSource = new ImageTweetSource(articles, baseURI, riskFirstWikiDir, tags);
		tweets = imageTweetSource.getRandomTweets(amount("images", 1));
		potentialTweets.addAll(tweets);
		
		TweetSource articleTweetSource = new ArticleTweetSource(articles, baseURI, tags, riskFirstWikiDir);
		tweets = articleTweetSource.getRandomTweets(amount("articles", 1));
		potentialTweets.addAll(tweets);
		
		TweetSource quoteTweetSource = new  QuoteTweetSource(articles, baseURI, tags, riskFirstWikiDir);
		tweets = quoteTweetSource.getRandomTweets(amount("quotes", 1));
		potentialTweets.addAll(tweets);
	}

	public static boolean tweetsArticle(Article a) {
		return a.getFile().getName().contains("Tweets");
	}
}
