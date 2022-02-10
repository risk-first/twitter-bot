package twitter4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;

public class V2Search extends TwitterBaseImpl {

	ObjectMapper om = new ObjectMapper();
	
	public V2Search(Configuration conf, Authorization auth) {
		super(conf, auth);
	}

	
	private HttpResponse get(String url, HttpParameter... params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.get(url, params, auth, this);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, params, auth, this);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MiniStatus {
		
		
		String id;
		String text;
		Map<String, Long> publicMetrics;

	
		@JsonProperty("public_metrics")
		public Map<String, Long> getPublicMetrics() {
			return publicMetrics;
		}
		public void setPublicMetrics(Map<String, Long> publicMetrics) {
			this.publicMetrics = publicMetrics;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		

	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MiniUser {
		
		String id;
		String name;
		String username;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		
		
	}
	

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Results  {
		
		List<MiniStatus> data;
		Map<String, Object> meta;
		Map<String, List<MiniUser>> includes;
		
		
		public List<MiniStatus> getData() {
			return data;
		}
		public void setData(List<MiniStatus> data) {
			this.data = data;
		}
		public Map<String, Object> getMeta() {
			return meta;
		}
		public void setMeta(Map<String, Object> meta) {
			this.meta = meta;
		}
		public Map<String, List<MiniUser>> getIncludes() {
			return includes;
		}
		public void setIncludes(Map<String, List<MiniUser>> includes) {
			this.includes = includes;
		}
		
		
	}
	
	public Results search(String endpoint, HttpParameter[] params) throws TwitterException {
		try {
			return deserialize(get("https://api.twitter.com/"+endpoint, params), Results.class);
		} catch (Exception e) {
			throw new TwitterException(e);
		}
	}
	
    public Results search(Query query) throws TwitterException {
        try {
			if (query.nextPage() != null) {
			    return deserialize(get(
			            "https://api.twitter.com/2/tweets/search/recent" + query.nextPage()), Results.class);
			} else {
				List<HttpParameter> params = new ArrayList<>(Arrays.asList(
						new HttpParameter("query", query.getQuery()),
						new HttpParameter("expansions","author_id"),
						new HttpParameter("tweet.fields","public_metrics"),
						new HttpParameter("max_results", 100)
						));
				
				if (query.getMaxId() != -1 ) {
					params.add(new HttpParameter("until_id", query.getMaxId()));
				}
				
			    HttpResponse stream = get("https://api.twitter.com/2/tweets/search/recent", params.toArray(HttpParameter[]::new));
				return deserialize(stream, Results.class);
			}
		} catch (Exception e) {
			throw new TwitterException(e);
		}
    }

    
    public Results search30Days(Query query, String env) throws TwitterException {
        try {
			if (query.nextPage() != null) {
			    return deserialize(get(
			            "https://api.twitter.com/1.1/tweets/search/30day/"+env+".json"), Results.class);
			} else {
				List<HttpParameter> params = new ArrayList<>(Arrays.asList(
						new HttpParameter("query", query.getQuery())
						));
				
				if (query.getMaxId() != -1 ) {
					params.add(new HttpParameter("until_id", query.getMaxId()));
				}
				
			    HttpResponse stream = get("https://api.twitter.com/1.1/tweets/search/30day/"+env+".json", params.toArray(HttpParameter[]::new));
				Object o = stream.asJSONObject();
			    return factory.createQueryResult(stream, query);
			}
		} catch (Exception e) {
			throw new TwitterException(e);
		}
    }

   


    private <X> X deserialize(HttpResponse httpResponse, Class<X> c) throws Exception {
    	String val = httpResponse.asString();
		return om.readValue(val, c);
	}


	private boolean isOk(HttpResponse response) {
        return response != null && response.getStatusCode() < 300;
    }
}
