package ar.edu.iua.TruckTeck.model.business;


import java.util.List;

import ar.edu.iua.TruckTeck.model.Alarm;
import ar.edu.iua.TruckTeck.model.OrderDetail;
import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

public interface ITemperatureAlertConfigBusiness {

    TemperatureAlertConfig getConfig() throws NotFoundException;

    TemperatureAlertConfig updateConfig(Double threshold, List<String> emails) throws NotFoundException;

    void resetEmailSent(Alarm alarm) throws NotFoundException, BusinessException, FoundException;

    boolean checkAndSendAlert(OrderDetail detail) throws NotFoundException;

    TemperatureAlertConfig firstConfig(Double threshold, java.util.List<String> emails) throws NotFoundException;
}

