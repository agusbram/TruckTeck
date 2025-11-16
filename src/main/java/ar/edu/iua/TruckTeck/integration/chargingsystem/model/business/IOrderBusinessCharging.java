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
    public Order addExternalCharging(String json) throws BusinessException, EmptyFieldException, NotFoundException;

    /**
    * Obtiene el valor preestablecido (preset) asociado a un número y código de activación determinados.
    *
    * @param number Número de referencia o identificador asociado al preset.
    * @param activationCode Código de activación utilizado para validar el acceso al preset.
    * @return El valor preestablecido (preset) correspondiente al número y código proporcionados.
    * @throws BusinessException Si ocurre un error inesperado en la capa de negocio.
    * @throws NotFoundException Si no se encuentra un preset asociado al número o código de activación especificado.
    */
    public Double getPreset(String number, String activationCode) throws BusinessException, NotFoundException;

    /**
    * Cambia el estado de una orden a “cargada” (loaded) según el número de referencia proporcionado.
    *
    * @param number Número de referencia o identificador de la orden cuyo estado será modificado.
    * @return La orden actualizada después de cambiar su estado a “cargada”.
    * @throws BusinessException Si ocurre un error inesperado en la capa de negocio.
    * @throws NotFoundException Si no se encuentra una orden con el número de referencia especificado.
    */
    public Order changeStateLoaded(String number) throws BusinessException, NotFoundException;

}
