package com.nsss.pizzamsmsacrustbackend.request;

public class GetUserRequest {
    private String username;

    public GetUserRequest() {
     
    }

    public GetUserRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
