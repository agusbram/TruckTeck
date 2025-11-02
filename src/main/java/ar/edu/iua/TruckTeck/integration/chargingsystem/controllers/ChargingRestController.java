package ar.edu.iua.TruckTeck.integration.chargingsystem.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;
import ar.edu.iua.TruckTeck.controllers.Constants;
import ar.edu.iua.TruckTeck.integration.chargingsystem.model.business.IOrderBusinessCharging;

@RestController
@RequestMapping(Constants.URL_ORDERS_CHARGING)
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
    @GetMapping(value = "/number/{number}/code/{code}")
    public ResponseEntity<?> load(@PathVariable String number,@PathVariable String code) {
        try {
            return new ResponseEntity<>(orderBusiness.getPreset(number,code), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

}
