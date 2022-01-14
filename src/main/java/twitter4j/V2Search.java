package twitter4j;

import java.util.List;
import java.util.Map;

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
	
	public static class MiniStatus {
		
		
		String id;
		String text;

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
	
	
	public static class Results  {
		
		List<MiniStatus> data;
		Map<String, Object> meta;
		
		
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
		
		
	}
	
	
    public Results search(Query query) throws TwitterException {
        try {
			if (query.nextPage() != null) {
			    return deserialize(get(
			            "https://api.twitter.com/2/tweets/search/recent" + query.nextPage()), Results.class);
			} else {
				HttpParameter[] params = new HttpParameter[] { new HttpParameter("query", query.getQuery()) };
			    return deserialize(get(
			            "https://api.twitter.com/2/tweets/search/recent", params), Results.class);
			}
		} catch (Exception e) {
			throw new TwitterException(e);
		}
    }

   


    private <X> X deserialize(HttpResponse httpResponse, Class<X> c) throws Exception {
		return om.readValue(httpResponse.asStream(), c);
	}


	private boolean isOk(HttpResponse response) {
        return response != null && response.getStatusCode() < 300;
    }
}
