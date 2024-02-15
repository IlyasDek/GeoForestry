package kz.eospatial.GeoForestry.repo;

import kz.eospatial.GeoForestry.models.Forestry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForestryRepository extends JpaRepository<Forestry, Long> {
    Optional<Forestry> findByToken(String token);
}
