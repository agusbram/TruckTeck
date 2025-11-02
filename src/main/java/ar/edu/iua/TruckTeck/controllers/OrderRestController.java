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

import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.IOrderBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;

/**
 * Controlador REST para la gestión de órdenes.
 * <p>
 * Proporciona endpoints para listar y agregar órdenes mediante solicitudes HTTP.
 * Utiliza las rutas definidas en {@link Constants#URL_ORDERS}.
 * </p>
 */
@RestController
@RequestMapping(Constants.URL_ORDERS)
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

}

    



