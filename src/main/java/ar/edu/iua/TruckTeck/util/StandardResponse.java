package ar.edu.iua.TruckTeck.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa una respuesta estándar de la API.
 * <p>
 * Esta clase se utiliza para unificar la estructura de las respuestas, 
 * tanto en operaciones exitosas como en errores. Permite incluir información 
 * de desarrollo de manera opcional para depuración, y puede ocultar excepciones 
 * sensibles en producción mediante la anotación {@code @JsonIgnore}.
 * </p>
 *
 * <p>
 * Anotaciones:
 * <ul>
 *   <li>{@code @NoArgsConstructor}: Genera un constructor sin argumentos.</li>
 *   <li>{@code @AllArgsConstructor}: Genera un constructor con todos los atributos.</li>
 *   <li>{@code @Getter} / {@code @Setter}: Genera métodos getter y setter para todos los atributos.</li>
 * </ul>
 * </p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StandardResponse<T> {
    /**
     * Mensaje descriptivo de la respuesta.
     * <p>
     * Si se establece, se devolverá como mensaje principal; si es {@code null}, 
     * se utilizará el mensaje de la excepción {@link #ex} si está presente.
     * </p>
     */
    private String message;

    /**
     * Datos de la respuesta (payload).
     * Se incluye en respuestas exitosas que devuelven información.
     */
    private T data;

    /**
     * Excepción asociada a la respuesta, si la hubiera.
     * <p>
     * Se ignora durante la serialización JSON para no exponer detalles sensibles.
     * </p>
     */
    @JsonIgnore
    private Throwable ex;

    /**
     * Estado HTTP de la respuesta.
     * <p>
     * Se ignora durante la serialización JSON. Puede obtenerse su código mediante {@link #getCode()}.
     * </p>
     */
    @JsonIgnore
    private HttpStatus httpStatus;

    /**
     * Obtiene el código numérico correspondiente al {@link #httpStatus}.
     *
     * @return Código HTTP de la respuesta.
     */
    public int getCode() {
        return httpStatus.value();
    }
    
    /**
     * Indica si la información de desarrollo (stack trace) está habilitada.
     * <p>
     * Permite incluir detalles técnicos para depuración en entornos de desarrollo.
     * Se ignora en la serialización JSON.
     * </p>
     */
    @JsonIgnore
    private boolean devInfoEnabled;

    /**
     * Obtiene la información de desarrollo (stack trace) si {@link #devInfoEnabled} está habilitado.
     *
     * @return Stack trace de la excepción como cadena, o {@code null} si {@link #devInfoEnabled} es {@code false}.
     */
    public String getDevInfo() {
        if(devInfoEnabled) {
            if(ex != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                return sw.toString();
            } else {
                return "No stack trace";
            }
        } else {
            return null;
        }
    }

    /**
     * Obtiene el mensaje principal de la respuesta.
     * <p>
     * Se devuelve {@link #message} si está definido, de lo contrario se devuelve
     * {@link Throwable#getMessage()} de {@link #ex}, o {@code null} si ambos son {@code null}.
     * </p>
     *
     * @return Mensaje de la respuesta.
     */
    public String getMessage() {
        if(message != null) 
            return message;
        if(ex != null) 
            return ex.getMessage();
        return null;
    }

}