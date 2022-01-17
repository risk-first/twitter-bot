package org.riskfirst.twitter;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.riskfirst.Article;
import org.riskfirst.ArticleState;

import twitter4j.StatusUpdate;

public class ArticleTweetSource extends AbstractRiskFirstWikiTweetSource {
	
	public ArticleTweetSource(List<Article> articles, URI baseUri, List<String> tags, String riskFirstWikiDir) {
		super(articles, baseUri, tags, riskFirstWikiDir);
	}
	
	@Override
	public List<StatusUpdate> getAllTweets() {
		List<StatusUpdate> out = new ArrayList<>();
		getArticlesInState(EnumSet.of(ArticleState.TWEETABLE)).stream().forEach(a -> getTweetsFor(a, out));
		return out;
	}

	public void getTweetsFor(Article a, List<StatusUpdate> out) {
		try {
			StatusUpdate su = new StatusUpdate(getArticleUrl(a)+suffix());
			System.out.println("Potential tweet: "+su);
			out.add(su);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
