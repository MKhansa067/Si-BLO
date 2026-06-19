package com.rent.siblo.controller;

import com.rent.siblo.service.MidtransService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final MidtransService midtransService;

    public PaymentController(MidtransService midtransService) {
        this.midtransService = midtransService;
    }

    @GetMapping("/token")
    public String getToken(
            @RequestParam(defaultValue = "10000") Integer amount
    ) throws Exception {

        return midtransService.createTransaction(amount);
    }
}