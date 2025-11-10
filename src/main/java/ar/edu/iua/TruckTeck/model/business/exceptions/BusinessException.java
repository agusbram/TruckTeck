package ar.edu.iua.TruckTeck.model.business.exceptions;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
* Representa una excepción de negocio dentro de la capa de lógica empresarial.
* <p>
* Esta excepción se utiliza para indicar errores relacionados con las reglas 
* de negocio o condiciones específicas del dominio, que no necesariamente 
* corresponden a fallos técnicos.
* </p>
*
* <p>
* Ejemplos de uso:
* <ul>
*   <li>Validación de datos de entrada en procesos de negocio.</li>
*   <li>Restricciones de negocio no cumplidas.</li>
*   <li>Condiciones específicas que interrumpen el flujo normal de ejecución.</li>
* </ul>
* </p>
*
*/
@NoArgsConstructor
public class BusinessException extends Exception {

    /**
     * Crea una nueva excepción de negocio con un mensaje descriptivo y una causa subyacente.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     * @param ex      Excepción que causó esta excepción.
     */
    @Builder
    public BusinessException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Crea una nueva excepción de negocio con un mensaje descriptivo.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     */
    @Builder
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de negocio a partir de una causa subyacente.
     *
     * @param ex Excepción que causó esta excepción.
     */
    @Builder
    public BusinessException(Throwable ex) {
        super(ex);
    }
}