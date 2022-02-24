package org.riskfirst.tweetprint.image.kite9;

import java.util.List;

import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.vdurmont.emoji.EmojiParser;

public class EmojiToMarkupParser extends EmojiParser {

	interface ImageTransformer {
		Element transform(UnicodeCandidate unicodeCandidate);
	}

	public static void parseInto(String input, Element e, ImageTransformer transformer) {
		int prev = 0;
		List<UnicodeCandidate> replacements = getUnicodeCandidates(input);
		for (UnicodeCandidate candidate : replacements) {
			if (prev < candidate.getEmojiStartIndex()) {
				String textChunk = input.substring(prev, candidate.getEmojiStartIndex());
				appendTextElement(textChunk, e);
			}

			e.appendChild(transformer.transform(candidate));

			prev = candidate.getFitzpatrickEndIndex();
		}
		
		if (prev < input.length()) {
			String textChunk = input.substring(prev);
			appendTextElement(textChunk, e);
		}
	}

	private static void appendTextElement(String textChunk, Element e) {
		Text t = e.getOwnerDocument().createTextNode(textChunk);
		Element textElement = e.getOwnerDocument().createElementNS(Kite9Namespaces.SVG_NAMESPACE, "svg:text");
		textElement.appendChild(t);
		e.appendChild(textElement);
	}

}
