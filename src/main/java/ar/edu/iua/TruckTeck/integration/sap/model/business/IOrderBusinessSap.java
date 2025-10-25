package ar.edu.iua.TruckTeck.integration.sap.model.business;


import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.IOrderBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.EmptyFieldException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;

public interface IOrderBusinessSap extends IOrderBusiness {
    /**
     * Agrega una nueva orden a partir de una representación externa en formato JSON.
     *
     * @param json Cadena en formato JSON que contiene los datos de la orden a registrar.
     * @return La orden agregada, con la información generada tras su registro.
     * @throws FoundException Si ya existe una orden con las mismas características o identificador.
     * @throws BusinessException Si ocurre un error inesperado en la capa de negocio.
     * @throws EmptyFieldException Si el nombre del producto viene vacío o es nulo, entonces se lanza esta excepcion
     */
     public Order addExternalSap(String json) throws FoundException, BusinessException, EmptyFieldException;
}
