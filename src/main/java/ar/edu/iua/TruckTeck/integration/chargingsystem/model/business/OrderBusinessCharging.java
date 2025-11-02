package ar.edu.iua.TruckTeck.integration.chargingsystem.model.business;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.iua.TruckTeck.integration.sap.model.OrderSapJsonDeserializer;
import ar.edu.iua.TruckTeck.model.Order;

import ar.edu.iua.TruckTeck.model.business.OrderBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.EmptyFieldException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.OrderRepository;
import ar.edu.iua.TruckTeck.util.JsonUtiles;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderBusinessCharging extends OrderBusiness implements IOrderBusinessCharging{

    /**
     * Repositorio para acceder a los datos de órdenes.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private OrderRepository orderDAO;

    public Double getPreset(String number, String activationCode) throws BusinessException, NotFoundException{

        Optional<Order> r;

        try {
            r = orderDAO.findByActivationCode(number,activationCode);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("La orden o el codigo incorrecto, orden: " + number + "/codigo:" + activationCode).build();
        }
        return r.get().getPreset();
        
    }

    public Order addExternalCharging(String json) throws BusinessException, EmptyFieldException{
        
        ObjectMapper mapper = JsonUtiles.getObjectMapper(Order.class,
				new OrderSapJsonDeserializer(Order.class),null);
		Order order = null;
		try {
			order = mapper.readValue(json, Order.class);

            // Se obtiene el número de la orden del objeto JSON recibido
			String order_number = order.getNumber();

            // Si el número de la orden viene vacío o es nulo => se lanza la excepcion creada hacia el endpoint b2b
            if (order_number == null || order_number.isBlank()) {
               throw EmptyFieldException.builder()
                   .message("El número de la orden es obligatorio")
                   .build();
            }

		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		} 

        // Aqui se guarda en la base de datos el producto deserializado
		return order;


    }

}
