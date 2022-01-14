package org.riskfirst.autotagbot;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 * Hangs around waiting for mentions.  Then, kicks into action and summarizes the thread in a few 
 * choice hashtags.
 * 
 * Needs to be restarted each hour using cron
 * 
 * @author rob@kite9.com
 *
 */
public class AutoTagsBot {
	
	public static final long ONE_HOUR = 60*60*1000;

	public static void main(String[] args) throws Exception {
		Twitter twitter =  TwitterFactory.getSingleton();
		GPT3 gpt3 = new GPT3(GPT3.getGPTKey());
		ThreadSummarizer ts = new ThreadSummarizer(twitter, gpt3);
		
		twitter.updateStatus(ts.summarize(1478694051738173445l));
//		
//	    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
//		twitterStream.addListener(new MentionListener());
//		FilterQuery fq = new FilterQuery("@AutoTagsBot");
//	    twitterStream.filter(fq);
	}
}