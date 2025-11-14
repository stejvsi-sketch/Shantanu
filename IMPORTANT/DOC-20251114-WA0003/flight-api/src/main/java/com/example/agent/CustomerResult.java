package com.example.agent;

public class CustomerResult {
    public Long id;
    public String name;
    public String email;
    public String phonenumber;

    public CustomerResult() {}

    public CustomerResult(Long id, String name, String email, String phonenumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phonenumber = phonenumber;
    }
}