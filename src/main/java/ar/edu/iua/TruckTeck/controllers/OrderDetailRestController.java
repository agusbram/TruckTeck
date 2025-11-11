package ar.edu.iua.TruckTeck.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.model.business.IOrderBusiness;
import ar.edu.iua.TruckTeck.model.business.IOrderDetailBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;

@RestController
@RequestMapping(Constants.URL_ORDERS)
public class OrderDetailRestController {

    /**
     * Componente de negocio encargado de construir respuestas estándar.
     */
    @Autowired
    private IStandardResponseBusiness response;

    /**
     * Componente de negocio encargado de la lógica de órdenes.
     */
    @Autowired
    private IOrderDetailBusiness orderBusiness;

    @GetMapping(value = "/detail/{id}")
    public ResponseEntity<?> list(@PathVariable long id) {
        try {
            return new ResponseEntity<>(orderBusiness.listId(id), HttpStatus.OK);
        } catch(BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
             HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
