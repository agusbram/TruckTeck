package ar.edu.iua.TruckTeck.model.business.exceptions;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Representa una excepción que indica que un recurso o entidad no fue encontrado.
 * <p>
 * Esta excepción se utiliza cuando se intenta acceder, cargar, actualizar o eliminar
 * un recurso que no existe en el sistema.
 * </p>
 *
 * <p>
 * Ejemplos de uso:
 * <ul>
 *   <li>Intentar obtener un producto con un ID inexistente.</li>
 *   <li>Buscar un usuario que no se encuentra registrado.</li>
 *   <li>Acceder a un registro que ha sido eliminado previamente.</li>
 * </ul>
 * </p>
 */
@NoArgsConstructor
public class NotFoundException extends Exception {

    /**
     * Crea una nueva excepción de tipo {@code NotFoundException} con un mensaje descriptivo 
     * y una causa subyacente.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     * @param ex      Excepción que causó esta excepción.
     */
    @Builder
    public NotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Crea una nueva excepción de tipo {@code NotFoundException} con un mensaje descriptivo.
     *
     * @param message Mensaje que describe el motivo de la excepción.
     */
    @Builder
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de tipo {@code NotFoundException} a partir de una causa subyacente.
     *
     * @param ex Excepción que causó esta excepción.
     */
    @Builder
    public NotFoundException(Throwable ex) {
        super(ex);
    }
}