package org.riskfirst.autotagbot;

import java.awt.Choice;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Choice {
		
		String text;
		int index;
		String finishReason;
		
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public String getFinishReason() {
			return finishReason;
		}
		public void setFinishReason(String finishReason) {
			this.finishReason = finishReason;
		}
		
		
	}

	List<Choice> choices;

	public List<Choice> getChoices() {
		return choices;
	}

	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}
	
}
