package ar.edu.iua.TruckTeck.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.model.Conciliation;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.IOrderBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;
import ar.edu.iua.TruckTeck.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la gestión de órdenes.
 * <p>
 * Proporciona endpoints para listar y agregar órdenes mediante solicitudes HTTP.
 * Utiliza las rutas definidas en {@link Constants#URL_ORDERS}.
 * </p>
 */
@RestController
@RequestMapping(Constants.URL_ORDERS)
@Tag(description = "API Servicios relacionados con Órdenes", name = "Order")
public class OrderRestController {
    /**
     * Componente de negocio encargado de construir respuestas estándar.
     */
    @Autowired
    private IStandardResponseBusiness response;

    /**
     * Componente de negocio encargado de la lógica de órdenes.
     */
    @Autowired
    private IOrderBusiness orderBusiness;

    /**
     * Endpoint para obtener la lista de todas las órdenes.
     * <p>
     * Responde a solicitudes HTTP GET y devuelve las órdenes en formato JSON.
     * </p>
     *
     * @return ResponseEntity que contiene la lista de órdenes y el código HTTP correspondiente.
     *         - {@link HttpStatus#OK} si la operación fue exitosa.
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @Operation(operationId = "list-orders", summary = "Lista todas las órdenes.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la lista de órdenes.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Order.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {
        try {
            return new ResponseEntity<>(orderBusiness.list(), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para agregar una nueva orden.
     * <p>
     * Responde a solicitudes HTTP POST con un objeto {@link Order} en el cuerpo de la solicitud.
     * Devuelve la ubicación del nuevo recurso en el encabezado HTTP 'Location'.
     * </p>
     *
     * @param order La orden a agregar.
     * @return ResponseEntity que indica el resultado de la operación.
     *         - {@link HttpStatus#CREATED} si la orden se creó correctamente.
     *         - {@link HttpStatus#FOUND} si ya existe una orden similar ({@link FoundException}).
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAP')")
    @Operation(operationId = "add-order", summary = "Crea una nueva orden.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Orden a crear", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden creada. Se retorna header 'location' con la URI del nuevo recurso."),
        @ApiResponse(responseCode = "302", description = "Orden ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping(value = "")
    public ResponseEntity<?> add(@RequestBody Order order) {
        try {
            Order response = orderBusiness.add(order);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_ORDERS + "/" + response.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }

    /**
     * Obtiene una orden por su identificador único.
     *
     * @param id Identificador numérico de la orden a buscar.
     * @return Un {@link ResponseEntity} que contiene la orden encontrada (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra la orden (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(operationId = "load-order", summary = "Carga una orden por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador de la orden.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve una Orden.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra la orden para el identificador informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> load(@PathVariable long id) {
        try {
            return new ResponseEntity<>(orderBusiness.load(id), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

     /**
     * Obtiene una orden por su número de orden.
     *
     * @param number Número de la orden a buscar.
     * @return Un {@link ResponseEntity} que contiene la orden encontrada (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra la orden (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(operationId = "load-order-by-number", summary = "Carga una orden por su número de orden.")
    @Parameter(in = ParameterIn.PATH, name = "number", schema = @Schema(type = "string"), required = true, description = "Número de la orden a buscar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve una Orden.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra la orden para el número informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    @GetMapping(value = "/number/{number}")
    public ResponseEntity<?> load(@PathVariable String number) {
        try {
            return new ResponseEntity<>(orderBusiness.load(number), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Actualiza una orden existente con los datos proporcionados.
     *
     * @param order Objeto {@link Order} con los datos actualizados de la orden.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la actualización es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si la orden no existe (HTTP 404)
     *         o si la orden ya existe (HTTP 302).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "update-order", summary = "Actualiza una orden existente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Orden con datos a actualizar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden actualizada correctamente."),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "302", description = "Conflicto: orden ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PutMapping(value = "")
    public ResponseEntity<?> update(@RequestBody Order order) {
        try {
            orderBusiness.update(order);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        } catch(FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }

    /**
     * Elimina una orden existente según su identificador.
     *
     * @param id Identificador numérico de la orden a eliminar.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la eliminación es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si la orden no existe (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "delete-order", summary = "Elimina una orden por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador de la orden a eliminar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden eliminada correctamente."),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        try {
            orderBusiness.delete(id);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Obtiene la conciliación de una orden finalizada por su número de orden.
     * <p>
     * La conciliación solo está disponible para órdenes en estado FINALIZED.
     * Incluye datos de pesaje, diferencias y promedios de parámetros de carga.
     * </p>
     *
     * @param number Número de la orden a conciliar.
     * @return Un {@link ResponseEntity} que contiene el objeto {@link Conciliation} (HTTP 200 OK),
     *         o un mensaje de error si la orden no está finalizada o no existe (HTTP 404/500).
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(operationId = "get-conciliation", summary = "Obtiene la conciliación de una orden finalizada por su número de orden.")
    @Parameter(in = ParameterIn.PATH, name = "number", schema = @Schema(type = "string"), required = true, description = "Número de la orden a conciliar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la conciliación de la orden.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Conciliation.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada o no finalizada", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    @GetMapping(value = "/number/{number}/conciliation")
    public ResponseEntity<?> getConciliation(@PathVariable String number) {
        try {
            return new ResponseEntity<>(orderBusiness.findConciliation(number), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

}



