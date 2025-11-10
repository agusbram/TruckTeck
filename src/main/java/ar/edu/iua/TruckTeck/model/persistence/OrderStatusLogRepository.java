package ar.edu.iua.TruckTeck.model.persistence;

import ar.edu.iua.TruckTeck.model.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {
    
    // Obtener historial de cambios de una orden
    List<OrderStatusLog> findByOrderNumberOrderByTimestampAsc(Long orderNumber);
}