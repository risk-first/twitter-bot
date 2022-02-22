package org.riskfirst.follows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class UnfollowSmallAccounts {

	public static void main(String[] args) throws TwitterException {
		Twitter twitter = TwitterFactory.getSingleton();
		Set<Long> following = Following.getAllFollowingIds(twitter);
		Set<Long> followers = Following.getAllFollowersIds(twitter);
		
		Set<Long> difference = new HashSet<Long>(following);
		difference.removeAll(followers);
		
		final AtomicInteger counter = new AtomicInteger();
		List<List<Long>> chunks = new ArrayList<>(difference.stream()
				.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 100))
				.values());
				
		
		for (List<Long> chunk : chunks) {
			long[] array = chunk.stream().mapToLong(Long::longValue).toArray();
			ResponseList<User> users = twitter.lookupUsers(array);
			
			for (User u : users) {
				if (u.getFollowersCount() < 500) {
					System.out.println(u.getScreenName()+ "  "+ u.getFollowersCount()+"   "+u.getDescription());
					twitter.destroyFriendship(u.getId());
				}
			}
		}
	}
	
	public static long[] unboxed(final Long[] array) {
	    return Arrays.stream(array)
	                 .filter(Objects::nonNull)
	                 .mapToLong(Long::longValue)
	                 .toArray();
	}
}
