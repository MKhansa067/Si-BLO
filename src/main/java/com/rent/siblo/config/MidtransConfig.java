package com.rent.siblo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidtransConfig {

    @Value("${midtrans.server-key:dummy-server-key}")
    private String serverKey;

    @Value("${midtrans.client-key:dummy-client-key}")
    private String clientKey;

    public String getServerKey() {
        return serverKey;
    }

    public String getClientKey() {
        return clientKey;
    }
}