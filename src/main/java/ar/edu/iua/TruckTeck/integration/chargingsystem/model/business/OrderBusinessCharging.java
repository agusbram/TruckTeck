package ar.edu.iua.TruckTeck.integration.chargingsystem.model.business;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.iua.TruckTeck.integration.chargingsystem.model.OrderChargingJsonDeserializar;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.OrderDetail;
import ar.edu.iua.TruckTeck.model.business.IOrderBusiness;
import ar.edu.iua.TruckTeck.model.business.OrderBusiness;
import ar.edu.iua.TruckTeck.model.business.TemperatureAlertConfigBusiness;
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

    @Autowired
    private TemperatureAlertConfigBusiness temperatureAlertConfigBusiness;

    /**
     * Repositorio para acceder a los datos de órdenes.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private OrderRepository orderDAO;

    /**
     * Repositorio para acceder a los detalles de las órdenes.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private OrderDetailRepository orderDetailDAO;

    /**
     * Capa de negocio encargada de la lógica central de las órdenes.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private IOrderBusiness orderBusiness;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;



    /**
     * Obtiene el valor preestablecido (preset) asociado a un número de orden y código de activación.
     *
     * @param activationCode Código de activación asociado a la orden.
     * @param number Número identificador de la orden.
     * @return El valor preestablecido (preset) correspondiente a la orden indicada.
     * @throws BusinessException Si ocurre un error inesperado en la capa de negocio o durante la consulta.
     * @throws NotFoundException Si no se encuentra una orden que coincida con el número y el código de activación proporcionados.
     */
    public Double getPreset(String activationCode, String number) throws BusinessException, NotFoundException{
        Optional<Order> r;

        try {
            r = orderDAO.findByActivationCode(activationCode,number);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("La orden o el codigo incorrecto, orden: " + number + "/codigo:" + activationCode).build();
        }
        return r.get().getPreset();
    }

    /**
     * Agrega una nueva orden a partir de una representación externa en formato JSON.
     * <p>
     * Este método valida los campos recibidos, verifica el estado actual de la orden
     * y registra los detalles de carga correspondientes.
     * </p>
     *
     * @param json Cadena en formato JSON que contiene los datos de la orden a registrar.
     * @return La entidad {@link Order} actualizada y persistida en la base de datos.
     * @throws BusinessException Si ocurre un error inesperado en la capa de negocio o si los datos son inconsistentes.
     * @throws EmptyFieldException Si el número de la orden viene vacío o nulo.
     * @throws NotFoundException Si no se encuentra la orden correspondiente al número recibido.
     */
    public Order addExternalCharging(String json) throws BusinessException, EmptyFieldException, NotFoundException{

        ObjectMapper mapper = JsonUtiles.getObjectMapper(Order.class,
				new OrderChargingJsonDeserializar(Order.class),null);
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

            order = orderBusiness.load(order_number);

            if(order.getState() != OrderState.TARA_REGISTERED){
                throw BusinessException.builder().message("El estado es incorrecto:" + order.getState())
                   .build();
            }

            if(charge.getCaudal()<=0){
                throw BusinessException.builder().message("El caudal debe ser mayor a 0:" + charge.getCaudal())
                   .build();
            }

            if (order.getAccumulatedMass() != null && charge.getAccumulatedMass() < order.getAccumulatedMass()) {
                throw BusinessException.builder()
                .message("La masa acumulada contiene información errónea: " + charge.getAccumulatedMass())
                .build();
            }

            OrderDetail detail = new OrderDetail();
            detail.setDensity(charge.getDensity());
            detail.setAccumulatedMass(charge.getAccumulatedMass());
            detail.setTemperature(charge.getTemperature());
            detail.setCaudal(charge.getCaudal());
            detail.setTimestamp(LocalDateTime.now());
            detail.setOrder(order);


            if (order.getDensity() == null &&
            order.getAccumulatedMass() == null &&
            order.getTemperature() == null &&
            order.getCaudal() == null){

                order.setStartLoading(LocalDateTime.now());


            }

            order.setEndLoading(LocalDateTime.now());
            order.setAccumulatedMass(charge.getAccumulatedMass());
            order.setDensity(charge.getDensity());
            order.setTemperature(charge.getTemperature());
            order.setCaudal(charge.getCaudal());

            // Ya pasaron 10 segundos desde endLoading
            LocalDateTime lastTimestamp = orderDetailDAO.findLastTimestampByOrderId(order.getId());
            if (lastTimestamp == null ||
                Duration.between(lastTimestamp, detail.getTimestamp()).getSeconds() >= Constants.FREQUENCY) {
                orderDetailDAO.save(detail);
            }
            // Verifica si la temperatura supera el límite y manda mail si corresponde
            try {
                boolean alertSent = temperatureAlertConfigBusiness.checkAndSendAlert(charge.getTemperature());
                if (alertSent)
                    messagingTemplate.convertAndSend("/topic/alarm", true);
            } catch (NotFoundException e) {
                log.error("No se pudo verificar alerta de temperatura: " + e.getMessage());
            }
            // Notificar a los suscriptores sobre el nuevo detalle de la orden
            messagingTemplate.convertAndSend("/topic/detail", detail);

		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		} 

        // Aqui se guarda en la base de datos el producto deserializado
		return  orderDAO.save(order);

    }

    /**
     * Cambia el estado de una orden a “cargada” (LOADING) según el número de referencia proporcionado.
     *
     * @param number Número identificador de la orden cuyo estado será modificado.
     * @return La entidad {@link Order} actualizada con el nuevo estado.
     * @throws BusinessException Si ocurre un error inesperado en la capa de negocio o si la orden está en un estado no permitido.
     * @throws NotFoundException Si no se encuentra una orden con el número de referencia especificado.
     */
    public Order changeStateLoaded(String number) throws BusinessException, NotFoundException{

        Order order = new Order();
        try {
            order = orderBusiness.load(number);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(order == null) {
            throw NotFoundException.builder().message("No se encuentra la Orden con número: " + number).build();
        }
        if(order.getState() != OrderState.TARA_REGISTERED){
            throw BusinessException.builder().message("Esta orden se encuentra en un estado no permitido: " + order.getState()).build();
        }
        order.setState(OrderState.LOADING);
        order.setCloseOrder(LocalDateTime.now());
        return orderDAO.save(order);
    }

}

//  private Double accumulatedMass;  // Última masa acumulada recibida
//     private Double density;          // Última densidad
//     private Double temperature;      // Última temperatura
//     private Double caudal;           // Último caudal
