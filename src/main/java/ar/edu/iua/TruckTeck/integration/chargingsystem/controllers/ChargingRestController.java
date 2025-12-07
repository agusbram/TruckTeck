package ar.edu.iua.TruckTeck.integration.chargingsystem.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.EmptyFieldException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;
import ar.edu.iua.TruckTeck.util.StandardResponse;
import ar.edu.iua.TruckTeck.controllers.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.iua.TruckTeck.integration.chargingsystem.model.business.IOrderBusinessCharging;

@RestController
@RequestMapping(Constants.URL_ORDERS_CHARGING)
@Tag(name = "Charging", description = "API Integración con sistema de carga (Charging System)")
@PreAuthorize("hasRole('ADMIN') or hasRole('CHARGING, CHARGING_SYSTEM')")
public class ChargingRestController {
    
    /**
     * Componente de negocio encargado de construir respuestas estándar.
     */
    @Autowired
    private IStandardResponseBusiness response;


    /**
     * Componente de negocio encargado de la lógica de productos.
     */
    @Autowired
    private IOrderBusinessCharging orderBusiness;


     /**
     * Obtiene una orden por su número de orden.
     *
     * @param number Número de la orden a buscar.
     * @return Un {@link ResponseEntity} que contiene la orden encontrada (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra la orden (HTTP 404).
     */
    @Operation(operationId = "get-preset", summary = "Obtiene una orden por número y código de preset del cargador")
    @Parameter(in = ParameterIn.PATH, name = "number", schema = @Schema(type = "string"), required = true, description = "Número de la orden")
    @Parameter(in = ParameterIn.PATH, name = "code", schema = @Schema(type = "string"), required = true, description = "Código del preset")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la orden encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @GetMapping(value = "/number/{number}/code/{code}")
    public ResponseEntity<?> load(@PathVariable String number,@PathVariable String code) {
        try {
            return new ResponseEntity<>(orderBusiness.getPreset(code,number), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Crea una orden recibida desde un sistema externo de carga (integración B2B).
     * <p>
     * Este endpoint recibe un payload de texto plano o JSON representando los datos de la orden.
     * Devuelve un encabezado <b>Location</b> con la referencia del recurso creado.
     *
     * @param httpEntity Entidad HTTP que contiene el cuerpo (payload) enviado por el sistema B2B.
     * @return Un {@link ResponseEntity} con:
     *         <ul>
     *             <li>HTTP 201 (Created) si la orden fue creada correctamente, incluyendo header <i>location</i>.</li>
     *             <li>HTTP 400 (Bad Request) si los datos son inválidos o faltan campos obligatorios.</li>
     *             <li>HTTP 302 (Found) si algún recurso relacionado no se encuentra disponible.</li>
     *             <li>HTTP 500 (Internal Server Error) si ocurre un error interno de negocio.</li>
     *         </ul>
     */
    @Operation(operationId = "add-external-charging", summary = "Crea una orden desde el sistema de carga (B2B)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload recibido desde el sistema B2B (texto/JSON)", required = true, content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden creada correctamente. Se retorna header 'location' con referencia al recurso."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "302", description = "Recurso relacionado no encontrado (Found)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping(value = "/b2b")
    public ResponseEntity<?> addExternal(HttpEntity<String> httpEntity) {
		try {
			Order response = orderBusiness.addExternalCharging(httpEntity.getBody());
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("location", Constants.URL_ORDERS_CHARGING 
                                + "/id:" + response.getId() 
                                + "/AccumulatedMass:" + response.getAccumulatedMass()
                                + "/Density:" + response.getDensity() 
                                + "/Temperature:" + response.getTemperature() 
                                + "/Caudal:" + response.getCaudal()
                                + "/EndLoading:" + response.getEndLoading());
			return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
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

    /**
     * Marca una orden como "cargada" (loaded) desde el sistema externo de carga (B2B).
     * <p>
     * Este endpoint actualiza el estado de la orden para indicar que el proceso de carga fue completado.
     *
     * @param number Número de la orden que se desea marcar como cargada.
     * @return Un {@link ResponseEntity} que contiene:
     *         <ul>
     *             <li>HTTP 200 (OK) si la orden fue marcada exitosamente como cargada.</li>
     *             <li>HTTP 302 (Found) si el recurso relacionado no se encuentra.</li>
     *             <li>HTTP 500 (Internal Server Error) si ocurre una excepción de negocio.</li>
     *         </ul>
     */
    @Operation(operationId = "patch-mark-loaded", summary = "Marca una orden como 'loaded' desde el sistema de carga (B2B)")
    @Parameter(in = ParameterIn.PATH, name = "number", schema = @Schema(type = "string"), required = true, description = "Número de la orden a marcar como cargada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden marcada como cargada y creada/actualizada"),
        @ApiResponse(responseCode = "302", description = "Recurso relacionado no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PatchMapping(value = "loaded/b2b/{number}")
    public ResponseEntity<?> loaded(@PathVariable String number) {
        try {
            Order response = orderBusiness.changeStateLoaded(number);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_ORDERS_CHARGING + "/" + response.getId());
            return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).build();
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }
}
