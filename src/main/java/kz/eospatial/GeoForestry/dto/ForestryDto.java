package kz.eospatial.GeoForestry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.eospatial.GeoForestry.models.GeoCoordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.locationtech.jts.geom.Coordinate;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForestryDto {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Map style URL cannot be blank")
    @URL(message = "Map style URL must be a valid URL")
    private String mapStyleUrl;

//    @NotNull(message = "Boundaries cannot be null")
//    private List<GeoCoordinate> boundaries;

    @NotNull(message = "Center cannot be null")
    private GeoCoordinate center;;

    @NotNull(message = "Token expiration date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate tokenExpirationDate;
}
