package com.example.flight;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_email", columnNames = {"email"})
})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "name must be alphabetical and less than 50 characters")
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, length = 120)
    private String email;

    @NotBlank
    @Pattern(regexp = "^0\\d{10}$", message = "phonenumber must start with 0 and be 11 digits")
    @Column(nullable = false, length = 11)
    private String phonenumber;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
}