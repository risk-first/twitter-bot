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

public class TwitterQuizBuilder implements QuizBuilder {

	Twitter t;
	
	public TwitterQuizBuilder() {
		this.t = TwitterFactory.getSingleton();
	}
	
	
	@Override
	public Quiz getQuiz(String screenName) {
		try {
			Query q = new Query();
			q.setCount(50);
			q.setResultType(ResultType.recent);
			q.setQuery("by:"+screenName);
			QueryResult qr = t.search(q);
			List<Question> qs = generateQuestions(qr, screenName);
			Quiz quiz = createQuiz(qr.getTweets().get(0).getUser());
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


	private List<Question> generateQuestions(QueryResult qr, String screenName) {
		Map<Integer, List<Status>> byLikes = qr.getTweets().stream()
			.collect(Collectors.groupingBy(t -> t.getFavoriteCount()));
			
		
		if (byLikes.size() < 20) {
			throw new RuntimeException("Not enough information / tweet activity to create quiz for "+screenName);
		}
		
		List<Integer> items = new ArrayList<Integer>(byLikes.keySet());
		Collections.shuffle(items);
		items = items.subList(0,20);
		
		return items.stream()
				.map(i -> getRandomQuestion(byLikes.get(i)))
				.collect(Collectors.toList());
	}


	private Question getRandomQuestion(List<Status> list) {
		Status randomStatus = list.get(new Random().nextInt(list.size()));
		Question out = new Question();
		out.setTweetHtml(randomStatus.getText());
		out.setLikes(randomStatus.getFavoriteCount());
		return out;
	}

	
}
