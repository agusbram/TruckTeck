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
 * Entidad que almacena los datos de carga en tiempo real de una orden.
 * <p>
 * Cada registro representa un "snapshot" de las mediciones del sistema de carga
 * en un momento específico durante el proceso de carga del producto en el camión.
 * </p>
 * 
 * <h3>Propósito:</h3>
 * <ul>
 *   <li>Registrar la evolución temporal de la carga (masa acumulada)</li>
 *   <li>Monitorear las condiciones del producto (densidad, temperatura)</li>
 *   <li>Controlar el ritmo de carga (caudal)</li>
 *   <li>Permitir cálculos de promedios y análisis post-carga</li>
 * </ul>
 * 
 * <p><b>Nota:</b> El campo {@code timestamp} es crucial para ordenar los registros cronológicamente
 * y calcular tasas de cambio o promedios ponderados por tiempo.</p>
 * 
 * @see Order
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "order_details")
public class OrderDetail {

    /**
     * Identificador único del registro de detalle.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Orden a la que pertenece este registro de carga.
     * <p>
     * La relación {@code @ManyToOne} permite recuperar todos los detalles
     * de una orden específica para análisis temporal.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Momento exacto en que se tomó esta medición.
     * <p>
     * Fundamental para:
     * <ul>
     *   <li>Ordenar cronológicamente los registros</li>
     *   <li>Calcular duración total de carga</li>
     *   <li>Detectar anomalías temporales en el proceso</li>
     * </ul>
     * </p>
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Masa total acumulada hasta este momento, medida en kilogramos (kg).
     * <p>
     * Representa el peso total del producto cargado desde el inicio
     * hasta el timestamp de esta medición.
     * </p>
     */
    @Column(nullable = false)
    private Double accumulatedMass;

    /**
     * Densidad del producto en el momento de la medición.
     * <p>
     * Permite verificar la calidad y consistencia del producto
     * durante el proceso de carga.
     * </p>
     */
    @Column(nullable = false)
    private Double density;

    /**
     * Temperatura del producto en grados Celsius (°C).
     * <p>
     * Parámetro crítico para productos sensibles a la temperatura,
     * asegura que se mantienen dentro de rangos seguros.
     * </p>
     */
    @Column(nullable = false)
    private Double temperature;

    /**
     * Caudal de carga en kilogramos por hora (kg/h).
     * <p>
     * Indica la velocidad a la que se está cargando el producto.
     * Permite:
     * <ul>
     *   <li>Monitorear el ritmo de carga</li>
     *   <li>Detectar problemas en el sistema de bombeo</li>
     *   <li>Estimar tiempo restante de carga</li>
     * </ul>
     * </p>
     */
    @Column(nullable = false)
    private Double caudal;
}
