package org.riskfirst.tweetprint.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.dto.user.UserV2;
import io.github.redouane59.twitter.signature.TwitterCredentials;

@Configuration
@EnableWebSecurity
public class AuthConfig extends WebSecurityConfigurerAdapter {

	/**
	 * Allows access to pretty much everything.  /github endpoint is secured by oauth2 at the moment (via github).
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().frameOptions().disable();
		
		http.authorizeRequests()
		.antMatchers("/").permitAll() 			// home page
		.antMatchers("/webjars/**").permitAll()	// javascript dependencies
		.antMatchers("/stylesheet.js").permitAll()	// error page
		.antMatchers("/stylesheet.css").permitAll()	// error page
		.antMatchers("/oauth2/**").permitAll()	// oauth login
		.antMatchers("/login/**").permitAll()	// oauth login
		.antMatchers("/authorized/**").permitAll()	// oauth login
		.antMatchers("/console/**").permitAll()
		.antMatchers("/public/**").permitAll()		// public pages/stylesheets etc.
		.antMatchers("/command/v1").permitAll()		
		.antMatchers("/actuator/**").permitAll()	// health/metrics
		.antMatchers("/api/renderer").permitAll()
		.antMatchers("/api/renderer.*").permitAll()	// used for book, other remote rendering
		.antMatchers("/**").authenticated()
		.and()
		.oauth2Login(oauth2 -> oauth2.userInfoEndpoint().userService(in -> {
			System.out.println("user in: "+in);
			TwitterCredentials tc = TwitterCredentials.builder().bearerToken(in.getAccessToken().getTokenValue()).build();
			TwitterClient twitter = new TwitterClient(tc);
			Optional<UserV2> me = twitter.getRequestHelperV2().getRequest("https://api.twitter.com/2/users/me", UserV2.class);
			
			List<GrantedAuthority> authorities = in.getClientRegistration()
				.getScopes().stream()
				.map(s -> new SimpleGrantedAuthority("SCOPE_"+s))
				.collect(Collectors.toList());
			
			
			DefaultOAuth2User out = new DefaultOAuth2User(
					authorities, 
					Collections.singletonMap("name", me.get().getId()), "name");
			
			return out;
		}));	
		
		
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
}

