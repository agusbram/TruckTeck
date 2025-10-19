
package ar.edu.iua.TruckTeck.model.persistence;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.model.Client;

/**
 * Repositorio para la gestión de la persistencia de {@link client}.
 * <p>
 * Extiende {@link JpaRepository} para proporcionar operaciones CRUD básicas y
 * consultas personalizadas sobre la entidad client.
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
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    /**
     * Busca un Cliente por su nombre.
     *
     * @param client nombre del Cliente.
     * @return {@link Optional} que contiene el Cliente si se encuentra, o vacío si no existe.
     */
    @Query("SELECT c FROM Client c WHERE c.companyName = :client")
    Optional<Client> findByCompanyName(@Param("client") String client);

    /**
     * Busca un Cliente por su dni.
     * Para encontrar al Cliente que no tenga el mismo id que se pasa por parámetro y así poder actualizarlo correctamente.
     *
     * @param client Numero de documento del Cliente
     * @param id Identificador del Cliente a excluir de la búsqueda.
     * @return {@link Optional} que contiene el Cliente si se encuentra, o vacío si no existe.
     */
    @Query("SELECT c FROM Client c WHERE c.companyName = :client AND c.id <> :id")
    Optional<Client> findByCompanyNameAndIdNot(@Param("client") String client, @Param("id") long id);

}
