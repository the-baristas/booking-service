package com.utopia.bookingservice.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.utopia.bookingservice.entity.ChargeRequest;
import com.utopia.bookingservice.service.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@RestController
@RequestMapping(path="/payments")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @GetMapping("/health")
    public String health(){
        return "ye";
    }

    @PostMapping
    public Charge charge(@RequestBody ChargeRequest chargeRequest) throws StripeException{
        System.out.println(chargeRequest);

        Charge charge = stripeService.charge(chargeRequest);
        return charge;
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(StripeException e){
        return e.getMessage();
    }

}
