package ar.edu.iua.TruckTeck.integration.tms.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ar.edu.iua.TruckTeck.controllers.Constants;
import ar.edu.iua.TruckTeck.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.iua.TruckTeck.integration.tms.model.business.IOrderTmsBusiness;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;


@RestController
@RequestMapping(value = Constants.URL_TMS, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "TMS", description = "API Integración TMS para pesajes")
@PreAuthorize("hasRole('ADMIN') or hasRole('TMS')")
public class TmsRestController {

    /**
     * Logger para la clase {@link TmsRestController}.
     */
    private static final Logger log = LoggerFactory.getLogger(TmsRestController.class);

    /**
     * Servicio de negocio para operaciones TMS.
     */
    @Autowired
    private IOrderTmsBusiness orderTmsBusiness;

    /**
     * Utilidad para construir respuestas estándar de error.
     */
    @Autowired
    private IStandardResponseBusiness standardResponse;

    /**
     * Registra el pesaje inicial de una orden basada en el número de orden y el peso proporcionado.
     * <p>
     * <b>Endpoint:</b> {@code POST /api/v1/tms/b2b/weighing/initial}
     * </p>
     * <p>
     * Recibe el body como JSON con los campos {@code number} (número de orden) y {@code initialWeight} (peso inicial en kg).
     * Busca la orden por número, valida que esté en estado PENDING, registra el peso inicial, genera un código de activación
     * y cambia el estado a TARA_REGISTERED.
     * </p>
     * 
     * @param orderBody Objeto {@link Order} con los datos del pesaje inicial (number y initialWeight).
     * @return {@link ResponseEntity} con:
     *         - {@link HttpStatus#OK} si el pesaje se registró correctamente (incluye header "location"),
     *         - {@link HttpStatus#NOT_FOUND} si no se encuentra la orden,
     *         - {@link HttpStatus#INTERNAL_SERVER_ERROR} si ocurre un error de negocio.
     */
    @Operation(operationId = "register-initial-weighing", summary = "Registra el pesaje inicial de una orden (TMS)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON con fields: number (string) y initialWeight (double)", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pesaje inicial registrado (header 'location' con recurso)."),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping(value = "b2b/weighing/initial")
    //public ResponseEntity<?> registerInitialWeighing(@PathVariable String number, @PathVariable Double weight) {
    public ResponseEntity<?> registerInitialWeighing(@RequestBody Order orderBody) {
        String number = orderBody.getNumber();
        Double initialWeight = orderBody.getInitialWeight();

        try {

            log.info("TMS API: Recibiendo pesaje inicial para camión: {}, peso: {}", number, initialWeight);

            // Registrar el pesaje inicial en la capa de negocio
            Order order = orderTmsBusiness.registerInitialWeighing(number, initialWeight);

            log.info("TMS API: Pesaje inicial registrado. Orden ID: {}, Código: {}", order.getId(), order.getActivationCode());

            // Respuesta mínima: devolvemos únicamente la ubicación del recurso (orden)
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_ORDERS + "/" + order.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);

        } catch (NotFoundException e) {
            log.warn("TMS API: {}", e.getMessage());
            return new ResponseEntity<>(
                standardResponse.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        } catch (BusinessException e) {
            log.error("TMS API: Error de negocio: {}", e.getMessage());
            return new ResponseEntity<>(standardResponse.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("TMS API: Error interno al registrar pesaje inicial", e);
            return new ResponseEntity<>(
                standardResponse.build(HttpStatus.INTERNAL_SERVER_ERROR, e, "Error interno del servidor"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Registra el pesaje final de una orden basada en el número de orden y el peso final proporcionado.
     * <p>
     * <b>Endpoint:</b> {@code POST /api/v1/tms/b2b/weighing/final}
     * </p>
     * <p>
     * Recibe el body como JSON con los campos {@code number} (número de orden) y {@code finalWeight} (peso final en kg).
     * Busca la orden por número, valida que esté en estado LOADING, registra el peso final
     * y cambia el estado a FINALIZED.
     * </p>
     * 
     * @param orderBody Objeto {@link Order} con los datos del pesaje final (number y finalWeight).
     * @return {@link ResponseEntity} con:
     *         - {@link HttpStatus#OK} si el pesaje se registró correctamente (incluye header "location"),
     *         - {@link HttpStatus#NOT_FOUND} si no se encuentra la orden,
     *         - {@link HttpStatus#BAD_REQUEST} si la orden no está en estado correcto.
     */
    @Operation(operationId = "register-final-weighing", summary = "Registra el pesaje final de una orden (TMS)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON con fields: number (string) y finalWeight (double)", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pesaje final registrado (header 'location' con recurso)."),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o estado incorrecto", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping(value = "b2b/weighing/final")
    public ResponseEntity<?> registerFinalWeighing(@RequestBody Order orderBody) {
        String number = orderBody.getNumber();
        Double finalWeight = orderBody.getFinalWeight();


        try {
            log.info("TMS API: Recibiendo pesaje final para código: {}, peso: {}", number, finalWeight);

            // Registrar el pesaje final en la capa de negocio
            Order order = orderTmsBusiness.registerFinalWeighing(number, finalWeight);

            log.info("TMS API: Pesaje final registrado. Orden ID: {}, Peso: {}", order.getId(), finalWeight);

            // Respuesta mínima: devolvemos únicamente la ubicación del recurso (orden)
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_ORDERS + "/" + order.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);

        } catch (NotFoundException e) {
            log.warn("TMS API: {}", e.getMessage());
            return new ResponseEntity<>(
                standardResponse.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        } catch (BusinessException e) {
            log.error("TMS API: Error de negocio: {}", e.getMessage());
            return new ResponseEntity<>(
                standardResponse.build(HttpStatus.BAD_REQUEST, e, e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("TMS API: Error interno al registrar pesaje final", e);
            return new ResponseEntity<>(
                standardResponse.build(HttpStatus.INTERNAL_SERVER_ERROR, e, "Error interno del servidor"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
