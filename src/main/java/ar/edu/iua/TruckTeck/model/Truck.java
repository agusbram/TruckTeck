package ar.edu.iua.TruckTeck.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un camión del sistema.
 * <p>
 * Los camiones son los vehículos utilizados para transportar productos.
 * Cada orden especifica qué camión realizará el transporte de la carga.
 * </p>
 * 
 * <h3>Características:</h3>
 * <ul>
 *   <li><b>domain:</b> Patente única del camión (ej: "ABC123")</li>
 *   <li><b>cisterns:</b> Array que representa las cisternas del camión, permitiendo
 *       configuraciones multi-cisterna para transportes compartimentados</li>
 * </ul>
 * 
 * <h3>Integración con SAP:</h3>
 * <p>
 * El campo {@code externalCode} almacena el código del camión en el sistema SAP,
 * permitiendo sincronización y referencia cruzada con el ERP externo.
 * Este campo es opcional ({@code nullable = true}) para camiones creados
 * directamente en el sistema TMS sin origen en SAP.
 * </p>
 * 
 * @see Order
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trucks")
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String domain;

    private String description;

    private int[] cisterns;

    @Column(unique = true, nullable = true)
    private String externalCode; // Codigo de SAP para este camion

}
