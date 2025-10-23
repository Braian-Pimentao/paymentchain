package com.paymentchain.transaction.controllers;

import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionRestController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public List<Transaction> findAll(){
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public Transaction findById(@PathVariable("id") Long id) {
        return transactionService.findById(id);
    }

    @PutMapping("/{id}")
    public Transaction updateTransaction( @PathVariable Long id, Transaction transaction) {
        return transactionService.updateTransaction(id, transaction);
    }

    @PostMapping
    public ResponseEntity<Transaction> saveTransaction(@RequestBody Transaction transaction) {
        Transaction transactionSaved = transactionService.saveTransaction(transaction);
        return ResponseEntity.ok(transactionSaved);
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(Long id) {
        transactionService.deleteTransaction(id);
    }

    @GetMapping("/byAccountIban")
    public List<Transaction> findByAccountIban(@RequestParam("accountIban") String accountIban){
        return transactionService.findByAccountIban(accountIban);
    }



}
