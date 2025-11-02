package ar.edu.iua.TruckTeck.integration.chargingsystem.model.business;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.iua.TruckTeck.integration.sap.model.OrderSapJsonDeserializer;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.OrderDetail;
import ar.edu.iua.TruckTeck.model.business.IOrderBusiness;
import ar.edu.iua.TruckTeck.model.business.OrderBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.EmptyFieldException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.enums.OrderState;
import ar.edu.iua.TruckTeck.model.persistence.OrderDetailRepository;
import ar.edu.iua.TruckTeck.model.persistence.OrderRepository;
import ar.edu.iua.TruckTeck.util.JsonUtiles;
import lombok.extern.slf4j.Slf4j;
import ar.edu.iua.TruckTeck.controllers.Constants;

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

    @Autowired
    private OrderDetailRepository orderDetailDAO;

    @Autowired
    private IOrderBusiness orderBusiness;



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

    public Order addExternalCharging(String json) throws BusinessException, EmptyFieldException, NotFoundException{
        
        ObjectMapper mapper = JsonUtiles.getObjectMapper(Order.class,
				new OrderSapJsonDeserializer(Order.class),null);
		Order charge = null;
        Order order = null;
        
		try {
			charge = mapper.readValue(json, Order.class);

            // Se obtiene el número de la orden del objeto JSON recibido
			String order_number = charge.getNumber();

            // Si el número de la orden viene vacío o es nulo => se lanza la excepcion creada hacia el endpoint b2b
            if (order_number == null || order_number.isBlank()) {
               throw EmptyFieldException.builder()
                   .message("El número de la orden es obligatorio")
                   .build();
            }

            if(charge.getCaudal()<=0){
                throw BusinessException.builder().message("El caudal debe ser mayor a 0:" + charge.getCaudal())
                   .build();
            }

            order = orderBusiness.load(order_number);
            
            OrderDetail detail = new OrderDetail();
            detail.setDensity(charge.getDensity());
            detail.setAccumulatedMass(charge.getAccumulatedMass());
            detail.setTemperature(charge.getAccumulatedMass());
            detail.setCaudal(charge.getCaudal());
            detail.setTimestamp(LocalDateTime.now());
            detail.setOrder(charge);

            if (order.getDensity() == null &&
                order.getAccumulatedMass() == null &&
                order.getTemperature() == null &&
                order.getCaudal() == null &&
                order.getState() == OrderState.TARA_REGISTERED){

                order.setAccumulatedMass(charge.getAccumulatedMass());
                order.setDensity(charge.getDensity());
                order.setTemperature(charge.getTemperature());
                order.setCaudal(charge.getCaudal());

                order.setStartLoading(LocalDateTime.now());
                order.setEndLoading(LocalDateTime.now());
                order.setState(OrderState.LOADING);

                orderDetailDAO.save(detail);
                return orderDAO.save(order);
            }
            if(order.getAccumulatedMass()<detail.getAccumulatedMass() || order.getCaudal()<= 0){
                throw BusinessException.builder().message("La masa acumulada contiene informacion erronea" + detail.getAccumulatedMass())
                   .build();
            }
            
            // Ya pasaron 10 segundos desde endLoading
            if (Duration.between(order.getEndLoading(), LocalDateTime.now()).getSeconds() >= Constants.FREQUENCY) {
                orderDetailDAO.save(detail);
            }

            order.setEndLoading(LocalDateTime.now());
            return orderDAO.save(order);

		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		} 

        // Aqui se guarda en la base de datos el producto deserializado
		return order;

    }

}

//  private Double accumulatedMass;  // Última masa acumulada recibida
//     private Double density;          // Última densidad
//     private Double temperature;      // Última temperatura
//     private Double caudal;           // Último caudal
