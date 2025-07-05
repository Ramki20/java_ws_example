package com.example.service;

import com.example.generated.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SoapRequestBuilderService {
    
    /**
     * Create MapEntry list from a Map
     */
    public List<MapEntry> createMapEntries(Map<String, String> keyValuePairs) {
        List<MapEntry> mapEntries = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
            MapEntry mapEntry = new MapEntry();
            mapEntry.setKey(entry.getKey());
            mapEntry.setValue(entry.getValue());
            mapEntries.add(mapEntry);
        }
        
        return mapEntries;
    }
    
    /**
     * Create UserIdentity object
     */
    public UserIdentity createUserIdentity(String authSystemId, String authzSystemId, String loginName) {
        UserIdentity userIdentity = new UserIdentity();
        userIdentity.setAuthenticationSystemIdentifier(authSystemId);
        userIdentity.setAuthorizationSystemIdentifier(authzSystemId);
        userIdentity.setUserLoginName(loginName);
        return userIdentity;
    }
    
    /**
     * Create RequestToken object
     */
    public RequestToken createRequestToken(String applicationIdentifier, String requestHost) {
        RequestToken requestToken = new RequestToken();
        requestToken.setApplicationIdentifier(applicationIdentifier);
        requestToken.setRequestHost(requestHost);
        return requestToken;
    }
    
    /**
     * Create list of OfficeType from strings
     */
    public List<OfficeType> createOfficeTypes(String... officeTypeStrings) {
        List<OfficeType> officeTypes = new ArrayList<>();
        
        for (String officeTypeString : officeTypeStrings) {
            try {
                OfficeType officeType = OfficeType.fromValue(officeTypeString);
                officeTypes.add(officeType);
            } catch (IllegalArgumentException e) {
                // Handle invalid office type
                throw new IllegalArgumentException("Invalid office type: " + officeTypeString + 
                    ". Valid values are: FSA, FLP, FLPFinance, FLPJurisdiction");
            }
        }
        
        return officeTypes;
    }
    
    /**
     * Extract string values from ListType
     */
    public List<String> extractListValues(ListType listType) {
        if (listType == null || listType.getListValue() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(listType.getListValue());
    }
    
    /**
     * Create FindMatchingUserIdentityRequest
     */
    public FindMatchingUserIdentityRequest createFindMatchingUserIdentityRequest(Map<String, String> searchCriteria) {
        FindMatchingUserIdentityRequest request = new FindMatchingUserIdentityRequest();
        List<MapEntry> mapEntries = createMapEntries(searchCriteria);
        request.getMapEntry().addAll(mapEntries);
        return request;
    }
    
    /**
     * Create FindOfficesByEauthIdRequest
     */
    public FindOfficesByEauthIdRequest createFindOfficesByEauthIdRequest(String usdaEauthId, 
            List<String> officeTypeStrings, String applicationId, String requestHost) {
        FindOfficesByEauthIdRequest request = new FindOfficesByEauthIdRequest();
        request.setUsdaEauthId(usdaEauthId);
        
        List<OfficeType> officeTypes = createOfficeTypes(officeTypeStrings.toArray(new String[0]));
        request.getOfficeType().addAll(officeTypes);
        
        RequestToken requestToken = createRequestToken(applicationId, requestHost);
        request.setRequestToken(requestToken);
        
        return request;
    }
    
    /**
     * Create FindUsersByCriteriaRequest
     */
    public FindUsersByCriteriaRequest createFindUsersByCriteriaRequest(String officeId, 
            String roleName, String applicationId, String requestHost) {
        FindUsersByCriteriaRequest request = new FindUsersByCriteriaRequest();
        request.setOfficeId(officeId);
        request.setRoleName(roleName);
        
        RequestToken requestToken = createRequestToken(applicationId, requestHost);
        request.setRequestToken(requestToken);
        
        return request;
    }
    
    /**
     * Create GetUserRolesRequest
     */
    public GetUserRolesRequest createGetUserRolesRequest(String authSystemId, 
            String authzSystemId, String loginName) {
        GetUserRolesRequest request = new GetUserRolesRequest();
        UserIdentity userIdentity = createUserIdentity(authSystemId, authzSystemId, loginName);
        request.setUserIdentity(userIdentity);
        return request;
    }
    
    /**
     * Create a complete search criteria request with validation
     */
    public List<MapEntry> createUserSearchCriteria(String username, String email, String firstName, 
                                                  String lastName, String department) {
        Map<String, String> criteria = new java.util.HashMap<>();
        
        if (username != null && !username.trim().isEmpty()) {
            criteria.put("username", username.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            criteria.put("email", email.trim());
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            criteria.put("firstName", firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            criteria.put("lastName", lastName.trim());
        }
        if (department != null && !department.trim().isEmpty()) {
            criteria.put("department", department.trim());
        }
        
        return createMapEntries(criteria);
    }
    
    /**
     * Validate OfficeType string
     */
    public boolean isValidOfficeType(String officeTypeString) {
        try {
            OfficeType.fromValue(officeTypeString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get all valid office type values
     */
    public List<String> getValidOfficeTypes() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add("FSA");
        validTypes.add("FLP");
        validTypes.add("FLPFinance");
        validTypes.add("FLPJurisdiction");
        return validTypes;
    }
    
    /**
     * Create a safe RequestToken with validation
     */
    public RequestToken createValidatedRequestToken(String applicationIdentifier, String requestHost) {
        if (applicationIdentifier == null || applicationIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Application identifier cannot be null or empty");
        }
        if (requestHost == null || requestHost.trim().isEmpty()) {
            throw new IllegalArgumentException("Request host cannot be null or empty");
        }
        
        return createRequestToken(applicationIdentifier.trim(), requestHost.trim());
    }
    
    /**
     * Create a validated UserIdentity
     */
    public UserIdentity createValidatedUserIdentity(String authSystemId, String authzSystemId, String loginName) {
        if (loginName == null || loginName.trim().isEmpty()) {
            throw new IllegalArgumentException("User login name cannot be null or empty");
        }
        
        return createUserIdentity(
            authSystemId != null ? authSystemId.trim() : "",
            authzSystemId != null ? authzSystemId.trim() : "",
            loginName.trim()
        );
    }
    
    /**
     * Check if ListType has any values
     */
    public boolean hasValues(ListType listType) {
        return listType != null && 
               listType.getListValue() != null && 
               !listType.getListValue().isEmpty();
    }
    
    /**
     * Get count of values in ListType
     */
    public int getValueCount(ListType listType) {
        if (!hasValues(listType)) {
            return 0;
        }
        return listType.getListValue().size();
    }
}