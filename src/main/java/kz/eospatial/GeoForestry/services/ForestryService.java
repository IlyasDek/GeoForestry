package kz.eospatial.GeoForestry.services;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.mappers.ForestryMapper;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.repo.ForestryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForestryService {

    private static final Logger log = LoggerFactory.getLogger(ForestryService.class);
    private final ForestryRepository forestryRepository;
    private final TokenService tokenService;
    private final ForestryMapper forestryMapper = ForestryMapper.INSTANCE;

    public ForestryService(ForestryRepository forestryRepository, TokenService tokenService) {
        this.forestryRepository = forestryRepository;
        this.tokenService = tokenService;
    }

    public ForestryDto addForestry(ForestryDto forestryDto) {
        log.info("Received request to add forestry: {}", forestryDto);
        Forestry forestry = forestryMapper.toModel(forestryDto);
        String generatedToken = tokenService.generateToken();
        forestry.setToken(generatedToken);
        log.info("Generated new token for forestry: {}", generatedToken);
        Forestry savedForestry = forestryRepository.save(forestry);
        log.info("Forestry saved with ID: {} and token: {}", savedForestry.getId(), savedForestry.getToken());
        ForestryDto resultDto = forestryMapper.toDto(savedForestry);
        log.info("Returning saved forestry DTO: {}", resultDto);
        return resultDto;
    }

    public ForestryDto updateForestry(Long id, ForestryDto forestryDto) {
        Forestry existingForestry = forestryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with id: " + id));
        existingForestry.setName(forestryDto.getName());
        existingForestry.setMapStyleUrl(forestryDto.getMapStyleUrl());
        existingForestry.setLatitude(forestryDto.getLatitude());
        existingForestry.setLongitude(forestryDto.getLongitude());
        existingForestry.setTokenExpirationDate(forestryDto.getTokenExpirationDate());
        log.info("Updating forestry with ID: {}", id);
        return forestryMapper.toDto(forestryRepository.save(existingForestry));
    }

    public List<ForestryDto> getAllForestries() {
        log.info("Retrieving all forestries");
        return forestryRepository.findAll().stream()
                .map(forestryMapper::toDto)
                .collect(Collectors.toList());
    }

    public ForestryDto getForestryById(Long id) {
        log.info("Retrieving forestry with ID: {}", id);
        return forestryRepository.findById(id)
                .map(forestryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with id: " + id));
    }

    public Optional<ForestryDto> getForestryByToken(String token) {
        log.info("Retrieving forestry with token: {}", token);
        return forestryRepository.findByToken(token)
                .map(forestryMapper::toDto);
    }

    public boolean deleteForestry(Long id) {
        if (forestryRepository.existsById(id)) {
            forestryRepository.deleteById(id);
            log.info("Deleted forestry with ID: {}", id);
            return true;
        }
        log.warn("Attempted to delete forestry with ID: {}, but it does not exist", id);
        return false;
    }
}
