package org.riskfirst.autotagsbot;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Query {

	String prompt;
	float temperature = 0.7f;
	int maxTokens = 10;
	int topP = 1;
	float frequencyPenalty = 0f;
	float presencePenalty = 0f;
	List<String> stop = Arrays.asList("\n");

	public Query(String query) {
		super();
		this.prompt = query;
	}

	public Query() {
		super();
	}

	
	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	
	public float getTemperature() {
		return temperature;
	}
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	
	@JsonProperty(value = "max_tokens")
	public int getMaxTokens() {
		return maxTokens;
	}
	public void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
	}

	@JsonProperty(value = "top_p")
	public int getTopP() {
		return topP;
	}
	public void setTopP(int topP) {
		this.topP = topP;
	}

	@JsonProperty(value = "frequency_penalty")
	public float getFrequencyPenalty() {
		return frequencyPenalty;
	}
	public void setFrequencyPenalty(float frequencyPenalty) {
		this.frequencyPenalty = frequencyPenalty;
	}

	@JsonProperty(value = "presence_penalty")
	public float getPresencePenalty() {
		return presencePenalty;
	}
	public void setPresencePenalty(float presencePenalty) {
		this.presencePenalty = presencePenalty;
	}
	
	public List<String> getStop() {
		return stop;
	}
	public void setStop(List<String> stop) {
		this.stop = stop;
	}
	
	
}
