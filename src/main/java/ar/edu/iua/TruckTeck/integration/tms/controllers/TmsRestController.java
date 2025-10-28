package ar.edu.iua.TruckTeck.integration.tms.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.iua.TruckTeck.controllers.Constants;
import ar.edu.iua.TruckTeck.integration.tms.model.business.IOrderTmsBusiness;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;

/**
 * Controlador REST para la integración con el sistema externo de balanza TMS (Terminal Management System).
 * <p>
 * Este controlador expone endpoints HTTP que son consumidos por el sistema de balanza
 * para registrar los pesos de los camiones en dos momentos clave del proceso de carga:
 * </p>
 * 
 * <p><b>Flujo de Integración TMS:</b></p>
 * <ol>
 *   <li><b>Pesaje Inicial (Tara):</b> El camión vacío llega a la balanza y se registra su peso</li>
 *   <li><b>Carga:</b> El camión se carga con el producto (gestionado por otros sistemas)</li>
 *   <li><b>Pesaje Final:</b> El camión cargado vuelve a la balanza para registrar el peso final</li>
 * </ol>
 * 
 * <p><b>Endpoints disponibles:</b></p>
 * <ul>
 *   <li><code>POST /api/v1/tms/weighing/initial</code> - Registra el pesaje inicial (tara)</li>
 *   <li><code>POST /api/v1/tms/weighing/final</code> - Registra el pesaje final</li>
 * </ul>
 * 
 * <p><b>Formato de Respuesta:</b></p>
 * <pre>
 * {
 *   "success": true,
 *   "message": "Pesaje registrado correctamente",
 *   "data": {
 *     "orderNumber": 123,
 *     "activationCode": "48765",
 *     "truckDomain": "AB805",
 *     "weight": 8500.5,
 *     "timestamp": "2025-10-25T15:13:04.466472655",
 *     "state": "TARA_REGISTERED"
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Las respuestas están diseñadas para ser mínimas, exponiendo solo la información
 * necesaria para el sistema externo sin comprometer datos sensibles del negocio.
 * </p>
 * 
 * @see ar.edu.iua.TruckTeck.integration.tms.model.business.IOrderTmsBusiness
 * @see ar.edu.iua.TruckTeck.model.enums.OrderState
 */
