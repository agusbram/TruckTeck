package ar.edu.iua.TruckTeck.model;

import java.time.LocalDateTime;

import ar.edu.iua.TruckTeck.auth.model.User;
import ar.edu.iua.TruckTeck.model.enums.OrderState;
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

/**
 * Entidad que representa una alarma de temperatura guardada cuando se supera el umbral configurado.
 * <p>
 * Cada vez que durante el proceso de carga la temperatura actual excede el umbral definido en
 * {@link TemperatureAlertConfig}, se crea un registro de {@code Alarm} con los datos del evento
 * obtenidos desde {@link OrderDetail}.
 * </p>
 * 
 * <h3>Datos almacenados (obtenidos de OrderDetail y configuración):</h3>
 * <ul>
 *   <li><b>orderNumber:</b> detail.getOrder().getNumber()</li>
 *   <li><b>eventDateTime:</b> detail.getTimestamp()</li>
 *   <li><b>currentTemperature:</b> detail.getTemperature()</li>
 *   <li><b>thresholdTemperature:</b> config.getThreshold()</li>
 *   <li><b>orderState:</b> detail.getOrder().getState()</li>
 * </ul>
 * 
 * <h3>Flujo de creación:</h3>
 * <ol>
 *   <li>Sistema de carga reporta temperatura que excede el umbral.</li>
 *   <li>Se crea un registro de {@code Alarm} con los datos del {@link OrderDetail}.</li>
 *   <li>Se envía notificación por email a los destinatarios configurados.</li>
 *   <li>Se publica mensaje WebSocket para alertas en tiempo real.</li>
 * </ol>
 * 
 * @see TemperatureAlertConfig
 * @see OrderDetail
 * @see Order
 * @see OrderState
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alarms")
public class Alarm {

    /**
     * Identificador único de la alarma.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de la orden donde se registró el evento de temperatura.
     * <p>
     * Obtenido de: {@code detail.getOrder().getNumber()}
     * </p>
     */
    @Column(nullable = false)
    private String orderNumber;

    /**
     * Fecha y hora exacta cuando se detectó que la temperatura superó el umbral.
     * <p>
     * Obtenido de: {@code detail.getTimestamp()}
     * </p>
     */
    @Column(nullable = false)
    private LocalDateTime eventDateTime;

    /**
     * Temperatura actual registrada en el momento del evento (en grados Celsius).
     * <p>
     * Obtenido de: {@code detail.getTemperature()}
     * </p>
     */
    @Column(nullable = false)
    private Double currentTemperature;

    /**
     * Umbral de temperatura configurado que fue superado (en grados Celsius).
     * <p>
     * Obtenido de: {@code config.getThreshold()}
     * </p>
     */
    @Column(nullable = false)
    private Double thresholdTemperature;

    /* Estado de la alarma (aceptado o no aceptado) */
    @Column(nullable = false, length = 20)
    private Boolean alarmState = true;

    // Usuario que acepta la alarma
    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User user;

    // Observaciones cuando se acepta la alarma
    @Column(length = 500)
    private String observations;

    // Fecha y hora cuando el usuario acepta la alarma
    @Column(nullable = true)
    private LocalDateTime acceptedDateTime;

    /**
     * Constructor de conveniencia para crear una alarma con todos los datos del evento.
     * 
     * @param orderNumber Número de la orden (detail.getOrder().getNumber()).
     * @param eventDateTime Fecha y hora del evento (detail.getTimestamp()).
     * @param currentTemperature Temperatura actual registrada (detail.getTemperature()).
     * @param thresholdTemperature Umbral de temperatura que fue superado (config.getThreshold()).
     * @param orderState Estado de la orden en el momento del evento (detail.getOrder().getState()).
     */
    public Alarm(String orderNumber, LocalDateTime eventDateTime, Double currentTemperature, Double thresholdTemperature, Boolean alarmState) {
        this.orderNumber = orderNumber;
        this.eventDateTime = eventDateTime;
        this.currentTemperature = currentTemperature;
        this.thresholdTemperature = thresholdTemperature;
        this.alarmState = alarmState;
    }
}
