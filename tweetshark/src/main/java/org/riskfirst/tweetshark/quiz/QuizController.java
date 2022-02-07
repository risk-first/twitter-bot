package org.riskfirst.tweetshark.quiz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

public class QuizController {

	private QuizBuilder qb;
	
	public QuizController(QuizBuilder qb) {
		this.qb = qb;
	}
	
	@GetMapping(path = "/quiz/{screenname}")
	@ResponseBody
	public Quiz getQuiz(@PathVariable(name = "screenname") String screenName) {
		return qb.getQuiz(screenName);
	}
	
}
