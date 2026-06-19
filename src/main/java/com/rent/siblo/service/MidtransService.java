package com.rent.siblo.service;

import com.rent.siblo.config.MidtransConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MidtransService {

    private final MidtransConfig midtransConfig;

    public MidtransService(MidtransConfig midtransConfig) {
        this.midtransConfig = midtransConfig;
    }

    public String createTransaction(Integer amount) {

        String serverKey = midtransConfig.getServerKey();

        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", UUID.randomUUID().toString());
        transactionDetails.put("gross_amount", amount);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transaction_details", transactionDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(serverKey, "");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://app.sandbox.midtrans.com/snap/v1/transactions",
                request,
                Map.class
        );

        Map body = response.getBody();

        return body.get("token").toString();
    }
}