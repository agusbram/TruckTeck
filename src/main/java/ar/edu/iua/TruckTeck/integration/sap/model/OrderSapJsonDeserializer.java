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
    
    public Order deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        Order order = new Order();
        JsonNode node = jp.getCodec().readTree(jp);
        
        String number = JsonUtiles.getString(node, "order,number,order_number".split(","), null);
        String externalCode = JsonUtiles.getString(node, "external_code,code,externalCode".split(","), null);
        
        Driver driver = JsonUtiles.getDriver(node, "driver,chofer,conductor".split(","), null);

        // JsonNode nodeAux = JsonUtiles.getDriver(node, "driver,chofer,conductor".split(","), null);

        /* Driver driver = new Driver();
        driver.setId(JsonUtiles.getLong(nodeAux,"driver_id,id_driver".split(","), 0L));
        driver.setName(JsonUtiles.getString(nodeAux,"name,nombre".split(","), null));
        driver.setSurname(JsonUtiles.getString(nodeAux,"surname,apellido".split(","), null));
        driver.setDocumentNumber(JsonUtiles.getString(nodeAux,"document_number,dni,documento".split(","), null));
        driver.setExternalCode(JsonUtiles.getString(nodeAux,"externalCodeDriver,external_code_driver,codigo_sap_driver".split(","), null)); */

        Client client = JsonUtiles.getClient(node, "client,cliente".split(","), null);

        Truck truck = JsonUtiles.getTruck(node, "truck,camion".split(","), null);

        Product product = JsonUtiles.getProduct(node, "product,producto,order_product,product_order".split(","), null);

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
