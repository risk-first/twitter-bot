package org.riskfirst.tweetprint.auth;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.redouane59.twitter.TwitterClient;

public class UserPreferencesService {
	
	private String charityListId;
	
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
	
	public UserPreferencesService(TwitterClient tweetPrintClient, String charityListId) {
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
		Optional<ObjectNode> node = tweetPrintClient.getRequestHelperV1()
				.getRequest("https://api.twitter.com/1.1/lists/memberships.json"+
						"?user_id="+twitterUserId+
						"&filter_to_owned_lists=true", ObjectNode.class);

		if (node.isPresent()) {
			ObjectNode on = node.get();
			JsonNode data = on.get("lists");
			for (int i = 0; i < data.size(); i++) {
				String id = data.get(i).get("id").asText();
				if (charityListId.equals(id)) {
					return true;
				}
			}
			
		}
		
		return false;
	}

	private boolean isTweetPrintFollowing(String twitterUserId) {
		switch (tweetPrintClient.getRelationType(
			tweetPrintClient.getUserIdFromAccessToken(), twitterUserId)) {
		case FOLLOWING:
		case FRIENDS:
			return true;
		case FOLLOWER:
		case NONE:
		default:
			return false;
		}
	}
	
	public void setPreferences(String twitterUserId, Preferences prefs) {
		if (prefs.isPermissionGiven()) {
			tweetPrintClient.follow(twitterUserId);
		} else {
			tweetPrintClient.unfollow(twitterUserId);
		}
		
		if (prefs.isDonateToCharity()) {
			tweetPrintClient.addListMember(charityListId, twitterUserId);
		} else {
			tweetPrintClient.removeListMember(charityListId, twitterUserId);
		}
 	}
}
