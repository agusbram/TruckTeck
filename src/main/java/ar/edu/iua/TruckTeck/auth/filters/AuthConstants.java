package ar.edu.iua.TruckTeck.auth.filters;

/**
 * Clase de constantes utilizadas en el proceso de autenticación y generación de tokens JWT.
 * <p>
 * Esta clase centraliza los valores comunes empleados durante la creación, validación
 * y transporte de los tokens de autenticación, garantizando consistencia en el uso
 * de parámetros dentro del sistema de seguridad.
 * </p>
 * 
 * <p><b>Autor:</b> Equipo IW3 - Universidad Argentina</p>
 * <p><b>Versión:</b> 1.0.0</p>
 */
public final class AuthConstants {
    /**
     * Tiempo de expiración del token JWT en milisegundos.
     * <p>
     * Por defecto, equivale a 1 hora (60 minutos × 60 segundos × 1000 milisegundos).
     * </p>
     */
	public static final long EXPIRATION_TIME = (60 * 60 * 1000);

    /**
     * Clave secreta utilizada para firmar los tokens JWT.
     * <p>
     * Esta clave debe mantenerse protegida y nunca exponerse públicamente,
     * ya que su filtración comprometería la seguridad del sistema.
     * </p>
     */
	public static final String SECRET = "MyVerySecretKey_9fA7xQ3pL2tB8rS6wM4zN1cH";
	
    /**
     * Nombre del encabezado HTTP donde se espera recibir el token de autenticación.
     * <p>Ejemplo: <code>Authorization: Bearer &lt;token&gt;</code></p>
     */
	public static final String AUTH_HEADER_NAME = "Authorization";

    /**
     * Nombre del parámetro HTTP alternativo para recibir el token JWT en solicitudes
     * donde no se utilicen encabezados (por ejemplo, en peticiones GET).
     */
	public static final String AUTH_PARAM_NAME = "authtoken";

    /**
     * Prefijo utilizado para identificar el tipo de token en el encabezado HTTP.
     * <p>Ejemplo: <code>Bearer &lt;token&gt;</code></p>
     */
	public static final String TOKEN_PREFIX = "Bearer ";
}
