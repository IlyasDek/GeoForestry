package kz.eospatial.GeoForestry.models;

import jakarta.persistence.*;

import java.time.LocalDate;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class Forestry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String name;
    private String mapStyleUrl;
    @Column(length = 1000)
    private String boundaries;
    private String center;
    private String mapBoxToken;
    private String token;
    private LocalDate tokenExpirationDate;

    @Override
    public String toString() {
        return "Forestry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mapStyleUrl='" + mapStyleUrl + '\'' +
                ", boundaries='" + boundaries + '\'' +
                ", center='" + center + '\'' +
                ", mapBoxToken='" + mapBoxToken + '\'' +
                ", token='" + token + '\'' +
                ", tokenExpirationDate=" + tokenExpirationDate +
                '}';
    }

    public Forestry(Long id, String name, String mapStyleUrl,
                    String boundaries, String center, String mapBoxToken, String token, LocalDate tokenExpirationDate) {
        this.id = id;
        this.name = name;
        this.mapStyleUrl = mapStyleUrl;
        this.boundaries = boundaries;
        this.center = center;
        this.mapBoxToken = mapBoxToken;
        this.token = token;
        this.tokenExpirationDate = tokenExpirationDate;
    }

    public Forestry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(String boundaries) {
        this.boundaries = boundaries;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getMapBoxToken() {
        return mapBoxToken;
    }

    public void setMapBoxToken(String mapBoxToken) {
        this.mapBoxToken = mapBoxToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDate getTokenExpirationDate() {
        return tokenExpirationDate;
    }

    public void setTokenExpirationDate(LocalDate tokenExpirationDate) {
        this.tokenExpirationDate = tokenExpirationDate;
    }
}
