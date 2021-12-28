package org.riskfirst.twitter.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.ResponseList;
import twitter4j.Status;
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
	
	
	public static void main(String[] args) throws TwitterException {
		
		Twitter twitter = TwitterFactory.getSingleton();
		
		List<Ratio> done = new ArrayList<RetweetTheBigStuff.Ratio>();
		
		for(int i = 0; i<8; i++) {
			Paging p = new Paging(i+1, 200);
			
			
			ResponseList<Status> tl = twitter.getHomeTimeline(p);
			
		
			for (Status status : tl) {
				
				if ((!status.isRetweet()) && (status.getInReplyToScreenName() == null)) {
					float num = status.getRetweetCount() + status.getFavoriteCount();
					
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
		
		Collections.sort(done);
		
		for (Ratio ratio : done) {
			System.out.println(ratio);
		}
		
	}
}
