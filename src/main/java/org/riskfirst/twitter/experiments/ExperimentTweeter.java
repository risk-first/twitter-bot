package org.riskfirst.twitter.experiments;

import twitter4j.TwitterException;

public class ExperimentTweeter {

	public static void main(String[] args) throws Exception {
		try {
			LikeTheLittleGuys.main(args);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		try {
			SearchAndFollow.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
