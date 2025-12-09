package ar.edu.iua.TruckTeck.model.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.model.Alarm;

/**
 * Repositorio de persistencia para la entidad {@link Alarm}.
 * <p>
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}
 * y consultas personalizadas para recuperar alarmas de temperatura
 * según diferentes criterios de búsqueda.
 * </p>
 * 
 * <p><b>Consultas personalizadas:</b></p>
 * <ul>
 *   <li>{@link #findByOrderNumber(String)} - Buscar todas las alarmas de una orden específica</li>
 *   <li>{@link #findByEventDateTimeBetween(LocalDateTime, LocalDateTime)} - Buscar alarmas en un rango de fechas</li>
 *   <li>{@link #findAllByOrderByEventDateTimeDesc()} - Listar todas las alarmas ordenadas por fecha descendente</li>
 * </ul>
 * 
 * @see ar.edu.iua.TruckTeck.model.Alarm
 * @see ar.edu.iua.TruckTeck.model.TemperatureAlertConfig
 */
@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    /**
     * Busca todas las alarmas registradas para un número de orden específico.
     * <p>
     * Útil para auditoría y análisis de eventos de temperatura
     * asociados a una orden de carga particular.
     * </p>
     * 
     * @param orderNumber Número de la orden.
     * @return Lista de alarmas registradas para esa orden (puede estar vacía).
     */
    @Query("SELECT a FROM Alarm a WHERE a.orderNumber = :orderNumber ORDER BY a.eventDateTime DESC")
    List<Alarm> findByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * Obtiene todas las alarmas ordenadas por fecha descendente (más reciente primero).
     * 
     * @return Lista de todas las alarmas del sistema ordenadas cronológicamente.
     */
    @Query("SELECT a FROM Alarm a ORDER BY a.eventDateTime DESC")
    List<Alarm> findAllByOrderByEventDateTimeDesc();
}
