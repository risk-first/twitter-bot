package org.riskfirst.tweetshark;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;


/**
 * Configuration of security protocols and the url patterns to match them.
 * 
 * @author robmoffat
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	/**
	 * Allows access to pretty much everything.  /github endpoint is secured by oauth2 at the moment (via github).
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().frameOptions().disable();
		
		http.authorizeRequests()
		.antMatchers("/").permitAll() 			// home page
		.antMatchers("/quiz/**").permitAll()	// javascript dependencies
		.antMatchers("/stylesheet.js").permitAll()	// error page
		.antMatchers("/stylesheet.css").permitAll()	// error page
		.antMatchers("/oauth/token").permitAll()	// oauth login
		.antMatchers("/console/**").permitAll()
		.antMatchers("/public/**").permitAll()		// public pages/stylesheets etc.
		.antMatchers("/command/v1").permitAll()		
		.antMatchers("/actuator/**").permitAll()	// health/metrics
		.antMatchers("/api/renderer").permitAll()
		.antMatchers("/api/renderer.*").permitAll()	// used for book, other remote rendering
		.antMatchers("/**").authenticated()
		.requestMatchers(r -> {
			// this lets us skip oauth2 if the user provides a token that might be compatible with github.
			return (r.getRequestURI().startsWith("/github") && 
					(r.getHeader("Authorization") == null));
		}).authenticated()
		.and()
		.oauth2Login();
		
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
