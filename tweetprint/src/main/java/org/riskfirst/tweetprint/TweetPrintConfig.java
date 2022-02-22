package org.riskfirst.tweetprint;

import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TweetPrintConfig implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		Kite9Log.Companion.setFactory(logable -> new Kite9LogImpl(logable));
	}

}
