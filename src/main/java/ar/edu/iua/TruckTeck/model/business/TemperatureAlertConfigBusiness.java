package ar.edu.iua.TruckTeck.model.business;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.TemperatureAlertConfigRepository;
import ar.edu.iua.TruckTeck.util.EmailService;

@Service
public class TemperatureAlertConfigBusiness  implements ITemperatureAlertConfigBusiness{

    @Autowired
    private TemperatureAlertConfigRepository repository;

    @Autowired
    private EmailService emailService;

    /**
     * Devuelve siempre la configuración única.
     */
    public TemperatureAlertConfig getConfig() throws NotFoundException {
        return repository.findById(1L)
        .orElseThrow(() -> new NotFoundException("No se encontró la configuración de temperatura (id=1)"));
    }

    /**
     * Actualiza threshold y emails.
     */
    public TemperatureAlertConfig updateConfig(Double threshold, java.util.List<String> emails) throws NotFoundException {
        TemperatureAlertConfig config = getConfig();

        if (threshold != null) config.setThreshold(threshold);
        if (emails != null) config.setEmails(emails);

        return repository.save(config);
    }

    /**
     * Actualiza threshold y emails.
     */
    public TemperatureAlertConfig firstConfig(Double threshold, java.util.List<String> emails) throws NotFoundException {
        TemperatureAlertConfig config = new TemperatureAlertConfig();

        if (threshold != null) config.setThreshold(threshold);
        if (emails != null) config.setEmails(emails);

        return repository.save(config);
    }

    /**
     * Marca que ya se envió un email (para evitar spam).
     */
    public void setEmailSent()  throws NotFoundException {
        TemperatureAlertConfig config = getConfig();
        config.setEmailAlreadySent(true);
        repository.save(config);
    }

    /**
     * Resetea el flag para cuando llegue una nueva orden.
     */
    public void resetEmailSent()  throws NotFoundException {
        TemperatureAlertConfig config = getConfig();
        config.setEmailAlreadySent(false);
        repository.save(config);
    }

    /**
 * Verifica la temperatura actual y envía un email si supera el threshold.
 * Devuelve true si se envió el mail de alerta.
 */
    public boolean checkAndSendAlert(double currentTemperature) throws NotFoundException {
        TemperatureAlertConfig config = getConfig();

        double threshold = config.getThreshold();

        // Si ya enviamos y no queremos spamear, no hacemos nada
       if (config.isEmailAlreadySent()) {
           return false;
        }

        // ¿Superó el umbral?
       if (currentTemperature > threshold) {
           // Verificamos que existan emails configurados
           if (config.getEmails() == null || config.getEmails().isEmpty()) {
               throw new RuntimeException("No hay emails configurados para enviar alerta");
          }

          // Enviar email a todos los configurados
           for (String email : config.getEmails()) {
             emailService.sendEmail(
                  email,
                  "⚠️ Alerta de temperatura",
                  "La temperatura actual (" + currentTemperature + "°C) superó el límite configurado (" + threshold + "°C)."
            );
            }

            // Evitar el spam
            config.setEmailAlreadySent(true);
            repository.save(config);

            return true; // alerta enviada
        }

        return false; // no superó el umbral
    }
}

