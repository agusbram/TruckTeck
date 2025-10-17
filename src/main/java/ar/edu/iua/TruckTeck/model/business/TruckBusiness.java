package ar.edu.iua.TruckTeck.model.business;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.Truck;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.TruckRepository;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementación de la interfaz {@link ITruckBusiness} que gestiona la lógica 
 * de negocio para los camiones.
 * <p>
 * Esta clase utiliza {@link TruckRepository} para acceder a los datos persistentes 
 * y aplica la lógica de negocio correspondiente. Los métodos lanzan excepciones
 * específicas para manejar errores de negocio, recursos no encontrados o duplicados.
 * </p>
 *
 * <p>
 * Anotaciones:
 * <ul>
 *   <li>{@code @Service}: Marca la clase como un componente de servicio de Spring.</li>
 *   <li>{@code @Slf4j}: Proporciona un logger para registrar errores y eventos importantes.</li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
public class TruckBusiness implements ITruckBusiness {

    /**
     * Repositorio para acceder a los datos de camiones.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private TruckRepository truckDAO;


    /**
     * Obtiene la lista completa de camiones.
     *
     * @return Lista de camiones existentes en la base de datos.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     */
    @Override
    public List<Truck> list() throws BusinessException {
        try {
            return truckDAO.findAll();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    /**
     * Obtiene un camion por su identificador único.
     *
     * @param id Identificador del camion.
     * @return El {@link Truck} correspondiente al id proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el camion con el id especificado.
     */
    @Override
    public Truck load(Long id) throws BusinessException, NotFoundException {
        Optional<Truck> r;

        try {
            r = truckDAO.findById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el camion con id: " + id).build();
        }
        return r.get();
    }

    /**
     * Obtiene un camion por su nombre.
     *
     * @param truck Nombre del camion.
     * @return El {@link Truck} correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra un camion con el nombre especificado.
     */
    @Override
    public Truck load(String truck) throws BusinessException, NotFoundException {
        Optional<Truck> r;

        try {
            r = truckDAO.findByDomain(truck);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el camion de nombre: " + truck).build();
        }
        return r.get();
    }

    /**
     * Agrega un nuevo camion.
     * <p>
     * Antes de agregar, verifica que no exista otro camion con el mismo id o nombre.
     * </p>
     *
     * @param truck camion a agregar.
     * @return El {@link Truck} agregado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws FoundException Si ya existe un camion con el mismo id o nombre.
     */
    @Override
    public Truck add(Truck truck) throws BusinessException, FoundException {
        try {
            load(truck.getId());
            throw FoundException.builder().message("Se encontró el camion con id: " + truck.getId()).build();
        } catch(NotFoundException e) {
        }
        try {
            load(truck.getDomain());
            throw FoundException.builder().message("Se encontró el camion con nombre: " + truck.getDomain()).build();
        } catch(NotFoundException e) {
        }

        try {
            return truckDAO.save(truck);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Actualiza un camion existente.
     * <p>
     * Verifica que el camion a actualizar exista y que no haya otro camion con el mismo nombre.
     * </p>
     *
     * @param truck camion con los datos actualizados.
     * @return El {@link Truck} actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el camion a actualizar.
     * @throws FoundException Si ya existe otro camion con el mismo nombre.
     */
    @Override
    public Truck update(Truck truck) throws FoundException, BusinessException, NotFoundException {
        load(truck.getId());
        Optional<Truck> nombreExistente = null;

        try {
            nombreExistente = truckDAO.findByDomainAndIdNot(truck.getDomain(), truck.getId());
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(nombreExistente.isPresent()) {
            throw FoundException.builder().message("Ya existe un camion con el nombre: " + truck.getDomain()).build();
        }

        try {
            return truckDAO.save(truck);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Elimina un camion por su identificador.
     *
     * @param id Identificador del camion a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el camion con el id especificado.
     */
    @Override
    public void delete(Long id) throws BusinessException, NotFoundException {
        load(id);
        try {
            truckDAO.deleteById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }
    
}
