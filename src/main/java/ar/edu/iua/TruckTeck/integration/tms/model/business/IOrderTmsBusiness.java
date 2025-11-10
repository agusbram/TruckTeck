package ar.edu.iua.TruckTeck.integration.tms.model.business;

import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

/**
 * Interfaz de negocio para la gestión de pesajes TMS (Terminal Management System).
 * <p>
 * Define las operaciones necesarias para registrar los pesajes inicial y final
 * de las órdenes de carga, integrándose con el sistema externo de balanza.
 * </p>
 */
public interface IOrderTmsBusiness {
    
    /**
     * Registra el pesaje inicial (tara) de un camión vacío.
     * <p>
     * Busca una orden en estado PENDING asociada al dominio del camión,
     * registra el peso inicial y genera un código de activación de 5 dígitos.
     * La orden pasa al estado TARA_REGISTERED.
     * </p>
     * 
     * @param number numero de orden del camión
     * @param initialWeight Peso del camión vacío en kilogramos
     * @return La orden actualizada con el pesaje inicial registrado
     * @throws BusinessException Si ocurre un error en la lógica de negocio
     * @throws NotFoundException Si no se encuentra una orden pendiente para el camión
     * @throws FoundException Si la orden ya tiene un pesaje inicial registrado
     */
    Order registerInitialWeighing(String number, Double initialWeight) 
        throws BusinessException, NotFoundException, FoundException;

    /**
     * Registra el pesaje final de un camión cargado.
     * <p>
     * Busca una orden en estado LOADING mediante su código de activación,
     * registra el peso final del camión cargado y calcula el peso neto.
     * La orden pasa al estado FINALIZED.
     * </p>
     * 
     * @param activationCode Código de activación de 5 dígitos generado en el pesaje inicial
     * @param finalWeight Peso del camión cargado en kilogramos
     * @return La orden actualizada con el pesaje final registrado
     * @throws BusinessException Si ocurre un error en la lógica de negocio
     * @throws NotFoundException Si no se encuentra una orden con el código de activación
     * @throws FoundException Si la orden no está en estado LOADING
     */
    Order registerFinalWeighing(String activationCode, Double finalWeight) 
        throws BusinessException, NotFoundException, FoundException;
}
