package ar.edu.iua.TruckTeck.model.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.auth.model.User;
import ar.edu.iua.TruckTeck.auth.model.persistence.UserRepository;
import ar.edu.iua.TruckTeck.model.Alarm;
import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.OrderDetail;
import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.TemperatureAlertConfigRepository;
import ar.edu.iua.TruckTeck.util.EmailService;

import java.time.LocalDateTime;
import java.util.List;

/**
* Servicio encargado de gestionar la configuración de alertas de temperatura.
* <p>
* Esta clase administra la única configuración existente en el sistema 
* (almacenada siempre con id = 1), permitiendo:
* <ul>
*     <li>Obtener la configuración</li>
*     <li>Actualizar umbral de temperatura y lista de correos</li>
*     <li>Registrar y resetear el estado del envío de alertas</li>
*     <li>Validar la temperatura actual y enviar alertas por email</li>
* </ul>
* Implementa {@link ITemperatureAlertConfigBusiness}.
*/
@Service
public class TemperatureAlertConfigBusiness  implements ITemperatureAlertConfigBusiness{

    private static final Logger log = LoggerFactory.getLogger(TemperatureAlertConfigBusiness.class);

    @Autowired
    private TemperatureAlertConfigRepository repository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IOrderBusiness orderBusiness;

    @Autowired
    private IAlarmBusiness alarmBusiness;

    @Autowired
    private UserRepository userRepository;

    /**
    * Obtiene la configuración única del sistema (siempre id = 1).
    *
    * @return la configuración almacenada
    * @throws NotFoundException si no existe la configuración inicial
    */
    public TemperatureAlertConfig getConfig() throws NotFoundException {
        return repository.findById(1L)
        .orElseThrow(() -> new NotFoundException("No se encontró la configuración de temperatura (id=1)"));
    }

    /**
    * Actualiza el umbral de temperatura y la lista de emails configurados.
    *
    * @param threshold nuevo umbral de temperatura, puede ser null
    * @param emails lista de correos para alertas, puede ser null
    * @return la configuración actualizada
    * @throws NotFoundException si no se encuentra la configuración base
    */
    public TemperatureAlertConfig updateConfig(Double threshold, List<String> emails) throws NotFoundException {
        TemperatureAlertConfig config = getConfig();

        if (threshold != null) config.setThreshold(threshold);
        if (emails != null) config.setEmails(emails);

        return repository.save(config);
    }

    /**
     * Crea la configuración inicial (solo se usa la primera vez que se monta el sistema).
     * Siempre guarda la configuración bajo id = 1.
     *
     * @param threshold umbral inicial de temperatura
     * @param emails lista inicial de correos
     * @return la configuración almacenada
     * @throws NotFoundException no se lanza actualmente, pero se mantiene para consistencia
     */
    public TemperatureAlertConfig firstConfig(Double threshold, List<String> emails) throws NotFoundException {
        TemperatureAlertConfig config = new TemperatureAlertConfig();
        config.setId(1L); // Always use id=1 as the single configuration

        if (threshold != null) config.setThreshold(threshold);
        if (emails != null) config.setEmails(emails);

        return repository.save(config);
    }


    /**
     * Resetea el estado de envío de correo para permitir nuevos avisos.
     *
     * @param id Identificador de la alarma asociada
     * @throws NotFoundException si la configuración no existe
     */
    public void resetEmailSent(Alarm alarm)  throws NotFoundException, BusinessException, FoundException {

        Alarm alarmBD = alarmBusiness.load(alarm.getId());
        Order order = orderBusiness.load(alarmBD.getOrderNumber());

        // Reset de la orden
        order.setTemperatureAlarmSent(false);

        // Estado de la alarma
        alarmBD.setAlarmState(false);

        // Cargar el usuario REAL desde la BD
        if (alarm.getUser() != null && alarm.getUser().getIdUser() != null) {
            User userBD = userRepository.findById(alarm.getUser().getIdUser()).orElseThrow(() -> new NotFoundException("User no encontrado"));
            alarmBD.setUser(userBD);
        }

        // Observaciones y fecha
        alarmBD.setObservations(alarm.getObservations());
        alarmBD.setAcceptedDateTime(LocalDateTime.now());

        // Guardar cambios
        orderBusiness.update(order);
        alarmBusiness.update(alarmBD);
    }

    /**
     * Verifica la temperatura actual y envía un correo de alerta si supera el
     * umbral configurado y si no se envió previamente un aviso.
     * <p>
     * Si la alerta se envía, se marca como enviada para evitar spam.
     *
     * @param currentTemperature temperatura actual medida
     * @return true si se envió un email de alerta; false si no era necesario o no se pudo enviar
     * @throws NotFoundException si la configuración no existe
     */
    public boolean checkAndSendAlert(OrderDetail detail) throws NotFoundException {
        TemperatureAlertConfig config = getConfig();

        double threshold = config.getThreshold();

        log.info("Verificando temperatura: {} vs threshold: {}", detail.getTemperature(), threshold);

        // Si ya enviamos y no queremos spamear, no hacemos nada
       if (detail.getOrder().getTemperatureAlarmSent()) {
           log.info("Email ya fue enviado previamente, no se reenviará para evitar spam");
           return false;
        }

        // ¿Superó el umbral?
       if (detail.getTemperature() > threshold) {
           log.info("La temperatura actual (" + detail.getTemperature() + "°C) superó el límite configurado (" + threshold + "°C).");
           
           // Guardar la alarma en la base de datos ANTES de enviar emails
           try {
               alarmBusiness.saveAlarm(detail, threshold);
               log.info("Alarma guardada en base de datos para orden {}", detail.getOrder().getNumber());
           } catch (BusinessException e) {
               log.error("Error al guardar alarma en BD: {}", e.getMessage(), e);
               // Continuar con el envío de email aunque falle el guardado
           }
           
           // Verificamos que existan emails configurados
           if (config.getEmails() == null || config.getEmails().isEmpty()) {
               log.error("No hay emails configurados para enviar alerta");
               return false; // Retornar false en lugar de lanzar excepción
          }

          log.info("Encontrados {} emails configurados", config.getEmails().size());

          // Enviar email a todos los configurados
           for (String email : config.getEmails()) {
               try {
                   log.info("Enviando email a: {}", email);
                   emailService.sendEmail(
                        email,
                        "⚠️ Alerta de temperatura - Orden " + detail.getOrder().getNumber(),
                        String.format(
                            """
                            *ALERTA DE TEMPERATURA*

                            Se ha detectado que la temperatura actual ha superado el límite configurado.

                            *Detalles de la orden:*
                            - Número de orden: %s
                            - Fecha y hora del evento: %s

                            *Lectura registrada:*
                            - Temperatura actual: %.2f °C
                            - Umbral configurado: %.2f °C

                            --
                            Sistema de Alarma TruckTeck
                            """,
                            detail.getOrder().getNumber(),
                            detail.getTimestamp().toString(),
                            detail.getTemperature(),
                            threshold
                        )
                    );
                   log.info("Email de alerta enviado exitosamente a {}", email);
               } catch (Exception e) {
                   log.error("Error al enviar email a {}: {}", email, e.getMessage(), e);
               }
           }

            // Evitar el spam
            log.info("Marcando alerta enviada como true para evitar spam");
            repository.save(config);

            return true; // alerta enviada
        }

        log.debug("Temperatura {} no superó el threshold {}", detail.getTemperature(), threshold);
        return false; // no superó el umbral
    }
}

