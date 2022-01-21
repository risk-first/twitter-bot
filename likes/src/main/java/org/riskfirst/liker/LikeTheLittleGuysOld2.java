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
public class LikeTheLittleGuysOld2 {
	
	public static void main(String[] args) throws TwitterException {
		
		Twitter twitter = TwitterFactory.getSingleton();
		
		Date sixHourAgo = Date.from(Instant.now().minus(6, ChronoUnit.HOURS));
		
		Set<Long> alreadyLiked = new HashSet<>();
				
		for(int i = 0; i<4; i++) {
			Paging p = new Paging(i+1, 200);
			ResponseList<Status> tl = twitter.getHomeTimeline(p);
			
		
			for (Status status : tl) {
				
				if (!status.getCreatedAt().before(sixHourAgo)) {
				
					if ((!status.isFavorited())) {
						int den = status.getRetweetCount() + status.getFavoriteCount();
						User u = status.getUser();
						int follows = u.getFollowersCount();
						
						if (!alreadyLiked.contains(u.getId())) {
							if ((den < 5) && (follows < 1000)) {
								doLike(twitter, alreadyLiked, status, u);
							}
						}
					}
				
				}
				
				if (alreadyLiked.size() > 20) {
					return;
				}
			}
			
			
		}
		
		
	}

	private static void doLike(Twitter twitter, Set<Long> alreadyLiked, Status status, User u) throws TwitterException {
		Status s = twitter.createFavorite(status.getId());
		System.out.println("Liked "+u.getScreenName()+"("+u.getFollowersCount()+") at "+status.getCreatedAt());
		alreadyLiked.add(u.getId());
		
		if (s.getRateLimitStatus() != null) {
			return;
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}
}
