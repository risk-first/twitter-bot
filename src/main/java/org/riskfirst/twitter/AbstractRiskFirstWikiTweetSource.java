package org.riskfirst.twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.riskfirst.Article;
import org.riskfirst.ArticleState;

public abstract class AbstractRiskFirstWikiTweetSource extends AbstractTweetSource implements TweetSource {
	
	protected List<Article> articles;
	protected URI baseUri;
	protected List<String> hashtags;
	protected String riskFirstWikiDir;

	public AbstractRiskFirstWikiTweetSource(List<Article> articles, URI baseUri, List<String> hashtags, String riskFirstWikiDir) {
		this.baseUri = baseUri;
		this.articles = articles;
		this.hashtags = hashtags;
		this.riskFirstWikiDir = riskFirstWikiDir;
	}

	public List<Article> getArticlesInState(EnumSet<ArticleState> states) {
		List<Article> out =  articles.stream().filter(a -> states.contains(a.getState())).collect(Collectors.toList());
		return out;
	}
	

	protected String getArticleUrl(Article a) throws IOException {
		return a.getUrl(baseUri.toString(), riskFirstWikiDir);
	}
	
	protected File getImageFile(String articleUrl, String imageUrl) throws FileNotFoundException {
		File out;
		try {
			if (imageUrl.startsWith("/")) {
				out = new File(riskFirstWikiDir, imageUrl);
			} else {
				URI uri = new URI(articleUrl);
				
				String dir = uri.getPath().substring(0, uri.getPath().lastIndexOf("/"));
				
				out = new File(riskFirstWikiDir, dir);
				out = new File(out, imageUrl);
				out = out.getCanonicalFile();
			}
		} catch (Exception e) {
			throw new FileNotFoundException("Couldn't find: "+ e.getMessage());
		}
		
		if (!out.exists()) {
			throw new FileNotFoundException("Image not found: "+out.toString());
		}
		return out;
	}
	
	/**
	 * Only adds a single hashtag now.
	 */
	public String suffix() { 
		Collections.shuffle(hashtags);
		List<String> sl = hashtags.subList(0, Math.min(1, hashtags.size()-1));
		return " "+sl.stream().map(a -> "#"+a).reduce((a, b) -> a+" "+b).orElse("")+" #riskFirst ("+new SimpleDateFormat("dd/MM/yy").format(new Date())+")"; 
	}
	
	public String stripMarkdown(String in) {
		return in.replace("**","").replace("_", "").replaceAll("<!--.*?-->","");
	}
}
