package ar.edu.iua.TruckTeck.model.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.iua.TruckTeck.model.TemperatureAlertConfig;

public interface TemperatureAlertConfigRepository extends JpaRepository<TemperatureAlertConfig, Long> {
}
