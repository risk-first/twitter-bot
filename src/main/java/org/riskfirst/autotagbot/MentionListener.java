package org.riskfirst.autotagbot;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class MentionListener implements StatusListener {

	@Override
	public void onException(Exception ex) {
		ex.printStackTrace();
	}

	@Override
	public void onStatus(Status status) {
		if (status.getUserMentionEntities() != null) {
			
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
	}

	@Override
	public void onStallWarning(StallWarning warning) {
	}

}
