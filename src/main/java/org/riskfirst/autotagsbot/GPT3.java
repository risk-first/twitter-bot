package org.riskfirst.autotagsbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;

public class GPT3 {

	
	private static final Client CLIENT = ClientBuilder
			.newBuilder()
			.register(JacksonFeature.class)
			.build();
	
	String apiKey;
	
	public GPT3(String apiKey) {
		super();
		this.apiKey = apiKey;
	}
	
	public String call(String text) {
		WebTarget wt = CLIENT.target("https://api.openai.com/v1/engines/davinci/completions");
		Query q = new Query(text);
		Entity<Query> data = Entity.entity(q, MediaType.APPLICATION_JSON);
		
		Response done = wt.request(MediaType.APPLICATION_JSON)
				.header("Authorization","Bearer "+apiKey)
				.post(data, Response.class);
		
		return done.getChoices().get(0).getText();	
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String apiKey = getGPTKey();
		System.out.println(new GPT3(apiKey).call("This is the way to my "));
	}

	public static String getGPTKey() throws IOException, FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileReader(new File("openai.properties")));
		String apiKey = props.getProperty("key");
		return apiKey;
	}
	
		
}
