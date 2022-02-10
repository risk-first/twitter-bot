package org.riskfirst.tweetshark.quiz;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class QuizController {

	private QuizBuilder qb;
	
	public QuizController(QuizBuilder qb) {
		this.qb = qb;
	}
	
	@GetMapping(path = "/quiz/{screenname}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Quiz getQuiz(@PathVariable(name = "screenname") String screenName) {
		return qb.getQuiz(screenName);
	}
	
}
