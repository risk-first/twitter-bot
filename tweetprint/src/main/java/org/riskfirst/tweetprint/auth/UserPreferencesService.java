package org.riskfirst.tweetprint.auth;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.redouane59.twitter.TwitterClient;

public class UserPreferencesService {
	
	private long charityListId;
	
	static class Preferences {
		
		private boolean permissionGiven;
		private boolean donateToCharity;
		
		public Preferences() {
			super();
		}
		
		public Preferences(boolean permissionGiven, boolean donateToCharity) {
			super();
			this.permissionGiven = permissionGiven;
			this.donateToCharity = donateToCharity;
		}
		
		public boolean isPermissionGiven() {
			return permissionGiven;
		}
		
		public void setPermissionGiven(boolean permissionGiven) {
			this.permissionGiven = permissionGiven;
		}
		
		public boolean isDonateToCharity() {
			return donateToCharity;
		}
		
		public void setDonateToCharity(boolean donateToCharity) {
			this.donateToCharity = donateToCharity;
		}
		
	}

	TwitterClient tweetPrintClient;
	
	public UserPreferencesService(TwitterClient tweetPrintClient, long charityListId) {
		this.tweetPrintClient = tweetPrintClient;
		this.charityListId = charityListId;
	}

	public Preferences getPreferences(String twitterUserId) {
		boolean amIFollowing = isTweetPrintFollowing(twitterUserId);
		boolean isDonateToCharity = amIFollowing ? isDonateToCharity(twitterUserId) : false;
		Preferences out = new Preferences(amIFollowing, isDonateToCharity);
		return out;
	}

	private boolean isDonateToCharity(String twitterUserId) {
		Optional<JsonNode> node = tweetPrintClient.getRequestHelperV1()
				.getRequest("https://api.twitter.com/1.1/lists/memberships"+
						"?user_id="+twitterUserId+
						"&filter_to_owned_lists=true", JsonNode.class);

		if (node.isPresent()) {
			// some processing
			return true;
			
		} else {
			return false;
		}
	}

	private boolean isTweetPrintFollowing(String twitterUserId) {
		switch (tweetPrintClient.getRelationType(
			tweetPrintClient.getUserIdFromAccessToken(), twitterUserId)) {
		case FOLLOWER:
		case FRIENDS:
			return true;
		case FOLLOWING:
		case NONE:
		default:
			return false;
		}
	}
	
	public void setPreferences(long twitterUserId, Preferences prefs) {
		
	}
}
