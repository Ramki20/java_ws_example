package com.example.service;

import com.example.generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.List;

@Service
public class AuthorizationSoapClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationSoapClientService.class);
    private static final String NAMESPACE = "http://web.service.eas.citso.fsa.usda.gov";
    
    private final WebServiceTemplate webServiceTemplate;
    private final ObjectFactory objectFactory;
    
    @Autowired
    public AuthorizationSoapClientService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
        this.objectFactory = new ObjectFactory();
    }
    
    /**
     * Find matching user identity using map entries
     */
    public UserIdentity findMatchingUserIdentity(List<MapEntry> mapEntries) {
        try {
            logger.debug("Finding matching user identity with {} map entries", mapEntries.size());
            
            // Create the request object using generated classes
            FindMatchingUserIdentityRequest request = new FindMatchingUserIdentityRequest();
            request.getMapEntry().addAll(mapEntries);
            
            // Create JAXBElement for the request - use the element factory method
            JAXBElement<FindMatchingUserIdentityRequest> requestElement = 
                new JAXBElement<>(new QName(NAMESPACE, "FindMatchingUserIdentityRequest"), 
                                FindMatchingUserIdentityRequest.class, request);
            
            // Send request and get response
            @SuppressWarnings("unchecked")
            JAXBElement<FindMatchingUserIdentityResponse> responseElement = 
                (JAXBElement<FindMatchingUserIdentityResponse>) webServiceTemplate.marshalSendAndReceive(requestElement);
            
            FindMatchingUserIdentityResponse response = responseElement.getValue();
            UserIdentity userIdentity = response.getUserIdentity();
            
            logger.debug("Found user identity: {}", userIdentity != null ? userIdentity.getUserLoginName() : "null");
            return userIdentity;
            
        } catch (SoapFaultClientException e) {
            logger.error("SOAP fault occurred while finding matching user identity: {}", e.getFaultStringOrReason());
            throw new RuntimeException("Failed to find matching user identity: " + e.getFaultStringOrReason(), e);
        } catch (Exception e) {
            logger.error("Error occurred while finding matching user identity", e);
            throw new RuntimeException("Failed to find matching user identity", e);
        }
    }
    
    /**
     * Find offices by EAuth ID
     */
    public ListType findOfficesByEauthId(String usdaEauthId, List<OfficeType> officeTypes, RequestToken requestToken) {
        try {
            logger.debug("Finding offices for EAuth ID: {} with {} office types", usdaEauthId, officeTypes.size());
            
            // Create the request object
            FindOfficesByEauthIdRequest request = new FindOfficesByEauthIdRequest();
            request.setUsdaEauthId(usdaEauthId);
            request.getOfficeType().addAll(officeTypes);
            request.setRequestToken(requestToken);
            
            // Create JAXBElement for the request
            JAXBElement<FindOfficesByEauthIdRequest> requestElement = 
                new JAXBElement<>(new QName(NAMESPACE, "FindOfficesByEauthIdRequest"), 
                                FindOfficesByEauthIdRequest.class, request);
            
            // Send request and get response
            @SuppressWarnings("unchecked")
            JAXBElement<FindOfficesByEauthIdResponse> responseElement = 
                (JAXBElement<FindOfficesByEauthIdResponse>) webServiceTemplate.marshalSendAndReceive(requestElement);
            
            FindOfficesByEauthIdResponse response = responseElement.getValue();
            ListType offices = response.getOffices();
            
            logger.debug("Found {} offices", offices != null && offices.getListValue() != null ? offices.getListValue().size() : 0);
            return offices;
            
        } catch (SoapFaultClientException e) {
            logger.error("SOAP fault occurred while finding offices by EAuth ID: {}", e.getFaultStringOrReason());
            throw new RuntimeException("Failed to find offices by EAuth ID: " + e.getFaultStringOrReason(), e);
        } catch (Exception e) {
            logger.error("Error occurred while finding offices by EAuth ID", e);
            throw new RuntimeException("Failed to find offices by EAuth ID", e);
        }
    }
    
    /**
     * Find users by criteria
     */
    public ListType findUsersByCriteria(String officeId, String roleName, RequestToken requestToken) {
        try {
            logger.debug("Finding users for office: {} with role: {}", officeId, roleName);
            
            // Create the request object
            FindUsersByCriteriaRequest request = new FindUsersByCriteriaRequest();
            request.setOfficeId(officeId);
            request.setRoleName(roleName);
            request.setRequestToken(requestToken);
            
            // Create JAXBElement for the request
            JAXBElement<FindUsersByCriteriaRequest> requestElement = 
                new JAXBElement<>(new QName(NAMESPACE, "FindUsersByCriteriaRequest"), 
                                FindUsersByCriteriaRequest.class, request);
            
            // Send request and get response
            @SuppressWarnings("unchecked")
            JAXBElement<FindUserCriteriaResponse> responseElement = 
                (JAXBElement<FindUserCriteriaResponse>) webServiceTemplate.marshalSendAndReceive(requestElement);
            
            FindUserCriteriaResponse response = responseElement.getValue();
            ListType users = response.getUsers();
            
            logger.debug("Found {} users", users != null && users.getListValue() != null ? users.getListValue().size() : 0);
            return users;
            
        } catch (SoapFaultClientException e) {
            logger.error("SOAP fault occurred while finding users by criteria: {}", e.getFaultStringOrReason());
            throw new RuntimeException("Failed to find users by criteria: " + e.getFaultStringOrReason(), e);
        } catch (Exception e) {
            logger.error("Error occurred while finding users by criteria", e);
            throw new RuntimeException("Failed to find users by criteria", e);
        }
    }
    
    /**
     * Get user roles
     */
    public GetUserRolesResponse getUserRoles(UserIdentity userIdentity) {
        try {
            logger.debug("Getting roles for user: {}", userIdentity.getUserLoginName());
            
            // Create the request object
            GetUserRolesRequest request = new GetUserRolesRequest();
            request.setUserIdentity(userIdentity);
            
            // Create JAXBElement for the request
            JAXBElement<GetUserRolesRequest> requestElement = 
                new JAXBElement<>(new QName(NAMESPACE, "GetUserRolesRequest"), 
                                GetUserRolesRequest.class, request);
            
            // Send request and get response
            @SuppressWarnings("unchecked")
            JAXBElement<GetUserRolesResponse> responseElement = 
                (JAXBElement<GetUserRolesResponse>) webServiceTemplate.marshalSendAndReceive(requestElement);
            
            GetUserRolesResponse response = responseElement.getValue();
            
            logger.debug("Found {} roles for user", 
                response.getUserRoles() != null && response.getUserRoles().getListValue() != null ? 
                response.getUserRoles().getListValue().size() : 0);
            
            return response;
            
        } catch (SoapFaultClientException e) {
            logger.error("SOAP fault occurred while getting user roles: {}", e.getFaultStringOrReason());
            throw new RuntimeException("Failed to get user roles: " + e.getFaultStringOrReason(), e);
        } catch (Exception e) {
            logger.error("Error occurred while getting user roles", e);
            throw new RuntimeException("Failed to get user roles", e);
        }
    }
    
    /**
     * Check if service is healthy
     */
    public boolean isHealthy() {
        try {
            logger.debug("Checking service health");
            
            // Create the request object (empty for health check)
            IsHealthy request = new IsHealthy();
            
            // Create JAXBElement for the request
            JAXBElement<IsHealthy> requestElement = 
                new JAXBElement<>(new QName(NAMESPACE, "isHealthy"), IsHealthy.class, request);
            
            // Send request and get response
            @SuppressWarnings("unchecked")
            JAXBElement<IsHealthyResponse> responseElement = 
                (JAXBElement<IsHealthyResponse>) webServiceTemplate.marshalSendAndReceive(requestElement);
            
            boolean result = responseElement.getValue().isReturn();
            logger.debug("Service health check result: {}", result);
            return result;
            
        } catch (SoapFaultClientException e) {
            logger.warn("SOAP fault during health check: {}", e.getFaultStringOrReason());
            return false;
        } catch (Exception e) {
            logger.warn("Error during health check", e);
            return false;
        }
    }
}