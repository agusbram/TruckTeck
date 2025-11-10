package ar.edu.iua.TruckTeck.model.enums;

/**
 * Estados posibles de una orden en el sistema TMS.
 * <p>
 * Este enum representa la máquina de estados que controla el flujo de trabajo
 * de una orden desde su creación hasta su finalización.
 * </p>
 * 
 * <h3>Flujo de estados y transiciones:</h3>
 * <pre>
 * PENDING → TARA_REGISTERED → LOADING → FINALIZED
 * </pre>
 * 
 * <h3>Descripción de estados y sistemas que los activan:</h3>
 * <ul>
 *   <li><b>PENDING:</b> Estado inicial al crear la orden (API REST o SAP).
 *       La orden espera que el sistema de balanza registre el peso inicial del camión vacío.</li>
 *   
 *   <li><b>TARA_REGISTERED:</b> El sistema TMS (balanza) registró el peso inicial (tara).
 *       Activado por: {@code POST /api/v1/tms/b2b/weighing/initial}.
 *       El camión está autorizado para iniciar la carga.</li>
 *   
 *   <li><b>LOADING:</b> La carga está en progreso, el sistema recibe datos en tiempo real.
 *       El cambio a este estado puede ser automático al recibir el primer {@link ar.edu.iua.TruckTeck.model.OrderDetail}.
 *       Durante este estado se registran múltiples mediciones de carga.</li>
 *   
 *   <li><b>FINALIZED:</b> La carga finalizó y se registró el peso final del camión cargado.
 *       Activado por: {@code POST /api/v1/tms/b2b/weighing/final}.
 *       Estado terminal, no permite más transiciones.</li>
 * </ul>
 * 
 * <p><b>Nota:</b> Cada transición de estado queda registrada en {@link ar.edu.iua.TruckTeck.model.OrderStatusLog}
 * para auditoría, incluyendo timestamp, usuario/sistema responsable y observaciones.</p>
 * 
 * @see ar.edu.iua.TruckTeck.model.Order
 * @see ar.edu.iua.TruckTeck.model.OrderStatusLog
 * @see ar.edu.iua.TruckTeck.integration.tms.model.business.OrderTmsBusiness
 */
public enum OrderState {
    PENDING,            // 1 - Pendiente de pesaje inicial
    TARA_REGISTERED,    // 2 - Con pesaje inicial registrado
    LOADING,            // 3 - En carga
    FINALIZED           // 4 - Finalizada
}