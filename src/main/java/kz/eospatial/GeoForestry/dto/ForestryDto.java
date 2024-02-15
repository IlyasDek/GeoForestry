package kz.eospatial.GeoForestry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForestryDto {

    private String name;
    private String mapStyleUrl;
    private Double latitude;
    private Double longitude;
    private LocalDate tokenExpirationDate;
}
