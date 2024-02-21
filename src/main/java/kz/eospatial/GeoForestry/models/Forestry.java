package kz.eospatial.GeoForestry.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDate;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
@Entity
public class Forestry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
    private String mapStyleUrl;
//    @Column(columnDefinition = "geometry(Polygon,4326)")
//    private Polygon boundaries;

    @Column(columnDefinition = "geometry(Point,4326)")
    @Type(type = "jts_geometry")
    private Point center;
    private String token;
    private LocalDate tokenExpirationDate;

    public Forestry(Long id, String name, String mapStyleUrl,
//                    Polygon boundaries,
                    Point center, String token, LocalDate tokenExpirationDate) {
        this.id = id;
        this.name = name;
        this.mapStyleUrl = mapStyleUrl;
//        this.boundaries = boundaries;
        this.center = center;
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

//    public Polygon getBoundaries() {
//        return boundaries;
//    }
//
//    public void setBoundaries(Polygon boundaries) {
//        this.boundaries = boundaries;
//    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
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
