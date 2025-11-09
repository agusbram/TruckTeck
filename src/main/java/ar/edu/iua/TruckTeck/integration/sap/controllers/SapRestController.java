package ar.edu.iua.TruckTeck.integration.sap.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.controllers.Constants;
import ar.edu.iua.TruckTeck.integration.sap.model.business.IOrderBusinessSap;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.EmptyFieldException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;
import ar.edu.iua.TruckTeck.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.URL_ORDERS_SAP)
@Tag(name = "SAP", description = "API Integración SAP para recepción de órdenes")
public class SapRestController {
    /**
     * Componente de negocio encargado de la lógica de ordenes
     */
	@Autowired
	private IOrderBusinessSap orderBusinessSap;

    /**
     * Componente encargado de construir respuestas estándar de error.
     */
	@Autowired
	private IStandardResponseBusiness response;
    
    /**
     * Agrega una nueva orden a partir de un mensaje externo en formato JSON.
     * <p>
     * Responde a solicitudes HTTP POST en la ruta
     * <code>/api/v1/orders/sap/b2b</code>.
     * <br>
     * Se utiliza principalmente para recibir la orden directamente desde un sistema externo, donde el cuerpo de la
     * petición se recibe como {@link String} y se procesa internamente.
     * </p>
     *
     * @param httpEntity Entidad HTTP que contiene el JSON con los datos del producto
     *                   en su cuerpo.
     * @return {@link ResponseEntity} con:
     *         - {@link HttpStatus#CREATED} si se crea correctamente (incluyendo la ubicación en el header),
     *         - {@link HttpStatus#FOUND} si ya existe un producto con el mismo código,
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un problema de negocio.
     */
	@Operation(operationId = "add-order-sap", summary = "Crea una orden desde SAP (B2B)")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload JSON enviado por SAP (JSON crudo)", required = true, content = @Content(mediaType = "application/json", schema = @Schema(type = "string")))
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Orden creada correctamente. Se retorna header 'location' con la URI del nuevo recurso."),
		@ApiResponse(responseCode = "302", description = "Recurso relacionado no encontrado (Found)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
		@ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
		@ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
	})
	@PostMapping(value = "/b2b")
	public ResponseEntity<?> addExternal(HttpEntity<String> httpEntity) {
		try {
			Order response = orderBusinessSap.addExternalSap(httpEntity.getBody());
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("location", Constants.URL_ORDERS_SAP + "/" + response.getId());
			return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (FoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
		} catch(EmptyFieldException e) {
			/**
			 * Esto comunica claramente al cliente que el problema está en los datos enviados, no en el servidor.
			 * La solicitud del cliente está mal formada
			 * Se decidió crear una excepción personalizada para indicar explícitamente al programador cual fue el problema
			 * que lanzó la misma. Además, se indica en la request 400 BAD REQUEST con un mensaje claro que dice:
			 * El nombre del producto es obligatorio.
			 */
			return new ResponseEntity<>(response.build(HttpStatus.BAD_REQUEST, e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
	}

}
