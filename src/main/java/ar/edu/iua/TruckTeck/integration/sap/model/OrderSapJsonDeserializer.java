package ar.edu.iua.TruckTeck.integration.sap.model;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ar.edu.iua.TruckTeck.model.Client;
import ar.edu.iua.TruckTeck.model.Driver;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.Product;
import ar.edu.iua.TruckTeck.model.Truck;
import ar.edu.iua.TruckTeck.util.JsonUtiles;

public class OrderSapJsonDeserializer extends StdDeserializer<Order> {
    /**
     * Constructor protegido requerido por Jackson.
     *
     * @param vc Clase de la entidad a deserializar.
     */
	public OrderSapJsonDeserializer(Class<?> vc) {
		super(vc);
	}
    
    /**
    * Deserializa un objeto JSON en una instancia de {@link Order}.
    *
    * <p>Este método interpreta un nodo JSON y construye un objeto {@link Order}
    * completo, incluyendo sus entidades relacionadas ({@link Driver}, {@link Client},
    * {@link Truck} y {@link Product}), utilizando los métodos auxiliares definidos
    * en la clase {@link JsonUtiles}.</p>
    *
    * <p>Durante el proceso de deserialización, se contempla la posibilidad de que
    * los campos del JSON tengan nombres alternativos (por ejemplo, <i>"order"</i>,
    * <i>"number"</i>, <i>"order_number"</i> para el número de orden). El primer
    * atributo encontrado será el utilizado.</p>
    *
    * <p>Si un nodo requerido no se encuentra, se asignan valores por defecto:
    * <ul>
    *   <li>Valores numéricos → 0 o 0.0</li>
    *   <li>Cadenas → {@code null}</li>
    *   <li>Fechas → {@link LocalDateTime#now()}</li>
    * </ul></p>
    *
    * @param jp   El {@link JsonParser} utilizado por Jackson para leer el contenido JSON.
    * @param ctxt El contexto de deserialización de Jackson, que puede proporcionar
    *             configuración adicional o manejo de excepciones.
    * @return Una instancia completamente inicializada de {@link Order}.
    * @throws IOException        Si ocurre un error al leer o procesar el flujo JSON.
    * @throws JacksonException   Si el formato JSON es inválido o inconsistente con la estructura esperada.
    *
    * @see JsonUtiles#getString(JsonNode, String[], String)
    * @see JsonUtiles#getLong(JsonNode, String[], Long)
    * @see JsonUtiles#getInt(JsonNode, String[], int)
    * @see JsonUtiles#getDouble(JsonNode, String[], Double)
    * @see JsonUtiles#getLocalDateTime(JsonNode, String[], LocalDateTime)
    */
    public Order deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        Order order = new Order();
        JsonNode node = jp.getCodec().readTree(jp);
        
        String number = JsonUtiles.getString(node, "order,number,order_number".split(","), null);
        String externalCode = JsonUtiles.getString(node, "external_code,code,externalCode".split(","), null);
        
        // Deserializacion de Driver
        JsonNode nodeAux = JsonUtiles.getNode(node, "driver,chofer,conductor".split(","), null);

        Driver driver = new Driver();
        driver.setId(JsonUtiles.getLong(nodeAux,"driver_id,id_driver".split(","), 0L));
        driver.setName(JsonUtiles.getString(nodeAux,"name,nombre".split(","), null));
        driver.setSurname(JsonUtiles.getString(nodeAux,"surname,apellido".split(","), null));
        driver.setDocumentNumber(JsonUtiles.getString(nodeAux,"document_number,dni,documento,documentNumber".split(","), null));
        driver.setExternalCode(JsonUtiles.getString(nodeAux,"externalCodeDriver,external_code_driver,codigo_sap_driver,external_code".split(","), null));


        // Deserializacion de Client
        nodeAux = JsonUtiles.getNode(node, "client,cliente".split(","), null);
        Client client = new Client();
        client.setId(JsonUtiles.getLong(nodeAux,"client_id,id_client".split(","), 0L));
        client.setCompanyName(JsonUtiles.getString(nodeAux,"name,nombre,company_name,name_company,nombre_compania,compania_nombre,companyName".split(","), null));
        client.setContactName(JsonUtiles.getString(nodeAux,"contact_name,contacto,contact,name_contact,contactName".split(","), null));
        client.setExternalCode(JsonUtiles.getString(nodeAux,"externalCodeClient,external_code_client,codigo_sap_client,external_code".split(","), null));

        // Deserializacion de Truck
        nodeAux = JsonUtiles.getNode(node, "truck,camion".split(","), null);
        Truck truck = new Truck();
		truck.setId(JsonUtiles.getLong(nodeAux,"truck_id,id_truck".split(","), 0L));
		truck.setDomain(JsonUtiles.getString(nodeAux,"domain,dominio,patente".split(","), null));
		truck.setDescription(JsonUtiles.getString(nodeAux,"description,description_truck,truck_description,descripcion_camion,camion_descripcion".split(","), null));
		truck.setCisterns(JsonUtiles.getInt(nodeAux,"cisterns,cisterna,cisternas,cistern".split(","), 0));
		truck.setExternalCode(JsonUtiles.getString(nodeAux,"externalCodeTruck,external_code_truck,codigo_sap_truck,external_code".split(","), null));

        // Deserializacion de Product
        nodeAux = JsonUtiles.getNode(node, "product,producto,order_product,product_order".split(","), null);
        Product product = new Product();
		product.setId(JsonUtiles.getLong(nodeAux,"product_id,id_product".split(","), 0L));
		product.setName(JsonUtiles.getString(nodeAux,"name,nombre,nombre_producto,product_name".split(","), null));
		product.setDescription(JsonUtiles.getString(nodeAux,"description_product,descripcion_producto,product_description,description".split(","), null));
		product.setExternalCode(JsonUtiles.getString(nodeAux,"externalCodeProduct,external_code_product,codigo_sap_product,external_code".split(","), null));


        LocalDateTime scheduledDate = JsonUtiles.getLocalDateTime(node, "scheduled_date,scheduledDate,date_scheduled,dateScheduled".split(","), LocalDateTime.now());

        Double preset = JsonUtiles.getDouble(node, "preset,pre_set,preSet".split(","), 0.0);


        order.setNumber(number);
        order.setExternalCode(externalCode);
        order.setDriver(driver);
        order.setClient(client);
        order.setTruck(truck);
        order.setProduct(product);
        order.setScheduledDate(scheduledDate);
        order.setPreset(preset);

        return order;
    }
}
