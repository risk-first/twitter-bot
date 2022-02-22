package org.riskfirst.follows;

import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * This is a known to work experiment.  Run this every week.
 */
public class SearchAndFollow {

	static Properties props = new Properties();

	public static void main(String[] args) throws Exception {
		props.load(new FileReader(new File("follows.properties")));

		Twitter twitter = TwitterFactory.getSingleton();
		
		List<String> topics = Arrays.asList(props.getProperty("topics").split(","));
		Collections.shuffle(topics);
		
		Set<Long> alreadyFollowing = Following.getAllFollowingIds(twitter);
		int newFollows = 0;
		
		for(int t=0; t<5;t++){
			String topic = topics.get(t);
			System.out.println("Checking topic: "+topic);
			
			for (int i = 1; i < 51; i++) {
				ResponseList<User> users = twitter.searchUsers(topic, i);

				Date twoDaysAgo = Date.from(Instant.now().minus(2, ChronoUnit.DAYS));

				for (User user : users) {
					if ((user.getFollowersCount() < 450) && (!alreadyFollowing.contains(user.getId()))) {
						Status s = user.getStatus();

						if ((s != null) && (s.getCreatedAt().after(twoDaysAgo))) {
							System.out.println("Added: " + user.getScreenName()+" for topic "+topic);
							twitter.createFriendship(user.getScreenName());
							newFollows++;
							
							if (newFollows > 20) {
								return;
							}
						}

					}
				}
			}
		}
	}

}
