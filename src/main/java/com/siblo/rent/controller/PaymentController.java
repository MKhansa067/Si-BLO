package com.siblo.rent.controller;

import com.midtrans.httpclient.error.MidtransError;
import com.siblo.rent.config.MidtransConfig;
import com.siblo.rent.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final MidtransConfig midtransConfig;

    public PaymentController(PaymentService paymentService, MidtransConfig midtransConfig) {
        this.paymentService = paymentService;
        this.midtransConfig = midtransConfig;
    }

    @PostMapping("/charge/{bookingId}")
    public ResponseEntity<?> charge(@PathVariable Long bookingId) {
        try {
            Map<String, String> result = paymentService.createPaymentForBooking(bookingId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (MidtransError e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Gagal membuat transaksi Midtrans: " + e.getMessage()));
        }
    }

    @GetMapping("/client-key")
    public ResponseEntity<Map<String, String>> getClientKey() {
        return ResponseEntity.ok(Map.of("clientKey", midtransConfig.getClientKey()));
    }
}