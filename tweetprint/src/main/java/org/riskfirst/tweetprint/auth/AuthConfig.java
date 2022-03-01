package org.riskfirst.tweetprint.auth;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth.common.signature.SharedConsumerSecretImpl;
import org.springframework.security.oauth.consumer.BaseProtectedResourceDetails;
import org.springframework.security.oauth.consumer.OAuthConsumerSupport;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.ProtectedResourceDetailsService;
import org.springframework.security.oauth.consumer.client.CoreOAuthConsumerSupport;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerContextFilter;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerProcessingFilter;
import org.springframework.security.oauth.consumer.token.HttpSessionBasedTokenServices;
import org.springframework.security.oauth.consumer.token.OAuthConsumerTokenServices;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@SuppressWarnings("deprecation")
public class AuthConfig extends WebSecurityConfigurerAdapter {

	public static final String RESOURCE_NAME = "requiresLogin";

	private static final class TweetPrintOauthConsumerContextFilter extends OAuthConsumerContextFilter {
		
		private String callbackUri;
		
		public TweetPrintOauthConsumerContextFilter(String callbackUri) {
			super();
			this.callbackUri = callbackUri;
		}

		@Override
		protected String getCallbackURL(HttpServletRequest request) {
			return callbackUri;
		}
	}

	@Value("${twitter.consumerKey}") String consumerKey;
	@Value("${twitter.consumerSecret}") String consumerSecret;
	@Value("${spring.security.oauth.twitter.request-token-uri}") String requestTokenUri;
	@Value("${spring.security.oauth.twitter.user-authorization-uri}") String userAuthorizationUri;
	@Value("${spring.security.oauth.twitter.access-token-uri}") String accessTokenUri;
	@Value("${spring.security.oauth.twitter.callback-uri}") String callbackUri;

	/**
	 * Allows access to pretty much everything.  /github endpoint is secured by oauth2 at the moment (via github).
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().frameOptions().disable();
		
		http.addFilterAfter(this.oauthConsumerContextFilter(), SwitchUserFilter.class);
		http.addFilterAfter(this.oauthConsumerProcessingFilter(), TweetPrintOauthConsumerContextFilter.class);
		
		http.cors().configurationSource(new CorsConfigurationSource() {
			
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				CorsConfiguration config = new CorsConfiguration();
				config.addAllowedOrigin("*");
				config.setAllowedMethods(Arrays.asList("*"));
				return config;
			}
		});
		
	} 
    // IMPORTANT: this must not be a Bean
	TweetPrintOauthConsumerContextFilter oauthConsumerContextFilter() {
		TweetPrintOauthConsumerContextFilter filter = new TweetPrintOauthConsumerContextFilter(callbackUri);
        filter.setConsumerSupport(this.consumerSupport());
        return filter;
    }

    // IMPORTANT: this must not be a Bean
    OAuthConsumerProcessingFilter oauthConsumerProcessingFilter() {
        OAuthConsumerProcessingFilter filter = new OAuthConsumerProcessingFilter();
        filter.setProtectedResourceDetailsService(this.prds());

        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map =
            new LinkedHashMap<>();

        // one entry per oauth:url element in xml
        map.put(
            // 1st arg is equivalent of url:pattern in xml
            // 2nd arg is equivalent of url:httpMethod in xml
            new AntPathRequestMatcher("/user/**", null),
            // arg is equivalent of url:resources in xml
            // IMPORTANT: this must match the ids in prds() and prd() below
            Collections.singletonList(new SecurityConfig(RESOURCE_NAME))
        );

        filter.setObjectDefinitionSource(
            new DefaultFilterInvocationSecurityMetadataSource(map)
        );

        return filter;
    }

    @Bean
    OAuthConsumerSupport consumerSupport() {
        CoreOAuthConsumerSupport consumerSupport = new CoreOAuthConsumerSupport();
        consumerSupport.setProtectedResourceDetailsService(prds());
        return consumerSupport;
    }

    @Bean
    ProtectedResourceDetailsService prds() {
        return (String id) -> {
            switch (id) {
            // this must match the id in prd() below
            case RESOURCE_NAME:
                return prd();
            }
            throw new RuntimeException("Invalid id: " + id);
        };
    }

    ProtectedResourceDetails prd() {
        BaseProtectedResourceDetails details = new BaseProtectedResourceDetails();

        // this must be present and match the id in prds() and prd() above
        details.setId(RESOURCE_NAME);

        details.setConsumerKey(consumerKey);
        details.setSharedSecret(new SharedConsumerSecretImpl(consumerSecret));

        details.setRequestTokenURL(requestTokenUri);
        details.setUserAuthorizationURL(userAuthorizationUri);
        details.setAccessTokenURL(accessTokenUri);
        details.setUse10a(true);

        // any other service-specific settings

        return details;
    }

    @Bean
    OAuthConsumerTokenServices tokenServices() {
    	return new HttpSessionBasedTokenServices();
    }
}

