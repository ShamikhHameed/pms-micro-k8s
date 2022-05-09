package com.nsss.pizzamsmsacrustbackend.request;

public class GetCrustRequest {
    private String name;

    public GetCrustRequest() {
    }

    public GetCrustRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }   
}