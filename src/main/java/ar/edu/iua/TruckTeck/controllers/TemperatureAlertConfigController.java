package ar.edu.iua.TruckTeck.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;
import ar.edu.iua.TruckTeck.model.Truck;
import ar.edu.iua.TruckTeck.model.business.ITemperatureAlertConfigBusiness;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;

@RestController
@RequestMapping(Constants.URL_ALARM)
public class TemperatureAlertConfigController {

    @Autowired
    private IStandardResponseBusiness response;

    @Autowired
    private ITemperatureAlertConfigBusiness temperatureAlertConfigBusiness;

    @PutMapping("")
    public ResponseEntity<?> updateConfig(@RequestBody TemperatureAlertConfig temp) {
        try {
            TemperatureAlertConfig updated =
                    temperatureAlertConfigBusiness.updateConfig(temp.getThreshold(), temp.getEmails());

            return new ResponseEntity<>(updated, HttpStatus.OK);

        } catch (NotFoundException e) {
            return new ResponseEntity<>(
                response.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        }
    }

    @PutMapping("/reset-email")
    public ResponseEntity<?> resetEmailSent() {
        try {
            temperatureAlertConfigBusiness.resetEmailSent();
            return new ResponseEntity<>(
                response.build(HttpStatus.OK, null, "Flag emailAlreadySent reseteado correctamente"),
                HttpStatus.OK
            );

        } catch (NotFoundException e) {
            return new ResponseEntity<>(
            response.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
            HttpStatus.NOT_FOUND
            );
        }
    }


    @PostMapping(value = "/first")
    public ResponseEntity<?> fistConfig(@RequestBody TemperatureAlertConfig temp) {
        try {
            TemperatureAlertConfig response = temperatureAlertConfigBusiness.firstConfig(temp.getThreshold(), temp.getEmails());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_ALARM + "/" + response.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(
                response.build(HttpStatus.NOT_FOUND, e, e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        }
    }
}

