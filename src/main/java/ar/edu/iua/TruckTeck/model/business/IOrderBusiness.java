package ar.edu.iua.TruckTeck.model.business;

import java.util.List;

import ar.edu.iua.TruckTeck.model.Conciliation;
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
    * Carga una orden específica a partir número de orden.
    *
    * @param number Número de la orden a cargar.
    * @return orden correspondiente al número proporcionado.
    * @throws BusinessException Si ocurre un error en la lógica de negocio.
    * @throws NotFoundException  Si no se encuentra una orden con el número dado.
    */
    public Order load(String number) throws BusinessException, NotFoundException;


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

    /**
     * Obtiene la conciliación de una orden finalizada.
     * <p>
     * Calcula y retorna todos los datos de conciliación incluyendo pesos,
     * diferencias y promedios de parámetros de carga.
     * </p>
     *
     * @param number Número de la orden para la cual se solicita la conciliación.
     * @return Objeto {@link Conciliation} con todos los datos calculados.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si no se encuentra la orden con el número especificado.
     */
    public Conciliation findConciliation(String number) throws BusinessException, NotFoundException;

    // public Integer registerTare(long orderNumber, float tareWeight) throws BusinessException, NotFoundException;
    
    // public void addDetail(long orderNumber, OrderDetail detail) throws BusinessException, NotFoundException;
    
    // public void closeOrder(long orderNumber) throws BusinessException, NotFoundException;
    
    // public Map<String, Object> finalizeOrderAndReconcile(long orderNumber, float finalWeight) throws BusinessException, NotFoundException;
    
    // public Map<String, Object> getReconciliation(long orderNumber) throws BusinessException, NotFoundException;
}
