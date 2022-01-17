package org.riskfirst.autotagsbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.V2Search;
import twitter4j.V2Search.MiniStatus;
import twitter4j.V2Search.MiniUser;
import twitter4j.V2Search.Results;

public class ThreadSummarizer {
	
	Twitter t;
	GPT3 gpt3;
	String header;
	String screenName;
	
	public ThreadSummarizer(Twitter t, GPT3 gpt3, String screenName) throws IOException {
		this.t = t;
		this.gpt3 = gpt3;
		this.screenName = screenName;
		this.header = new String(
				ThreadSummarizer.class.getResourceAsStream("template.txt").readAllBytes(), 
				StandardCharsets.UTF_8);
	}

	public void summarize(Status s, Status replyTo) {
		try {
			StringBuilder sb = new StringBuilder();
			List<Status> hist = getHistory(s);
			
			if (alreadyInThread(hist)) {
				return;
			}
			
			Status top = hist.stream().filter(ss -> ss.getInReplyToStatusId() == 0).findFirst().orElse(s);
			
			hist.forEach(t -> sb.append(simplifyText(t.getText())));
			
			Results allReplies = getReplies(top);
			
			if (alreadyReplied(allReplies)) {
				return;
			}
			
			if (allReplies.getData()==null) {
				return;
			}
			
			if (justMyReply(allReplies, replyTo)) {
				return;
			}
			
			if (allReplies.getData().size() > 20) {
				allReplies = getAuthorsReplies(s);
			}
			
			if (allReplies.getData().size() > 20) {
				Collections.reverse(allReplies.getData());
				allReplies.setData(allReplies.getData().subList(0, 20));
			}
			
			allReplies.getData().forEach(t -> sb.append(simplifyText(t.getText())));
			
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
			
			if (tags.length() < 5) {
				return;
			}
			
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
			
			t.updateStatus(out);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	private boolean justMyReply(Results allReplies, Status replyTo) {
		if (allReplies.getData().size()==1) {
			for (MiniStatus ms : allReplies.getData()) {
				if (""+replyTo.getId() == ms.getId()) {
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean alreadyInThread(List<Status> ls) {
		for (Status status : ls) {
			if (screenName.equals(status.getUser().getScreenName())) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean alreadyReplied(Results r) {
		if (r.getIncludes() == null) { 
			return false;
		}
		
		List<MiniUser> users = r.getIncludes().get("users");
		for (MiniUser miniUser : users) {
			if (screenName.equals(miniUser.getUsername())) {
				return true;
			}
		}
		
		return false;
	}

	private String simplifyText(String t) {
		return t.replace("\n", " ").replaceAll("[^\\x20-\\x7e]", "")+"    ";
	}

	public Results getReplies(Status s) throws TwitterException {
		Query q = new Query("conversation_id:"+s.getId());
		V2Search search = new V2Search(t.getConfiguration(), t.getAuthorization());
		return search.search(q);
	}
	
	public Results getAuthorsReplies(Status s) throws TwitterException {
		Query q = new Query("conversation_id:"+s.getId()+" from:"+s.getUser().getScreenName());
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
 	
	
	public void summarize(long l) throws TwitterException {
		summarize(getStatus(l), null);
	}
	
	public void summarize(Status s) throws TwitterException {
		summarize(s, null);
	}
	
	
	public void summarize(long l, Status inReplyTo) throws TwitterException {
		summarize(getStatus(l), inReplyTo);
	}

	private Status getStatus(long l) throws TwitterException {
		return t.lookup(l).get(0);
	}
	
}
