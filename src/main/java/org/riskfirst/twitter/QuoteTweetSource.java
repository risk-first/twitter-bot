package org.riskfirst.twitter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.riskfirst.Article;
import org.riskfirst.Link;

import twitter4j.StatusUpdate;

public class QuoteTweetSource extends AbstractRiskFirstWikiTweetSource {
	
	public QuoteTweetSource(List<Article> articles, URI baseUri, List<String> tags) {
		super(articles, baseUri, tags);
	}
	
	@Override
	public List<StatusUpdate> getAllTweets() {
		List<StatusUpdate> out = new ArrayList<>();
		articles.stream().forEach(a -> getTweetsFor(a, out));
		return out;
	}

	public void getTweetsFor(Article a, List<StatusUpdate> out) {
		String text = a.getText();
		String[] lines = text.split("\\r?\\n");
		
		for (String string : lines) {
			if (string.startsWith(">")) {
				string = string.substring(1);
				StatusUpdate su = new StatusUpdate(""+deMarkdown(string, a) + " - from "+a.getUrl(baseUri.toString()));
				System.out.println("Potential tweet: "+su);
				out.add(su);
			}
		}
	}
	
	public String deMarkdown(String text, Article a) {
		StringBuilder sb = new StringBuilder();
		Article.processLine(text, 0, 
				link -> link.getText(), 
				t ->sb.append(t), a);
		 
		return stripMarkdown(sb.toString())+suffix(2);
	}
}
