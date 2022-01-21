package org.riskfirst.liker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * Updated to only like one thing per user.
 * 
 * @author rob@kite9.com
 *
 */
public class LikeTheLittleGuys {
	
	public static class ScoredStatus implements Comparable<ScoredStatus> {
		
		Status s;
		float score;
		
		public ScoredStatus(Status s) {
			this.s = s;
			this.score = calcScore(s);
		}

		private float calcScore(Status status) {
			int interactions = status.getRetweetCount() + status.getFavoriteCount();
			return interactions;
		}

		@Override
		public int compareTo(ScoredStatus o) {
			return Float.compare(score, o.score);
		}

		@Override
		public String toString() {
			return ""+ score+" "+s.getCreatedAt().getTime()+ " (RT: "+s.getRetweetCount()+") (L: "+s.getFavoriteCount()+") ("+s.getUser().getScreenName()+": "+s.getUser().getFollowersCount()+") " + s.getText().replaceAll("\n","");
		}
		
		
		
	}
	
	
	
	public static void main(String[] args) throws TwitterException {
		
		Twitter twitter = TwitterFactory.getSingleton();
		
		Date sixHourAgo = Date.from(Instant.now().minus(3, ChronoUnit.HOURS));
		
		Set<Long> alreadyLiked = new HashSet<>();
		
		Map<User, ScoredStatus> bestTweetByUser = new HashMap<User, ScoredStatus>();
		
		int count = 0;
				
		for(int i = 0; i<4; i++) {
			Paging p = new Paging(i+1, 200);
			ResponseList<Status> tl = twitter.getHomeTimeline(p);
			
			if (tl.getRateLimitStatus().getRemaining() == 0) {
				System.out.println(tl.getRateLimitStatus());
				return;
			}
		
			for (Status status : tl) {
				boolean toLike = filter(status, sixHourAgo);
				ScoredStatus ss = new ScoredStatus(status);

				System.out.println(count+" " + ss);
				count++;
				
				if (toLike) {
					if (bestTweetByUser.containsKey(status.getUser())) {
						ScoredStatus existing = bestTweetByUser.get(status.getUser());
						if (existing.score > ss.score) {
							bestTweetByUser.put(status.getUser(), ss);
						}
					} else {
						bestTweetByUser.put(status.getUser(), ss);
					}
				}
			}
		}
		
		// lowest scoring tweets
		List<ScoredStatus> allTweets = new ArrayList<>(bestTweetByUser.values());
		Collections.sort(allTweets);
		allTweets = allTweets.subList(0, 20);
		
		for (ScoredStatus scoredStatus : allTweets) {
			System.out.println("LIKING: "+scoredStatus);
			likeIt(twitter, alreadyLiked, scoredStatus.s);
		}
			
		
	}


	private static boolean filter(Status status, Date after)
			throws TwitterException {
		if (!status.getCreatedAt().before(after)) {
		
			if ((!status.isFavorited())) {
				int den = status.getRetweetCount() + status.getFavoriteCount();
				User u = status.getUser();
				int follows = u.getFollowersCount();
				
					if ((den < 5) || (follows < 1000)) {
						return true;
					}
				}
		}
	
		return false;
	}

	private static void likeIt(Twitter twitter, Set<Long> alreadyLiked, Status status) throws TwitterException {
		User u = status.getUser();
		if (!alreadyLiked.contains(status.getId())) {
			Status s = twitter.createFavorite(status.getId());
			System.out.println("Liked "+u.getScreenName()+"("+u.getFollowersCount()+") at "+status.getCreatedAt());
			alreadyLiked.add(u.getId());
			
			if (s.getRateLimitStatus() != null) {
				throw new TwitterException("Rate limited - stopping");
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
	}
}
