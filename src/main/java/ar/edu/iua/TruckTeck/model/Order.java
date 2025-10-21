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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long number; // Número de orden (PK)

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
    private LocalDateTime finalDataReception;    // Último dato recibido

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
