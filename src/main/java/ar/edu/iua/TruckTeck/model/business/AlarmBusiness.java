package ar.edu.iua.TruckTeck.model.business;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.Alarm;
import ar.edu.iua.TruckTeck.model.OrderDetail;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.AlarmRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la interfaz {@link IAlarmBusiness} que gestiona la lógica 
 * de negocio para las alarmas de temperatura.
 * <p>
 * Esta clase utiliza {@link AlarmRepository} para persistir y recuperar alarmas,
 * y aplica la lógica de negocio correspondiente. Las alarmas se crean automáticamente
 * cuando la temperatura registrada en un {@link OrderDetail} supera el umbral configurado.
 * </p>
 *
 * <p>
 * Anotaciones:
 * <ul>
 *   <li>{@code @Service}: Marca la clase como un componente de servicio de Spring.</li>
 *   <li>{@code @Slf4j}: Proporciona un logger para registrar eventos y errores.</li>
 * </ul>
 * </p>
 * 
 * @see ar.edu.iua.TruckTeck.model.Alarm
 * @see ar.edu.iua.TruckTeck.model.business.TemperatureAlertConfigBusiness
 */
@Service
@Slf4j
public class AlarmBusiness implements IAlarmBusiness {

    /**
     * Repositorio para acceder a los datos de alarmas.
     * <p>
     * Inyectado automáticamente por Spring.
     * </p>
     */
    @Autowired
    private AlarmRepository alarmRepository;

    /**
     * Guarda una nueva alarma de temperatura en la base de datos.
     * <p>
     * Crea un registro de alarma con los datos obtenidos del {@link OrderDetail}:
     * <ul>
     *   <li>orderNumber: detail.getOrder().getNumber()</li>
     *   <li>eventDateTime: detail.getTimestamp()</li>
     *   <li>currentTemperature: detail.getTemperature()</li>
     *   <li>thresholdTemperature: (parámetro recibido)</li>
     *   <li>orderState: detail.getOrder().getState()</li>
     * </ul>
     * </p>
     * 
     * @param detail Detalle de la orden con los datos de temperatura y timestamp.
     * @param thresholdTemperature Umbral de temperatura que fue superado.
     * @return La alarma creada y guardada en la base de datos.
     * @throws BusinessException Si ocurre un error al guardar la alarma.
     */
    @Override
    public Alarm saveAlarm(OrderDetail detail, Double thresholdTemperature) throws BusinessException {
        try {
            Alarm alarm = new Alarm(
                detail.getOrder().getNumber(),
                detail.getTimestamp(),
                detail.getTemperature(),
                thresholdTemperature,
                true
            );
            
            Alarm savedAlarm = alarmRepository.save(alarm);
            log.info("Alarma guardada exitosamente: Orden {}, Estado {}, Temperatura {}°C > {}°C", 
                     alarm.getOrderNumber(), alarm.getAlarmState(), alarm.getCurrentTemperature(), alarm.getThresholdTemperature());
            
            return savedAlarm;
        } catch(Exception e) {
            log.error("Error al guardar alarma: {}", e.getMessage(), e);
            throw BusinessException.builder().ex(e).message("Error al guardar la alarma: " + e.getMessage()).build();
        }
    }

    /**
     * Obtiene la lista completa de todas las alarmas registradas,
     * ordenadas por fecha descendente (más reciente primero).
     *
     * @return Lista de todas las alarmas del sistema.
     * @throws BusinessException Si ocurre un error al recuperar las alarmas.
     */
    @Override
    public List<Alarm> listAll() throws BusinessException {
        try {
            return alarmRepository.findAllByOrderByEventDateTimeDesc();
        } catch(Exception e) {
            log.error("Error al listar alarmas: {}", e.getMessage(), e);
            throw BusinessException.builder().ex(e).message("Error al listar alarmas: " + e.getMessage()).build();
        }
    }

    /**
     * Carga una alarma específica por su identificador único.
     *
     * @param id Identificador de la alarma.
     * @return La alarma correspondiente al ID.
     * @throws BusinessException Si ocurre un error en la lógica de negocio.
     * @throws NotFoundException Si no se encuentra una alarma con ese ID.
     */
    @Override
    public Alarm load(Long id) throws BusinessException, NotFoundException {
        Optional<Alarm> alarm;
        
        try {
            alarm = alarmRepository.findById(id);
        } catch(Exception e) {
            log.error("Error al cargar alarma con id {}: {}", id, e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        
        if(alarm.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra la alarma con id: " + id).build();
        }
        
        return alarm.get();
    }

    /**
     * Actualiza un cliente existente.
     * <p>
     * Verifica que el cliente a actualizar exista y que no haya otro cliente con el mismo nombre.
     * </p>
     *
     * @param alarm alarma con los datos actualizados.
     * @return La {@link Alarm} actualizada.
     * @throws BusinessException Si ocurre un error en la lógica de negocio o en el acceso a datos.
     * @throws NotFoundException Si no se encuentra la alarma a actualizar.
     * @throws FoundException Si ya existe otra alarma con el mismo nombre.
     */
    @Override
    public Alarm update(Alarm alarm) throws BusinessException, NotFoundException {
        load(alarm.getId());
        
        try {
            return alarmRepository.save(alarm);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    /**
     * Obtiene todas las alarmas registradas para un número de orden específico,
     * ordenadas por fecha descendente.
     *
     * @param orderNumber Número de la orden.
     * @return Lista de alarmas de esa orden (puede estar vacía).
     * @throws BusinessException Si ocurre un error al recuperar las alarmas.
     */
    @Override
    public List<Alarm> findByOrderNumber(String orderNumber) throws BusinessException {
        try {
            return alarmRepository.findByOrderNumber(orderNumber);
        } catch(Exception e) {
            log.error("Error al buscar alarmas para orden {}: {}", orderNumber, e.getMessage(), e);
            throw BusinessException.builder().ex(e).message("Error al buscar alarmas: " + e.getMessage()).build();
        }
    }

}
