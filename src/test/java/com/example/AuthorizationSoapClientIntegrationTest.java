package com.example;

import com.example.generated.*;
import com.example.service.AuthorizationSoapClientService;
import com.example.service.SoapRequestBuilderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ws.soap.client.SoapFaultClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthorizationSoapClientIntegrationTest {
    
    @Autowired
    private AuthorizationSoapClientService soapClientService;
    
    @Autowired
    private SoapRequestBuilderService requestBuilderService;
    
    @Test
    public void testHealthCheckIntegration() {
        // This test will actually call the SOAP service
        assertDoesNotThrow(() -> {
            boolean healthy = soapClientService.isHealthy();
            // The service should respond (true or false)
            System.out.println("Service health status: " + healthy);
        });
    }
    
    @Test
    public void testFindMatchingUserIdentityIntegration() {
        // Prepare test data
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("username", "testuser");
        searchCriteria.put("email", "test@example.com");
        
        List<MapEntry> mapEntries = requestBuilderService.createMapEntries(searchCriteria);
        
        // This might throw an exception if user is not found - that's expected
        assertDoesNotThrow(() -> {
            try {
                UserIdentity userIdentity = soapClientService.findMatchingUserIdentity(mapEntries);
                if (userIdentity != null) {
                    assertNotNull(userIdentity.getUserLoginName());
                    System.out.println("Found user: " + userIdentity.getUserLoginName());
                } else {
                    System.out.println("No user found matching criteria");
                }
            } catch (RuntimeException e) {
                // Expected if user doesn't exist
                assertTrue(e.getCause() instanceof SoapFaultClientException ||
                          e.getMessage().contains("Failed to find matching user identity"));
                System.out.println("Expected exception: " + e.getMessage());
            }
        });
    }
    
    @Test
    public void testFindOfficesByEauthIdIntegration() {
        // Prepare test data
        String testEauthId = "TEST123";
        List<OfficeType> officeTypes = requestBuilderService.createOfficeTypes("FSA");
        RequestToken requestToken = requestBuilderService.createRequestToken("TestApp", "localhost");
        
        assertDoesNotThrow(() -> {
            try {
                ListType offices = soapClientService.findOfficesByEauthId(testEauthId, officeTypes, requestToken);
                assertNotNull(offices);
                // offices.getListValue() might be empty, but should not be null
                assertNotNull(offices.getListValue());
                System.out.println("Found " + offices.getListValue().size() + " offices");
            } catch (RuntimeException e) {
                // Expected if EAuth ID doesn't exist
                assertTrue(e.getCause() instanceof SoapFaultClientException ||
                          e.getMessage().contains("Failed to find offices"));
                System.out.println("Expected exception: " + e.getMessage());
            }
        });
    }
    
    @Test
    public void testFindUsersByCriteriaIntegration() {
        // Prepare test data
        String officeId = "TEST_OFFICE";
        String roleName = "USER";
        RequestToken requestToken = requestBuilderService.createRequestToken("TestApp", "localhost");
        
        assertDoesNotThrow(() -> {
            try {
                ListType users = soapClientService.findUsersByCriteria(officeId, roleName, requestToken);
                assertNotNull(users);
                assertNotNull(users.getListValue());
                System.out.println("Found " + users.getListValue().size() + " users");
            } catch (RuntimeException e) {
                // Expected if office/role doesn't exist
                assertTrue(e.getCause() instanceof SoapFaultClientException ||
                          e.getMessage().contains("Failed to find users"));
                System.out.println("Expected exception: " + e.getMessage());
            }
        });
    }
    
    @Test
    public void testGetUserRolesIntegration() {
        // Prepare test data
        UserIdentity userIdentity = requestBuilderService.createUserIdentity(
            "TEST_AUTH_SYS",
            "TEST_AUTHZ_SYS",
            "testuser"
        );
        
        assertDoesNotThrow(() -> {
            try {
                GetUserRolesResponse response = soapClientService.getUserRoles(userIdentity);
                assertNotNull(response);
                assertNotNull(response.getUserRoles());
                assertNotNull(response.getUserRoles().getListValue());
                System.out.println("Found " + response.getUserRoles().getListValue().size() + " roles");
            } catch (RuntimeException e) {
                // Expected if user doesn't exist
                assertTrue(e.getCause() instanceof SoapFaultClientException ||
                          e.getMessage().contains("Failed to get user roles"));
                System.out.println("Expected exception: " + e.getMessage());
            }
        });
    }
    
    @Test
    public void testInvalidOfficeTypeHandling() {
        // Test that invalid office types are handled properly
        assertThrows(IllegalArgumentException.class, () -> {
            requestBuilderService.createOfficeTypes("INVALID_OFFICE_TYPE");
        });
    }
    
    @Test
    public void testRequestBuilderService() {
        // Test MapEntry creation
        Map<String, String> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");
        
        List<MapEntry> mapEntries = requestBuilderService.createMapEntries(testMap);
        assertEquals(2, mapEntries.size());
        
        // Test UserIdentity creation
        UserIdentity userIdentity = requestBuilderService.createUserIdentity("auth", "authz", "user");
        assertEquals("auth", userIdentity.getAuthenticationSystemIdentifier());
        assertEquals("authz", userIdentity.getAuthorizationSystemIdentifier());
        assertEquals("user", userIdentity.getUserLoginName());
        
        // Test RequestToken creation
        RequestToken token = requestBuilderService.createRequestToken("app", "host");
        assertEquals("app", token.getApplicationIdentifier());
        assertEquals("host", token.getRequestHost());
        
        // Test valid OfficeType creation
        List<OfficeType> officeTypes = requestBuilderService.createOfficeTypes("FSA", "FLP");
        assertEquals(2, officeTypes.size());
        assertEquals(OfficeType.FSA, officeTypes.get(0));
        assertEquals(OfficeType.FLP, officeTypes.get(1));
        
        // Test validation methods
        assertTrue(requestBuilderService.isValidOfficeType("FSA"));
        assertFalse(requestBuilderService.isValidOfficeType("INVALID"));
        
        List<String> validTypes = requestBuilderService.getValidOfficeTypes();
        assertEquals(4, validTypes.size());
        assertTrue(validTypes.contains("FSA"));
        assertTrue(validTypes.contains("FLP"));
        assertTrue(validTypes.contains("FLPFinance"));
        assertTrue(validTypes.contains("FLPJurisdiction"));
    }
    
    @Test
    public void testAdvancedUserSearch() {
        // Test the advanced user search criteria builder
        List<MapEntry> criteria = requestBuilderService.createUserSearchCriteria(
            "john.doe", "john@example.com", "John", "Doe", "IT"
        );
        
        assertEquals(5, criteria.size());
        
        // Test with some null values
        List<MapEntry> partialCriteria = requestBuilderService.createUserSearchCriteria(
            "jane.doe", null, "Jane", null, null
        );
        
        assertEquals(2, partialCriteria.size());
    }
    
    @Test
    public void testValidationMethods() {
        // Test validated request token creation
        assertDoesNotThrow(() -> {
            RequestToken token = requestBuilderService.createValidatedRequestToken("app", "host");
            assertEquals("app", token.getApplicationIdentifier());
            assertEquals("host", token.getRequestHost());
        });
        
        // Test validation failures
        assertThrows(IllegalArgumentException.class, () -> {
            requestBuilderService.createValidatedRequestToken("", "host");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            requestBuilderService.createValidatedRequestToken("app", null);
        });
        
        // Test validated user identity creation
        assertDoesNotThrow(() -> {
            UserIdentity identity = requestBuilderService.createValidatedUserIdentity("auth", "authz", "user");
            assertEquals("user", identity.getUserLoginName());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            requestBuilderService.createValidatedUserIdentity("auth", "authz", "");
        });
    }
    
    @Test
    public void testListTypeUtilities() {
        // Test hasValues method
        ListType emptyList = new ListType();
        assertFalse(requestBuilderService.hasValues(emptyList));
        assertFalse(requestBuilderService.hasValues(null));
        
        ListType populatedList = new ListType();
        populatedList.getListValue().add("item1");
        populatedList.getListValue().add("item2");
        assertTrue(requestBuilderService.hasValues(populatedList));
        
        // Test getValueCount method
        assertEquals(0, requestBuilderService.getValueCount(emptyList));
        assertEquals(0, requestBuilderService.getValueCount(null));
        assertEquals(2, requestBuilderService.getValueCount(populatedList));
        
        // Test extractListValues method
        List<String> extracted = requestBuilderService.extractListValues(populatedList);
        assertEquals(2, extracted.size());
        assertEquals("item1", extracted.get(0));
        assertEquals("item2", extracted.get(1));
        
        List<String> extractedEmpty = requestBuilderService.extractListValues(emptyList);
        assertTrue(extractedEmpty.isEmpty());
        
        List<String> extractedNull = requestBuilderService.extractListValues(null);
        assertTrue(extractedNull.isEmpty());
    }
}