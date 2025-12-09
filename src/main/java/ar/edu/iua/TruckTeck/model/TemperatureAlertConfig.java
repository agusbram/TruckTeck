package ar.edu.iua.TruckTeck.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "temperature_alert_config")
public class TemperatureAlertConfig {

    @Id
    private Long id; // usaremos siempre id = 1

    private Double threshold; // temperatura de peligro

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "temperature_alert_emails",
        joinColumns = @JoinColumn(name = "config_id")
    )
    @Column(name = "email")
    private List<String> emails = new ArrayList<>();
}
