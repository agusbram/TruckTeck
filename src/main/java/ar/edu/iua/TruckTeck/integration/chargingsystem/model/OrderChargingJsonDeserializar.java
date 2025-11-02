package ar.edu.iua.TruckTeck.integration.chargingsystem.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import ar.edu.iua.TruckTeck.util.JsonUtiles;

import ar.edu.iua.TruckTeck.model.Order;

public class OrderChargingJsonDeserializar {

    public Order deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        Order order = new Order();
        JsonNode node = jp.getCodec().readTree(jp);
        
        Double accumulatedMass = JsonUtiles.getDouble(node, "accumulatedMass,accumulated_mass,masa,masa_acumulada".split(","), -1);
        Double density = JsonUtiles.getDouble(node, "density,densidad".split(","), -1);
        Double temperature = JsonUtiles.getDouble(node, "temperature,temperatura".split(","), -1);
        Double caudal = JsonUtiles.getDouble(node,"caudal,caudales".split(","), -1);

        order.setCaudal(caudal);
        order.setTemperature(temperature);
        order.setDensity(density);
        order.setAccumulatedMass(accumulatedMass);
        return order;
    }
}

