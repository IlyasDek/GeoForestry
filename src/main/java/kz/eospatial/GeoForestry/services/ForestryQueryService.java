package kz.eospatial.GeoForestry.services;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.mappers.ForestryMapper;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.repo.ForestryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForestryQueryService {

    private static final Logger log = LoggerFactory.getLogger(ForestryQueryService.class);
    private final ForestryRepository forestryRepository;
    private final ForestryMapper forestryMapper;

    @Autowired
    public ForestryQueryService(ForestryRepository forestryRepository, ForestryMapper forestryMapper) {
        this.forestryRepository = forestryRepository;
        this.forestryMapper = forestryMapper;
    }

    public List<ForestryDto> getAllForestries() {
        log.info("Retrieving all forestries");
        List<ForestryDto> forestryDtos = forestryRepository.findAll().stream()
                .map(forestryMapper::toDtoWithToken)
                .collect(Collectors.toList());
        log.info(forestryDtos.toString());
        return forestryDtos;
    }

    public ForestryDto getForestryById(Long id) {
        log.info("Retrieving forestry with ID: {}", id);
        return forestryRepository.findById(id)
                .map(forestryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with id: " + id));
    }

    public ForestryDto getForestryByName(String name) {
        log.info("Retrieving forestry with name: {}", name);
        return forestryRepository.findByName(name)
                .map(forestryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));
    }

    public ForestryDto getForestryByRegion(String region) {
        log.info("Retrieving forestry with region: {}", region);
        return forestryRepository.findByRegion(region)
                .map(forestryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + region));
    }

    public Optional<ForestryDto> getForestryByToken(String token) {
        log.info("Retrieving forestry with token: {}", token);
        Optional<ForestryDto> list = forestryRepository.findByToken(token)
                .map(forestryMapper::toDto);
        log.info(list.toString());
        return list;
    }

    public List<ForestryDto> getForestriesByTokenExpirationDate(LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving all forestries with token expiration date range: {} to {}", startDate, endDate);
        List<Forestry> forestries;
        if (startDate != null && endDate != null) {
            forestries = forestryRepository.findAllByTokenExpirationDateBetween(startDate, endDate);
        } else if (startDate != null) {
            forestries = forestryRepository.findAllByTokenExpirationDate(startDate);
        } else {
            // Определите, как вы хотите обрабатывать запросы без даты или предоставьте все лесничества
            forestries = new ArrayList<>();
        }
        return forestries.stream()
                .map(forestryMapper::toDto)
                .collect(Collectors.toList());
    }
}
