package org.riskfirst.twitter;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.riskfirst.Article;

import twitter4j.StatusUpdate;

/**
 * Now uses quotes held in images.
 */
public class QuoteTweetSource extends AbstractRiskFirstWikiTweetSource {
	
	private String quoteDir;
	
	public QuoteTweetSource(List<Article> articles, URI baseUri, List<String> tags, String riskFirstBaseDir, String quoteDir) {
		super(articles, baseUri, tags, riskFirstBaseDir);
		this.quoteDir = quoteDir;
	}
	
	@Override
	public List<StatusUpdate> getAllTweets() {
		File f = new File(riskFirstWikiDir+quoteDir);
		
		System.out.println("getting quote tweets: "+f);
		
		return Arrays.stream(f.listFiles())
				.filter(ff -> ff.getName().endsWith("png"))
				.map(ff -> {
			String articleUrl = createArticleUrl(ff);
			StatusUpdate out = new StatusUpdate("from "+articleUrl+" "+suffix());
			out.setMedia(ff);
			System.out.println("Potential Tweet:"+out);
			return out;
			
		}).collect(Collectors.toList());
		
	}

	private String createArticleUrl(File ff) {
		String name = ff.getName();
		int us = name.lastIndexOf("_");
		String article = name.substring(0, us);
		return baseUri + article;
	}
}
