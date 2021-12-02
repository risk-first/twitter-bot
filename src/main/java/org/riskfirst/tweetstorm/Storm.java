package org.riskfirst.tweetstorm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.riskfirst.Article;
import org.riskfirst.ArticleLoader;
import org.riskfirst.twitter.ImageTweetSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class Storm {
	
    static Properties props = new Properties();
    
    static String riskFirstWikiDir = "../website";

    public static final Object END = new Object() {

		@Override
		public String toString() {
			return "END";
		}  	
    	
    };
    
    public static final Object BULLET = new Object() {

		@Override
		public String toString() {
			return "BULLET";
		}  	
    	
    };
	
	public static final int MAX_CHARS = 200;
	public static final int MAX_CHARS_OUT = 270;
	
	URI imageUrl;
	URI pageUrl;
	List<TweetStructure> tweets = new ArrayList<>();
	List<String> tags;
	File f;
	String description;
	String title;
	int nextNumber = 1;
	
	Set<Object> alreadyLinked = new HashSet<>();
	
	enum Mode { NORMAL, BULLETS, SKIP, ORDERED }
	
	static final Pattern END_OF_SENTENCE = Pattern.compile("[\\.\\!\\?]\\ [\\ ]*");
	
	private void processYaml(String yaml) throws Exception {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		TypeReference<Map<String, Object>> tr = new TypeReference<Map<String,Object>>() {};
		Map<String, Object> values = mapper.readValue(yaml, tr);
		
		title = (String) values.get("title");
		description = (String) values.get("description");
	}
	
	
	public Storm(String fileName, String url, List<String> tags) throws Exception {
		this.tags = tags;
		this.pageUrl = new URI(url);
		f = new File(fileName);
		
		List<Extension> extensions = Arrays.asList(TablesExtension.create());
		Parser parser = Parser.builder()
		        .extensions(extensions)
		        .build();
		
		String s = ArticleLoader.toString(new FileInputStream(f));
		
		List<Object> stuff = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		if (s.startsWith("---\n")) {
			// remove meta
			s = s.substring(4);
			int yamlEnd = s.lastIndexOf("---\n");
			processYaml(s.substring(0, yamlEnd));
			s = s.substring(yamlEnd+4);
			stuff.add(description);
		}
		
		Document d = (Document) parser.parse(s);

		
		d.accept(new AbstractVisitor() {
			
			Stack<Mode> mode = new Stack<>();
			
			public void emptyBuffer() {
				String full = sb.toString();
				sb.setLength(0);

				Matcher m = END_OF_SENTENCE.matcher(full);
				int lastMatch = 0;
				while (true)  {
					String sen;
					
					if (m.find(lastMatch)) {
						sen = full.substring(lastMatch, m.end());
						lastMatch = m.end();
					} else {
						sen = full.substring(lastMatch);
						lastMatch = full.length();
					}
					if (sen.length() > 0) {
						stuff.add(sen);
					} else {
						return;
					}
				}
				
			}
			
			@Override
			public void visit(BlockQuote blockQuote) {
				emptyBuffer();
				stuff.add(blockQuote);
			}

			@Override
			public void visit(BulletList bulletList) {
				mode.push(Mode.BULLETS);
				super.visit(bulletList);
				mode.pop();
			}

			@Override
			public void visit(Code code) {
				sb.append("[");
				sb.append(code.getLiteral());
				sb.append("]");
			}

			@Override
			public void visit(Emphasis emphasis) {
				sb.append("/");
				super.visit(emphasis);
				sb.append("/");
			}

			@Override
			public void visit(HardLineBreak hardLineBreak) {
				emptyBuffer();
				super.visit(hardLineBreak);
			}

			@Override
			public void visit(Heading heading) {
				if (heading.getLevel() == 1) {
					stuff.add(new Link(pageUrl.toString(), ""));
					emptyBuffer();
					stuff.add(END);
				}
				
			}

			@Override
			public void visit(Image image) {
				emptyBuffer();
				if (alreadyLinked.add(image.getDestination())) {
					stuff.add(image);
				}
			}

			@Override
			public void visit(Link link) {
				if ((!link.getDestination().toLowerCase().contains("glossary")) &&
				 (alreadyLinked.add(link.getDestination())) && (!link.getDestination().contains("#"))) {
					emptyBuffer();
					stuff.add(link);
				}
				super.visit(link);
			}

			@Override
			public void visit(ListItem listItem) {
				emptyBuffer();
				if (mode.peek() == Mode.BULLETS) {
					stuff.add(BULLET);
				} else if (mode.peek() == Mode.ORDERED) {
					stuff.add(nextNumber++);
				}
				super.visit(listItem);
				emptyBuffer();
			}

			@Override
			public void visit(OrderedList orderedList) {
				emptyBuffer();
				nextNumber = 1;
				mode.push(Mode.ORDERED);
				super.visit(orderedList);
				emptyBuffer();
				mode.pop();
			}

			@Override
			public void visit(Paragraph paragraph) {
				emptyBuffer();
//				if (mode.peek() == Mode.BULLETS) {
//					stuff.add(BULLET);
//				} else if (mode.peek() == Mode.ORDERED) {
//					stuff.add(nextNumber++);
//				}
				super.visit(paragraph);
				emptyBuffer();
			}

			@Override
			public void visit(SoftLineBreak softLineBreak) {
				emptyBuffer();
				super.visit(softLineBreak);
			}

			@Override
			public void visit(StrongEmphasis strongEmphasis) {
				sb.append("*");
				super.visit(strongEmphasis);
				sb.append("*");
			}

			@Override
			public void visit(Text text) {
				sb.append(text.getLiteral());
			}

			@Override
			public void visit(Document document) {
				emptyBuffer();
				mode.push(Mode.NORMAL);
				super.visit(document);
				mode.pop();
			}
			
		});
		
		for (Object ts : stuff) {
			System.out.println(ts);
		}
		
		collateTweets(stuff);
		numbering();		
		pepperTags();
		fixUrls();

		for (TweetStructure ts : tweets) {
			System.out.println(ts);
			int urlLen = (ts.url instanceof String) ? ((String) ts.url).length() : 0;
			if (ts.text.length() + urlLen > MAX_CHARS_OUT) {
				throw new Exception("Tweet too long");
			}
		}
		
	}
	
	private void fixUrls() {
		for (TweetStructure ts : tweets) {
			if (ts.url instanceof String) {
				ts.url = pageUrl.resolve(((String) ts.url).replace(".md", "")).toString();
			}
		}
	}

	private void numbering() {
		for (int i = 0; i < tweets.size(); i++) {
			TweetStructure ts = tweets.get(i);
			ts.text = ts.text + " ("+(i+1)+"/"+(tweets.size())+")";
		}
		
	}

	private void pepperTags() {
		for (String tag: tags) {
			boolean done = false;
			while (!done) {
				int i = new Random().nextInt(tweets.size());
				TweetStructure ts = tweets.get(i);
				if (ts.text.length() < MAX_CHARS) {
					ts.text = ts.text + " #"+tag;
					done = true;
				}
			}
		}
	}

	static class TweetStructure {
		
		String text = "";
		
		Object url;
				
		public String toString() {
			return "---\n"+text+"\n"+url+"\n--- ("+text.length()+")";
		}
		
	}
	
	
	private void collateTweets(List<Object> stuff) throws FileNotFoundException {
		TweetStructure next = new TweetStructure();
		int blockQuotes = 0;
		
		for (Object s : stuff) {
			if (s == END) {
				next = newTweet(next);
			} else if (s instanceof String) {
				if (((String) s).length() + next.text.length() > MAX_CHARS) {
					tweets.add(next);
					next = new TweetStructure();
				}
				
				if (((String) s).length() > 0) {
					s = s + " ";
				}
				
				next.text += s;
			} else if (s instanceof Link) {
				next = newUrlTweet(next);
				next.url = ((Link) s).getDestination();
			} else if (s instanceof BlockQuote) {
				next = newUrlTweet(next);
				next.url = new File(riskFirstWikiDir + Article.createQuoteFilePath(f, blockQuotes, riskFirstWikiDir));
				blockQuotes++;
				if (!((File)next.url).exists()) {
					throw new RuntimeException("Can't find "+next.url);
				}
			} else if (s instanceof Image) {
				next = newUrlTweet(next);
				next.url = ImageTweetSource.getImageFile(riskFirstWikiDir, pageUrl.toString(), ((Image) s).getDestination());
				
				if (!((File)next.url).exists()) {
					throw new RuntimeException("Can't find "+next.url);
				}
			} else if (s instanceof Number) {
				next = newTweet(next);
				next.text = ""+s+")";
			} else if (s == BULLET) {
				next = newTweet(next);
				next.text = " - ";
			}
		}
		
		if ((next.url != null) || (next.text != null)) {
			tweets.add(next);
		}
		
	}


	public TweetStructure newUrlTweet(TweetStructure next) {
		if (next.url != null) {
			tweets.add(next);
			next = new TweetStructure();
		}
		return next;
	}


	public TweetStructure newTweet(TweetStructure next) {
		if ((next.url != null) || (next.text.length() > 0)) {
			tweets.add(next);
			next = new TweetStructure();
		}
		return next;
	}

	static Status top;

	public static void main(String[] args) throws Exception {
		try {
			props.load(new FileReader(new File("tweeter.properties")));
			List<String> tags = Arrays.asList(props.getProperty("tags").split(","));

			Storm s = new Storm(args[0], args[1], tags);
			
			Twitter twitter = TwitterFactory.getSingleton();
			
			for (TweetStructure ts : s.tweets) {
				StatusUpdate su;
				if (ts.url instanceof File) {
					su = new StatusUpdate(ts.text);
					su.setMedia((File) ts.url);
				} else if (ts.url instanceof String) {
					su = new StatusUpdate(ts.url+"\n"+ ts.text);
				} else {
					su = new StatusUpdate(ts.text);
				}
				
				if (top != null) {
					su.setInReplyToStatusId(top.getId());
				}
				
				top = twitter.updateStatus(su);				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
