package ar.edu.iua.TruckTeck.model.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.enums.OrderState;

/**
 * Repositorio de persistencia para la entidad {@link Order}.
 * <p>
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}
 * y consultas personalizadas mediante JPQL para casos de uso específicos.
 * </p>
 * 
 * <p><b>Consultas personalizadas:</b></p>
 * <ul>
 *   <li>{@link #findByNumber(String)} - Buscar orden por número de orden</li>
 *   <li>{@link #findByTruckDomainAndState(String, OrderState)} - Buscar orden por dominio de camión y estado (TMS)</li>
 *   <li>{@link #findByActivationCode(String)} - Buscar orden por código de activación (TMS)</li>
 * </ul>
 * 
 * @see ar.edu.iua.TruckTeck.model.Order
 * @see ar.edu.iua.TruckTeck.model.enums.OrderState
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Busca una orden por el dominio del camión asociado y un estado específico.
     * <p>
     * <b>Uso principal:</b> Sistema TMS al registrar el pesaje inicial.
     * Busca órdenes en estado PENDING para un camión específico.
     * </p>
     * 
     * <p><b>Query SQL generado:</b></p>
     * <pre>
     * SELECT o.* FROM orders o 
     * JOIN trucks t ON t.id = o.truck_id 
     * WHERE t.domain = ? AND o.state = ?
     * </pre>
     * 
     * @param domain Dominio/patente del camión (ej: "AB805")
     * @param state Estado de la orden (ej: OrderState.PENDING)
     * @return Optional con la orden encontrada, o empty si no existe ninguna orden en ese estado para el camión
     */
    @Query("SELECT o FROM Order o WHERE o.truck.domain = :domain AND o.state = :state")
    Optional<Order> findByTruckDomainAndState(@Param("domain") String domain, @Param("state") OrderState state);

    /**
     * Busca una orden por su código de activación único de 5 dígitos.
     * <p>
     * <b>Uso principal:</b> Sistema TMS al registrar el pesaje final.
     * El código de activación se genera durante el pesaje inicial y permite
     * identificar la orden sin necesidad de conocer el dominio del camión.
     * </p>
     * 
     * <p><b>Formato del código:</b> 5 dígitos numéricos (ej: "48765", "00123")</p>
     * 
     * @param activationCode Código de activación único generado en el pesaje inicial
     * @param number numero de orden.
     * @return Optional con la orden encontrada, o empty si el código no es válido
     */
    @Query("SELECT o FROM Order o WHERE o.activationCode = :activationCode AND o.number = :number")
    Optional<Order> findByActivationCode(@Param("activationCode") String activationCode,@Param("number") String number);

    /**
     * Busca una orden por su número de orden.
     * @param number número de la orden
     * @return Optional con la orden si existe
     */
    Optional<Order> findByNumber(String number);

    /**
     * Busca una orden por su código externo.
     * @param externalCode
     * @return
     */
    Optional<Order> findByExternalCode (String externalCode);
}
