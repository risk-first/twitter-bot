package org.riskfirst.oldexperiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Query.ResultType;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class ReaderQuery {

	static class Ratio implements Comparable<Ratio> {
		float r;
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
	
	  static Properties props = new Properties();
	
	public static void main(String[] args) throws TwitterException, FileNotFoundException, IOException {
		props.load(new FileReader(new File("tweeter.properties")));
		
		Twitter twitter = TwitterFactory.getSingleton();
		
		List<Ratio> done = new ArrayList<ReaderQuery.Ratio>();
		Query q = new Query("crypto");
		
		for(int i = 0; i<15; i++) {
			QueryResult tl = twitter.search(q);
			for (Status status : tl.getTweets()) {
				
				if (!status.isRetweet()) {
					float den = status.getRetweetCount() + status.getFavoriteCount();
					
					User u = status.getUser();
					float num = u.getFollowersCount();
					
					if ((num > 80) && (num < 700)) {
						float ratio = den / num;
						Ratio r = new Ratio();
						r.r = ratio;
						r.s = status;
						
						done.add(r);
					}
					System.out.println(status.getId());
				}
			}
		
			q = tl.nextQuery();
		
		}
		
		Collections.sort(done);
		
		for (Ratio ratio : done) {
			System.out.println(ratio);
		}
		
	}
}
