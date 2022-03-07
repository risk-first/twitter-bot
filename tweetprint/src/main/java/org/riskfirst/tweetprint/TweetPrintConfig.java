package org.riskfirst.tweetprint;

import java.net.URISyntaxException;

import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.riskfirst.tweetprint.auth.UserPreferencesService;
import org.riskfirst.tweetprint.rewardful.RewardfulService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;

@Configuration
public class TweetPrintConfig implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		Kite9Log.Companion.setFactory(logable -> new Kite9LogImpl(logable));
	}

	@Bean
	public UserPreferencesService userPreferencesService(
			TwitterClient tc,
			@Value("${tweet-print.charity-list:0}") long charityListId) {
		return new UserPreferencesService(tc, charityListId);
	}
	
	@Bean
	public RewardfulService rewardfulService(
			@Value("${rewardful.api-secret}") String secretKey) 
			throws URISyntaxException {
		return new RewardfulService(secretKey);
	}
	
	@Bean
	public TwitterClient tweetPrintClient(
		@Value("${twitter.consumerKey}") String consumerKey,
		@Value("${twitter.consumerSecret}") String consumerSecret,
		@Value("${twitter.accessToken}") String accessToken,
		@Value("${twitter.accessTokenSecret}") String accessTokenSecret) {
	
		return new TwitterClient(
			TwitterCredentials.builder()
				.accessToken(accessToken)
				.accessTokenSecret(accessTokenSecret)
				.apiKey(consumerKey)
				.apiSecretKey(consumerSecret).build());
	}
	
}
