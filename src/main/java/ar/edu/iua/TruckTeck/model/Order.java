package ar.edu.iua.TruckTeck.model;

import java.time.LocalDateTime;

import ar.edu.iua.TruckTeck.model.enums.OrderState;
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
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
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

    @Enumerated(EnumType.STRING)
    private OrderState state;

    // A partir de abajo se muestran los datos que se irán mostrando en tiempo real, reflejando el último registro recibido
    private float accumulatedMass;

    private float density;

    private float temperature;

    private float caudal;

    private LocalDateTime finalDataReception;

    /**
     * 
     * Cambiar el estado de la orden usando el valor del enum.
     * @param newStatus valor del enum a establecer
     */
    public void setStatus(OrderState newStatus) {
        this.state = newStatus;
    }

    /**
     * Establece el estado inicial de la orden como PENDING al crear una nueva instancia.
     */
    public Order() {
        this.state = OrderState.PENDING;
    }

    /**
     * Cambia el estado de la orden si la transición es válida.
     * Esto permite únicamente cambiar a un estado de orden específico según el orden lógico que debería seguir.
     * @param nextState el nuevo estado al que se desea cambiar
     */
    public void changeState(OrderState nextState) {
        if (state.canTransitionTo(nextState)) {
            this.state = nextState;
        } else {
            throw new IllegalStateException(
                "Invalid transition: " + state + " -> " + nextState
            );
        }
    }

    /**
     * Configurador compatible con versiones anteriores que acepta los antiguos códigos de cadena  
     * ("INICIADO", "PESANDO", "CARGANDO", "FINALIZADO") y los asigna * al código correspondiente {@link OrderState}.
     * @param newStatus codigo string
     */
    /* public void setStatusFromString(String newStatus) {
        if (newStatus == null) {
            this.state = null;
            return;
        }
        switch (newStatus.toUpperCase()) {
            case "INICIADA":
                this.state = OrderState.PENDING;
                break;
            case "PESAJE":
                this.state = OrderState.TARA_REGISTERED;
                break;
            case "CARGA":
                this.state = OrderState.LOADING;
                break;
            case "FINALIZADA":
                this.state = OrderState.FINALIZED;
                break;
            default:
                throw new IllegalArgumentException("Estado no válido: " + newStatus);
        }
    } */

    /**
     * Etiqueta intuitiva para el estado actual. 
     * @return muestra la cadena o es nula
     */
    /* public String getStatusDisplay() {
        if (this.state == null) return null;
        switch (this.state) {
            case PENDING:
                return "Iniciado";
            case TARA_REGISTERED:
                return "Tara";
            case LOADING:
                return "Carga";
            case FINALIZED:
                return "Finalizado";
            default:
                return this.state.name();
        }
    } */

    
}
