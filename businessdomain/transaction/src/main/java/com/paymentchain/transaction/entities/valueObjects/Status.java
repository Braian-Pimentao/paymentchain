package com.paymentchain.transaction.entities.valueObjects;

import lombok.Getter;

@Getter
public enum Status {
    PENDIENTE(01),
    LIQUIDADA(02),
    RECHAZADA(03),
    CANCELADA(04);

    private final int codigo;

    Status(int codigo) {
        this.codigo = codigo;
    }
}
