package ar.edu.iua.TruckTeck.integration.chargingsystem.model.business;

import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.EmptyFieldException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

public interface IOrderBusinessCharging {

    /**
     * Agrega una nueva orden a partir de una representación externa en formato JSON.
     *
     * @param json Cadena en formato JSON que contiene los datos de la orden a registrar.
     * @return Los detalles durante la carga.
     * @throws BusinessException Si ocurre un error inesperado en la capa de negocio.
     * @throws EmptyFieldException Si el nombre del producto viene vacío o es nulo, entonces se lanza esta excepcion
     */
    public Order addExternalCharging(String json) throws BusinessException, EmptyFieldException;

    public Double getPreset(String number, String activationCode) throws BusinessException, NotFoundException;

}
