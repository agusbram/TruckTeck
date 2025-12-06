package ar.edu.iua.TruckTeck.model.business;


import java.util.List;

import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

public interface ITemperatureAlertConfigBusiness {

    TemperatureAlertConfig getConfig() throws NotFoundException;

    TemperatureAlertConfig updateConfig(Double threshold, List<String> emails) throws NotFoundException;

    void setEmailSent() throws NotFoundException;

    void resetEmailSent() throws NotFoundException;

    boolean checkAndSendAlert(double currentTemperature) throws NotFoundException;

    TemperatureAlertConfig firstConfig(Double threshold, java.util.List<String> emails) throws NotFoundException;
}

