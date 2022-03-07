package org.riskfirst.tweetprint.auth;

import java.util.Optional;

import org.riskfirst.tweetprint.auth.UserPreferencesService.Preferences;
import org.riskfirst.tweetprint.rewardful.Affiliate;
import org.riskfirst.tweetprint.rewardful.RewardfulApi;
import org.riskfirst.tweetprint.rewardful.RewardfulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.token.OAuthConsumerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.UserV2;
import io.github.redouane59.twitter.signature.TwitterCredentials;

@Controller
@SuppressWarnings("deprecation")
public class UserController {
	
	@Autowired
	private OAuthConsumerTokenServices tokenServices;
	
	@Autowired
	UserPreferencesService prefs;
	
	@Autowired
	RewardfulService rewardful;
	
	@Value("${twitter.consumerKey}") String consumerKey;
	@Value("${twitter.consumerSecret}") String consumerSecret;

    @GetMapping("/user")
    public ModelAndView user() {
    	Optional<UserV2> me = lookupAuthenticatedTwitterUser();
    	Preferences preferences = prefs.getPreferences(me.get().getId());
    	Optional<Affiliate> affiliate = rewardful.getAffiliate(me.get().getName());
    	boolean hasAccount = affiliate.isPresent();
				
		return new ModelAndView("store/account")
			.addObject("user", me.get())
			.addObject("page", "user")
			.addObject("preferences", preferences)
			.addObject("hasAccount", hasAccount)
			.addObject("affiliate", affiliate.orElseGet(() -> null));
    }
    
	protected Optional<UserV2> lookupAuthenticatedTwitterUser() {
		OAuthConsumerToken token = tokenServices.getToken(AuthConfig.RESOURCE_NAME);
    	
		TwitterCredentials creds = TwitterCredentials.builder()
			.accessToken(token.getValue())
			.accessTokenSecret(token.getSecret())
			.apiKey(consumerKey)
			.apiSecretKey(consumerSecret).build();

		TwitterClient twitter = new TwitterClient(creds);
		Optional<UserV2> me = twitter.getRequestHelperV1().getRequest("https://api.twitter.com/2/users/me?expansions=pinned_tweet_id&user.fields=profile_image_url", UserV2.class);
		return me;
	}

}
