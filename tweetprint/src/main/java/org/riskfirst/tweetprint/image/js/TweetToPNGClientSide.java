package org.riskfirst.tweetprint.image.js;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.ModelAndView;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Controller
public class TweetToPNGClientSide implements InitializingBean {
	
	private WebClient wc;

	@GetMapping(path="/view/{screenName}/{tweetId}")
	public ModelAndView respond(
			@PathVariable("screenName") String screenName, 
			@PathVariable("tweetId") long tweetId,
			@RequestParam(name = "theme", defaultValue = "light") String theme) throws Exception {
		ModelAndView out = new ModelAndView("tweet-view");
		out.addObject("tweetId", tweetId);
		out.addObject("screenName", screenName);
		out.addObject("theme", theme);
		addRelevantHtml(getTweetHtml(tweetId), out);
		return out;
	}

	private String getTweetHtml(long tweetId) {
		return wc.get()
			.uri(uriBuilder -> 
				uriBuilder
					.queryParam("id", ""+tweetId)
					.build())
			.accept(MediaType.TEXT_HTML)
			.retrieve().bodyToMono(String.class).block();
	}
	
	private void addRelevantHtml(String wholePage, ModelAndView mav) {
		Document doc = Jsoup.parse(wholePage);
		Elements styleTags = doc.getElementsByTag("style");
		
		String styles = styleTags.stream()
			.map(e -> e.text())
			.reduce(String::concat)
			.orElse("");
		
		mav.addObject("tweetStyle", styles);
	
		Element theTweet = doc.getElementById("app");
		mav.addObject("tweetHtml", theTweet.html());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		HttpClient httpClient = HttpClient.create()
			  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
			  .responseTimeout(Duration.ofMillis(5000))
			  .doOnConnected(conn -> 
			    	conn.addHandlerLast(new ReadTimeoutHandler(3000, TimeUnit.MILLISECONDS))
			    		.addHandlerLast(new WriteTimeoutHandler(3000, TimeUnit.MILLISECONDS)));

		wc = WebClient.builder()
			.baseUrl("https://platform.twitter.com/embed/Tweet.html")
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();

	}
	


}
