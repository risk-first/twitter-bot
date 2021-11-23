package org.riskfirst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Article {
	
	private final ArticleState state;
	private final File f;
	private transient List<Link> links;
	private transient List<Link> quotes;
	
	public File getFile() {
		return f;
	}

	public Article(ArticleState state, File f) {
		super();
		this.state = state;
		this.f = f;
	}

	public ArticleState getState() {
		return state;
	}
	
	static Pattern LINK_PATTERN = Pattern.compile("(\\!)?\\[([^\\]]*?)\\]\\((.*?)\\)(\\{(.*?)\\})?");
	
	public List<Link> getLinks() {
		if (links == null) {
			process();
		}
		
		return links;
	}
	
	public List<Link> getQuotes() {
		if (quotes == null) {
			process();
		}
		
		return quotes;
	}
	
	private void process() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			links = new ArrayList<>();
			quotes = new ArrayList<>();
			String line = br.readLine();
			int number = 1;
			while (line != null) {
				processLine(line, number);
				line = br.readLine();
				number++;
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't get links:", e);
		}
	}
	
	public void processLine(String line, int number) {
		Matcher linkMatcher = LINK_PATTERN.matcher(line);
		while (linkMatcher.find()) {
			String bang = linkMatcher.group(1);
			String text = linkMatcher.group(2);
			String url = linkMatcher.group(3);
			links.add(new Link(bang != null, text, url, number, this));
		}
		
		if (line.startsWith(">") && (line.indexOf("- [") > -1)) {
			// it's a quote
			String quoteLink = createQuoteFilePath(this.f, quotes.size());
			quotes.add(new Link(true, "", quoteLink, number, this));
		}
		
	}

	public static String createQuoteFilePath(File f, int i) {
		String quoteLink = f.getPath()
			.replace("../website", "/images/generated/quotes")
			.replace(".md", "-"+i+".png");
		return quoteLink;
	}

	@Override
	public String toString() {
		return "Article [state=" + state + ", f=" + f + "]";
	}
	
	public String getUrl(String baseUri, String homeDir) throws IOException {
		return baseUri+this.f.getPath().substring(homeDir.length()+1).replace(".md", "");
	}
	
}
