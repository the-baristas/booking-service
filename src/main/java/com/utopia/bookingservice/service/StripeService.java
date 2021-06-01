package com.utopia.bookingservice.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.utopia.bookingservice.entity.ChargeRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private String secretKey = "sk_test_51IxHqJEGbf0XUVjsQgueCUfotDJ2GA52JV40YLwdZGLBdDnmRQS001KSfNFewnY8kVbqUTrtkr664omfaNcRfWlg00yQJ4Txjb";

    @PostConstruct
    public void init(){
        Stripe.apiKey = secretKey;
    }

    public Charge charge(ChargeRequest chargeRequest) throws StripeException {
        Map<String, Object> chargeParams = new HashMap<String, Object>();
        chargeParams.put("amount", chargeRequest.getAmount());
        chargeParams.put("currency", chargeRequest.getCurrency());
        chargeParams.put("description", chargeRequest.getDescription());
        chargeParams.put("source", chargeRequest.getStripeToken());

        return Charge.create(chargeParams);
    }
}
