package org.riskfirst.twitter;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.riskfirst.Article;
import org.riskfirst.ArticleState;

import twitter4j.StatusUpdate;

public class ArticleTweetSource extends AbstractRiskFirstWikiTweetSource {
	
	public ArticleTweetSource(List<Article> articles, URI baseUri, List<String> tags) {
		super(articles, baseUri, tags);
	}
	
	@Override
	public List<StatusUpdate> getAllTweets() {
		List<StatusUpdate> out = new ArrayList<>();
		getArticlesInState(EnumSet.of(ArticleState.REVIEWED)).stream().forEach(a -> getTweetsFor(a, out));
		return out;
	}

	public void getTweetsFor(Article a, List<StatusUpdate> out) {
		StatusUpdate su = new StatusUpdate(a.getUrl(baseUri.toString())+suffix());
		System.out.println("Potential tweet: "+su);
		out.add(su);
	}
	
	public String deMarkdown(String text, Article a) {
		StringBuilder sb = new StringBuilder();
		Article.processLine(text, 0, 
				link -> sb.append(link.getText()), 
				t ->sb.append(t), a);
		 
		return stripMarkdown(sb.toString())+suffix();
	}
}
