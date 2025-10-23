package com.paymentchain.transaction.service;

import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.entities.valueObjects.Status;
import com.paymentchain.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public void procesarTransaccion(Transaction transaction) {
        double fee = transaction.getFee();
        if (fee > 0) {
            transaction.setAmount(transaction.getAmount() - fee);
        }
    }

    public void validarMonto(Transaction transaction) {
        if (transaction.getAmount() == 0) {
            throw new IllegalArgumentException("El monto de la transacción no puede ser 0.");
        }
    }

    public void validarFechaYEstado(Transaction transaction) {
        Date fechaActual = new Date();
        if (transaction.getDateTime().after(fechaActual)) {
            transaction.setStatus(Status.PENDIENTE);
        } else {
            transaction.setStatus(Status.LIQUIDADA);
        }
    }

    public Transaction saveTransaction(Transaction transaction) {
        validarMonto(transaction);
        procesarTransaccion(transaction);
        validarFechaYEstado(transaction);
        return transactionRepository.save(transaction);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id).get();
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Transaction updateTransaction(Long id, Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id).get();
        existingTransaction.setAmount(transaction.getAmount());
        existingTransaction.setFee(transaction.getFee());
        existingTransaction.setDateTime(transaction.getDateTime());
        existingTransaction.setDescription(transaction.getDescription());
        existingTransaction.setChannel(transaction.getChannel());
        validarMonto(existingTransaction);
        return transactionRepository.save(existingTransaction);
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public List<Transaction> findByAccountIban(String accountIban) {
        return transactionRepository.findByAccountIban(accountIban);
    }


}