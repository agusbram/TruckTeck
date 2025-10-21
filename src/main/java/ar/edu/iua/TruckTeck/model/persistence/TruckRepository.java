package ar.edu.iua.TruckTeck.model.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.model.Truck;

/**
 * Repositorio para la gestión de la persistencia de {@link Truck}.
 * <p>
 * Extiende {@link JpaRepository} para proporcionar operaciones CRUD básicas y
 * consultas personalizadas sobre la entidad truck.
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
public interface TruckRepository extends JpaRepository<Truck, Long> {
    
    /**
     * Busca un Camion por su nombre o descripción exacta.
     *
     * @param truck Nombre o descripción del Camion a buscar.
     * @return {@link Optional} que contiene el Camion si se encuentra, o vacío si no existe.
     */
    @Query("SELECT t FROM Truck t WHERE t.domain = :truck")
    Optional<Truck> findByDomain(@Param("truck") String truck);

    /**
     * Busca un Camion por su nombre, excluyendo un identificador específico.
     * Para encontrar al Camion que no tenga el mismo id que se pasa por parámetro y así poder actualizarlo correctamente.
     *
     * @param truck Nombre o descripción del Camion a buscar.
     * @param id Identificador del Camion a excluir de la búsqueda.
     * @return {@link Optional} que contiene el Camion si se encuentra, o vacío si no existe.
     */
    @Query("SELECT t FROM Truck t WHERE t.domain = :truck AND t.id <> :id")
    Optional<Truck> findByDomainAndIdNot(@Param("truck") String truck, @Param("id") long id);

    /**
     * Busca un Camion por su código externo.
     * @param externalCode
     * @return
     */
    Optional<Truck> findByExternalCode(String externalCode);
}
