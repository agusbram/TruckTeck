package ar.edu.iua.TruckTeck.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.model.Client;
import ar.edu.iua.TruckTeck.model.business.IClientBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.iua.TruckTeck.util.StandardResponse;

/**
 * Controlador REST para la gestión de clientes.
 * <p>
 * Proporciona endpoints para listar y agregar clientes mediante solicitudes HTTP.
 * Utiliza las rutas definidas en {@link Constants#URL_CLIENTS}.
 * </p>
 */
@RestController
@RequestMapping(Constants.URL_CLIENTS)
@Tag(description = "API Servicios relacionados con Clientes", name = "Client")
public class ClientRestController {

    /**
     * Componente de negocio encargado de construir respuestas estándar.
     */
    @Autowired
    private IStandardResponseBusiness response;

    /**
     * Componente de negocio encargado de la lógica de clientes.
     */
    @Autowired
    private IClientBusiness clientBusiness;

    /**
     * Endpoint para obtener la lista de todos los clientes.
     * <p>
     * Responde a solicitudes HTTP GET y devuelve los clientes en formato JSON.
     * </p>
     *
     * @return ResponseEntity que contiene la lista de clientes y el código HTTP correspondiente.
     *         - {@link HttpStatus#OK} si la operación fue exitosa.
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "list-clients", summary = "Lista todos los clientes.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la lista de clientes.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Client.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> list() {
        try {
            return new ResponseEntity<>(clientBusiness.list(), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para agregar un nuevo cliente.
     * <p>
     * Responde a solicitudes HTTP POST con un objeto {@link Client} en el cuerpo de la solicitud.
     * Devuelve la ubicación del nuevo recurso en el encabezado HTTP 'Location'.
     * </p>
     *
     * @param client El cliente a agregar.
     * @return ResponseEntity que indica el resultado de la operación.
     *         - {@link HttpStatus#CREATED} si el cliente se creó correctamente.
     *         - {@link HttpStatus#FOUND} si ya existe un cliente similar ({@link FoundException}).
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @PostMapping(value = "")
    @Operation(operationId = "add-client", summary = "Crea un nuevo cliente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Cliente a crear", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Client.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado. Se retorna header 'location' con la URI del nuevo recurso."),
        @ApiResponse(responseCode = "302", description = "Cliente ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> add(@RequestBody Client client) {
        try {
            Client response = clientBusiness.add(client);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_CLIENTS + "/" + response.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }

    /**
     * Obtiene un cliente por su identificador único.
     *
     * @param id Identificador numérico del cliente a buscar.
     * @return Un {@link ResponseEntity} que contiene el cliente encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el cliente (HTTP 404).
     */
    @GetMapping(value = "/{id}")
    @Operation(operationId = "load-client", summary = "Carga un cliente por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del cliente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve un Cliente.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Client.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra el cliente para el identificador informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    public ResponseEntity<?> load(@PathVariable long id) {
        try {
            return new ResponseEntity<>(clientBusiness.load(id), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
     /**
     * Obtiene un cliente por su nombre de compañía.
     *
     * @param companyName Nombre de la compañía del cliente a buscar.
     * @return Un {@link ResponseEntity} que contiene el cliente encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el cliente (HTTP 404).
     */
    @GetMapping(value = "/by-companyName/{companyName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "load-client-by-companyName", summary = "Carga un cliente por su nombre de compañía.")
    @Parameter(in = ParameterIn.PATH, name = "companyName", schema = @Schema(type = "string"), required = true, description = "Nombre de la compañía del cliente a buscar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve un Cliente.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Client.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra el cliente para el nombre de compañía informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    public ResponseEntity<?> load(@PathVariable String companyName) {
        try {
            return new ResponseEntity<>(clientBusiness.load(companyName), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Actualiza un cliente existente con los datos proporcionados.
     *
     * @param client Objeto {@link Client} con los datos actualizados del cliente.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la actualización es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el cliente no existe (HTTP 404)
     *         o si el cliente existe (HTTP 302).
     */
    @PutMapping(value = "")
    @Operation(operationId = "update-client", summary = "Actualiza un cliente existente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Cliente con datos a actualizar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Client.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente."),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ar.edu.iua.TruckTeck.util.StandardResponse.class))),
        @ApiResponse(responseCode = "302", description = "Conflicto: cliente ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ar.edu.iua.TruckTeck.util.StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ar.edu.iua.TruckTeck.util.StandardResponse.class)))
    })
    public ResponseEntity<?> update(@RequestBody Client client) {
        try {
            clientBusiness.update(client);
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
     * Elimina un cliente existente según su identificador.
     *
     * @param id Identificador numérico del cliente a eliminar.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la eliminación es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el cliente no existe (HTTP 404).
     */
    @DeleteMapping(value = "/{id}")
    @Operation(operationId = "delete-client", summary = "Elimina un cliente por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del cliente a eliminar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente eliminado correctamente."),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> delete(@PathVariable long id) {
        try {
            clientBusiness.delete(id);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
}

