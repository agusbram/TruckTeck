package ar.edu.iua.TruckTeck.model.business;


import java.util.List;

import ar.edu.iua.TruckTeck.model.Client;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;


/**
 * Interfaz que define las operaciones de negocio para la gestión de clientes.
 * <p>
 * Incluye métodos para listar, buscar, agregar, actualizar y eliminar clientes,
 * manejando las excepciones correspondientes a reglas de negocio y existencia de datos.
 * </p>
 */
public interface IClientBusiness {
    /**
     * Obtiene la lista completa de clientes.
     *
     * @return Lista de clientes existentes.
     * @throws BusinessException Si ocurre un error general en la lógica de negocio.
     */
    public List<Client> list() throws BusinessException;

     /**
     * Carga un cliente específico a partir de su identificador único.
     *
     * @param id Identificador del cliente a cargar.
     * @return cliente correspondiente al identificador proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un cliente con el identificador dado.
     */
    public Client load(Long id) throws BusinessException, NotFoundException;

    /**
     * Carga un cliente específico a partir de su nombre o descripción.
     *
     * @param client Nombre o descripción del cliente a cargar.
     * @return cliente correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un cliente con el nombre dado.
     */
    public Client load(String client) throws BusinessException, NotFoundException;

    /**
     * Agrega un nuevo cliente al sistema.
     *
     * @param client cliente a agregar.
     * @return cliente agregado, incluyendo su identificador generado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws FoundException    Si ya existe un cliente igual en el sistema.
     */
    public Client add(Client client) throws BusinessException, FoundException;

    /**
     * Actualiza los datos de un cliente existente.
     *
     * @param client cliente con los datos actualizados.
     * @return cliente actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el cliente a actualizar no existe.
     * @throws FoundException Si ya existe un cliente igual en el sistema.
     */
    public Client update(Client client) throws BusinessException, NotFoundException, FoundException;

    /**
     * Elimina un cliente del sistema a partir de su identificador.
     *
     * @param id Identificador del cliente a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el cliente a eliminar no existe.
     */
    public void delete(Long id) throws BusinessException, NotFoundException;
}
