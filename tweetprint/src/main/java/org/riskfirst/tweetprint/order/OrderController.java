package org.riskfirst.tweetprint.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.riskfirst.tweetprint.flow.OrderDetails;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oddprints.prodigi.pojos.CountryCode;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.ShippingAddressCollection;
import com.stripe.param.checkout.SessionCreateParams.ShippingAddressCollection.AllowedCountry;
import com.stripe.param.checkout.SessionCreateParams.ShippingOption;
import com.stripe.param.checkout.SessionCreateParams.ShippingOption.ShippingRateData;
import com.stripe.param.checkout.SessionCreateParams.ShippingOption.ShippingRateData.Type;

@Controller
public class OrderController implements InitializingBean {
	
	public static final String ORDER_DETAILS_METADATA = "orderDetails";

	private List<AllowedCountry> allowedCountries = new ArrayList<AllowedCountry>();
	
	public OrderController() {
		super();
				
		
		Arrays.stream(CountryCode.values())
			.forEach(cc -> {
				try {
					AllowedCountry ac = AllowedCountry.valueOf(cc.name());
					allowedCountries.add(ac);
				} catch (Exception e) {
				}
			});
	}	
	

	@Value("${stripe.api-key}")
	String apiKey;
	
	@Value("${tweetprint.base-url}")
	String baseUrl;
		
	@GetMapping("/checkout")
	public RedirectView checkout(@RequestParam(name = ORDER_DETAILS_METADATA) String b64Order) throws Exception {
		OrderDetails od = new ObjectMapper().readValue(Base64.getDecoder().decode(b64Order), OrderDetails.class);
		SessionCreateParams params = SessionCreateParams.builder()
			.putMetadata(ORDER_DETAILS_METADATA, b64Order)
			.setMode(SessionCreateParams.Mode.PAYMENT)
			.setShippingAddressCollection(
				ShippingAddressCollection.builder()
					.addAllAllowedCountry(allowedCountries)
				.build())
			
			.addShippingOption(
				ShippingOption.builder()
						.setShippingRateData(ShippingRateData.builder()
								.setType(Type.FIXED_AMOUNT)
								.setFixedAmount(
										ShippingRateData.FixedAmount.builder()
										.setAmount(0L).setCurrency("usd").build())
								.setDisplayName("Free Shipping")
								// Delivers between 5-7 business days
								.setDeliveryEstimate(ShippingRateData.DeliveryEstimate
										.builder()
										.setMinimum(
												ShippingRateData.DeliveryEstimate.Minimum
														.builder()
														.setUnit(
																ShippingRateData.DeliveryEstimate.Minimum.Unit.BUSINESS_DAY)
														.setValue(5L).build())
										.setMaximum(
												ShippingRateData.DeliveryEstimate.Maximum
														.builder()
														.setUnit(
																ShippingRateData.DeliveryEstimate.Maximum.Unit.BUSINESS_DAY)
														.setValue(7L).build())
										.build())
								.build())
						.build())
			
			
			
			.setSuccessUrl(baseUrl+"/success.html")
			.setCancelUrl(baseUrl+"/cancel.html")
			.addLineItem(
				SessionCreateParams.LineItem.builder()
					.setQuantity(1L)
					.setPriceData(
						SessionCreateParams.LineItem.PriceData.builder()
							.setCurrency("usd")
							.setUnitAmount(300L)
							.setProductData(
								SessionCreateParams.LineItem.PriceData.ProductData.builder()
									.setName(od.cardType.toString())
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