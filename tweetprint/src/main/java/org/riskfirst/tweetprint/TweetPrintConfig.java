package org.riskfirst.tweetprint;

import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
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
	public TwitterClient twitterClient(
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
