package org.riskfirst.twitter.experiments;

import java.util.LinkedHashSet;
import java.util.Set;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class Following {

	
	public static Set<Long> getAllFollowingIds(Twitter t) throws TwitterException {
		Set<Long> out = new LinkedHashSet<>();
		long cursor = -1;
		boolean hasNext = true;
		while (hasNext) {
			IDs in = t.getFriendsIDs(cursor);
			for (Long long1 : in.getIDs()) {
				out.add(long1);
			}
		
			hasNext = in.hasNext();
			cursor = in.getNextCursor();
		}
		
		return out;
	}
}
