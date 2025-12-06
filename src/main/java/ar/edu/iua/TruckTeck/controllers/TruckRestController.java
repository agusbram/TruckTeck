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

import ar.edu.iua.TruckTeck.model.Truck;
import ar.edu.iua.TruckTeck.model.business.ITruckBusiness;
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
 * Controlador REST para la gestión de camiones.
 * <p>
 * Proporciona endpoints para listar y agregar camiones mediante solicitudes HTTP.
 * Utiliza las rutas definidas en {@link Constants#URL_TRUCKS}.
 * </p>
 */
@RestController
@RequestMapping(Constants.URL_TRUCKS)
@Tag(description = "API Servicios relacionados con Camiones", name = "Truck")
public class TruckRestController {

    /**
     * Componente de negocio encargado de construir respuestas estándar.
     */
    @Autowired
    private IStandardResponseBusiness response;

    /**
     * Componente de negocio encargado de la lógica de camiones.
     */
    @Autowired
    private ITruckBusiness truckBusiness;

    /**
     * Endpoint para obtener la lista de todos los camiones.
     * <p>
     * Responde a solicitudes HTTP GET y devuelve los camiones en formato JSON.
     * </p>
     *
     * @return ResponseEntity que contiene la lista de camiones y el código HTTP correspondiente.
     *         - {@link HttpStatus#OK} si la operación fue exitosa.
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @Operation(operationId = "list-trucks", summary = "Lista todos los camiones.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la lista de camiones.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Truck.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {
        try {
            return new ResponseEntity<>(truckBusiness.list(), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para agregar un nuevo camión.
     * <p>
     * Responde a solicitudes HTTP POST con un objeto {@link Truck} en el cuerpo de la solicitud.
     * Devuelve la ubicación del nuevo recurso en el encabezado HTTP 'Location'.
     * </p>
     *
     * @param truck El camión a agregar.
     * @return ResponseEntity que indica el resultado de la operación.
     *         - {@link HttpStatus#CREATED} si el camión se creó correctamente.
     *         - {@link HttpStatus#FOUND} si ya existe un camión similar ({@link FoundException}).
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @Operation(operationId = "add-truck", summary = "Crea un nuevo camión.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Camión a crear", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Truck.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Camión creado. Se retorna header 'location' con la URI del nuevo recurso."),
        @ApiResponse(responseCode = "302", description = "Camión ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "")
    public ResponseEntity<?> add(@RequestBody Truck truck) {
        try {
            Truck response = truckBusiness.add(truck);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_TRUCKS + "/" + response.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }

    /**
     * Obtiene un camión por su identificador único.
     *
     * @param id Identificador numérico del camión a buscar.
     * @return Un {@link ResponseEntity} que contiene el camión encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el camión (HTTP 404).
     */
    @Operation(operationId = "load-truck", summary = "Carga un camión por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del camión.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve un Camión.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Truck.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra el camión para el identificador informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> load(@PathVariable long id) {
        try {
            return new ResponseEntity<>(truckBusiness.load(id), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
     /**
     * Obtiene un camión por su dominio (patente).
     *
     * @param truck Dominio (patente) del camión a buscar (ej: "ABC123").
     * @return Un {@link ResponseEntity} que contiene el camión encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el camión (HTTP 404).
     */
    @Operation(operationId = "load-truck-by-name", summary = "Carga un camión por su dominio (patente).")
    @Parameter(in = ParameterIn.PATH, name = "truck", schema = @Schema(type = "string"), required = true, description = "Dominio (patente) del camión a buscar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve un Camión.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Truck.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra el camión para el dominio informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/by-name/{truck}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> load(@PathVariable String truck) {
        try {
            return new ResponseEntity<>(truckBusiness.load(truck), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Actualiza un camión existente con los datos proporcionados.
     *
     * @param truck Objeto {@link Truck} con los datos actualizados del camión.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la actualización es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el camión no existe (HTTP 404)
     *         o si el camión existe (HTTP 302).
     */
    @Operation(operationId = "update-truck", summary = "Actualiza un camión existente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Camión con datos a actualizar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Truck.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión actualizado correctamente."),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "302", description = "Conflicto: camión ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "")
    public ResponseEntity<?> update(@RequestBody Truck truck) {
        try {
            truckBusiness.update(truck);
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
     * Elimina un camion existente según su identificador.
     *
     * @param id Identificador numérico del camion a eliminar.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la eliminación es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el camion no existe (HTTP 404).
     */
    @Operation(operationId = "delete-truck", summary = "Elimina un camión por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del camión a eliminar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión eliminado correctamente."),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        try {
            truckBusiness.delete(id);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
}
