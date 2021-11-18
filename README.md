Tweets quotes, images, etc. from the Risk-First project.

### Deploying

Locally:

```
mvn assembly:assembly
scp target/twitter-bot-0.1-SNAPSHOT-jar-with-dependencies.jar deploy@automation.riskfirst.org:~
```

On Box:

```
cd risk-first
./doEverything.sh
```