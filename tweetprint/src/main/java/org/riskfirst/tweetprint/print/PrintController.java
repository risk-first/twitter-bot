package org.riskfirst.tweetprint.print;

import java.net.MalformedURLException;
import java.net.URL;

import com.oddprints.prodigi.Prodigi;
import com.oddprints.prodigi.pojos.Address;
import com.oddprints.prodigi.pojos.CountryCode;
import com.oddprints.prodigi.pojos.Order;
import com.oddprints.prodigi.pojos.OrderResponse;
import com.oddprints.prodigi.pojos.Recipient;

/**
 * This is a stripe webhook that responds to order-complete events.
 * 
 * @author rob@kite9.com
 *
 */
public class PrintController {

	
	public void callPrint() throws MalformedURLException {
		// Choose Environment.LIVE for real orders
		Prodigi prodigi = new Prodigi(Prodigi.Environment.SANDBOX, "YOUR-API-KEY");

		// Create Recipient
		Recipient recipient = new Recipient("Bob", new Address("line1", "line2", "90210", CountryCode.GB, "Bristol", "WST"));

		// Create Order
		Order order = new Order.Builder(Order.ShippingMethod.Standard, recipient)
		        .addImage(new URL("https://www.oddprints.com/images/header-dogcat.jpg"), "GLOBAL-PHO-4x6", 1)
		        .build();

		// Submit to API
		OrderResponse response = prodigi.createOrder(order);
	}
	
}
