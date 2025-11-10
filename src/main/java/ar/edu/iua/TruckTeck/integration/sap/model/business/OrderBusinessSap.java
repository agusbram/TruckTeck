package ar.edu.iua.TruckTeck.integration.sap.model.business;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.iua.TruckTeck.integration.sap.model.OrderSapJsonDeserializer;
import ar.edu.iua.TruckTeck.model.Client;
import ar.edu.iua.TruckTeck.model.Driver;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.Product;
import ar.edu.iua.TruckTeck.model.Truck;
import ar.edu.iua.TruckTeck.model.business.OrderBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.EmptyFieldException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.persistence.OrderRepository;
import ar.edu.iua.TruckTeck.util.JsonUtiles;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderBusinessSap extends OrderBusiness implements IOrderBusinessSap {

    @Autowired
    private OrderRepository orderDAO;

    /**
     * Agrega una nueva orden a partir de una representación en formato JSON proveniente del sistema SAP.
     * <p>
     * Utiliza un {@link ObjectMapper} configurado con un deserializador
     * personalizado {@link OrderSapJsonDeserializer} para convertir
     * la cadena JSON en un objeto {@link Order}.
     * </p>
     * <p>
     * Valida que los campos obligatorios estén presentes (número de orden, conductor, cliente, camión, producto y preset).
     * Si algún campo falta o está vacío, lanza {@link EmptyFieldException}.
     * Una vez validado, reutiliza la lógica de {@link #add(Order)} para registrar la orden.
     * </p>
     *
     * @param json Cadena en formato JSON que representa la orden a registrar.
     * @return La orden agregada y persistida en la base de datos.
     * @throws FoundException    Si la orden ya existe en el sistema.
     * @throws BusinessException Si ocurre un error inesperado durante la deserialización o el guardado.
     * @throws EmptyFieldException Si falta algún campo obligatorio (número, conductor, cliente, camión, producto, preset).
     */
	@Override
	public Order addExternalSap(String json) throws FoundException, BusinessException, EmptyFieldException {

        log.info("Iniciando proceso de deserialización de la orden desde JSON...");

		ObjectMapper mapper = JsonUtiles.getObjectMapper(Order.class,
				new OrderSapJsonDeserializer(Order.class),null);
		Order order = null;

        log.info("JSON recibido en addExternalSap: " + json);

		try {
			order = mapper.readValue(json, Order.class);
            
            if (orderDAO.findByExternalCode(order.getExternalCode()).isPresent()) {
                throw FoundException.builder().message("Se encontró la orden con el código externo: " + order.getExternalCode()).build();
            }

            log.info("Orden deserializada: " + order);

            // Se obtiene el número de la orden del objeto JSON recibido
			String order_number = order.getNumber();

            log.info("Número de la orden: " + order_number);

            // Si el número de la orden viene vacío o es nulo => se lanza la excepcion creada hacia el endpoint b2b
            if (order_number == null || order_number.isBlank()) {
               throw EmptyFieldException.builder()
                   .message("El número de la orden es obligatorio")
                   .build();
            }

            log.info("Validando campos obligatorios de la orden...");

            Driver driver = order.getDriver();

            log.info("Conductor de la orden: " + driver);

            if (driver == null || driver.getDocumentNumber() == null || driver.getDocumentNumber().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El conductor de la orden es obligatorio")
                    .build();
            }

            log.info("Validando cliente de la orden...");

            Client client = order.getClient();

            log.info("Cliente de la orden: " + client);

            if (client == null || client.getCompanyName() == null || client.getCompanyName().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El cliente de la orden es obligatorio")
                    .build();
            }

            log.info("Validando camión de la orden...");

            Truck truck = order.getTruck();

            log.info("Camión de la orden: " + truck);

            if (truck == null || truck.getDomain() == null || truck.getDomain().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El camión de la orden es obligatorio")
                    .build();
            }

            log.info("Validando producto de la orden...");

            Product product = order.getProduct();

            log.info("Producto de la orden: " + product);
            if (product == null || product.getName() == null || product.getName().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El producto de la orden es obligatorio")
                    .build();
            }

            log.info("Validando preset de la orden...");

            Double preset = order.getPreset();

            log.info("Preset de la orden: " + preset);
            if (preset == null || preset <= 0.0) {
                throw EmptyFieldException.builder()
                    .message("El preset de la orden es obligatorio y debe ser mayor a 0")
                    .build();
            }

            log.info("Todos los campos obligatorios están presentes. Procediendo a agregar la orden...");

            // Establecer la fecha y hora de recepción inicial de la orden como el momento actual
            order.setInitialReception(LocalDateTime.now());

		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		} 

        log.info("Agregando la orden a la base de datos...");

        // Aqui se guarda en la base de datos el producto deserializado
		return add(order);

	}
}
