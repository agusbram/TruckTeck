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
 * Entidad que representa un cliente del sistema.
 * <p>
 * Los clientes son las empresas o personas que solicitan órdenes de carga
 * de productos. Cada orden debe estar asociada a un cliente específico.
 * </p>
 * 
 * <h3>Integración con SAP:</h3>
 * <p>
 * El campo {@code externalCode} almacena el código del cliente en el sistema SAP,
 * permitiendo sincronización y referencia cruzada con el ERP externo.
 * Este campo es opcional ({@code nullable = true}) para clientes creados
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
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String companyName;

    private String contactName;

    @Column(unique = true, nullable = true)
    private String externalCode; // Codigo de SAP para este cliente
}