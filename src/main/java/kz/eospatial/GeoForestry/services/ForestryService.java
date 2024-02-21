package kz.eospatial.GeoForestry.services;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.exeptions.ForestryNameExistsException;
import kz.eospatial.GeoForestry.mappers.AbstractForestryMapper;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.repo.ForestryRepository;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate;


import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForestryService {

    private static final Logger log = LoggerFactory.getLogger(ForestryService.class);
    private final ForestryRepository forestryRepository;
    private final TokenService tokenService;
    private final AbstractForestryMapper forestryMapper;
//    private GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);


    public ForestryService(ForestryRepository forestryRepository, TokenService tokenService, AbstractForestryMapper forestryMapper1) {
    this.forestryRepository = forestryRepository;
        this.tokenService = tokenService;
    this.forestryMapper = forestryMapper1;
}

    private void checkForestryNameUniqueness(String name) {
        if (forestryRepository.existsByName(name)) {
            throw new ForestryNameExistsException("Forestry with name " + name + " already exists.");
        }
    }

    private void logSavedForestry(Forestry forestry) {
        log.info("Forestry saved with ID: {} and token: {}", forestry.getId(), forestry.getToken());
    }

    @Transactional
    public AbstractMap.SimpleEntry<ForestryDto, String> addForestry(ForestryDto forestryDto) {
        log.info("Received request to add forestry: {}", forestryDto.getName());
        log.info("Center from DTO: Longitude = {}, Latitude = {}", forestryDto.getCenter().getLongitude(), forestryDto.getCenter().getLatitude());
        checkForestryNameUniqueness(forestryDto.getName());

        Forestry forestry = convertToForestryEntity(forestryDto);
        log.info("Forestry entity with geometric data prepared for saving: {}", forestry);

        String generatedToken = tokenService.generateToken();
        forestry.setToken(generatedToken);

        Forestry savedForestry = saveForestry(forestry);
        log.info("Forestry entity with geometric data saved: {}", savedForestry);

        return new AbstractMap.SimpleEntry<>(forestryMapper.toDto(savedForestry), generatedToken);
    }

    private Forestry convertToForestryEntity(ForestryDto forestryDto) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Преобразование GeoCoordinate центра в Coordinate JTS
        Point centerPoint = geometryFactory.createPoint(new Coordinate(forestryDto.getCenter().getLongitude(), forestryDto.getCenter().getLatitude()));
        centerPoint.setSRID(4326); // Установка SRID для центра
        log.info("Serialized center for saving: {}", centerPoint.toText());

//        // Преобразование списка GeoCoordinate границ в массив Coordinate JTS и создание полигона
//        Coordinate[] boundaryCoords = forestryDto.getBoundaries().stream()
//                .map(geoCoord -> new Coordinate(geoCoord.getLongitude(), geoCoord.getLatitude()))
//                .toArray(Coordinate[]::new);
//        // Добавляем первую точку в конец, если это необходимо, чтобы замкнуть полигон
//        if (boundaryCoords.length > 0 && !boundaryCoords[0].equals2D(boundaryCoords[boundaryCoords.length - 1])) {
//            boundaryCoords = Arrays.copyOf(boundaryCoords, boundaryCoords.length + 1);
//            boundaryCoords[boundaryCoords.length - 1] = boundaryCoords[0];
//        }
//        Polygon polygon = geometryFactory.createPolygon(geometryFactory.createLinearRing(boundaryCoords), null);
//        polygon.setSRID(4326); // Установка SRID для полигона

        Forestry forestry = new Forestry();
        forestry.setName(forestryDto.getName());
        forestry.setMapStyleUrl(forestryDto.getMapStyleUrl());
        forestry.setTokenExpirationDate(forestryDto.getTokenExpirationDate());
        forestry.setCenter(centerPoint);
//        forestry.setBoundaries(polygon);

        return forestry;
    }


    private Forestry saveForestry(Forestry forestry) {
        return forestryRepository.save(forestry);
    }


    @Transactional
    public ForestryDto updateForestry(String name, ForestryDto forestryDto) {
        Forestry existingForestry = forestryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));

        forestryMapper.updateForestryFromDto(forestryDto, existingForestry);

        Forestry updatedForestry = forestryRepository.save(existingForestry);
        log.info("Forestry updated with name: {}", name);
        return forestryMapper.toDto(updatedForestry);
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

    @Transactional
    public boolean deleteForestryByName(String name) {
        if (forestryRepository.existsByName(name)) {
            forestryRepository.deleteByName(name);
            log.info("Deleted forestry with name: {}", name);
            return true;
        }
        log.warn("Attempted to delete forestry with name: {}, but it does not exist", name);
        return false;
    }

    @Transactional
    public String regenerateTokenForForestry(String name) {
        Forestry forestry = forestryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));
        String newToken = tokenService.generateToken();
        forestry.setToken(newToken);
        forestryRepository.save(forestry);
        log.info("Regenerated token for forestry with name: {}", name);
        return newToken;
    }

    @Transactional
    public ForestryDto updateTokenExpirationDate(String name, LocalDate newExpirationDate) {
        Forestry forestry = forestryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));
        forestry.setTokenExpirationDate(newExpirationDate);
        Forestry updatedForestry = forestryRepository.save(forestry);
        log.info("Updated token expiration date for forestry with name: {}", name);
        return forestryMapper.toDto(updatedForestry);
    }

    public List<ForestryDto> getForestriesByTokenExpirationDate(LocalDate date) {
        log.info("Retrieving all forestries with token expiration date: {}", date);
        return forestryRepository.findAllByTokenExpirationDate(date).stream()
                .map(forestryMapper::toDto)
                .collect(Collectors.toList());
    }
}
