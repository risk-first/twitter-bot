package org.riskfirst.tweetprint.rewardful;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Paginated<X> {

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class Pagination {
		
		long previousPage;
		long currentPage;
		long nextPage;
		long count;
		long limit;
		long totalPages;
		long totalCount;
	}
	
	public Pagination pagination;
	
	public List<X> data;
	
}
