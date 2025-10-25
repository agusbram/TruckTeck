package ar.edu.iua.TruckTeck.model;

import jakarta.persistence.Column;
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
import java.time.LocalDateTime;


/**
 * Cada registro de OrderDetail es un "snapshot" de la carga en un momento específico
 * timestamp es crucial para ordenar los datos y calcular promedios después
 * La relación @ManyToOne con Order te permite buscar todos los detalles de una orden
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // ID del detalle (PK)

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Orden a la que pertenece este detalle

    @Column(nullable = false)
    private LocalDateTime timestamp; // Momento de recepcion del dato

    @Column(nullable = false)
    private Double accumulatedMass; // Masa acumulada en ese momento (kg)

    @Column(nullable = false)
    private Double density;  // Densidad en ese momento

    @Column(nullable = false)
    private Double temperature; // Temperatura en ese momento (°C)

    @Column(nullable = false)
    private Double caudal; // Caudal en ese momento (kg/h)
}
