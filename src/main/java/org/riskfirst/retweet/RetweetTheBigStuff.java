package org.riskfirst.retweet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class RetweetTheBigStuff {

	static class Ratio implements Comparable<Ratio> {
		float r;
		float num, den;
		
		Status s;
		
		@Override
		public int compareTo(Ratio o) {
			return Float.compare(r, o.r);
		}

		@Override
		public String toString() {
			return "Ratio [r=" + r + ", user=" + s.getUser().getName() + ", t="+s.getText()+" ]";
		}
		
		
	}
	
	
	public static void main(String[] args) throws TwitterException, IOException, URISyntaxException {
		
		Twitter twitter = TwitterFactory.getSingleton();
		
		InputStream is = RetweetTheBigStuff.class.getResourceAsStream("/retweet-templates.txt");
		List<String> templates = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.toList());
		
		List<Ratio> done = new ArrayList<RetweetTheBigStuff.Ratio>();
		
		for(int i = 0; i<4; i++) {
			Paging p = new Paging(i+1, 200);
			
			
			ResponseList<Status> tl = twitter.getHomeTimeline(p);
			
		
			for (Status status : tl) {
				
				if ((!status.isRetweet()) && (status.getInReplyToScreenName() == null)) {
					float num = status.getRetweetCount() + status.getFavoriteCount();
					
					if (!status.isRetweetedByMe()) {
						User u = status.getUser();
						float den = u.getFollowersCount();
						float ratio = num / den;
						if ((num > 8) && (ratio > 0.001)) {
							Ratio r = new Ratio();
							r.r = ratio;
							r.s = status;
							
							done.add(r);
						}
					}
				}
			}
		}
		
		Collections.sort(done);
		Collections.reverse(done);
		
		int toRetweet = Math.min(done.size(), 5);
		
		for (int i = 0; i < toRetweet; i++) {
			Ratio r = done.get(i);
			
			String template = templates.get(i);
			template = template.replace("@", "@"+r.s.getUser().getScreenName());
			
			StatusUpdate tweet = new StatusUpdate(template);
			tweet.setInReplyToStatusId(r.s.getId());
			
			twitter.updateStatus(tweet);
		}
		
		for (Ratio ratio : done) {
			System.out.println(ratio);
		}
		
	}
}
