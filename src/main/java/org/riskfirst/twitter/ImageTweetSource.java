package org.riskfirst.twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.riskfirst.Article;
import org.riskfirst.ArticleState;
import org.riskfirst.Link;

import twitter4j.StatusUpdate;

public class ImageTweetSource extends AbstractRiskFirstWikiTweetSource implements TweetSource {
		
	public ImageTweetSource(List<Article> articles, URI baseUri, String riskFirstWikiDir, List<String> hashtags) {
		super(articles, baseUri, hashtags, riskFirstWikiDir);
	}

	@Override
	public List<StatusUpdate> getAllTweets() {
		return getArticlesInState(EnumSet.of(ArticleState.TWEETABLE)).stream()
			.flatMap(a -> getImagesFromArticle(a).stream())
			.filter(l -> !l.isExternal())
			.filter(l -> l.isImage())
			.filter(l -> !l.getUrl().contains("/state/"))
			.map(l -> convertToStatusUpdate(l))
			.filter(l -> l != null)
			.collect(Collectors.toList());
	}
	
	private StatusUpdate convertToStatusUpdate(Link l) {
		try {
			String articleUrl = getArticleUrl(l.getArticle());
			StatusUpdate out = new StatusUpdate("\""+ stripMarkdown(l.getText())+"\" "+suffix()+"- from "+articleUrl+" ");
			out.setMedia(getImageFile(articleUrl, l.getUrl()));
			return out;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	public List<Link> getImagesFromArticle(Article a) {
		return a.getLinks().stream().filter(l -> l.isImage()).collect(Collectors.toList());
	}

}
