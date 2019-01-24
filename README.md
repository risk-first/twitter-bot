Tweets quotes, images, etc. from the risk first project.

### Deploying

Locally:

```
mvn assembly:assembly
scp target/twitter-bot-0.1-SNAPSHOT-jar-with-dependencies.jar deploy@server.kite9.org:~/risk-first
```

On Box:

```
cd risk-first
./doEverything.sh
```