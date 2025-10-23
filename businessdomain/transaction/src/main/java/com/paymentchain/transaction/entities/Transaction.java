package com.paymentchain.transaction.entities;

import com.paymentchain.transaction.entities.valueObjects.Channel;
import com.paymentchain.transaction.entities.valueObjects.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String reference;
    private String accountIban;
    private Date dateTime;
    private double amount;
    private double fee;
    private String description;
    private Status status;
    private Channel channel;
}
