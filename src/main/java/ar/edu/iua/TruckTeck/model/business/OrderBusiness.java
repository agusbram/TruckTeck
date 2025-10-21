package ar.edu.iua.TruckTeck.model.business;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.iua.TruckTeck.model.Client;
import ar.edu.iua.TruckTeck.model.Driver;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.Product;
import ar.edu.iua.TruckTeck.model.Truck;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.ClientRepository;
import ar.edu.iua.TruckTeck.model.persistence.OrderRepository;
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
public class OrderBusiness implements IOrderBusiness {

    /**
     * Repositorio para acceder a los datos de órdenes.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private OrderRepository orderDAO;

    /**
     * Componente de negocio encargado de la lógica de camiones.
     */
    @Autowired
    private ITruckBusiness truckBusiness;

    /**
     * Componente de negocio encargado de la lógica de clientes.
     */
    @Autowired
    private IClientBusiness clientBusiness;

    /**
     * Componente de negocio encargado de la lógica de conductores.
     */
    @Autowired
    private IDriverBusiness driverBusiness;

    /**
     * Componente de negocio encargado de la lógica de productos.
     */
    @Autowired
    private IProductBusiness productBusiness;

    /**
     * Obtiene la lista completa de órdenes.
     *
     * @return Lista de órdenes existentes en la base de datos.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     */
    @Override
    public List<Order> list() throws BusinessException {
        try {
            return orderDAO.findAll();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    /**
     * Obtiene una orden por su identificador único.
     *
     * @param id Identificador de la orden.
     * @return La {@link Order} correspondiente al id proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra la orden con el id especificado.
     */ 
    @Override
    public Order load(Long id) throws BusinessException, NotFoundException {
        Optional<Order> r;

        try {
            r = orderDAO.findById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra la Orden con id: " + id).build();
        }
        return r.get();
    }

    /**
     * Agrega una nueva orden.
     * <p>
     * Antes de agregar, verifica que no exista otra orden con el mismo id o nombre.
     * </p>
     *
     * @param order orden a agregar.
     * @return La {@link Order} agregada.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws FoundException Si ya existe una orden con el mismo id o nombre.
     */
    @Override
    public Order add(Order order) throws BusinessException, FoundException {
        try {
            load(order.getNumber());
            throw FoundException.builder().message("Se encontró la orden con número: " + order.getNumber()).build();
        } catch(NotFoundException e) {
        }

        try {
            Client client = clientBusiness.load(order.getClient().getCompanyName());
            if (client != null)
                order.setClient(client); 
        } catch(NotFoundException e) {
            clientBusiness.add(order.getClient());
            try {
            order.setClient(clientBusiness.load(order.getClient().getCompanyName()));
            } catch (NotFoundException f) {
            }
        }
        

        try {
            Truck truck = truckBusiness.load(order.getTruck().getDomain());
            if (truck != null)
            order.setTruck(truck);
        } catch(NotFoundException e) {
            truckBusiness.add(order.getTruck());
            try {
            order.setTruck(truckBusiness.load(order.getTruck().getDomain()));
            } catch (NotFoundException f) {
            }
        }
        

        try {
            Product product = productBusiness.load(order.getProduct().getName());
            if (product != null)
            order.setProduct(product);
        } catch(NotFoundException e) {
            productBusiness.add(order.getProduct());
            try {
            order.setProduct(productBusiness.load(order.getProduct().getName()));
            } catch (NotFoundException f) {
            }
        }

        try {
            Driver driver = driverBusiness.load(order.getDriver().getDocumentNumber());
            if (driver != null)
            order.setDriver(driver);
        } catch(NotFoundException e) {
            driverBusiness.add(order.getDriver());
            try {
            order.setDriver(driverBusiness.load(order.getDriver().getDocumentNumber()));
            } catch (NotFoundException f) {
            }
        }

        try {
            return orderDAO.save(order);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

    }

    /**
     * Actualiza una orden existente.
     * <p>
     * Verifica que la orden a actualizar exista y que no haya otra orden con el mismo nombre.
     * </p>
     *
     * @param order orden con los datos actualizados.
     * @return La {@link Order} actualizada.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra la orden a actualizar.
     * @throws FoundException Si ya existe otra orden con el mismo nombre.
     */
    @Override
    public Order update(Order order) throws FoundException, BusinessException, NotFoundException {
        load(order.getNumber());

        try {
            return orderDAO.save(order);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("Error al Actualizar Orden").ex(e).build();
        }
    }

    @Override
    public void delete(Order order) throws BusinessException, NotFoundException {
        delete(order.getNumber());
    }

    /**
     * Elimina una orden por su identificador.
     *
     * @param id Identificador de la orden a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra la orden con el id especificado.
     */
    @Override
    public void delete(Long id) throws BusinessException, NotFoundException {
        load(id);
        try {
            orderDAO.deleteById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }
}
