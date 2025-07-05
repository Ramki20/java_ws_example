package com.example.service;

import com.example.generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

import java.util.List;

@Service
public class AuthorizationSoapClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationSoapClientService.class);
    
    private final WebServiceTemplate webServiceTemplate;
    
    @Autowired
    public AuthorizationSoapClientService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }
    
    /**
     * Find matching user identity using map entries
     */
    public UserIdentity findMatchingUserIdentity(List<MapEntry> mapEntries) {
        try {
            logger.debug("Finding matching user identity with {} map entries", mapEntries.size());
            
            // Create the request object using generated classes - send the element directly
            FindMatchingUserIdentityRequest request = new FindMatchingUserIdentityRequest();
            request.getMapEntry().addAll(mapEntries);
            
            // Send the request element directly (not wrapped in JAXBElement)
            FindMatchingUserIdentityResponse response = 
                (FindMatchingUserIdentityResponse) webServiceTemplate.marshalSendAndReceive(request);
            
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
            
            // Create the request object - send directly
            FindOfficesByEauthIdRequest request = new FindOfficesByEauthIdRequest();
            request.setUsdaEauthId(usdaEauthId);
            request.getOfficeType().addAll(officeTypes);
            request.setRequestToken(requestToken);
            
            // Send request directly
            FindOfficesByEauthIdResponse response = 
                (FindOfficesByEauthIdResponse) webServiceTemplate.marshalSendAndReceive(request);
            
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
            
            // Create the request object - send directly
            FindUsersByCriteriaRequest request = new FindUsersByCriteriaRequest();
            request.setOfficeId(officeId);
            request.setRoleName(roleName);
            request.setRequestToken(requestToken);
            
            // Send request directly
            FindUserCriteriaResponse response = 
                (FindUserCriteriaResponse) webServiceTemplate.marshalSendAndReceive(request);
            
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
            
            // Create the request object - send directly
            GetUserRolesRequest request = new GetUserRolesRequest();
            request.setUserIdentity(userIdentity);
            
            // Send request directly
            GetUserRolesResponse response = 
                (GetUserRolesResponse) webServiceTemplate.marshalSendAndReceive(request);
            
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
            
            // Create the request object - send directly
            IsHealthy request = new IsHealthy();
            
            // Send request directly
            IsHealthyResponse response = 
                (IsHealthyResponse) webServiceTemplate.marshalSendAndReceive(request);
            
            boolean result = response.isReturn();
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