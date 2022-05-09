package com.nsss.pizzamsmsaorderbackend.request;

public class GetToppingRequest {
    private String name;

    public GetToppingRequest() {
    }

    public GetToppingRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    
}
