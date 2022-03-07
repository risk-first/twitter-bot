package org.riskfirst.tweetprint.rewardful;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Affiliate {
	
	public String id;
	public String email;
	public String firstName;
	public String lastName;
	public long conversions;
	public List<Link> links;
	
	@Override
	public int hashCode() {
		return Objects.hash(email);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Affiliate other = (Affiliate) obj;
		return Objects.equals(email, other.email);
	}
	
	
}
