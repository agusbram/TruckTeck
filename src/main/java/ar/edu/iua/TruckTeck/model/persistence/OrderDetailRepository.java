package ar.edu.iua.TruckTeck.model.persistence;

import ar.edu.iua.TruckTeck.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    
    // Obtener todos los detalles de una orden, ordenados por timestamp
    List<OrderDetail> findByOrderNumberOrderByTimestampAsc(Long orderNumber);
    
    // Obtener el último detalle (para saber la masa acumulada final)
    @Query("SELECT od FROM OrderDetail od WHERE od.order.number = :orderNumber ORDER BY od.timestamp DESC LIMIT 1")
    Optional<OrderDetail> findLastByOrderNumber(@Param("orderNumber") Long orderNumber);
    
    // OPCIONAL para el parcial - Calcular promedios
    @Query("SELECT AVG(od.temperature) FROM OrderDetail od WHERE od.order.number = :orderNumber")
    Double calculateAvgTemperature(@Param("orderNumber") Long orderNumber);
    
    @Query("SELECT AVG(od.density) FROM OrderDetail od WHERE od.order.number = :orderNumber")
    Double calculateAvgDensity(@Param("orderNumber") Long orderNumber);
    
    @Query("SELECT AVG(od.caudal) FROM OrderDetail od WHERE od.order.number = :orderNumber")
    Double calculateAvgCaudal(@Param("orderNumber") Long orderNumber);
}