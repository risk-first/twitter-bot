package org.riskfirst.tweetprint.order;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Controller
public class OrderController implements InitializingBean {

	@Value("${stripe.api-key}")
	String apiKey;
	
	@Value("${tweetprint.base-url}")
	String baseUrl;
		
	@PostMapping("/checkout")
	public RedirectView checkout() throws StripeException {
		SessionCreateParams params = SessionCreateParams.builder()
			.setMode(SessionCreateParams.Mode.PAYMENT)
			.setSuccessUrl(baseUrl+"/success.html")
			.setCancelUrl(baseUrl+"/cancel.html")
			.addLineItem(
				SessionCreateParams.LineItem.builder()
					.setQuantity(1L)
					.setPriceData(
						SessionCreateParams.LineItem.PriceData.builder()
							.setCurrency("usd")
							.setUnitAmount(2000L)
							.setProductData(
								SessionCreateParams.LineItem.PriceData.ProductData.builder()
									.setName("T-shirt")
									.build())
							.build())
					.build())
			.build();
		
		Session session = Session.create(params);		
		return new RedirectView(session.getUrl());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Stripe.apiKey = apiKey;
	}
}