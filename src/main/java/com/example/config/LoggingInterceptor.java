package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class LoggingInterceptor implements ClientInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        if (logger.isDebugEnabled()) {
            logger.debug("SOAP Request: {}", getMessageContent(messageContext.getRequest().getPayloadSource()));
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        if (logger.isDebugEnabled()) {
            logger.debug("SOAP Response: {}", getMessageContent(messageContext.getResponse().getPayloadSource()));
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        if (logger.isWarnEnabled()) {
            logger.warn("SOAP Fault: {}", getMessageContent(messageContext.getResponse().getPayloadSource()));
        }
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        if (ex != null) {
            logger.error("SOAP call failed", ex);
        }
    }

    private String getMessageContent(Source source) {
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            logger.error("Error converting message to string", e);
            return "Error converting message";
        }
    }
}