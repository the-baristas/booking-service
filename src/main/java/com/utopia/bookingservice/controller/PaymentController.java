package com.utopia.bookingservice.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.utopia.bookingservice.dto.PaymentIntentInfoDto;
import com.utopia.bookingservice.entity.ChargeRequest;
import com.utopia.bookingservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping(path="/payments")
@CrossOrigin
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/health")
    public String health(){
        return "ye";
    }

    @GetMapping("/key")
    public String key(){
        return paymentService.getSecretKey();
    }

    @PostMapping("/payment-intent")
    public HashMap<String, String> createPaymentIntent(@RequestBody PaymentIntentInfoDto paymentInfo) throws StripeException {
        HashMap<String, Object> paymentInfoMap = new HashMap<String, Object>();
        paymentInfoMap.put("amount", paymentInfo.getAmount());
        paymentInfoMap.put("currency", paymentInfo.getCurrency());

        HashMap<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("clientSecret", paymentService.createPaymentIntent(paymentInfoMap, paymentInfo.getBookingId()));
        return toReturn;
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(StripeException e){
        return e.getMessage();
    }

}
