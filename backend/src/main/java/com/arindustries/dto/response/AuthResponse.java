package com.arindustries.dto.response;

public class AuthResponse {
    private String token;
    private String username;
    private String fullName;
    private String role;
    private long expiresIn;

    public AuthResponse(String token, String username, String fullName, String role, long expiresIn) {
        this.token = token; this.username = username; this.fullName = fullName;
        this.role = role; this.expiresIn = expiresIn;
    }
    public AuthResponse(String token, String username, String fullName, String role) {
        this(token, username, fullName, role, 86400000L);
    }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public long getExpiresIn() { return expiresIn; }
}
