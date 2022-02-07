package org.riskfirst.topicmail;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * Finds occasions where people have tweeted hacker news articles, and replies
 * to them with the comments from hacker news.
 * 
 * Won't reply to the same person within a day.
 * 
 * Now reduced to 14 stories to hopefully avoid the problem of being banned. I
 * really have no idea why this is happening.
 * 
 * @author rob@kite9.com
 *
 */
public class TopicMail {

	public static final int MAX_STORIES = 12;

	public static List<String> allTopics;
	
	public static Session session;
	
	public static Properties p;

	public static void main(String[] args) throws Exception {

		// load up the top 20 hacker news articles
		Twitter twitter = TwitterFactory.getSingleton();

		loadTopics();
		
		int count = 0;
		
		p = new Properties();
		p.load(new FileInputStream("mail.properties"));
		
		session = Session.getInstance(p, new Authenticator() {
		    @Override
		    protected PasswordAuthentication getPasswordAuthentication() {
		        String name = p.getProperty("mail.smtp.username");
				String pass = p.getProperty("mail.smtp.password");
				return new PasswordAuthentication(name, pass);
		    }
		});
		
		session.setDebug(false);
		
		for (String q : allTopics) {
			count = searchForStory(q, twitter, count);
		}

	}

	private static void loadTopics() throws Exception {
		File rep = new File("topics.json");
		allTopics = new ObjectMapper().readValue(rep, new TypeReference<List<String>>() {
		});
		System.out.println("All Topics: (" + allTopics + ")");
	}

	public static int searchForStory(String topic, Twitter t, int count) {
		try {
			System.out.println("# "+topic);
			LocalDate ld = LocalDate.now();
			String startOfDay = DateTimeFormatter.ISO_DATE.format(ld);

			Query q = new Query("\"" + topic + "\" +exclude:retweets since:" + startOfDay);

			// limit replies
			q.setCount(20);

			// todo - add the last hour/whatever
			QueryResult qr = t.search(q);

			for (Status s : qr.getTweets()) {
				if (filter(s)) {
					if (count < 20) {
						email(s, t);
						count ++;
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}

	private static void email(Status s, Twitter t) throws Exception {
		String url = "https://twitter.com/"+s.getUser().getScreenName()+"/status/"+s.getId();
		System.out.println(url+"    "+s.getFavoriteCount()+"    "+(s.getText().replace("\n", "")));
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(p.getProperty("mail.smtp.username")));
		message.setRecipients(
		  Message.RecipientType.TO, InternetAddress.parse(p.getProperty("mail.to")));
		message.setSubject("Twitter Notification");

		String msg = "Check this out: "+url+"\n"+s.getText();

		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(mimeBodyPart);

		message.setContent(multipart);

		Transport.send(message);
	}

	private static boolean filter(Status status) throws TwitterException {

		if ((!status.isFavorited())) {
			int den = status.getRetweetCount() + status.getFavoriteCount();
			User u = status.getUser();
			int follows = u.getFollowersCount();

			if ((den > 9) || (follows > 5000)) {
				return true;
			}
		}

		return false;
	}

}