@RestController
@RequestMapping(value = Constants.URL_TMS, produces = MediaType.APPLICATION_JSON_VALUE)
public class TmsRestController {

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
     * Registra el pesaje inicial (tara) de un camión vacío.
     * <p>
     * <b>Endpoint:</b> {@code POST /api/v1/tms/weighing/initial}
     * </p>
     * 
     * <p><b>Flujo de operación:</b></p>
     * <ol>
     *   <li>El sistema de balanza envía el dominio del camión y su peso vacío</li>
     *   <li>Se busca una orden en estado PENDING para ese camión</li>
     *   <li>Se registra el peso inicial y se genera un código de activación único</li>
     *   <li>La orden pasa al estado TARA_REGISTERED</li>
     *   <li>Se devuelve el código de activación para ser usado en el pesaje final</li>
     * </ol>
     * 
     * <p><b>Requisitos previos:</b></p>
     * <ul>
     *   <li>Debe existir una orden en estado PENDING para el camión especificado</li>
     *   <li>El camión debe estar registrado en el sistema</li>
     * </ul>
     * 
     * <p><b>Request Body (JSON):</b></p>
     * <pre>
     * {
     *   "domain": "AB805",      // Dominio/patente del camión
     *   "weight": 8500.5        // Peso del camión vacío en kg
     * }
     * </pre>
     * 
     * <p><b>Response exitosa (200 OK):</b></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Pesaje inicial registrado correctamente",
     *   "data": {
     *     "orderNumber": 1,
     *     "activationCode": "48765",           // Código para pesaje final
     *     "truckDomain": "AB805",
     *     "weight": 8500.5,
     *     "timestamp": "2025-10-25T15:13:04.466472655",
     *     "state": "TARA_REGISTERED"
     *   }
     * }
     * </pre>
     * 
     * <p><b>Errores posibles:</b></p>
     * <ul>
     *   <li><b>404 NOT_FOUND:</b> No existe una orden pendiente para el camión</li>
     *   <li><b>400 BAD_REQUEST:</b> Error en la lógica de negocio (ej: orden en estado incorrecto)</li>
     *   <li><b>500 INTERNAL_SERVER_ERROR:</b> Error interno del servidor</li>
     * </ul>
     * 
     * @param request Mapa con los parámetros domain (String) y weight (Double o Integer)
     * @return ResponseEntity con la respuesta estructurada y código HTTP correspondiente
     */
    @PostMapping(value = "/weighing/initial", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerInitialWeighing(HttpEntity<String> httpEntity) {
        try {
            // Parsear body JSON de forma explícita para tener control total
            String body = httpEntity.getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);

            // Extraer y validar parámetros del JSON
            if (node.get("domain") == null || node.get("weight") == null) {
                return new ResponseEntity<>(
                        standardResponse.build(HttpStatus.BAD_REQUEST, null, "Faltan campos 'domain' o 'weight'"),
                        HttpStatus.BAD_REQUEST);
            }

            String domain = node.get("domain").asText();
            // Soportar weight como Integer o Double para flexibilidad
            JsonNode weightNode = node.get("weight");
            Double weight = weightNode.isNumber() ? weightNode.asDouble() : Double.parseDouble(weightNode.asText());

            log.info("TMS API: Recibiendo pesaje inicial para camión: {}, peso: {}", domain, weight);

            // Registrar el pesaje inicial en la capa de negocio
            Order order = orderTmsBusiness.registerInitialWeighing(domain, weight);

            log.info("TMS API: Pesaje inicial registrado. Orden: {}, Código: {}", order.getNumber(), order.getActivationCode());

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
        } catch (JsonProcessingException e) {
            log.error("TMS API: JSON inválido en pesaje inicial", e);
            return new ResponseEntity<>(
                    standardResponse.build(HttpStatus.BAD_REQUEST, e, "JSON inválido"),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("TMS API: Error interno al registrar pesaje inicial", e);
            return new ResponseEntity<>(
                standardResponse.build(HttpStatus.INTERNAL_SERVER_ERROR, e, "Error interno del servidor"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Registra el pesaje final de un camión cargado.
     * <p>
     * <b>Endpoint:</b> {@code POST /api/v1/tms/weighing/final}
     * </p>
     * 
     * <p><b>Flujo de operación:</b></p>
     * <ol>
     *   <li>El sistema de balanza envía el código de activación y el peso final</li>
     *   <li>Se busca la orden correspondiente mediante el código de activación</li>
     *   <li>Se valida que la orden esté en estado LOADING (carga finalizada)</li>
     *   <li>Se registra el peso final</li>
     *   <li>Se calcula el peso neto (peso final - peso inicial)</li>
     *   <li>Se calcula la diferencia con el caudalímetro para conciliación</li>
     *   <li>La orden pasa al estado FINALIZED</li>
     * </ol>
     * 
     * <p><b>Requisitos previos:</b></p>
     * <ul>
     *   <li>La orden debe tener un pesaje inicial registrado</li>
     *   <li>La orden debe estar en estado LOADING (carga finalizada)</li>
     *   <li>El código de activación debe ser válido</li>
     * </ul>
     * 
     * <p><b>Request Body (JSON):</b></p>
     * <pre>
     * {
     *   "activationCode": "48765",    // Código generado en pesaje inicial
     *   "weight": 24000.75            // Peso del camión cargado en kg
     * }
     * </pre>
     * 
     * <p><b>Response exitosa (200 OK):</b></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Pesaje final registrado correctamente",
     *   "data": {
     *     "orderNumber": 1,
     *     "truckDomain": "AB805",
     *     "weight": 24000.75,                    // Peso final
     *     "timestamp": "2025-10-25T16:30:00",
     *     "state": "FINALIZED",
     *     "netWeight": 15500.25,                 // peso final - peso inicial
     *     "balanceDifference": 50.25             // Diferencia balanza vs caudalímetro
     *   }
     * }
     * </pre>
     * 
     * <p><b>Datos de conciliación:</b></p>
     * <ul>
     *   <li><b>netWeight:</b> Peso neto cargado = pesoFinal - pesoInicial</li>
     *   <li><b>balanceDifference:</b> Diferencia entre balanza y caudalímetro = netWeight - accumulatedMass</li>
     * </ul>
     * 
     * <p><b>Errores posibles:</b></p>
     * <ul>
     *   <li><b>404 NOT_FOUND:</b> No existe una orden con ese código de activación</li>
     *   <li><b>400 BAD_REQUEST:</b> La orden no está en estado LOADING o falta pesaje inicial</li>
     *   <li><b>500 INTERNAL_SERVER_ERROR:</b> Error interno del servidor</li>
     * </ul>
     * 
     * @param request Mapa con los parámetros activationCode (String) y weight (Double o Integer)
     * @return ResponseEntity con la respuesta estructurada incluyendo datos de conciliación
     */
    @PostMapping(value = "/weighing/final", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerFinalWeighing(HttpEntity<String> httpEntity) {
        try {
            // Parsear body JSON de forma explícita para tener control total
            String body = httpEntity.getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);

            // Extraer y validar parámetros del JSON
            if (node.get("activationCode") == null || node.get("weight") == null) {
                return new ResponseEntity<>(
                        standardResponse.build(HttpStatus.BAD_REQUEST, null, "Faltan campos 'activationCode' o 'weight'"),
                        HttpStatus.BAD_REQUEST);
            }

            String activationCode = node.get("activationCode").asText();
            // Soportar weight como Integer o Double para flexibilidad
            JsonNode weightNode = node.get("weight");
            Double weight = weightNode.isNumber() ? weightNode.asDouble() : Double.parseDouble(weightNode.asText());

            log.info("TMS API: Recibiendo pesaje final para código: {}, peso: {}", activationCode, weight);

            // Registrar el pesaje final en la capa de negocio
            Order order = orderTmsBusiness.registerFinalWeighing(activationCode, weight);

            // Calcular datos de conciliación
            Double netWeight = order.getFinalWeight() - order.getInitialWeight();
            Double accumulatedMass = order.getAccumulatedMass() != null ? order.getAccumulatedMass() : 0.0;
            Double balanceDifference = netWeight - accumulatedMass;

            log.info("TMS API: Pesaje final registrado. Orden: {}, Diferencia balanza-caudalímetro: {} kg", order.getNumber(), balanceDifference);

            // Respuesta mínima: devolvemos únicamente la ubicación del recurso actualizado
            HttpHeaders responseHeaders = new HttpHeaders();
            // Location hacia la orden actualizada
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
        } catch (JsonProcessingException e) {
            log.error("TMS API: JSON inválido en pesaje final", e);
            return new ResponseEntity<>(
                    standardResponse.build(HttpStatus.BAD_REQUEST, e, "JSON inválido"),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("TMS API: Error interno al registrar pesaje final", e);
            return new ResponseEntity<>(
                standardResponse.build(HttpStatus.INTERNAL_SERVER_ERROR, e, "Error interno del servidor"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
