package ar.edu.iua.TruckTeck.controllers;

/**
 * Clase que contiene las constantes utilizadas para definir las URLs de la API.
 * <p>
 * Esta clase centraliza las rutas base y específicas de los endpoints, 
 * facilitando su mantenimiento y evitando duplicación de cadenas literales
 * en el código.
 * </p>
 */
public class Constants {
    /**
     * URL base principal de la API.
     * Representa el prefijo común para todos los endpoints de la API.
     */
    public static final String URL_API = "/api";

    /**
     * Versión de la API.
     * Se utiliza para versionar los endpoints de la API y soportar cambios futuros.
    */
    public static final String URL_API_VERSION = "/v1";

    /**
     * URL base completa combinando {@link #URL_API} y {@link #URL_API_VERSION}.
     * Esta constante se utiliza como prefijo para todos los endpoints versionados.
     */
    public static final String URL_BASE = URL_API + URL_API_VERSION;

    /**
     * URL del endpoint de productos.
     * Construida a partir de {@link #URL_BASE} y la ruta específica "/products".
     */
    public static final String URL_PRODUCTS = URL_BASE + "/products";

    /**
     * URL del endpoint de productos.
     * Construida a partir de {@link #URL_BASE} y la ruta específica "/products".
     */
    public static final String URL_TRUCKS = URL_BASE + "/truks";

    /**
    * URL del endpoint de integración.
    * Construida a partir de {@link #URL_BASE} y la ruta específica "/integration".
    */
    // public static final String URL_INTEGRATION = URL_BASE + "/integration";

    /**
    * URL del endpoint de integración para el cliente 1.
    * Construida a partir de {@link #URL_INTEGRATION} y la ruta específica "/cli1".
    */
	// public static final String URL_INTEGRATION_CLI1 = URL_INTEGRATION + "/cli1";

    /**
    * URL del endpoint de integración para el cliente 2.
    * Construida a partir de {@link #URL_INTEGRATION} y la ruta específica "/cli2".
    */
	// public static final String URL_INTEGRATION_CLI2 = URL_INTEGRATION + "/cli2";

    // public static final String URL_LOGIN = URL_BASE + "/login";
}