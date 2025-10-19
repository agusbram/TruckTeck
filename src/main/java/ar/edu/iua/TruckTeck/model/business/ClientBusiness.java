package ar.edu.iua.TruckTeck.model.business;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.Client;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.ClientRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la interfaz {@link IClientBusiness} que gestiona la lógica 
 * de negocio para los productos.
 * <p>
 * Esta clase utiliza {@link ClientRepository} para acceder a los datos persistentes 
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
public class ClientBusiness implements IClientBusiness {

    /**
     * Repositorio para acceder a los datos de clientes.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private ClientRepository clientDAO;


    /**
     * Obtiene la lista completa de clientes.
     *
     * @return Lista de clientes existentes en la base de datos.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     */
    @Override
    public List<Client> list() throws BusinessException {
        try {
            return clientDAO.findAll();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    /**
     * Obtiene un cliente por su identificador único.
     *
     * @param id Identificador del cliente.
     * @return El {@link Client} correspondiente al id proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el cliente con el id especificado.
     */
    @Override
    public Client load(Long id) throws BusinessException, NotFoundException {
        Optional<Client> r;

        try {
            r = clientDAO.findById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el Cliente con id: " + id).build();
        }
        return r.get();
    }

    /**
     * Obtiene un cliente por su nombre.
     *
     * @param client Nombre del cliente.
     * @return El {@link Client} correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra un cliente con el nombre especificado.
     */
    @Override
    public Client load(String client) throws BusinessException, NotFoundException {
        Optional<Client> r;

        try {
            r = clientDAO.findByCompanyName(client);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el cliente de nombre: " + client).build();
        }
        return r.get();
    }

    /**
     * Agrega un nuevo cliente.
     * <p>
     * Antes de agregar, verifica que no exista otro cliente con el mismo id o nombre.
     * </p>
     *
     * @param client cliente a agregar.
     * @return El {@link Client} agregado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws FoundException Si ya existe un cliente con el mismo id o nombre.
     */
    @Override
    public Client add(Client client) throws BusinessException, FoundException {
        try {
            load(client.getId());
            throw FoundException.builder().message("Se encontró el cliente con id: " + client.getId()).build();
        } catch(NotFoundException e) {
        }
        try {
            load(client.getCompanyName());
            throw FoundException.builder().message("Se encontró el cliente con documento: " + client.getCompanyName()).build();
        } catch(NotFoundException e) {
        }

        try {
            return clientDAO.save(client);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Actualiza un cliente existente.
     * <p>
     * Verifica que el cliente a actualizar exista y que no haya otro cliente con el mismo nombre.
     * </p>
     *
     * @param client cliente con los datos actualizados.
     * @return El {@link Client} actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el cliente a actualizar.
     * @throws FoundException Si ya existe otro cliente con el mismo nombre.
     */
    @Override
    public Client update(Client client) throws FoundException, BusinessException, NotFoundException {
        load(client.getId());
        Optional<Client> nombreExistente = null;

        try {
            nombreExistente = clientDAO.findByCompanyNameAndIdNot(client.getCompanyName(), client.getId());
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(nombreExistente.isPresent()) {
            throw FoundException.builder().message("Ya existe un cliente con el documento: " + client.getCompanyName()).build();
        }

        try {
            return clientDAO.save(client);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Elimina un cliente por su identificador.
     *
     * @param id Identificador del cliente a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el cliente con el id especificado.
     */
    @Override
    public void delete(Long id) throws BusinessException, NotFoundException {
        load(id);
        try {
            clientDAO.deleteById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }
    
}