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

import ar.edu.iua.TruckTeck.model.Product;
import ar.edu.iua.TruckTeck.model.business.IProductBusiness;
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
 * Controlador REST para la gestión de productos.
 * <p>
 * Proporciona endpoints para listar y agregar productos mediante solicitudes HTTP.
 * Utiliza las rutas definidas en {@link Constants#URL_PRODUCTS}.
 * </p>
 */
@RestController
@RequestMapping(Constants.URL_PRODUCTS)
@Tag(description = "API Servicios relacionados con Productos", name = "Product")
public class ProductRestController {

    /**
     * Componente de negocio encargado de construir respuestas estándar.
     */
    @Autowired
    private IStandardResponseBusiness response;

    /**
     * Componente de negocio encargado de la lógica de productos.
     */
    @Autowired
    private IProductBusiness productBusiness;

    /**
     * Endpoint para obtener la lista de todos los productos.
     * <p>
     * Responde a solicitudes HTTP GET y devuelve los productos en formato JSON.
     * </p>
     *
     * @return ResponseEntity que contiene la lista de productos y el código HTTP correspondiente.
     *         - {@link HttpStatus#OK} si la operación fue exitosa.
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "list-products", summary = "Lista todos los productos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la lista de productos.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Product.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> list() {
        try {
            return new ResponseEntity<>(productBusiness.list(), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para agregar un nuevo producto.
     * <p>
     * Responde a solicitudes HTTP POST con un objeto {@link Product} en el cuerpo de la solicitud.
     * Devuelve la ubicación del nuevo recurso en el encabezado HTTP 'Location'.
     * </p>
     *
     * @param product El producto a agregar.
     * @return ResponseEntity que indica el resultado de la operación.
     *         - {@link HttpStatus#CREATED} si el producto se creó correctamente.
     *         - {@link HttpStatus#FOUND} si ya existe un producto similar ({@link FoundException}).
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un {@link BusinessException}.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "")
    @Operation(operationId = "add-product", summary = "Crea un nuevo producto.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Producto a crear", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado. Se retorna header 'location' con la URI del nuevo recurso."),
        @ApiResponse(responseCode = "302", description = "Producto ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> add(@RequestBody Product product) {
        try {
            Product response = productBusiness.add(product);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_PRODUCTS + "/" + response.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }

    /**
     * Obtiene un producto por su identificador único.
     *
     * @param id Identificador numérico del producto a buscar.
     * @return Un {@link ResponseEntity} que contiene el producto encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el producto (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "load-product", summary = "Este servicio permite cargar un producto por su id.")
	@Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del producto.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Devuelve un Producto.", content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)) }),
			@ApiResponse(responseCode = "500", description = "Error interno", content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)) }), 
			/* Se agrega a futuro para el final
            @ApiResponse(responseCode = "403", description = "No posee autorización para consumir este servicio", content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)) }),  */
			@ApiResponse(responseCode = "404", description = "No se encuentra el producto para el identificador informado", content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)) }) 
			
	})
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> load(@PathVariable long id) {
        try {
            return new ResponseEntity<>(productBusiness.load(id), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
     /**
     * Obtiene un producto por su nombre.
     *
     * @param product Nombre del producto a buscar.
     * @return Un {@link ResponseEntity} que contiene el producto encontrado (HTTP 200 OK),
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si no se encuentra el producto (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/by-name/{product}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "load-product-by-name", summary = "Carga un producto por su nombre.")
    @Parameter(in = ParameterIn.PATH, name = "product", schema = @Schema(type = "string"), required = true, description = "Nombre del producto a buscar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve un Producto.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))}),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "No se encuentra el producto para el nombre informado", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))})
    })
    public ResponseEntity<?> load(@PathVariable String product) {
        try {
            return new ResponseEntity<>(productBusiness.load(product), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Actualiza un producto existente con los datos proporcionados.
     *
     * @param product Objeto {@link Product} con los datos actualizados del producto.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la actualización es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el producto no existe (HTTP 404)
     *         o si el producto existe (HTTP 302).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "")
    @Operation(operationId = "update-product", summary = "Actualiza un producto existente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Producto con datos a actualizar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente."),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "302", description = "Conflicto: producto ya existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> update(@RequestBody Product product) {
        try {
            productBusiness.update(product);
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
     * Elimina un producto existente según su identificador.
     *
     * @param id Identificador numérico del producto a eliminar.
     * @return Un {@link ResponseEntity} con estado HTTP 200 si la eliminación es exitosa,
     *         o un mensaje de error si ocurre una excepción de negocio (HTTP 500)
     *         o si el producto no existe (HTTP 404).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{id}")
    @Operation(operationId = "delete-product", summary = "Elimina un producto por su id.")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "long"), required = true, description = "Identificador del producto a eliminar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente."),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<?> delete(@PathVariable long id) {
        try {
            productBusiness.delete(id);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
}