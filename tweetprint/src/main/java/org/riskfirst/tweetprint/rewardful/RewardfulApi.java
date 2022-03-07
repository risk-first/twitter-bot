package org.riskfirst.tweetprint.rewardful;

import javax.ws.rs.FormParam;
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
		@QueryParam("expand") String expand);
	
	@GET
	@Path("/v1/affiliates/{id}/sso")
	public SSOLink getSSOLink(@PathParam("id") String affiliateId);
	
	
	@POST
	@Path("/v1/affiliates")
	@Produces(MediaType.APPLICATION_JSON)
	public Affiliate create(
			@FormParam("first_name") String firstName,
			@FormParam("last_name") String lastName,
			@FormParam("email") String email,
			@FormParam("token") String screenName);
	
}
