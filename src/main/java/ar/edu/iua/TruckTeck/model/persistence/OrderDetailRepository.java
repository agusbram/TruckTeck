package ar.edu.iua.TruckTeck.model.persistence;

import ar.edu.iua.TruckTeck.model.OrderDetail;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    
    @Query("SELECT od.timestamp FROM OrderDetail od WHERE od.order.id = :orderId ORDER BY od.timestamp DESC LIMIT 1")
    LocalDateTime findLastTimestampByOrderId(@Param("orderId") Long orderId);

    OrderDetail findTopByOrderIdOrderByTimestampDesc(Long orderId);

    /* Obtener Promedios */

    @Query("SELECT AVG(d.temperature) FROM OrderDetail d WHERE d.order.id = :orderId")
    Double findAverageTemperatureByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT AVG(d.density) FROM OrderDetail d WHERE d.order.id = :orderId")
    Double findAverageDensityByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT AVG(d.caudal) FROM OrderDetail d WHERE d.order.id = :orderId")
    Double findAverageCaudalByOrderId(@Param("orderId") Long orderId);
}