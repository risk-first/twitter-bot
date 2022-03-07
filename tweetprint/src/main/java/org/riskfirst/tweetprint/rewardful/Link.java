package org.riskfirst.tweetprint.rewardful;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {
	
	public String id;
	public String url;
	
}
