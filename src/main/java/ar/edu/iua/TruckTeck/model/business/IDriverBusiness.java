package ar.edu.iua.TruckTeck.model.business;


import java.util.List;

import ar.edu.iua.TruckTeck.model.Driver;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;


/**
 * Interfaz que define las operaciones de negocio para la gestión de productos.
 * <p>
 * Incluye métodos para listar, buscar, agregar, actualizar y eliminar productos,
 * manejando las excepciones correspondientes a reglas de negocio y existencia de datos.
 * </p>
 */
public interface IDriverBusiness {
    /**
     * Obtiene la lista completa de choferes.
     *
     * @return Lista de choferes existentes.
     * @throws BusinessException Si ocurre un error general en la lógica de negocio.
     */
    public List<Driver> list() throws BusinessException;
    
    /**
     * Carga un chofer específico a partir de su identificador único.
     *
     * @param id Identificador del chofer a cargar.
     * @return Chofer correspondiente al identificador proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un chofer con el identificador dado.
     */
    public Driver load(Long id) throws BusinessException, NotFoundException;

    /**
     * Carga un chofer específico a partir de su nombre o descripción.
     *
     * @param driver Nombre o descripción del chofer a cargar.
     * @return Chofer correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un chofer con el nombre dado.
     */
    public Driver load(String driver) throws BusinessException, NotFoundException;

    /**
     * Agrega un nuevo chofer al sistema.
     *
     * @param driver Chofer a agregar.
     * @return Chofer agregado, incluyendo su identificador generado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws FoundException    Si ya existe un chofer igual en el sistema.
     */
    public Driver add(Driver driver) throws BusinessException, FoundException;

    /**
     * Actualiza los datos de un chofer existente.
     *
     * @param driver Chofer con los datos actualizados.
     * @return Chofer actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el chofer a actualizar no existe.
     * @throws FoundException Si ya existe un chofer igual en el sistema.
     */
    public Driver update(Driver driver) throws BusinessException, NotFoundException, FoundException;

    /**
     * Elimina un chofer del sistema a partir de su identificador.
     *
     * @param id Identificador del chofer a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el chofer a eliminar no existe.
     */
    public void delete(Long id) throws BusinessException, NotFoundException;
}