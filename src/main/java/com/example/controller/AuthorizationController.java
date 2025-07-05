package com.example.controller;

import com.example.generated.*;
import com.example.service.AuthorizationSoapClientService;
import com.example.service.SoapRequestBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/authorization")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthorizationController {
    
    private final AuthorizationSoapClientService soapClientService;
    private final SoapRequestBuilderService requestBuilderService;
    
    @Autowired
    public AuthorizationController(AuthorizationSoapClientService soapClientService,
                                 SoapRequestBuilderService requestBuilderService) {
        this.soapClientService = soapClientService;
        this.requestBuilderService = requestBuilderService;
    }
    
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        boolean healthy = soapClientService.isHealthy();
        return ResponseEntity.ok(new HealthResponse(healthy, healthy ? "Service is healthy" : "Service is not healthy"));
    }
    
    @PostMapping("/find-user-identity")
    public ResponseEntity<UserIdentityResponse> findUserIdentity(@RequestBody Map<String, String> searchCriteria) {
        List<MapEntry> mapEntries = requestBuilderService.createMapEntries(searchCriteria);
        UserIdentity userIdentity = soapClientService.findMatchingUserIdentity(mapEntries);
        
        UserIdentityResponse response = new UserIdentityResponse();
        if (userIdentity != null) {
            response.setFound(true);
            response.setUserIdentity(userIdentity);
            response.setMessage("User identity found successfully");
        } else {
            response.setFound(false);
            response.setMessage("No matching user identity found");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/find-offices")
    public ResponseEntity<OfficesResponse> findOffices(
            @RequestParam String eauthId,
            @RequestParam List<String> officeTypes,
            @RequestParam String applicationId,
            @RequestParam String requestHost) {
        
        // Validate office types
        for (String officeType : officeTypes) {
            if (!requestBuilderService.isValidOfficeType(officeType)) {
                return ResponseEntity.badRequest()
                    .body(new OfficesResponse(false, null, 
                        "Invalid office type: " + officeType + ". Valid types: " + 
                        requestBuilderService.getValidOfficeTypes()));
            }
        }
        
        List<OfficeType> officeTypeList = requestBuilderService.createOfficeTypes(
            officeTypes.toArray(new String[0])
        );
        RequestToken requestToken = requestBuilderService.createValidatedRequestToken(applicationId, requestHost);
        
        ListType offices = soapClientService.findOfficesByEauthId(eauthId, officeTypeList, requestToken);
        List<String> officeList = requestBuilderService.extractListValues(offices);
        
        OfficesResponse response = new OfficesResponse();
        response.setFound(!officeList.isEmpty());
        response.setOffices(officeList);
        response.setMessage(officeList.isEmpty() ? "No offices found for the given criteria" : 
                           "Found " + officeList.size() + " office(s)");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/find-users")
    public ResponseEntity<UsersResponse> findUsers(
            @RequestParam String officeId,
            @RequestParam String roleName,
            @RequestParam String applicationId,
            @RequestParam String requestHost) {
        
        RequestToken requestToken = requestBuilderService.createValidatedRequestToken(applicationId, requestHost);
        
        ListType users = soapClientService.findUsersByCriteria(officeId, roleName, requestToken);
        List<String> userList = requestBuilderService.extractListValues(users);
        
        UsersResponse response = new UsersResponse();
        response.setFound(!userList.isEmpty());
        response.setUsers(userList);
        response.setMessage(userList.isEmpty() ? "No users found for the given criteria" : 
                           "Found " + userList.size() + " user(s)");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/get-user-roles")
    public ResponseEntity<UserRolesResponse> getUserRoles(@RequestBody UserIdentityRequest userIdentityRequest) {
        UserIdentity userIdentity = requestBuilderService.createValidatedUserIdentity(
            userIdentityRequest.getAuthenticationSystemIdentifier(),
            userIdentityRequest.getAuthorizationSystemIdentifier(),
            userIdentityRequest.getUserLoginName()
        );
        
        GetUserRolesResponse rolesResponse = soapClientService.getUserRoles(userIdentity);
        List<String> roleList = requestBuilderService.extractListValues(rolesResponse.getUserRoles());
        
        UserRolesResponse response = new UserRolesResponse();
        response.setUserIdentity(rolesResponse.getUserIdentity() != null ? rolesResponse.getUserIdentity() : userIdentity);
        response.setRoles(roleList);
        response.setMessage(roleList.isEmpty() ? "No roles found for user" : 
                           "Found " + roleList.size() + " role(s) for user");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/advanced-user-search")
    public ResponseEntity<UserIdentityResponse> advancedUserSearch(@RequestBody AdvancedUserSearchRequest searchRequest) {
        List<MapEntry> mapEntries = requestBuilderService.createUserSearchCriteria(
            searchRequest.getUsername(),
            searchRequest.getEmail(),
            searchRequest.getFirstName(),
            searchRequest.getLastName(),
            searchRequest.getDepartment()
        );
        
        if (mapEntries.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new UserIdentityResponse(false, null, "At least one search criteria must be provided"));
        }
        
        UserIdentity userIdentity = soapClientService.findMatchingUserIdentity(mapEntries);
        
        UserIdentityResponse response = new UserIdentityResponse();
        if (userIdentity != null) {
            response.setFound(true);
            response.setUserIdentity(userIdentity);
            response.setMessage("User identity found successfully");
        } else {
            response.setFound(false);
            response.setMessage("No matching user identity found");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/office-types")
    public ResponseEntity<List<String>> getValidOfficeTypes() {
        return ResponseEntity.ok(requestBuilderService.getValidOfficeTypes());
    }
    
    // Response DTOs
    public static class HealthResponse {
        private boolean healthy;
        private String message;
        
        public HealthResponse() {}
        
        public HealthResponse(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
        }
        
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class UserIdentityResponse {
        private boolean found;
        private UserIdentity userIdentity;
        private String message;
        
        public UserIdentityResponse() {}
        
        public UserIdentityResponse(boolean found, UserIdentity userIdentity, String message) {
            this.found = found;
            this.userIdentity = userIdentity;
            this.message = message;
        }
        
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
        public UserIdentity getUserIdentity() { return userIdentity; }
        public void setUserIdentity(UserIdentity userIdentity) { this.userIdentity = userIdentity; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class OfficesResponse {
        private boolean found;
        private List<String> offices;
        private String message;
        
        public OfficesResponse() {}
        
        public OfficesResponse(boolean found, List<String> offices, String message) {
            this.found = found;
            this.offices = offices;
            this.message = message;
        }
        
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
        public List<String> getOffices() { return offices; }
        public void setOffices(List<String> offices) { this.offices = offices; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class UsersResponse {
        private boolean found;
        private List<String> users;
        private String message;
        
        public UsersResponse() {}
        
        public UsersResponse(boolean found, List<String> users, String message) {
            this.found = found;
            this.users = users;
            this.message = message;
        }
        
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
        public List<String> getUsers() { return users; }
        public void setUsers(List<String> users) { this.users = users; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class UserRolesResponse {
        private UserIdentity userIdentity;
        private List<String> roles;
        private String message;
        
        public UserRolesResponse() {}
        
        public UserRolesResponse(UserIdentity userIdentity, List<String> roles, String message) {
            this.userIdentity = userIdentity;
            this.roles = roles;
            this.message = message;
        }
        
        public UserIdentity getUserIdentity() { return userIdentity; }
        public void setUserIdentity(UserIdentity userIdentity) { this.userIdentity = userIdentity; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class UserIdentityRequest {
        private String authenticationSystemIdentifier;
        private String authorizationSystemIdentifier;
        private String userLoginName;
        
        public String getAuthenticationSystemIdentifier() {
            return authenticationSystemIdentifier;
        }
        
        public void setAuthenticationSystemIdentifier(String authenticationSystemIdentifier) {
            this.authenticationSystemIdentifier = authenticationSystemIdentifier;
        }
        
        public String getAuthorizationSystemIdentifier() {
            return authorizationSystemIdentifier;
        }
        
        public void setAuthorizationSystemIdentifier(String authorizationSystemIdentifier) {
            this.authorizationSystemIdentifier = authorizationSystemIdentifier;
        }
        
        public String getUserLoginName() {
            return userLoginName;
        }
        
        public void setUserLoginName(String userLoginName) {
            this.userLoginName = userLoginName;
        }
    }
    
    public static class AdvancedUserSearchRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String department;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }
}