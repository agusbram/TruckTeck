package ar.edu.iua.TruckTeck.model.business;

import java.util.List;

import ar.edu.iua.TruckTeck.model.Product;
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
public interface IProductBusiness {
    /**
     * Obtiene la lista completa de productos.
     *
     * @return Lista de productos existentes.
     * @throws BusinessException Si ocurre un error general en la lógica de negocio.
     */
    public List<Product> list() throws BusinessException;

     /**
     * Carga un producto específico a partir de su identificador único.
     *
     * @param id Identificador del producto a cargar.
     * @return Producto correspondiente al identificador proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un producto con el identificador dado.
     */
    public Product load(Long id) throws BusinessException, NotFoundException;

    /**
     * Carga un producto específico a partir de su nombre o descripción.
     *
     * @param product Nombre o descripción del producto a cargar.
     * @return Producto correspondiente al nombre proporcionado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException  Si no se encuentra un producto con el nombre dado.
     */
    public Product load(String product) throws BusinessException, NotFoundException;

    /**
     * Agrega un nuevo producto al sistema.
     *
     * @param product Producto a agregar.
     * @return Producto agregado, incluyendo su identificador generado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws FoundException    Si ya existe un producto igual en el sistema.
     */
    public Product add(Product product) throws BusinessException, FoundException;

    /**
     * Actualiza los datos de un producto existente.
     *
     * @param product Producto con los datos actualizados.
     * @return Producto actualizado.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el producto a actualizar no existe.
     * @throws FoundException Si ya existe un producto igual en el sistema.
     */
    public Product update(Product product) throws BusinessException, NotFoundException, FoundException;

    /**
     * Elimina un producto del sistema a partir de su identificador.
     *
     * @param id Identificador del producto a eliminar.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si el producto a eliminar no existe.
     */
    public void delete(Long id) throws BusinessException, NotFoundException;
}