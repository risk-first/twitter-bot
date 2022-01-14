package org.riskfirst.autotagsbot;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

public class MentionListener implements StatusListener {
	
	ThreadSummarizer ts;
	String screenName;
	
	public MentionListener(String screenName, ThreadSummarizer ts) {
		super();
		this.ts = ts;
		this.screenName = screenName;
	}

	@Override
	public void onException(Exception ex) {
		ex.printStackTrace();
	}

	@Override
	public void onStatus(Status status) {
		if (hasMention(status)) {
			if (status.getInReplyToStatusId() > 0) {
				try {
					ts.summarize(status.getInReplyToStatusId(), status);
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			} else if (status.getQuotedStatus() != null) {
				ts.summarize(status.getQuotedStatus(), status);
			}
			
		}
	}

	private boolean hasMention(Status status) {
		if (status.getUserMentionEntities() != null) {
			for (UserMentionEntity ume : status.getUserMentionEntities()) {
				if (screenName.equals(ume.getScreenName())) {
					return true;
				}
			}
		}
		
		return false;
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
