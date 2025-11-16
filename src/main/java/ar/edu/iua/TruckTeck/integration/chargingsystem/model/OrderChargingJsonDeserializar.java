package ar.edu.iua.TruckTeck.integration.chargingsystem.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ar.edu.iua.TruckTeck.util.JsonUtiles;

import ar.edu.iua.TruckTeck.model.Order;

public class OrderChargingJsonDeserializar extends StdDeserializer<Order>{


    /**
    * Deserializador personalizado para la entidad {@link Order}, utilizado en el proceso de carga externa
    * de datos provenientes del sistema de despacho o de un sistema de integración de carga.
    * <p>
    * Permite interpretar diferentes nombres de campos en el JSON de entrada (por ejemplo, en distintos idiomas
    * o formatos alternativos) y mapearlos correctamente a los atributos del objeto {@link Order}.
    * </p>
    * <p>
    * Este deserializador se utiliza en conjunto con {@link com.fasterxml.jackson.databind.ObjectMapper}
    * configurado en la clase de integración para convertir cadenas JSON en objetos de tipo {@link Order}.
    * </p>
    */
	public OrderChargingJsonDeserializar(Class<?> vc) {
		super(vc);
	}

    /**
     * Deserializa una representación JSON en un objeto {@link Order}.
     * <p>
     * El método permite reconocer múltiples alias para los nombres de los campos del JSON
     * (por ejemplo, "numero" o "number") y asignarlos correctamente a los atributos
     * de la entidad {@link Order}.
     * </p>
     *
     * @param jp   Parser JSON proporcionado por Jackson.
     * @param ctxt Contexto de deserialización de Jackson.
     * @return Un objeto {@link Order} con los valores extraídos del JSON.
     * @throws IOException Si ocurre un error al leer el flujo JSON.
     * @throws JacksonException Si se produce un error durante el proceso de deserialización.
     */
    public Order deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        // Obtención del nodo JSON
        Order order = new Order();
        JsonNode node = jp.getCodec().readTree(jp);

        // Obtención de los valores del JSON con nombres alternativos
        String number = JsonUtiles.getString(node, "number,numero".split(","), null);
        Double accumulatedMass = JsonUtiles.getDouble(node, "accumulatedMass,accumulated_mass,masa,masa_acumulada".split(","), -1);
        Double density = JsonUtiles.getDouble(node, "density,densidad".split(","), -1);
        Double temperature = JsonUtiles.getDouble(node, "temperature,temperatura".split(","), -1);
        Double caudal = JsonUtiles.getDouble(node,"caudal,caudales".split(","), -1);

        // Seteo de los valores en la entidad Order
        order.setCaudal(caudal);
        order.setTemperature(temperature);
        order.setDensity(density);
        order.setAccumulatedMass(accumulatedMass);
        order.setNumber(number);

        return order;
    }
}

