package org.riskfirst.tweetprint.order;

import static org.riskfirst.tweetprint.order.OrderController.ORDER_DETAILS_METADATA;

import java.io.IOException;
import java.util.Base64;

import org.riskfirst.tweetprint.flow.OrderDetails;
import org.riskfirst.tweetprint.print.PrintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.ShippingDetails;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@RestController
public class StripeWebhookController {
	
	public static final Logger LOG = LoggerFactory.getLogger(StripeWebhookController.class);
	
	@Value("${stripe.endpoint-secret}")
	String endpointSecret;
	
	@Autowired
	private PrintService printService;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	
	@PostMapping("/confirmation")
	public void processWebhook(
		@RequestHeader("Stripe-Signature") String stripeSignature,
		@RequestBody String payload) {
		Event event = null;
        StripeObject stripeObject = null;
		
		try {
			event = Webhook.constructEvent(payload, stripeSignature, endpointSecret);
	        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
	        
	        // Deserialize the nested object inside the event
	        if (dataObjectDeserializer.getObject().isPresent()) {
	            stripeObject = dataObjectDeserializer.getObject().get();
	        } else {
	        	throw new IllegalStateException(
	        		       String.format("Unable to deserialize event data object for %s", event));
	        }
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	        
	    processEvent(event, stripeObject);
	}


	private void processEvent(Event event, StripeObject stripeObject) {
		try {
			switch (event.getType()) {
			    case "checkout.session.completed":
			    	Session session = (Session) stripeObject;
			        LOG.info("Payment for " + session.getAmountTotal() + " succeeded.");
			        handleCheckoutSessionCompleted(session);
			        break;
			    default:
			        LOG.warn("Event type: {} not supported", event.getType());
			        break;
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process event: "+e.getMessage(), e);
		}
	}


	private void handleCheckoutSessionCompleted(Session session) throws JsonParseException, JsonMappingException, IOException {
		String b64details = session.getMetadata().get(ORDER_DETAILS_METADATA);
		OrderDetails details = objectMapper.readValue(Base64.getDecoder().decode(b64details), OrderDetails.class);
		String email = session.getCustomerDetails().getEmail();
		ShippingDetails shipping = session.getShipping();
		
		printService.callPrint(details, email, shipping, b64details);
		
	}
}
