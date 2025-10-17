package ar.edu.iua.TruckTeck.model.business;

import java.util.List;

import ar.edu.iua.TruckTeck.model.Truck;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;


/**
 * Interfaz que define las operaciones de negocio para la gestión de camiones.
 * <p>
 * Incluye métodos para listar, buscar, agregar, actualizar y eliminar camiones,
 * manejando las excepciones correspondientes a reglas de negocio y existencia de datos.
 * </p>
 */
public interface ITruckBusiness {
    /**
     * Obtiene la lista completa de camiones.
     *
     * @return Lista de camiones existentes.
     * @throws BusinessException Si ocurre un error general en la lógica de negocio.
     */
    public List<Truck> list() throws BusinessException;

     /**
     * Carga un camion específico a partir de su identificador único.
     *
     * @param id Identificador del camion a cargar.
     * @return camion correspondiente al identificador proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un camion con el identificador dado.
     */
    public Truck load(Long id) throws BusinessException, NotFoundException;

    /**
     * Carga un camion específico a partir de su nombre o descripción.
     *
     * @param truck Nombre o descripción del camion a cargar.
     * @return camion correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un camion con el nombre dado.
     */
    public Truck load(String truck) throws BusinessException, NotFoundException;

    /**
     * Agrega un nuevo camion al sistema.
     *
     * @param truck camion a agregar.
     * @return camion agregado, incluyendo su identificador generado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws FoundException    Si ya existe un camion igual en el sistema.
     */
    public Truck add(Truck truck) throws BusinessException, FoundException;

    /**
     * Actualiza los datos de un camion existente.
     *
     * @param truck camion con los datos actualizados.
     * @return camion actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el camion a actualizar no existe.
     * @throws FoundException Si ya existe un camion igual en el sistema.
     */
    public Truck update(Truck truck) throws BusinessException, NotFoundException, FoundException;

    /**
     * Elimina un camion del sistema a partir de su identificador.
     *
     * @param id Identificador del camion a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el camion a eliminar no existe.
     */
    public void delete(Long id) throws BusinessException, NotFoundException;
}
