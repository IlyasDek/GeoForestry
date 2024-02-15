package kz.eospatial.GeoForestry.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class Forestry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String mapStyleUrl;
    private Double latitude;
    private Double longitude;
    private String token;
    private LocalDate tokenExpirationDate;

    public Forestry(Long id, String name, String mapStyleUrl, Double latitude, Double longitude, String token, LocalDate tokenExpirationDate) {
        this.id = id;
        this.name = name;
        this.mapStyleUrl = mapStyleUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.token = token;
        this.tokenExpirationDate = tokenExpirationDate;
    }

    public Forestry() {
    }


}
