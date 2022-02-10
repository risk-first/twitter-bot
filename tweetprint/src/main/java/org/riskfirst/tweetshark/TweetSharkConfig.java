package org.riskfirst.tweetshark;

import org.riskfirst.tweetshark.quiz.QuizBuilder;
import org.riskfirst.tweetshark.quiz.TwitterQuizBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class TweetSharkConfig {

	@Bean
	public QuizBuilder quizBuilder() {
		return new TwitterQuizBuilder();
	}


}
