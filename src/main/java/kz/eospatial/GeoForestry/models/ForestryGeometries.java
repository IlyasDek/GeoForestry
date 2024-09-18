package kz.eospatial.GeoForestry.models;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "forestry_geometries")
public class ForestryGeometries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long forestryId;

    @Column(columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geom;


    public ForestryGeometries(Long id, Long forestryId, MultiPolygon geom) {
        this.id = id;
        this.forestryId = forestryId;
        this.geom = geom;
    }

    public ForestryGeometries() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getForestryId() {
        return forestryId;
    }

    public void setForestryId(Long forestryId) {
        this.forestryId = forestryId;
    }

    public MultiPolygon getGeom() {
        return geom;
    }

    public void setGeom(MultiPolygon geom) {
        this.geom = geom;
    }
}
