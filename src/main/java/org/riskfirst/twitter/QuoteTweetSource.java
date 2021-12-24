package org.riskfirst.twitter;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.riskfirst.Article;
import org.riskfirst.ArticleState;
import org.riskfirst.Link;

import twitter4j.StatusUpdate;

/**
 * Now uses quotes held in images.
 */
public class QuoteTweetSource extends AbstractRiskFirstWikiTweetSource {
		
	public QuoteTweetSource(List<Article> articles, URI baseUri, List<String> tags, String riskFirstBaseDir) {
		super(articles, baseUri, tags, riskFirstBaseDir);
	}
	
	@Override
	public List<StatusUpdate> getAllTweets() {
		return getArticlesInState(EnumSet.of(ArticleState.TWEETABLE)).stream()
			.flatMap(a -> a.getQuotes().stream())
			.filter(l -> !l.isExternal())
			.filter(l -> l.isImage())
			.map(l -> convertToStatusUpdate(l))
			.filter(l -> l != null)
			.collect(Collectors.toList());
	}
	
	private StatusUpdate convertToStatusUpdate(Link l) {
		try {
			String articleUrl = getArticleUrl(l.getArticle());
			StatusUpdate out = new StatusUpdate("From "+articleUrl+" "+suffix());
			out.setMedia(getImageFile(riskFirstWikiDir, articleUrl, l.getUrl()));
			return out;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
	
}
