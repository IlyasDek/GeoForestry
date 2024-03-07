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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForestryManagementService {

    private static final Logger log = LoggerFactory.getLogger(ForestryManagementService.class);
    private final ForestryRepository forestryRepository;
    private final TokenService tokenService;
    private final ForestryMapper forestryMapper;

    @Autowired
    public ForestryManagementService(ForestryRepository forestryRepository, TokenService tokenService, ForestryMapper forestryMapper) {
        this.forestryRepository = forestryRepository;
        this.tokenService = tokenService;
        this.forestryMapper = forestryMapper;
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
        existingForestry.setRegion(forestryDto.getRegion());
        existingForestry.setMapStyleUrl(forestryDto.getMapStyleUrl());
        existingForestry.setMapBoxToken(forestryDto.getMapBoxToken());
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

    public boolean deleteForestryByName(String name) {
        if (forestryRepository.existsByName(name)) {
            forestryRepository.deleteByName(name);
            log.info("Deleted forestry with name: {}", name);
            return true;
        }
        log.warn("Attempted to delete forestry with name: {}, but it does not exist", name);
        return false;
    }
}

