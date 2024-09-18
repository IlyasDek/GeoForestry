package kz.eospatial.GeoForestry.repo;

import kz.eospatial.GeoForestry.models.ForestryGeometries;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ForestryGeometriesRepository extends JpaRepository<ForestryGeometries, Long> {

    @Modifying
    @Query("UPDATE ForestryGeometries fg SET fg.geom = :geom WHERE fg.forestryId = :forestryId")
    void updateGeomByForestryId(@Param("forestryId") Long forestryId, @Param("geom") Geometry geom);

    @Modifying
    @Transactional
    @Query("DELETE FROM ForestryGeometries fg WHERE fg.forestryId = :forestryId")
    void deleteByForestryId(@Param("forestryId") Long forestryId);

    ForestryGeometries findByForestryId(Long forestryId);
}

