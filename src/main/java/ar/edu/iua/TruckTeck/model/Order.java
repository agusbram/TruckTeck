package ar.edu.iua.TruckTeck.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driverId;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client clientId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product productId;

    @ManyToOne
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truckId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long number;

    // Momento en que se recibe la orden desde el sistema externo
    private LocalDateTime initialReception;

    // Registro del pesaje vacío (tara)
    private LocalDateTime initialWeighing;

    // Momento del primer registro válido de detalle
    private LocalDateTime startLoading;

    // Momento del último registro válido de detalle
    private LocalDateTime endLoading;

    // Momento de recepción del pesaje final
    private LocalDateTime endWeighing;

    // A partir de abajo se muestran los datos que se irán mostrando en tiempo real, reflejando el último registro recibido
    private float accumulatedMass;

    private float density;

    private float temperature;

    private float caudal;

    private LocalDateTime finalDataReception;
}
