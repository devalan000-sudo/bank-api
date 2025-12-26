package com.lion.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDTO {
    @NotBlank(message = "El numero de cuenta origen es obligatorio")
    private String sourceAccountNumber;
    private String destinationAccountNumber;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser superior a 0")
    @DecimalMin(value = "0.01", message = "La transaccion minima es de 0.01")
    private BigDecimal amount;

    @NotBlank(message = "El tipo de transaccion es requerido")
    private String type;
}
