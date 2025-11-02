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
 * Entidad que representa un chofer/conductor del sistema.
 * <p>
 * Los choferes son los responsables de conducir los camiones y realizar
 * las operaciones de carga. Cada orden debe estar asociada a un chofer específico.
 * </p>
 * 
 * <h3>Integración con SAP:</h3>
 * <p>
 * El campo {@code externalCode} almacena el código del chofer en el sistema SAP,
 * permitiendo sincronización y referencia cruzada con el ERP externo.
 * Este campo es opcional ({@code nullable = true}) para choferes creados
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
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String surname;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(unique = true, nullable = true)
    private String externalCode; // Codigo de SAP para este chofer
}
