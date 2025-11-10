package ar.edu.iua.TruckTeck.model;

import lombok.AllArgsConstructor;
// import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa la conciliación de una orden finalizada.
 * <p>
 * Esta clase contiene todos los datos calculados para la conciliación
 * entre los valores del caudalímetro y la balanza (TMS), incluyendo
 * promedios de parámetros de carga.
 * </p>
 */
/* @Data */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Conciliation {
    
    /**
     * Pesaje inicial del camión (tara) en kilogramos.
     */
    private Double initialWeight;

    /**
     * Pesaje final del camión cargado en kilogramos.
     */
    private Double finalWeight;

    /**
     * Masa acumulada registrada por el caudalímetro en kilogramos.
     * Representa el último valor de masa acumulada durante la carga.
     */
    private Double accumulatedMass;

    /**
     * Peso neto calculado por la balanza en kilogramos.
     * Se calcula como: Pesaje final - Pesaje inicial
     */
    private Double netWeight;

    /**
     * Diferencia entre el peso neto de la balanza y la masa acumulada del caudalímetro.
     * Se calcula como: Neto por balanza - Producto cargado
     * Un valor positivo indica que la balanza registró más peso que el caudalímetro.
     */
    private Double differenceWeight;

    /**
     * Temperatura promedio durante el proceso de carga en grados Celsius.
     */
    private Double averageTemperature;

    /**
     * Densidad promedio del producto durante la carga en kg/m³.
     */
    private Double averageDensity;

    /**
     * Caudal promedio durante el proceso de carga en m³/h.
     */
    private Double averageCaudal;
}
