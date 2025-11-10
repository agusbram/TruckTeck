package ar.edu.iua.TruckTeck.model.persistence;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.model.Product;

/**
 * Repositorio para la gestión de la persistencia de {@link Product}.
 * <p>
 * Extiende {@link JpaRepository} para proporcionar operaciones CRUD básicas y
 * consultas personalizadas sobre la entidad Product.
 * </p>
 * 
 * <p>
 * Anotaciones:
 * <ul>
 *   <li>{@code @Repository}: Marca la interfaz como un componente de repositorio de Spring.</li>
 * </ul>
 * </p>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Busca un producto por su nombre o descripción exacta.
     *
     * @param product Nombre o descripción del producto a buscar.
     * @return {@link Optional} que contiene el producto si se encuentra, o vacío si no existe.
     */
    @Query("SELECT p FROM Product p WHERE p.name = :product OR p.description = :product")
    Optional<Product> findByProduct(@Param("product") String product);

    /**
     * Busca un producto por su nombre, excluyendo un identificador específico.
     * Para encontrar al producto que no tenga el mismo id que se pasa por parámetro y así poder actualizarlo correctamente.
     *
     * @param product Nombre o descripción del producto a buscar.
     * @param id Identificador del producto a excluir de la búsqueda.
     * @return {@link Optional} que contiene el producto si se encuentra, o vacío si no existe.
     */
    @Query("SELECT p FROM Product p WHERE (p.name = :product OR p.description = :product) AND p.id <> :id")
    Optional<Product> findByProductAndIdNot(@Param("product") String product, @Param("id") long id);

}