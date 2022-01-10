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