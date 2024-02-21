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

import java.time.LocalDate;
import java.util.List;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class ForestryDto {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Map style URL cannot be blank")
    @URL(message = "Map style URL must be a valid URL")
    private String mapStyleUrl;

    @NotNull(message = "Boundaries cannot be null")
    private List<GeoCoordinate> boundaries;

    @NotNull(message = "Center cannot be null")
    private GeoCoordinate center;;

    private String mapBoxToken;

    @NotNull(message = "Token expiration date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate tokenExpirationDate;

    public ForestryDto(String name, String mapStyleUrl, List<GeoCoordinate> boundaries,
                       GeoCoordinate center, String mapBoxToken, LocalDate tokenExpirationDate) {
        this.name = name;
        this.mapStyleUrl = mapStyleUrl;
        this.boundaries = boundaries;
        this.center = center;
        this.mapBoxToken = mapBoxToken;
        this.tokenExpirationDate = tokenExpirationDate;
    }

    public ForestryDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapStyleUrl() {
        return mapStyleUrl;
    }

    public void setMapStyleUrl(String mapStyleUrl) {
        this.mapStyleUrl = mapStyleUrl;
    }

    public List<GeoCoordinate> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(List<GeoCoordinate> boundaries) {
        this.boundaries = boundaries;
    }

    public GeoCoordinate getCenter() {
        return center;
    }

    public void setCenter(GeoCoordinate center) {
        this.center = center;
    }

    public String getMapBoxToken() {
        return mapBoxToken;
    }

    public void setMapBoxToken(String mapBoxToken) {
        this.mapBoxToken = mapBoxToken;
    }

    public LocalDate getTokenExpirationDate() {
        return tokenExpirationDate;
    }

    public void setTokenExpirationDate(LocalDate tokenExpirationDate) {
        this.tokenExpirationDate = tokenExpirationDate;
    }

    @Override
    public String toString() {
        return "ForestryDto{" +
                "name='" + name + '\'' +
                ", mapStyleUrl='" + mapStyleUrl + '\'' +
                ", boundaries=" + boundaries +
                ", center=" + center +
                ", mapBoxToken='" + mapBoxToken + '\'' +
                ", tokenExpirationDate=" + tokenExpirationDate +
                '}';
    }
}
