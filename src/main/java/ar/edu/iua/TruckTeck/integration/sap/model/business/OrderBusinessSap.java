package ar.edu.iua.TruckTeck.integration.sap.model.business;

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
import ar.edu.iua.TruckTeck.util.JsonUtiles;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderBusinessSap extends OrderBusiness implements IOrderBusinessSap {
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

            Driver driver = order.getDriver();

            if (driver == null || driver.getDocumentNumber() == null || driver.getDocumentNumber().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El conductor de la orden es obligatorio")
                    .build();
            }

            Client client = order.getClient();

            if (client == null || client.getCompanyName() == null || client.getCompanyName().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El cliente de la orden es obligatorio")
                    .build();
            }

            Truck truck = order.getTruck();
            if (truck == null || truck.getDomain() == null || truck.getDomain().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El camión de la orden es obligatorio")
                    .build();
            }

            Product product = order.getProduct();
            if (product == null || product.getName() == null || product.getName().isBlank()) {
                throw EmptyFieldException.builder()
                    .message("El producto de la orden es obligatorio")
                    .build();
            }

            Double preset = order.getPreset();
            if (preset == null || preset <= 0.0) {
                throw EmptyFieldException.builder()
                    .message("El preset de la orden es obligatorio y debe ser mayor a 0")
                    .build();
            }

		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		} 

        // Aqui se guarda en la base de datos el producto deserializado
		return add(order);

	}
}
