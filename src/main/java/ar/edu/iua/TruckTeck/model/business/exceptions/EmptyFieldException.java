package ar.edu.iua.TruckTeck.model.business.exceptions;

import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmptyFieldException extends Exception {
    /**
     * Crea una nueva excepción de tipo {@code EmptyFieldException} con un mensaje descriptivo
     * y una causa subyacente.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     * @param ex      Excepción que causó esta excepción.
     */
    @Builder
    public EmptyFieldException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Crea una nueva excepción de tipo {@code EmptyFieldException} con un mensaje descriptivo.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     */
    @Builder
    public EmptyFieldException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de tipo {@code EmptyFieldException} a partir de una causa subyacente.
     *
     * @param ex Excepción que causó esta excepción.
     */
    @Builder
    public EmptyFieldException(Throwable ex) {
        super(ex);
    }
}
