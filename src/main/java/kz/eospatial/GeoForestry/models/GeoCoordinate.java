package kz.eospatial.GeoForestry.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoCoordinate {
    private Double latitude;
    private Double longitude;
}
