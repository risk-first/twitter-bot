package org.riskfirst.tweetprint.rewardful;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

/**
 * Simple wrapper around Rewardful, so that we don't hammer it too hard.
 *
 * @author rob@kite9.com
 *
 */
public class RewardfulService {
	
	private static final int RESULTS_PER_PAGE = 100;
	private final RewardfulApi api;
	private transient Set<Affiliate> affiliateCache = new HashSet<Affiliate>();

	public RewardfulService(String apiSecret) throws URISyntaxException {
		this("https://api.getrewardful.com", apiSecret);
	}
	
	public RewardfulService(String url, String apiSecret) throws URISyntaxException {
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(apiSecret, "");
		Client jaxRs = ClientBuilder.newBuilder().build();
		jaxRs.register(feature);
		WebTarget wt = jaxRs.target(new URI(url));
		this.api = WebResourceFactory.newResource(RewardfulApi.class, wt);
	}
	
	public Optional<Affiliate> getAffiliate(String twitterScreenName) {
		Optional<Affiliate> out = findAffiliateInCache(twitterScreenName);
		if (out.isPresent()) {
			return out;
		} else {
			updateAffiliateCache();
			return findAffiliateInCache(twitterScreenName);
		}
	}

	private Optional<Affiliate> findAffiliateInCache(String twitterScreenName) {
		return affiliateCache.stream()
			.filter(a -> a.links.stream()
				.filter(l -> l.url.endsWith("/"+twitterScreenName))
				.findAny()
				.isPresent())
			.findFirst();
	}

	public Set<Affiliate> getAffiliates() {
		updateAffiliateCache();
		return affiliateCache;
	}

	private void updateAffiliateCache() {
		int page = (int) Math.floor((double) affiliateCache.size() / (double) RESULTS_PER_PAGE);
		Paginated<Affiliate> returnedData;
		do {
			returnedData = api.getAffiliates(page, RESULTS_PER_PAGE, Arrays.asList("links", "commission_stats"));
			affiliateCache.addAll(returnedData.data);
			if (returnedData.pagination.totalPages >= page) {
				page++;
			}			
		} while (page > -1);
	}
	
	
	public Affiliate create(String firstName,String lastName,String email,String twitterScreenName) {
		Affiliate out = api.create(firstName, lastName, email, twitterScreenName);
		affiliateCache.add(out);
		return out;
	}
	
	public String getAffiliateLink(String twitterScreenName) {
		Optional<Affiliate> aff = getAffiliate(twitterScreenName);
		if (aff.isPresent()) {
			SSOLink link = api.getSSOLink(aff.get().id); 
			return link.sso.url;
		} else {
			throw new UnsupportedOperationException("Can't create affiliate link, no account found for "+twitterScreenName);
		}
	}
	
}
