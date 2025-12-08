package ar.edu.iua.TruckTeck.model.business;

import java.util.List;

import ar.edu.iua.TruckTeck.model.Alarm;
import ar.edu.iua.TruckTeck.model.OrderDetail;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

/**
 * Interfaz que define las operaciones de negocio para la gestión de alarmas de temperatura.
 * <p>
 * Proporciona métodos para registrar nuevas alarmas cuando se detecta que la temperatura
 * supera el umbral configurado, así como para consultar el historial de alarmas.
 * </p>
 * 
 * <p><b>Caso de uso principal:</b></p>
 * <ul>
 *   <li>Guardar alarma cuando {@code detail.getTemperature() > config.getThreshold()}</li>
 *   <li>Consultar historial de alarmas por orden o rango de fechas</li>
 * </ul>
 * 
 * @see ar.edu.iua.TruckTeck.model.Alarm
 * @see ar.edu.iua.TruckTeck.model.TemperatureAlertConfig
 * @see ar.edu.iua.TruckTeck.model.OrderDetail
 */
public interface IAlarmBusiness {

    /**
     * Guarda una nueva alarma de temperatura en la base de datos.
     * <p>
     * Crea un registro de alarma con los datos obtenidos del {@link OrderDetail}
     * cuando se detecta que la temperatura supera el umbral configurado.
     * </p>
     * 
     * <p><b>Datos guardados:</b></p>
     * <ul>
     *   <li>orderNumber: detail.getOrder().getNumber()</li>
     *   <li>eventDateTime: detail.getTimestamp()</li>
     *   <li>currentTemperature: detail.getTemperature()</li>
     *   <li>thresholdTemperature: (parámetro)</li>
     * </ul>
     * 
     * @param detail Detalle de la orden con los datos de temperatura y timestamp.
     * @param thresholdTemperature Umbral de temperatura que fue superado.
     * @return La alarma creada y guardada en la base de datos.
     * @throws BusinessException Si ocurre un error al guardar la alarma.
     */
    Alarm saveAlarm(OrderDetail detail, Double thresholdTemperature) throws BusinessException;

    /**
     * Obtiene todas las alarmas registradas, ordenadas por fecha descendente.
     * 
     * @return Lista de todas las alarmas del sistema.
     * @throws BusinessException Si ocurre un error al recuperar las alarmas.
     */
    List<Alarm> listAll() throws BusinessException;

    /**
     * Carga una alarma específica por su identificador único.
     * 
     * @param id Identificador de la alarma.
     * @return La alarma correspondiente al ID.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si no se encuentra una alarma con ese ID.
     */
    Alarm load(Long id) throws BusinessException, NotFoundException;

    /**
     * Actualiza una alarma existente en la base de datos.
     * <p>
     * Verifica que la alarma a actualizar exista.
     * </p>
     * @param alarm alarma con los datos actualizados.
     * @return La {@link Alarm} actualizada.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra la alarma a actualizar.
     */
    public Alarm update(Alarm alarm) throws BusinessException, NotFoundException;

    /**
     * Obtiene todas las alarmas registradas para un número de orden específico.
     * 
     * @param orderNumber Número de la orden.
     * @return Lista de alarmas de esa orden (ordenadas por fecha descendente).
     * @throws BusinessException Si ocurre un error al recuperar las alarmas.
     */
    List<Alarm> findByOrderNumber(String orderNumber) throws BusinessException;

}
