package ar.edu.iua.TruckTeck.model.business;

import java.util.List;

import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

public interface IOrderBusiness {
    /**
     * Obtiene la lista completa de órdenes.
     *
     * @return Lista de órdenes existentes.
     * @throws BusinessException Si ocurre un error general en la lógica de negocio.
     */
    public List<Order> list() throws BusinessException;


    /**
    * Carga una orden específica a partir de su identificador único.
    *
    * @param id Identificador de la orden a cargar.
    * @return orden correspondiente al identificador proporcionado.
    * @throws BusinessException Si ocurre un error en la lógica de negocio.
    * @throws NotFoundException  Si no se encuentra una orden con el identificador dado.
    */
    public Order load(Long id) throws BusinessException, NotFoundException;

    /**
     * Agrega una nueva orden al sistema.
     *
     * @param order orden a agregar.
     * @return orden agregada, incluyendo su identificador generado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws FoundException    Si ya existe una orden igual en el sistema.
     */
    public Order add(Order order) throws BusinessException, FoundException;

    /**
     * Elimina una orden del sistema.
     *
     * @param order orden a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si la orden a eliminar no existe.
     */
    public void delete(Order order) throws BusinessException, NotFoundException;

    /**
     * Actualiza los datos de una orden existente.
     *
     * @param order orden con los datos actualizados.
     * @return orden actualizada.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si la orden a actualizar no existe.
     * @throws FoundException Si ya existe una orden igual en el sistema.
     */
    public Order update(Order order) throws BusinessException, NotFoundException, FoundException;

    /**
     * Elimina una orden del sistema a partir de su identificador.
     *
     * @param id Identificador de la orden a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si la orden a eliminar no existe.
     */
    public void delete(Long id) throws BusinessException, NotFoundException;
}
