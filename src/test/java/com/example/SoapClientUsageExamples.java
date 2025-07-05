package com.example;

import com.example.generated.*;
import com.example.service.AuthorizationSoapClientService;
import com.example.service.SoapRequestBuilderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example usage and test cases for the SOAP client
 */
@SpringBootTest
public class SoapClientUsageExamples {
    
    @Autowired
    private AuthorizationSoapClientService soapClientService;
    
    @Autowired
    private SoapRequestBuilderService requestBuilderService;
    
    @Test
    public void testHealthCheck() {
        boolean healthy = soapClientService.isHealthy();
        System.out.println("Service is healthy: " + healthy);
    }
    
    @Test
    public void testFindMatchingUserIdentity() {
        // Create search criteria
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("username", "john.doe");
        searchCriteria.put("email", "john.doe@example.com");
        
        // Convert to MapEntry list
        List<MapEntry> mapEntries = requestBuilderService.createMapEntries(searchCriteria);
        
        // Call service
        try {
            UserIdentity userIdentity = soapClientService.findMatchingUserIdentity(mapEntries);
            
            if (userIdentity != null) {
                System.out.println("Found user: " + userIdentity.getUserLoginName());
                System.out.println("Auth System ID: " + userIdentity.getAuthenticationSystemIdentifier());
                System.out.println("Authz System ID: " + userIdentity.getAuthorizationSystemIdentifier());
            } else {
                System.out.println("No user found matching the criteria");
            }
        } catch (RuntimeException e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
    }
    
    @Test
    public void testFindOfficesByEauthId() {
        // Create request parameters
        String eauthId = "12345";
        List<OfficeType> officeTypes = requestBuilderService.createOfficeTypes("FSA", "FLP");
        RequestToken requestToken = requestBuilderService.createRequestToken("MyApp", "localhost");
        
        // Call service
        try {
            ListType offices = soapClientService.findOfficesByEauthId(eauthId, officeTypes, requestToken);
            List<String> officeList = requestBuilderService.extractListValues(offices);
            
            System.out.println("Found offices: " + officeList);
        } catch (RuntimeException e) {
            System.out.println("Error finding offices: " + e.getMessage());
        }
    }
    
    @Test
    public void testFindUsersByCriteria() {
        // Create request parameters
        String officeId = "OFFICE123";
        String roleName = "ADMIN";
        RequestToken requestToken = requestBuilderService.createRequestToken("MyApp", "localhost");
        
        // Call service
        try {
            ListType users = soapClientService.findUsersByCriteria(officeId, roleName, requestToken);
            List<String> userList = requestBuilderService.extractListValues(users);
            
            System.out.println("Found users: " + userList);
        } catch (RuntimeException e) {
            System.out.println("Error finding users: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetUserRoles() {
        // Create user identity
        UserIdentity userIdentity = requestBuilderService.createUserIdentity(
            "AUTH_SYSTEM_123",
            "AUTHZ_SYSTEM_456", 
            "john.doe"
        );
        
        // Call service
        try {
            GetUserRolesResponse response = soapClientService.getUserRoles(userIdentity);
            
            List<String> roles = requestBuilderService.extractListValues(response.getUserRoles());
            System.out.println("User roles: " + roles);
            
            if (response.getUserIdentity() != null) {
                System.out.println("Updated user identity: " + response.getUserIdentity().getUserLoginName());
            }
        } catch (RuntimeException e) {
            System.out.println("Error getting user roles: " + e.getMessage());
        }
    }
    
    @Test
    public void testAdvancedUserSearch() {
        // Test advanced user search with multiple criteria
        List<MapEntry> criteria = requestBuilderService.createUserSearchCriteria(
            "jane.smith", 
            "jane.smith@example.com", 
            "Jane", 
            "Smith", 
            "Engineering"
        );
        
        try {
            UserIdentity userIdentity = soapClientService.findMatchingUserIdentity(criteria);
            
            if (userIdentity != null) {
                System.out.println("Advanced search found user: " + userIdentity.getUserLoginName());
            } else {
                System.out.println("Advanced search: No user found");
            }
        } catch (RuntimeException e) {
            System.out.println("Advanced search error: " + e.getMessage());
        }
    }
    
    @Test
    public void testValidationFeatures() {
        // Test office type validation
        System.out.println("Valid office types: " + requestBuilderService.getValidOfficeTypes());
        
        System.out.println("Is 'FSA' valid office type? " + requestBuilderService.isValidOfficeType("FSA"));
        System.out.println("Is 'INVALID' valid office type? " + requestBuilderService.isValidOfficeType("INVALID"));
        
        // Test validated request token creation
        try {
            RequestToken validToken = requestBuilderService.createValidatedRequestToken("MyApp", "localhost");
            System.out.println("Created valid token for app: " + validToken.getApplicationIdentifier());
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
        
        // Test validation failure
        try {
            requestBuilderService.createValidatedRequestToken("", "localhost");
        } catch (IllegalArgumentException e) {
            System.out.println("Expected validation error: " + e.getMessage());
        }
    }
    
    @Test
    public void testListTypeUtilities() {
        // Create a sample ListType
        ListType sampleList = new ListType();
        sampleList.getListValue().add("Item 1");
        sampleList.getListValue().add("Item 2");
        sampleList.getListValue().add("Item 3");
        
        // Test utility methods
        System.out.println("List has values: " + requestBuilderService.hasValues(sampleList));
        System.out.println("List value count: " + requestBuilderService.getValueCount(sampleList));
        
        List<String> extractedValues = requestBuilderService.extractListValues(sampleList);
        System.out.println("Extracted values: " + extractedValues);
        
        // Test with empty list
        ListType emptyList = new ListType();
        System.out.println("Empty list has values: " + requestBuilderService.hasValues(emptyList));
        System.out.println("Empty list value count: " + requestBuilderService.getValueCount(emptyList));
    }
    
    @Test
    public void testCompleteWorkflow() {
        System.out.println("\n=== Complete Workflow Example ===");
        
        // 1. Health check
        System.out.println("1. Checking service health...");
        boolean healthy = soapClientService.isHealthy();
        System.out.println("   Service is healthy: " + healthy);
        
        if (!healthy) {
            System.out.println("   Service is not healthy, skipping workflow");
            return;
        }
        
        // 2. Search for a user
        System.out.println("\n2. Searching for user...");
        Map<String, String> userCriteria = new HashMap<>();
        userCriteria.put("username", "admin");
        userCriteria.put("department", "IT");
        
        List<MapEntry> userSearch = requestBuilderService.createMapEntries(userCriteria);
        
        try {
            UserIdentity foundUser = soapClientService.findMatchingUserIdentity(userSearch);
            
            if (foundUser != null) {
                System.out.println("   Found user: " + foundUser.getUserLoginName());
                
                // 3. Get user roles
                System.out.println("\n3. Getting user roles...");
                GetUserRolesResponse rolesResponse = soapClientService.getUserRoles(foundUser);
                List<String> roles = requestBuilderService.extractListValues(rolesResponse.getUserRoles());
                System.out.println("   User roles: " + roles);
                
                // 4. Find offices for this user (simulate with EAuth ID)
                System.out.println("\n4. Finding offices...");
                List<OfficeType> officeTypes = requestBuilderService.createOfficeTypes("FSA");
                RequestToken token = requestBuilderService.createRequestToken("WorkflowApp", "localhost");
                
                ListType offices = soapClientService.findOfficesByEauthId("EAUTH123", officeTypes, token);
                List<String> officeList = requestBuilderService.extractListValues(offices);
                System.out.println("   Found offices: " + officeList);
                
            } else {
                System.out.println("   No user found matching criteria");
            }
            
        } catch (RuntimeException e) {
            System.out.println("   Workflow error: " + e.getMessage());
        }
        
        System.out.println("\n=== Workflow Complete ===");
    }
}

/**
 * Example of how to use the service in a business logic class
 */
class BusinessLogicService {
    
    private final AuthorizationSoapClientService soapClientService;
    private final SoapRequestBuilderService requestBuilderService;
    
    public BusinessLogicService(AuthorizationSoapClientService soapClientService,
                              SoapRequestBuilderService requestBuilderService) {
        this.soapClientService = soapClientService;
        this.requestBuilderService = requestBuilderService;
    }
    
    public List<String> getOfficesForUser(String eauthId, String applicationId) {
        try {
            // Create office types - you can customize based on your needs
            List<OfficeType> officeTypes = requestBuilderService.createOfficeTypes("FSA", "FLP");
            
            // Create request token
            RequestToken requestToken = requestBuilderService.createRequestToken(applicationId, "business-service");
            
            // Get offices
            ListType offices = soapClientService.findOfficesByEauthId(eauthId, officeTypes, requestToken);
            return requestBuilderService.extractListValues(offices);
            
        } catch (Exception e) {
            // Handle error appropriately
            throw new RuntimeException("Failed to get offices for user: " + eauthId, e);
        }
    }
    
    public UserIdentity authenticateUser(String username, String email) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put("username", username);
        criteria.put("email", email);
        
        List<MapEntry> mapEntries = requestBuilderService.createMapEntries(criteria);
        return soapClientService.findMatchingUserIdentity(mapEntries);
    }
    
    public boolean hasRole(UserIdentity userIdentity, String requiredRole) {
        GetUserRolesResponse response = soapClientService.getUserRoles(userIdentity);
        List<String> userRoles = requestBuilderService.extractListValues(response.getUserRoles());
        return userRoles.contains(requiredRole);
    }
    
    public boolean isServiceAvailable() {
        return soapClientService.isHealthy();
    }
}