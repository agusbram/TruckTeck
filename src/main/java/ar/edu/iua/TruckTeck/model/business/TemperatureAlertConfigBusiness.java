package ar.edu.iua.TruckTeck.model.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.TemperatureAlertConfigRepository;
import ar.edu.iua.TruckTeck.util.EmailService;
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
     * Marca que ya se envió un email de alerta para evitar envíos repetidos.
     *
     * @throws NotFoundException si la configuración no existe
     */
    public void setEmailSent()  throws NotFoundException {
        TemperatureAlertConfig config = getConfig();
        config.setEmailAlreadySent(true);
        repository.save(config);
    }

    /**
     * Resetea el estado de envío de correo para permitir nuevos avisos.
     *
     * @throws NotFoundException si la configuración no existe
     */
    public void resetEmailSent()  throws NotFoundException {
        TemperatureAlertConfig config = getConfig();
        config.setEmailAlreadySent(false);
        repository.save(config);
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
    public boolean checkAndSendAlert(double currentTemperature) throws NotFoundException {
        TemperatureAlertConfig config = getConfig();

        double threshold = config.getThreshold();

        log.info("Verificando temperatura: {} vs threshold: {}", currentTemperature, threshold);

        // Si ya enviamos y no queremos spamear, no hacemos nada
       if (config.isEmailAlreadySent()) {
           log.info("Email ya fue enviado previamente, no se reenviará para evitar spam");
           return false;
        }

        // ¿Superó el umbral?
       if (currentTemperature > threshold) {
           log.info("La temperatura actual (" + currentTemperature + "°C) superó el límite configurado (" + threshold + "°C).");
           
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
                       "⚠️ Alerta de temperatura",
                       "La temperatura actual (" + currentTemperature + "°C) superó el límite configurado (" + threshold + "°C)."
                   );
                   log.info("Email de alerta enviado exitosamente a {}", email);
               } catch (Exception e) {
                   log.error("Error al enviar email a {}: {}", email, e.getMessage(), e);
               }
           }

            // Evitar el spam
            log.info("Marcando alerta enviada como true para evitar spam");
            config.setEmailAlreadySent(true);
            repository.save(config);

            return true; // alerta enviada
        }

        log.debug("Temperatura {} no superó el threshold {}", currentTemperature, threshold);
        return false; // no superó el umbral
    }
}

