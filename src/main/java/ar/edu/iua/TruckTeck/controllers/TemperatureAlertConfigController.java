package ar.edu.iua.TruckTeck.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;
import ar.edu.iua.TruckTeck.model.business.ITemperatureAlertConfigBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;
import ar.edu.iua.TruckTeck.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de gestionar la configuración de alertas de temperatura.
 * 
 * <p>Permite:
 * <ul>
 *   <li>Actualizar una configuración existente</li>
 *   <li>Resetear el estado del envío de correo (emailAlreadySent)</li>
 *   <li>Registrar la primera configuración del sistema</li>
 * </ul>
 * </p>
 *
 * <p>Las operaciones delegan la lógica de negocio a {@link ITemperatureAlertConfigBusiness}
 * y construyen las respuestas estándar con {@link IStandardResponseBusiness}.</p>
 */
@RestController
@RequestMapping(Constants.URL_ALARM)
@Tag(description = "API Servicios relacionados con Alertas de Temperatura", name = "Alarm")
public class TemperatureAlertConfigController {

    @Autowired
    private IStandardResponseBusiness response;

    @Autowired
    private ITemperatureAlertConfigBusiness temperatureAlertConfigBusiness;

    /**
     * Actualiza la configuración de alerta de temperatura existente.
     *
     * @param temp Objeto con el nuevo umbral y la lista de correos destinatarios.
     * @return {@code 200 OK} con la configuración actualizada,
     *         o {@code 404 Not Found} si la configuración no existe.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(operationId = "update-alarm-config", summary = "Actualiza la configuración de alerta de temperatura.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Configuración a actualizar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = TemperatureAlertConfig.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devuelve la configuración actualizada.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TemperatureAlertConfig.class))}),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateConfig(@RequestBody TemperatureAlertConfig temp) {
        try {
            TemperatureAlertConfig updated =
                    temperatureAlertConfigBusiness.updateConfig(temp.getThreshold(), temp.getEmails());

            return new ResponseEntity<>(updated, HttpStatus.OK);

        } catch (NotFoundException e) {
            return new ResponseEntity<>(
                response.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        }
    }

    /**
     * Resetea la bandera {@code emailAlreadySent} que indica si ya se envió
     * una notificación por temperatura excedida.
     *
     * @return {@code 200 OK} si la bandera fue reseteada correctamente,
     *         o {@code 404 Not Found} si no se encuentra la configuración.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(operationId = "reset-alarm-email-flag", summary = "Resetea la flag de envío de correo (emailAlreadySent).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "La flag fue reseteada correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PutMapping(value = "/reset-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resetEmailSent() {
        try {
            temperatureAlertConfigBusiness.resetEmailSent();
            return new ResponseEntity<>(
                response.build(HttpStatus.OK, null, "La flag emailAlreadySent fue reseteada correctamente"),
                HttpStatus.OK
            );

        } catch (NotFoundException e) {
            return new ResponseEntity<>(
            response.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
            HttpStatus.NOT_FOUND
            );
        }
    }

    /**
     * Registra la primera configuración del sistema.
     *
     * <p>Este endpoint debe utilizarse únicamente cuando aún no existe una configuración previa.
     * Crea un nuevo registro con el umbral y la lista de correos proporcionados.</p>
     *
     * @param temp Configuración inicial a registrar.
     * @return {@code 201 Created} con la cabecera {@code Location} apuntando al recurso creado,
     *         o {@code 404 Not Found} si la operación no puede completarse.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(operationId = "create-first-alarm-config", summary = "Crea la primera configuración del sistema.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Configuración inicial a registrar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = TemperatureAlertConfig.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Configuración creada. Se retorna header 'location' con la URI del nuevo recurso."),
        @ApiResponse(responseCode = "404", description = "Operación no válida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping(value = "/first")
    public ResponseEntity<?> firstConfig(@RequestBody TemperatureAlertConfig temp) {
        try {
            TemperatureAlertConfig config = temperatureAlertConfigBusiness.firstConfig(temp.getThreshold(), temp.getEmails());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_ALARM + "/" + config.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(
                response.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        }
    }
}

