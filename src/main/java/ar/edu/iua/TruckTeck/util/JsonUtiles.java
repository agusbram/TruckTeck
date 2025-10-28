
package ar.edu.iua.TruckTeck.util;

import org.springframework.http.HttpStatus;

/**
 * Interfaz que define el contrato para construir respuestas estándar de la API.
 * <p>
 * Esta interfaz permite generar instancias de {@link StandardResponse} a partir 
 * de un estado HTTP, una excepción y un mensaje personalizado, centralizando 
 * el manejo de respuestas de error o éxito en la aplicación.
 * </p>
 */
public interface IStandardResponseBusiness {

    /**
     * Construye un objeto {@link StandardResponse} utilizando la información proporcionada.
     *
     * @param httpStatus Estado HTTP de la respuesta, representado por {@link HttpStatus}.
     * @param ex Excepción ocurrida durante la operación, puede ser {@code null}.
     * @param message Mensaje descriptivo que acompañará la respuesta, puede ser {@code null}.
     * @return Una instancia de {@link StandardResponse} con la información de estado, mensaje y excepción.
     */
    public StandardResponse build(HttpStatus httpStatus, Throwable ex, String message);
}
