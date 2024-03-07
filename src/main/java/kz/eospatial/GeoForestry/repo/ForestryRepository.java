package kz.eospatial.GeoForestry.repo;

import kz.eospatial.GeoForestry.models.Forestry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForestryRepository extends JpaRepository<Forestry, Long> {
    Optional<Forestry> findByToken(String token);
    Optional<Forestry> findByName(String name);
    Optional<Forestry> findByRegion(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    void deleteByName(String name);
    List<Forestry> findAllByTokenExpirationDate(LocalDate date);
    List<Forestry> findAllByTokenExpirationDateBetween(LocalDate startDate, LocalDate endDate);

}
