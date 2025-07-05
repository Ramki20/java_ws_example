package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import java.time.Duration;

@Configuration
public class WebServiceConfig {

    @Value("${soap.service.url:http://10.29.60.95:8080/easws/sharedservice/AuthorizationSharedService}")
    private String serviceUrl;

    @Value("${soap.service.timeout:30000}")
    private int timeout;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Use the package where JAX-WS generated your classes
        marshaller.setContextPath("com.example.generated");
        return marshaller;
    }

    @Bean
    public HttpUrlConnectionMessageSender messageSender() {
        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofMillis(timeout));
        sender.setReadTimeout(Duration.ofMillis(timeout));
        return sender;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller, 
                                                HttpUrlConnectionMessageSender messageSender) {
        WebServiceTemplate ws = new WebServiceTemplate();
        ws.setMarshaller(marshaller);
        ws.setUnmarshaller(marshaller);
        ws.setDefaultUri(serviceUrl);
        ws.setMessageSender(messageSender);
        
        // Optional: Add interceptors for logging
        ws.setInterceptors(new ClientInterceptor[]{
            new LoggingInterceptor()
        });
        
        return ws;
    }

    @Bean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }
}