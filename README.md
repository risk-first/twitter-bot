Tweets quotes, images, etc. from the Risk-First project.

### Deploying

Locally:

```
mvn assembly:assembly
scp target/twitter-bot-0.1-SNAPSHOT-jar-with-dependencies.jar deploy@automation.riskfirst.org:~
```

This creates and deploys an uber-jar.

copy into a function directory.  e.g.

```
liker
hackernews
risk-first
```

Each of these should have a `twitter4j.properties` in, and also `tweeter.properties` if there are other things to set.

On Box:

```
cd risk-first
java -jar twitter-bot-0.1-SNAPSHOT-jar-with-dependencies.jar org.riskfirst.twitter.Tweeter
```


# Configuring A Bot Account

## Apps

 - Followed this:   https://blog.dghubble.io/posts/friendly-twitter-bots/
 - Once you have an app, you enable OAuth1 settings, which allows you to ask for read/write perms
 - The URLs for OAuth don't matter - we're not going to use them


## twurl

You can use twurl to generate the twitter login keys for you.  

 - Log in to your bot account using safari
 - Run:

```
/usr/local/lib/ruby/gems/2.7.0/gems/twurl-0.9.6/bin/twurl authorize --consumer-key "API Key from App console" \
    --consumer-secret "API Secret from App console"
```

 - Then, hit up the URL it asks for.    
 - After that the ~/.twurlrc file contains the keys you need.   
    
