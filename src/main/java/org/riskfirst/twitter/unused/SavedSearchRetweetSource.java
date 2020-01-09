package org.riskfirst.twitter.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.riskfirst.twitter.AbstractRetweetSource;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SavedSearchRetweetSource extends AbstractRetweetSource {
	
	public SavedSearchRetweetSource(Twitter t) {
		super(t);
	}

	@Override
	public List<Long> getAllTweets() {
		try {
			// first, get saved searches
			ResponseList<SavedSearch> searches = t.getSavedSearches();
			
			List<Long> out = new ArrayList<>();
			
			for (SavedSearch ss : searches) {
				System.out.println("Saved Search: "+ss);
				Query query = new Query(ss.getQuery());
				query.setCount(60);
				QueryResult qr = t.search(query);
				qr.getTweets().stream().map(s -> getId(s)).forEach(id -> out.add(id));
			}
			
			return out;
		} catch (TwitterException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	public long getId(Status s) {
		System.out.println("SS: "+s.getText());
		return s.getId();
	}

}
