package org.riskfirst.autotagbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.V2Search;
import twitter4j.V2Search.Results;

public class ThreadSummarizer {
	
	Twitter t;
	GPT3 gpt3;
	String header;
	
	
	public ThreadSummarizer(Twitter t, GPT3 gpt3) throws IOException {
		this.t = t;
		this.gpt3 = gpt3;
		this.header = new String(
				ThreadSummarizer.class.getResourceAsStream("template.txt").readAllBytes(), 
				StandardCharsets.UTF_8);
	}

	public StatusUpdate summarize(Status s, Status replyTo) throws TwitterException {
		StringBuilder sb = new StringBuilder();
		List<Status> hist = getHistory(s);
		Status top = hist.stream().filter(ss -> ss.getInReplyToStatusId() == 0).findFirst().orElse(s);
		
		hist.forEach(t -> sb.append(simplifyText(t.getText())));
		
		getReplies(top).getData().forEach(t -> sb.append(simplifyText(t.getText())));
		
		String gptRequest = header + "### INPUT:\n" + sb.toString()+"\n\n### OUTPUT:\n";
		String gptResponse = gpt3.call(gptRequest);
		
		System.out.println("Request: "+sb.toString());
		System.out.println("Response: "+gptResponse);
		String tags = Arrays.stream(gptResponse.split(","))
			.map(r -> r.trim())
			.filter(r -> r.length() > 0)
			.map(r -> "#"+r.replace(" ", "")+" ")
			.limit(3)
			.reduce(String::concat).orElse("?");
		
		String tweetUrl = "https://twitter.com/"+s.getUser().getScreenName()+"/status/"+s.getId();
		
		String statusText = "In 3 tags: "+tags+"\n\n"+tweetUrl;
		StatusUpdate out = new StatusUpdate(statusText);
		if (replyTo != null ) {
			String prefix = "@" + replyTo.getUser().getScreenName() + "\n";
			out = new StatusUpdate(prefix + statusText);
			out.setInReplyToStatusId(replyTo.getId());
		} else {
			out = new StatusUpdate(statusText);
		}
		return out;
	}
	
	private String simplifyText(String t) {
		return t.replace("\n", "");
	}

	public Results getReplies(Status s) throws TwitterException {
		Query q = new Query("conversation_id:"+s.getId());
		V2Search search = new V2Search(t.getConfiguration(), t.getAuthorization());
		return search.search(q);
	}
	
	public List<Status> getHistory(Status s) throws TwitterException {
		List<Status> history;
		
		if (s.getInReplyToStatusId() > 0) {
			history = getHistory(getStatus(s.getInReplyToStatusId()));
		} else {
			history = new ArrayList<Status>();
		}
		
		history.add(s);
		
		return history;
	}
 	
	
	public StatusUpdate summarize(long l) throws TwitterException {
		return summarize(getStatus(l), null);
	}

	private Status getStatus(long l) throws TwitterException {
		return t.lookup(l).get(0);
	}
	
}
