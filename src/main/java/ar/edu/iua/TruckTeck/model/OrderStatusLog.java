package ar.edu.iua.TruckTeck.model;

import ar.edu.iua.TruckTeck.model.enums.OrderState;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad de auditoría que registra los cambios de estado de las órdenes.
 * <p>
 * Cada vez que una {@link Order} cambia de estado, se crea un registro en esta tabla
 * para mantener un historial completo de las transiciones, permitiendo trazabilidad
 * y análisis de flujos de trabajo.
 * </p>
 * 
 * <h3>Por qué esta entidad:</h3>
 * <p>
 * El enunciado especifica: <i>"Cada cambio de estado debe quedar registrado"</i>.
 * Esta entidad cumple ese requisito almacenando quién, cuándo y por qué se realizó
 * cada cambio de estado en el ciclo de vida de una orden.
 * </p>
 * 
 * @see Order
 * @see OrderState
 */
@Entity
@Table(name = "order_status_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLog {
    
    /**
     * Identificador único del registro de log.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Número de la orden que cambió de estado.
     * <p>
     * Se almacena como Long para permitir consultas rápidas sin JOIN con la tabla orders.
     * </p>
     */
    @Column(nullable = false)
    private Long orderNumber;
    
    /**
     * Estado anterior de la orden antes del cambio.
     * <p>
     * Puede ser {@code null} cuando la orden se crea por primera vez (no existe estado previo).
     * </p>
     */
    @Enumerated(EnumType.STRING)
    private OrderState fromState;
    
    /**
     * Estado nuevo al que transicionó la orden.
     * <p>
     * Este campo nunca es {@code null} ya que todo cambio tiene un estado destino.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState toState;
    
    /**
     * Identificador del actor que realizó el cambio de estado.
     * <p>
     * Puede ser:
     * <ul>
     *   <li>Un usuario del sistema (nombre de usuario o email)</li>
     *   <li>Un sistema externo (ej: "TMS", "SAP", "SISTEMA_CARGA")</li>
     *   <li>Un proceso automático (ej: "SISTEMA_AUTOMATICO")</li>
     * </ul>
     * </p>
     */
    @Column(nullable = false)
    private String actor;
    
    /**
     * Observaciones o notas adicionales sobre el cambio de estado.
     * <p>
     * Campo opcional que puede contener información contextual como:
     * <ul>
     *   <li>Razón del cambio</li>
     *   <li>Información adicional del sistema externo</li>
     *   <li>Mensajes de error o advertencia</li>
     * </ul>
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String note;
    
    /**
     * Momento exacto en que se realizó el cambio de estado.
     * <p>
     * Fundamental para construir la línea temporal completa de la orden
     * y calcular tiempos entre estados.
     * </p>
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
}