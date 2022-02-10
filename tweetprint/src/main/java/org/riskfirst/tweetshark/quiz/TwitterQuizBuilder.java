package org.riskfirst.tweetshark.quiz;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.V2Search;
import twitter4j.V2Search.MiniStatus;
import twitter4j.V2Search.Results;

public class TwitterQuizBuilder implements QuizBuilder {

	Twitter t;
	V2Search search;
	
	public TwitterQuizBuilder() {
		this.t = TwitterFactory.getSingleton();
		this.search = new V2Search(t.getConfiguration(), t.getAuthorization());
	}
	
	
	@Override
	public Quiz getQuiz(String screenName) {
		try {
			List<MiniStatus> foundSoFar = new ArrayList<MiniStatus>();
			Object qr;
			long maxTweet = -1;
			
			do {
				Query q = new Query();
				q.setCount(100);
				q.setResultType(ResultType.recent);
				q.setQuery("from:"+screenName); //+" exclude:retweets exlude:replies");
				
				q.setMaxId(maxTweet);
				
				qr = search.search30Days(q, "searchtweetsdev");
				
				
//				if (qr.getTweets().size() == 0) {
//					notEnoughTweets(screenName);
//				} else {
//					foundSoFar.addAll(qr.getData());
//					maxTweet = Long.parseLong(qr.getData().get(qr.getData().size()-1).getId())-1;
//				}
				
			} while (bucketTweets(foundSoFar).keySet().size() < 20);
			
			List<Question> qs = generateQuestions(foundSoFar, screenName);
			Quiz quiz = new Quiz(); //createQuiz(qr.getData().get(0).getUser());
			quiz.setQuestions(qs);
			return quiz;
		} catch (TwitterException e) {
			throw new RuntimeException("Coulnd't getQuiz", e);
		}
	}


	private Quiz createQuiz(User user) {
		Quiz out = new Quiz();
		LocalDate date = LocalDate.now();
		out.setDisplayName(user.getName());
		out.setScreenName(user.getScreenName());
		out.setDate(date.format(DateTimeFormatter.ofPattern("d MMM yyyy")));
		out.setAvatarUrl(user.getBiggerProfileImageURLHttps());
		return out;
	}


	private List<Question> generateQuestions(List<MiniStatus> qr, String screenName) {
		Map<Long, List<MiniStatus>> byLikes = bucketTweets(qr);
		List<Long> items = new ArrayList<Long>(byLikes.keySet());
		Collections.shuffle(items);
		items = items.subList(0,20);
		
		return items.stream()
				.map(i -> getRandomQuestion(byLikes.get(i)))
				.collect(Collectors.toList());
	}


	private Map<Long, List<MiniStatus>> bucketTweets(List<MiniStatus> qr) {
		Map<Long, List<MiniStatus>> byLikes = qr.stream()
			.collect(Collectors.groupingBy(t -> t.getPublicMetrics().get("like_count")));
		return byLikes; 
	}


	private void notEnoughTweets(String screenName) {
		throw new RuntimeException("Not enough information / tweet activity to create quiz for "+screenName);
	}


	private Question getRandomQuestion(List<MiniStatus> list) {
		MiniStatus randomStatus = list.get(new Random().nextInt(list.size()));
		Question out = new Question();
		out.setTweetHtml(randomStatus.getText());
		out.setLikes(randomStatus.getPublicMetrics().get("like_count"));
		return out;
	}

	
}
