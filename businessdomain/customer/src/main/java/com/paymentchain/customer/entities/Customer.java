package com.paymentchain.customer.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Customer {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private String firstName;
    private String lastName;
    private String phone;
}
