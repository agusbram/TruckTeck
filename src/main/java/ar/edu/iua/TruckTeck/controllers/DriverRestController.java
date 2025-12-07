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

import ar.edu.iua.TruckTeck.model.Driver;
import ar.edu.iua.TruckTeck.model.business.IDriverBusiness;
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
 * Controlador REST para la gestión de choferes.
 * <p>
 * Proporciona endpoints para listar y agregar choferes mediante solicitudes HTTP.
 * Utiliza las rutas definidas en {@link Constants#URL_DRIVERS}.
 * </p>
 */
@RestController
@RequestMapping(Constants.URL_DRIVERS)
@Tag(description = "API Servicios relacionados con Choferes", name = "Driver")
public class DriverRestController {

    /**
     * Componente de negocio encargado de construir respuestas estándar.
     */
    @Autowired
    private IStandardResponseBusiness response;

    /**
     * Componente de negocio encargado de la lógica de conductores/choferes.
     */
    @Autowired
    private IDriverBusiness driverBusiness;

    /**
     * Endpoint para obtener la lista de todos los choferes.
     * <p>
     * Responde a solicitudes HTTP GET y devuelve los choferes en formato JSON.
     * </p>
     *
     * @return ResponseEntity que contiene la lista de choferes y el código HTTP correspondiente.
     *         - {@link HttpStatus#OK} si la operación fue exitosa.
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "list-drivers", summary = "Lista todos los choferes.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la lista de choferes.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Driver.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> list() {
        try {
            return new ResponseEntity<>(driverBusiness.list(), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para agregar un nuevo chofer.
     * <p>
     * Responde a solicitudes HTTP POST con un objeto {@link Driver} en el cuerpo de la solicitud.
     * Devuelve la ubicación del nuevo recurso en el encabezado HTTP 'Location'.
     * </p>
     *
     * @param driver El chofer a agregar.
     * @return ResponseEntity que indica el resultado de la operación.
     *         - {@link HttpStatus#CREATED} si el chofer se creó correctamente.
     *         - {@link HttpStatus#FOUND} si ya existe un chofer similar ({@link FoundException}).
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "")
    @Operation(operationId = "add-driver", summary = "Crea un nuevo chofer.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Chofer a crear", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Driver.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Chofer creado. Se retorna header 'location' con la URI del nuevo recurso."),
        @ApiResponse(responseCode = "302", description = "Chofer ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> add(@RequestBody Driver driver) {
        try {
            Driver response = driverBusiness.add(driver);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_DRIVERS + "/" + response.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }

    /**
     * Obtiene un chofer por su identificador único.
     *
     * @param id Identificador numérico del chofer a buscar.
     * @return Un {@link ResponseEntity} que contiene el chofer encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el chofer (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}")
    @Operation(operationId = "load-driver", summary = "Carga un chofer por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del chofer.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve un Chofer.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Driver.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra el chofer para el identificador informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    public ResponseEntity<?> load(@PathVariable long id) {
        try {
            return new ResponseEntity<>(driverBusiness.load(id), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
     /**
     * Obtiene un chofer por su numero de documento.
     *
     * @param documentNumber Numero de documento del chofer a buscar.
     * @return Un {@link ResponseEntity} que contiene el chofer encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el chofer (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/by-document/{documentNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "load-driver-by-document", summary = "Carga un chofer por su número de documento.")
    @Parameter(in = ParameterIn.PATH, name = "documentNumber", schema = @Schema(type = "string"), required = true, description = "Número de documento del chofer a buscar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve un Chofer.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Driver.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra el chofer para el número de documento informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    public ResponseEntity<?> load(@PathVariable String documentNumber) {
        try {
            return new ResponseEntity<>(driverBusiness.load(documentNumber), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Actualiza un chofer existente con los datos proporcionados.
     *
     * @param driver Objeto {@link Driver} con los datos actualizados del chofer.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la actualización es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el chofer no existe (HTTP 404)
     *         o si el chofer existe (HTTP 302).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "")
    @Operation(operationId = "update-driver", summary = "Actualiza un chofer existente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Chofer con datos a actualizar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Driver.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chofer actualizado correctamente."),
        @ApiResponse(responseCode = "404", description = "Chofer no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "302", description = "Conflicto: chofer ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> update(@RequestBody Driver driver) {
        try {
            driverBusiness.update(driver);
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
     * Elimina un chofer existente según su identificador.
     *
     * @param id Identificador numérico del chofer a eliminar.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la eliminación es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el chofer no existe (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{id}")
    @Operation(operationId = "delete-driver", summary = "Elimina un chofer por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del chofer a eliminar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chofer eliminado correctamente."),
        @ApiResponse(responseCode = "404", description = "Chofer no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> delete(@PathVariable long id) {
        try {
            driverBusiness.delete(id);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
}
