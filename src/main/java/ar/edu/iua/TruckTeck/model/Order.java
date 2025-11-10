package ar.edu.iua.TruckTeck.model;

import java.time.LocalDateTime;

import ar.edu.iua.TruckTeck.model.enums.OrderState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una orden de carga de producto en el sistema TMS (Terminal Management System).
 * <p>
 * Esta entidad gestiona el ciclo de vida completo de una orden, desde su creación hasta su finalización,
 * pasando por los procesos de pesaje (tara y bruto) y carga del producto en el camión.
 * </p>
 * 
 * <h3>Ciclo de vida de la orden:</h3>
 * <ol>
 *   <li><b>PENDING:</b> Orden creada, esperando el registro del peso inicial (tara) del camión.</li>
 *   <li><b>TARA_REGISTERED:</b> Peso inicial registrado, camión listo para cargar producto.</li>
 *   <li><b>LOADING:</b> Carga en progreso, recibiendo datos de carga en tiempo real.</li>
 *   <li><b>FINALIZED:</b> Carga completada, peso final registrado y orden cerrada.</li>
 * </ol>
 * 
 * <h3>Integraciones externas:</h3>
 * <ul>
 *   <li><b>SAP:</b> Las órdenes pueden crearse desde el sistema SAP, identificadas por {@code externalCode}.</li>
 *   <li><b>TMS (Balanza):</b> Sistema de pesaje que registra pesos iniciales y finales vía endpoints REST.</li>
 *   <li><b>Sistema de Carga:</b> Envía datos de carga en tiempo real (masa, densidad, temperatura, caudal)
 *       almacenados en {@link OrderDetail}.</li>
 * </ul>
 * 
 * <h3>Relaciones:</h3>
 * <ul>
 *   <li>{@link Driver}: Chofer asignado a la orden.</li>
 *   <li>{@link Client}: Cliente que solicita el producto.</li>
 *   <li>{@link Truck}: Camión que transportará el producto.</li>
 *   <li>{@link Product}: Producto a cargar.</li>
 *   <li>{@link OrderDetail}: Detalles de la carga en tiempo real (relación uno-a-muchos).</li>
 *   <li>{@link OrderStatusLog}: Registro de auditoría de cambios de estado (relación uno-a-muchos).</li>
 * </ul>
 * 
 * @see OrderState
 * @see OrderDetail
 * @see OrderStatusLog
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // Clave primaria

    private String number; // Número de orden

    // ===== Integracion con sistemas externos mediante la logica del cliente =====
    @Column(unique = true, nullable = true)
    private String externalCode;        // Campo para contemplar los codigos externos del SAP por ejemplo
    private String activationCode;      // Código de 5 dígitos (se genera al registrar tara)

    // ======== Relaciones con otras Entidades ========
    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    // ======= Datos base ========
    private LocalDateTime scheduledDate;    // Fecha prevista de carga
    private Double preset;                  // Kg a cargar

    // ==== Timestamps del Proceso ====
    private LocalDateTime initialReception;      // Cuando se recibe la orden (Estado 1)
    private LocalDateTime initialWeighing;       // Cuando se registra tara (Estado 2)
    private LocalDateTime startLoading;          // Primer dato válido de carga
    private LocalDateTime endLoading;            // Último dato válido de carga
    private LocalDateTime endWeighing;           // Cuando se registra pesaje final (Estado 4)
    private LocalDateTime closeOrder;    // Último dato recibido

    // ======= Pesajes =========
    private Double initialWeight;  // Tara (pesaje vacío)
    private Double finalWeight;    // Pesaje final (con carga)

    // ======= Últimos valores de carga (cabecera) =======
    private Double accumulatedMass;  // Última masa acumulada recibida
    private Double density;          // Última densidad
    private Double temperature;      // Última temperatura
    private Double caudal;           // Último caudal
    
    // ======= Control de estado =======
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state = OrderState.PENDING; // Estado inicial de la orden por defecto
}
