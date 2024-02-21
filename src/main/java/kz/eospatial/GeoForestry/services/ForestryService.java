package kz.eospatial.GeoForestry.services;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.exeptions.ForestryNameExistsException;
import kz.eospatial.GeoForestry.mappers.ForestryMapper;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.models.GeoCoordinate;
import kz.eospatial.GeoForestry.repo.ForestryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
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

    public AbstractMap.SimpleEntry<ForestryDto, String> addForestry(ForestryDto forestryDto) {
        log.info("Received request to add forestry: {}", forestryDto);
        if (forestryRepository.existsByName(forestryDto.getName())) {
            throw new ForestryNameExistsException("Лесничество с именем " + forestryDto.getName() + " уже существует.");
        }

        log.info("DTO before mapping: {}", forestryDto);

        Forestry forestry = forestryMapper.toModel(forestryDto);
        log.info("Entity after mapping: {}", forestry.toString());

        String generatedToken = tokenService.generateToken();
        forestry.setToken(generatedToken);
        Forestry savedForestry = forestryRepository.save(forestry);

        log.info("Forestry saved with ID: {} and token: {}", savedForestry.getId(), savedForestry.getToken());

        ForestryDto resultDto = forestryMapper.toDto(savedForestry);
        return new AbstractMap.SimpleEntry<>(resultDto, generatedToken);
    }


    public ForestryDto updateForestry(String name, ForestryDto forestryDto) {
        Forestry existingForestry = forestryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));

        // Обновление полей, не связанных с геоданными
        existingForestry.setName(forestryDto.getName());
        existingForestry.setMapStyleUrl(forestryDto.getMapStyleUrl());
        existingForestry.setTokenExpirationDate(forestryDto.getTokenExpirationDate());

        // Обновление геоданных
        // Преобразование List<GeoCoordinate> и GeoCoordinate в строки и их установка
        String boundariesAsString = forestryDto.getBoundaries().stream()
                .map(coord -> coord.getLatitude() + "," + coord.getLongitude())
                .collect(Collectors.joining(";"));
        existingForestry.setBoundaries(boundariesAsString);

        GeoCoordinate center = forestryDto.getCenter();
        if (center != null) {
            String centerAsString = center.getLatitude() + "," + center.getLongitude();
            existingForestry.setCenter(centerAsString);
        }

        Forestry updatedForestry = forestryRepository.save(existingForestry);
        log.info("Forestry updated with name: {}", name);

        // Преобразование обратно в DTO для возврата
        ForestryDto resultDto = forestryMapper.toDto(updatedForestry);
        return resultDto;
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

    public ForestryDto getForestryByName(String name) {
        log.info("Retrieving forestry with name: {}", name);
        return forestryRepository.findByName(name)
                .map(forestryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));
    }


    public Optional<ForestryDto> getForestryByToken(String token) {
        log.info("Retrieving forestry with token: {}", token);
        return forestryRepository.findByToken(token)
                .map(forestryMapper::toDto);
    }

    public boolean deleteForestryByName(String name) {
        if (forestryRepository.existsByName(name)) {
            forestryRepository.deleteByName(name);
            log.info("Deleted forestry with name: {}", name);
            return true;
        }
        log.warn("Attempted to delete forestry with name: {}, but it does not exist", name);
        return false;
    }

    public String regenerateTokenForForestry(String name) {
        Forestry forestry = forestryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));
        String newToken = tokenService.generateToken();
        forestry.setToken(newToken);
        forestryRepository.save(forestry);
        log.info("Regenerated token for forestry with name: {}", name);
        return newToken;
    }

    public ForestryDto updateTokenExpirationDate(String name, LocalDate newExpirationDate) {
        Forestry forestry = forestryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));
        forestry.setTokenExpirationDate(newExpirationDate);
        Forestry updatedForestry = forestryRepository.save(forestry);
        log.info("Updated token expiration date for forestry with name: {}", name);
        return forestryMapper.toDto(updatedForestry);
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
