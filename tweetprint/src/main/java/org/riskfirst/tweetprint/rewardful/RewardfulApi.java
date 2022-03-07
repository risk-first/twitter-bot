package org.riskfirst.tweetprint.rewardful;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

public interface RewardfulApi {

	@GET
	@Path("/v1/affiliates")
	@Produces(MediaType.APPLICATION_JSON)
	public Paginated<Affiliate> getAffiliates(
		@QueryParam("page") long page,
		@QueryParam("limit") int limit,
		@QueryParam("expand") List<String> expand);
	
	@GET
	@Path("/v1/affiliates/{id}/sso")
	public SSOLink getSSOLink(@PathParam("id") String affiliateId);
	
	
	@POST
	@Path("/v1/affiliates")
	@Produces(MediaType.APPLICATION_JSON)
	public Affiliate create(
			@QueryParam("first_name") String firstName,
			@QueryParam("last_name") String lastName,
			@QueryParam("email") String email,
			@QueryParam("token") String screenName);
	
}
