package org.riskfirst;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ArticleLoader {

	public List<Article> loadArticles(String dir) throws FileNotFoundException, IOException {
		List<Article> out = new ArrayList<>();
		File f = new File(dir);
		recurse(f, out);
		return out;
	}

	private void recurse(File f, List<Article> out) throws FileNotFoundException, IOException {
		if (f.isDirectory()) {
			File[] contents = f.listFiles();
			for (int i = 0; i < contents.length; i++) {
				File f2 = contents[i];
				recurse(f2, out);
			}
		} else if (f.getName().endsWith(".md")) {
			String contents = toString(new FileInputStream(f));
			ArticleState as = getStateFor(contents);
			
			Article art = new Article(as, contents, f);
			out.add(art);
			System.out.println("Loaded: "+art);
			
		}
	}
	
	private String toString(InputStream inputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while(result != -1) {
		    buf.write((byte) result);
		    result = bis.read();
		}
		// StandardCharsets.UTF_8.name() > JDK 7
		return buf.toString("UTF-8");
	}

	private ArticleState getStateFor(String contents) {
		contents = contents.substring(0, contents.indexOf('\n')+1);
		if (contents.contains("state/uc.png")) {
			return ArticleState.UNDER_CONSTRUCTION;
		} else if (contents.contains("state/draft.png")) {
			return ArticleState.DRAFT;
		} else if (contents.contains("state/for-review.png")) {
			return ArticleState.FOR_REVIEW;
		} else if (contents.contains("state/reviewed.png")) {
			return ArticleState.REVIEWED;
		} else if (contents.contains("---")) {
			// this means meta data is set, the article is published.
			return ArticleState.REVIEWED;
		} else {
			return ArticleState.NONE;
		}
	}
	
	
}
