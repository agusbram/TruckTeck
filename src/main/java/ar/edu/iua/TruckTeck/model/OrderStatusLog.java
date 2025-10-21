package ar.edu.iua.TruckTeck.model;

import ar.edu.iua.TruckTeck.model.enums.OrderState;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Por qué esta entidad:
 * <ul>
 * <li>El enunciado dice: "Cada cambio de estado debe quedar registrado"</li>
 * <li>Permite trazabilidad completa</li>
 * <li>Auditoría para cumplimiento</li>
 * </ul>
 */
@Entity
@Table(name = "order_status_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long orderNumber;  // Número de la orden que cambió
    
    @Enumerated(EnumType.STRING)
    private OrderState fromState;  // Estado anterior (puede ser null si es creación)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState toState;  // Nuevo estado
    
    @Column(nullable = false)
    private String actor;  // Quién/qué hizo el cambio ("SYSTEM", "OPERATOR_01", "TMS")
    
    @Column(columnDefinition = "TEXT")
    private String note;  // Observaciones opcionales
    
    @Column(nullable = false)
    private LocalDateTime timestamp;  // Cuándo ocurrió
}