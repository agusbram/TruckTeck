package ar.edu.iua.TruckTeck.model.business;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.Driver;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.DriverRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la interfaz {@link IDriverBusiness} que gestiona la lógica 
 * de negocio para los choferes.
 * <p>
 * Esta clase utiliza {@link DriverRepository} para acceder a los datos persistentes 
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
public class DriverBusiness implements IDriverBusiness {

    /**
     * Repositorio para acceder a los datos de choferes.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private DriverRepository driverDAO;


    /**
     * Obtiene la lista completa de choferes.
     *
     * @return Lista de choferes existentes en la base de datos.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     */
    @Override
    public List<Driver> list() throws BusinessException {
        try {
            return driverDAO.findAll();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    /**
     * Obtiene un chofer por su identificador único.
     *
     * @param id Identificador del chofer.
     * @return El {@link Driver} correspondiente al id proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el chofer con el id especificado.
     */
    @Override
    public Driver load(Long id) throws BusinessException, NotFoundException {
        Optional<Driver> r;

        try {
            r = driverDAO.findById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el producto con id: " + id).build();
        }
        return r.get();
    }

    /**
     * Obtiene un chofer por su nombre.
     *
     * @param driver Nombre del chofer.
     * @return El {@link Driver} correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra un chofer con el nombre especificado.
     */
    @Override
    public Driver load(String driver) throws BusinessException, NotFoundException {
        Optional<Driver> r;

        try {
            r = driverDAO.findByDocumentNumber(driver);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el chofer de nombre: " + driver).build();
        }
        return r.get();
    }

    /**
     * Agrega un nuevo chofer.
     * <p>
     * Antes de agregar, verifica que no exista otro chofer con el mismo id o nombre.
     * </p>
     *
     * @param driver Chofer a agregar.
     * @return El {@link Driver} agregado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws FoundException Si ya existe un chofer con el mismo id o nombre.
     */
    @Override
    public Driver add(Driver driver) throws BusinessException, FoundException {
        try {
            load(driver.getId());
            throw FoundException.builder().message("Se encontró el chofer con id: " + driver.getId()).build();
        } catch(NotFoundException e) {
        }
        try {
            load(driver.getDocumentNumber());
            throw FoundException.builder().message("Se encontró el chofer con documento: " + driver.getDocumentNumber()).build();
        } catch(NotFoundException e) {
        }

        try {
            return driverDAO.save(driver);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Actualiza un chofer existente.
     * <p>
     * Verifica que el chofer a actualizar exista y que no haya otro chofer con el mismo nombre.
     * </p>
     *
     * @param driver Chofer con los datos actualizados.
     * @return El {@link Driver} actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el chofer a actualizar.
     * @throws FoundException Si ya existe otro chofer con el mismo nombre.
     */
    @Override
    public Driver update(Driver driver) throws FoundException, BusinessException, NotFoundException {
        load(driver.getId());
        Optional<Driver> nombreExistente = null;

        try {
            nombreExistente = driverDAO.findByDocumentNumberAndIdNot(driver.getDocumentNumber(), driver.getId());
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(nombreExistente.isPresent()) {
            throw FoundException.builder().message("Ya existe un chofer con el documento: " + driver.getDocumentNumber()).build();
        }

        try {
            return driverDAO.save(driver);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Elimina un chofer por su identificador.
     *
     * @param id Identificador del chofer a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el chofer con el id especificado.
     */
    @Override
    public void delete(Long id) throws BusinessException, NotFoundException {
        load(id);
        try {
            driverDAO.deleteById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }
    
}