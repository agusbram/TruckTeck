package ar.edu.iua.TruckTeck.model.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.model.Driver;

/**
 * Repositorio para la gestión de la persistencia de {@link Driver}.
 * <p>
 * Extiende {@link JpaRepository} para proporcionar operaciones CRUD básicas y
 * consultas personalizadas sobre la entidad Driver.
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
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    /**
     * Busca un Chofer por su dni.
     *
     * @param driver Numero de documento del chofer.
     * @return {@link Optional} que contiene el Chofer si se encuentra, o vacío si no existe.
     */
    @Query("SELECT p FROM Driver p WHERE p.documentNumber = :driver")
    Optional<Driver> findByDocumentNumber(@Param("driver") String driver);

    /**
     * Busca un Chofer por su dni.
     * Para encontrar al Chofer que no tenga el mismo id que se pasa por parámetro y así poder actualizarlo correctamente.
     *
     * @param driver Numero de documento del chofer
     * @param id Identificador del Chofer a excluir de la búsqueda.
     * @return {@link Optional} que contiene el Chofer si se encuentra, o vacío si no existe.
     */
    @Query("SELECT p FROM Driver p WHERE p.documentNumber = :driver AND p.id <> :id")
    Optional<Driver> findByDocumentNumberAndIdNot(@Param("driver") String driver, @Param("id") long id);

    /**
     * Busca un Chofer por su código externo.
     * @param externalCode
     * @return
     */
    Optional<Driver> findByExternalCode(String externalCode);
}
