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
    public static final String URL_TRUCKS = URL_BASE + "/trucks";

    /**
     * URL del endpoint de choferes.
     * Construida a partir de {@link #URL_BASE} y la ruta específica "/drivers".
     */
    public static final String URL_DRIVERS = URL_BASE + "/drivers";

    /**
     * URL del endpoint de clientes.
     * Construida a partir de {@link #URL_BASE} y la ruta específica "/drivers".
     */
    public static final String URL_CLIENTS = URL_BASE + "/clients";

    /**
     * URL del endpoint de ordenes.
     * Construida a partir de {@link #URL_BASE} y la ruta específica "/orders".
     */
    public static final String URL_ORDERS = URL_BASE + "/orders";

    /**
     * URL del endpoint de integración TMS (Terminal Manager System - Balanza).
     * Construida a partir de {@link #URL_BASE} y la ruta específica "/tms".
     * Este endpoint es consumido por el sistema externo de balanza para registrar pesajes.
     */
    public static final String URL_TMS = URL_BASE + "/tms";

    /**
     * URL del endpoint de ordenes sap.
     * Construida a partir de {@link #URL_BASE} y la ruta específica "/orders".
     */
    public static final String URL_ORDERS_SAP = URL_ORDERS + "/sap";


    public static final String URL_ORDERS_CHARGING = URL_BASE + "/charging";


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