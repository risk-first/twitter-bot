package org.riskfirst.tweetprint.flow;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class StaticController {

	@GetMapping({"/", ""})
	public ModelAndView index() {
		return new ModelAndView("index").addObject("page", "index");
	}

	@GetMapping({"/earn", ""})
	public ModelAndView earn() {
		return new ModelAndView("earn").addObject("page", "earn");
	}
	
	@GetMapping({"/eco", ""})
	public ModelAndView eco() {
		return new ModelAndView("eco").addObject("page", "eco");
	}
	

}
