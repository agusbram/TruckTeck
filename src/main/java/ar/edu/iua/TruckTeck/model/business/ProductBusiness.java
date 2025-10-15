package ar.edu.iua.TruckTeck.model.business;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.Product;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.ProductRepository;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementación de la interfaz {@link IProductBusiness} que gestiona la lógica 
 * de negocio para los productos.
 * <p>
 * Esta clase utiliza {@link ProductRepository} para acceder a los datos persistentes 
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
public class ProductBusiness implements IProductBusiness {

    /**
     * Repositorio para acceder a los datos de productos.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private ProductRepository productDAO;


    /**
     * Obtiene la lista completa de productos.
     *
     * @return Lista de productos existentes en la base de datos.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     */
    @Override
    public List<Product> list() throws BusinessException {
        try {
            return productDAO.findAll();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    /**
     * Obtiene un producto por su identificador único.
     *
     * @param id Identificador del producto.
     * @return El {@link Product} correspondiente al id proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el producto con el id especificado.
     */
    @Override
    public Product load(Long id) throws BusinessException, NotFoundException {
        Optional<Product> r;

        try {
            r = productDAO.findById(id);
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
     * Obtiene un producto por su nombre.
     *
     * @param product Nombre del producto.
     * @return El {@link Product} correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra un producto con el nombre especificado.
     */
    @Override
    public Product load(String product) throws BusinessException, NotFoundException {
        Optional<Product> r;

        try {
            r = productDAO.findByProduct(product);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el producto de nombre: " + product).build();
        }
        return r.get();
    }

    /**
     * Agrega un nuevo producto.
     * <p>
     * Antes de agregar, verifica que no exista otro producto con el mismo id o nombre.
     * </p>
     *
     * @param product Producto a agregar.
     * @return El {@link Product} agregado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws FoundException Si ya existe un producto con el mismo id o nombre.
     */
    @Override
    public Product add(Product product) throws BusinessException, FoundException {
        try {
            load(product.getId());
            throw FoundException.builder().message("Se encontró el producto con id: " + product.getId()).build();
        } catch(NotFoundException e) {
        }
        try {
            load(product.getName());
            throw FoundException.builder().message("Se encontró el producto con nombre: " + product.getName()).build();
        } catch(NotFoundException e) {
        }

        try {
            return productDAO.save(product);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Actualiza un producto existente.
     * <p>
     * Verifica que el producto a actualizar exista y que no haya otro producto con el mismo nombre.
     * </p>
     *
     * @param product Producto con los datos actualizados.
     * @return El {@link Product} actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el producto a actualizar.
     * @throws FoundException Si ya existe otro producto con el mismo nombre.
     */
    @Override
    public Product update(Product product) throws FoundException, BusinessException, NotFoundException {
        load(product.getId());
        Optional<Product> nombreExistente = null;

        try {
            nombreExistente = productDAO.findByProductAndIdNot(product.getName(), product.getId());
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(nombreExistente.isPresent()) {
            throw FoundException.builder().message("Ya existe un producto con el nombre: " + product.getName()).build();
        }

        try {
            return productDAO.save(product);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Elimina un producto por su identificador.
     *
     * @param id Identificador del producto a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra el producto con el id especificado.
     */
    @Override
    public void delete(Long id) throws BusinessException, NotFoundException {
        load(id);
        try {
            productDAO.deleteById(id);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }
    
}