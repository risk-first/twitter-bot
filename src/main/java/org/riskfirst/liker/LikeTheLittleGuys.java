package org.riskfirst.liker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * Find people with only a few followers, and like their tweets if there hasnt been 
 * that much interaction with them.
 * 
 * Ignore retweets.
 * 
 * @author rob@kite9.com
 *
 */
public class LikeTheLittleGuys {
	
	public static void main(String[] args) throws TwitterException {
		
		Twitter twitter = TwitterFactory.getSingleton();
		
		Date oneHourAgo = Date.from(Instant.now().minus(3, ChronoUnit.HOURS));
		
		Set<Long> alreadyLiked = new HashSet<>();
				
		for(int i = 0; i<4; i++) {
			Paging p = new Paging(i+1, 200);
			ResponseList<Status> tl = twitter.getHomeTimeline(p);
			
		
			for (Status status : tl) {
				
				if (status.getCreatedAt().before(oneHourAgo)) {
					return;
				}
				
				if ((!status.isRetweet()) && (!status.isFavorited())) {
					float den = status.getRetweetCount() + status.getFavoriteCount();
					User u = status.getUser();
					
					if (!alreadyLiked.contains(u.getId())) {
						if (den < 5) {
							twitter.createFavorite(status.getId());
							System.out.println("Liked "+u.getScreenName()+"("+u.getFollowersCount()+") at "+status.getCreatedAt());
							alreadyLiked.add(u.getId());
						}
					}
				}
				
				if (alreadyLiked.size() > 20) {
					return;
				}
			}
			
			
		}
		
		
	}
}
