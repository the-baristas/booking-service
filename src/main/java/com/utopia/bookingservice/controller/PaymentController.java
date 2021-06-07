package com.utopia.bookingservice.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.utopia.bookingservice.dto.BookingDto;
import com.utopia.bookingservice.dto.PaymentDto;
import com.utopia.bookingservice.dto.PaymentIntentInfoDto;
import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.ChargeRequest;
import com.utopia.bookingservice.entity.Payment;
import com.utopia.bookingservice.service.PaymentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;

@RestController
@RequestMapping(path="/payments")
@CrossOrigin
public class PaymentController {

    private ModelMapper modelMapper;

    @Autowired
    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService, ModelMapper modelMapper){
        this.paymentService = paymentService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/health")
    public String health(){
        return "ye";
    }

    @PostMapping("/payment-intent")
    public HashMap<String, String> createPaymentIntent(@RequestBody PaymentIntentInfoDto paymentInfo) throws StripeException {

        //Stripe API requires a Map<String, Object>
        HashMap<String, Object> paymentInfoMap = new HashMap<String, Object>();
        paymentInfoMap.put("amount", paymentInfo.getAmount());
        paymentInfoMap.put("currency", paymentInfo.getCurrency());

        //return the clientSecret which was just created
        HashMap<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("clientSecret", paymentService.createPaymentIntent(paymentInfoMap));
        return toReturn;
    }

    @PostMapping("")
    public PaymentDto createPayment(@RequestBody PaymentDto paymentDto) throws ParseException {
        System.out.println(paymentDto);
        Payment payment = convertToEntity(paymentDto);
        payment.setStripeId(paymentDto.getStripeId());
        System.out.println(payment);
        System.out.println(convertToDto(paymentService.createPayment(payment)));
        return convertToDto(paymentService.createPayment(payment));
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(StripeException e){
        return e.getMessage();
    }

    private PaymentDto convertToDto(Payment payment) {
        return modelMapper.map(payment, PaymentDto.class);
    }

    private Payment convertToEntity(PaymentDto paymentDto)
            throws ParseException {
        return modelMapper.map(paymentDto, Payment.class);
    }

}
