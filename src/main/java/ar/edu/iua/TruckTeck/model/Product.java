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
 * Entidad que representa un producto que puede ser cargado en los camiones.
 * <p>
 * Los productos son los materiales o sustancias que se transportan.
 * Cada orden especifica qué producto debe cargarse en el camión.
 * </p>
 * 
 * <h3>Integración con SAP:</h3>
 * <p>
 * El campo {@code externalCode} almacena el código del producto en el sistema SAP,
 * permitiendo sincronización y referencia cruzada con el ERP externo.
 * Este campo es opcional ({@code nullable = true}) para productos creados
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
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String description;

    @Column(unique = true, nullable = true)
    private String externalCode; // Codigo de SAP para este producto
}
