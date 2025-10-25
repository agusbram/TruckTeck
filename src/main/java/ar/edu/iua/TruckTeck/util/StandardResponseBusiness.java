package ar.edu.iua.TruckTeck.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


/**
 * Implementación de la interfaz {@link IStandardResponseBusiness} que construye 
 * respuestas estándar de la API.
 * <p>
 * Esta clase permite generar instancias de {@link StandardResponse} utilizando un 
 * estado HTTP, una excepción y un mensaje personalizado. Además, respeta la configuración 
 * de la aplicación para incluir información de desarrollo en la respuesta si está habilitada.
 * </p>
 *
 * <p>
 * Anotaciones:
 * <ul>
 *   <li>{@code @Service}: Marca la clase como un componente de servicio de Spring, 
 *       permitiendo su inyección en otros componentes.</li>
 * </ul>
 * </p>
 */
@Service
public class StandardResponseBusiness implements IStandardResponseBusiness{
    /**
     * Indica si la información de desarrollo (stack trace) debe incluirse en las respuestas.
     * <p>
     * Su valor se obtiene del archivo de configuración de la aplicación 
     * mediante la propiedad {@code dev.info.enabled}, con valor por defecto {@code false}.
     * </p>
     */
    @Value("${dev.info.enabled:false}")
    private boolean devInfoEnabled;

    /**
     * Construye un objeto {@link StandardResponse} con los datos proporcionados.
     *
     * @param httpStatus Estado HTTP que se asignará a la respuesta.
     * @param ex Excepción asociada a la respuesta, puede ser {@code null}.
     * @param message Mensaje descriptivo de la respuesta, puede ser {@code null}.
     * @return Una instancia de {@link StandardResponse} con el estado, mensaje y excepción definidos,
     *         y con la información de desarrollo habilitada según la configuración.
     */
    @Override
    public StandardResponse<?> build(HttpStatus httpStatus, Throwable ex, String message) {
        StandardResponse<?> sr = new StandardResponse<>();
        sr.setDevInfoEnabled(devInfoEnabled);
        sr.setMessage(message);
        sr.setHttpStatus(httpStatus);
        sr.setEx(ex);
        return sr;
    }
}