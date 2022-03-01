package org.riskfirst.tweetprint.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.token.OAuthConsumerTokenServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.UserV2;
import io.github.redouane59.twitter.signature.TwitterCredentials;

@RestController
@SuppressWarnings("deprecation")
public class UserPage {
	
	@Autowired
	private OAuthConsumerTokenServices tokenServices;
	
	@Value("${twitter.consumerKey}") String consumerKey;
	@Value("${twitter.consumerSecret}") String consumerSecret;

    @GetMapping("/user")
    public UserV2 user() {
    	OAuthConsumerToken token = tokenServices.getToken(AuthConfig.RESOURCE_NAME);
    	
		TwitterCredentials creds = TwitterCredentials.builder()
			.accessToken(token.getValue())
			.accessTokenSecret(token.getSecret())
			.apiKey(consumerKey)
			.apiSecretKey(consumerSecret).build();

		TwitterClient twitter = new TwitterClient(creds);
		
//		twitter.oa
		Optional<UserV2> me = twitter.getRequestHelperV1().getRequest("https://api.twitter.com/2/users/me", UserV2.class);
		return me.get();
    }
//		
//		List<GrantedAuthority> authorities = in.getClientRegistration()
//			.getScopes().stream()
//			.map(s -> new SimpleGrantedAuthority("SCOPE_"+s))
//			.collect(Collectors.toList());
//		
//		
//		
//		DefaultOAuth2User out = new DefaultOAuth2User(
//				authorities, 
//				Collections.singletonMap("name", me.get().getId()), "name");
//		
//		return out;
//    	
//    	return Collections.singletonMap("name", principal.getName());
//    }

}
