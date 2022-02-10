package org.riskfirst.tweetshark.quiz;

public class Question {

	String tweetHtml;
	
	long likes;
	
	public Question() {
		super();
	}

	public Question(String tweetHtml, long likes) {
		super();
		this.tweetHtml = tweetHtml;
		this.likes = likes;
	}

	public String getTweetHtml() {
		return tweetHtml;
	}

	public void setTweetHtml(String tweetHtml) {
		this.tweetHtml = tweetHtml;
	}

	public long getLikes() {
		return likes;
	}

	public void setLikes(long likes) {
		this.likes = likes;
	}
	
	
}
