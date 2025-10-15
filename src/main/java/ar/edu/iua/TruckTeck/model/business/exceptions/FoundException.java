package ar.edu.iua.TruckTeck.model.business.exceptions;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Representa una excepción que indica que un recurso o entidad ya existe 
 * o fue encontrado cuando no se esperaba.
 * <p>
 * Esta excepción puede utilizarse en situaciones donde, por ejemplo, 
 * se intenta crear un recurso que ya está registrado en el sistema 
 * o cuando se detecta una duplicidad en los datos.
 * </p>
 *
 * <p>
 * Ejemplos de uso:
 * <ul>
 *   <li>Intentar registrar un usuario con un correo ya existente.</li>
 *   <li>Crear una entidad en la base de datos con una clave duplicada.</li>
 *   <li>Detectar un conflicto por existencia previa de un recurso.</li>
 * </ul>
 * </p>
 *
 * @author  
 */
@NoArgsConstructor
public class FoundException extends Exception {

    /**
     * Crea una nueva excepción de tipo {@code FoundException} con un mensaje descriptivo 
     * y una causa subyacente.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     * @param ex      Excepción que causó esta excepción.
     */
    @Builder
    public FoundException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Crea una nueva excepción de tipo {@code FoundException} con un mensaje descriptivo.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     */
    @Builder
    public FoundException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de tipo {@code FoundException} a partir de una causa subyacente.
     *
     * @param ex Excepción que causó esta excepción.
     */
    @Builder
    public FoundException(Throwable ex) {
        super(ex);
    }
}
