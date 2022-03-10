package org.riskfirst.tweetprint.prodigi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import org.riskfirst.tweetprint.builder.OrderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.oddprints.prodigi.Prodigi;
import com.oddprints.prodigi.pojos.Address;
import com.oddprints.prodigi.pojos.CountryCode;
import com.oddprints.prodigi.pojos.Order;
import com.oddprints.prodigi.pojos.OrderResponse;
import com.oddprints.prodigi.pojos.Recipient;
import com.stripe.model.ShippingDetails;

/**
 * This is a stripe webhook that responds to order-complete events.
 * 
 * @author rob@kite9.com
 *
 */
@Service
public class PrintService implements InitializingBean {
	
	public static final Logger LOG = LoggerFactory.getLogger(PrintService.class);
	
	@Value("${prodigi.environment}")
	private Prodigi.Environment environment;
	
	@Value("${prodigi.apiKey}")
	private String apiKey;
	private Prodigi prodigi;
	
	@Value("${tweetprint.base-url}")
	private String baseUrl;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		prodigi = new Prodigi(environment, apiKey);
	}
	
	public void callPrint(OrderDetails od, String email, ShippingDetails shipping, String imagePayload) throws MalformedURLException {
		com.stripe.model.Address address = shipping.getAddress();
		CountryCode cc = CountryCode.valueOf(address.getCountry());
		Address prodigiAddress = new Address(address.getLine1(), address.getLine2(), address.getPostalCode(),cc, address.getCity(), address.getState());
		Recipient prodigiRecipient = new Recipient(shipping.getName(), prodigiAddress);

		URL u = new URL(baseUrl+"/print-quality?payload="+imagePayload); 
		
		// Create Order
		Order order = new Order.Builder(Order.ShippingMethod.Standard, prodigiRecipient)
		        .addImage(u, od.cardType.sku, 1)
		        .build();
		
		order.getItems().get(0).setAttributes(Collections.emptyMap());

		// Submit to API
		OrderResponse response = prodigi.createOrder(order);
		
		LOG.info("Processed Prodigi {}", response.getOutcome());
	}

}
