package kz.eospatial.GeoForestry.services;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.exeptions.ForestryNameExistsException;
import kz.eospatial.GeoForestry.mappers.ForestryMapper;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.models.ForestryGeometries;
import kz.eospatial.GeoForestry.models.GeoCoordinate;
import kz.eospatial.GeoForestry.repo.ForestryGeometriesRepository;
import kz.eospatial.GeoForestry.repo.ForestryRepository;
import kz.eospatial.GeoForestry.utils.GeoJsonUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForestryManagementService {

    private static final Logger log = LoggerFactory.getLogger(ForestryManagementService.class);
    private final ForestryRepository forestryRepository;
    private final TokenService tokenService;
    private final ForestryMapper forestryMapper;
    private final ForestryGeometriesRepository forestryGeometriesRepository;

    @Autowired
    public ForestryManagementService(ForestryRepository forestryRepository,
                                     TokenService tokenService,
                                     ForestryMapper forestryMapper,
                                     ForestryGeometriesRepository forestryGeometriesRepository) {
        this.forestryRepository = forestryRepository;
        this.tokenService = tokenService;
        this.forestryMapper = forestryMapper;
        this.forestryGeometriesRepository = forestryGeometriesRepository;
    }

//    // Метод для добавления лесничества с byte[]
//    public AbstractMap.SimpleEntry<ForestryDto, String> addForestry(ForestryDto forestryDto, byte[] geoJsonData) throws IOException {
//        return processForestry(forestryDto, geoJsonData);
//    }

    // Метод для добавления лесничества с MultipartFile
    public AbstractMap.SimpleEntry<ForestryDto, String> addForestry(ForestryDto forestryDto, MultipartFile geoJsonFile) throws IOException {
        return processForestry(forestryDto, geoJsonFile != null ? geoJsonFile.getBytes() : null);
    }

    // Унифицированный метод для обработки данных
    private AbstractMap.SimpleEntry<ForestryDto, String> processForestry(ForestryDto forestryDto, byte[] geoJsonData) throws IOException {
        log.info("Received request to add forestry: {}", forestryDto);

        if (forestryRepository.existsByName(forestryDto.getName())) {
            throw new ForestryNameExistsException("A forestry business with the name " + forestryDto.getName() + " already exists.");
        }

        Forestry forestry = forestryMapper.toModel(forestryDto);
        // Генерируем токен и сохраняем его в сущности
        String generatedToken = tokenService.generateToken();
        forestry.setToken(generatedToken);
        Forestry savedForestry = forestryRepository.save(forestry);
        log.info("New forestry ID: {}", savedForestry.getId());

        // Обработка геометрии (GeoJSON)
        if (geoJsonData != null && geoJsonData.length > 0) {
            String geoJson = new String(geoJsonData, StandardCharsets.UTF_8);
            log.info("GeoJSON content: {}", geoJson);
            MultiPolygon multiPolygon = GeoJsonUtils.parseGeoJson(geoJson);

            // Сохраняем геометрию в базе данных
            ForestryGeometries forestryGeometries = new ForestryGeometries();
            forestryGeometries.setForestryId(savedForestry.getId());
            forestryGeometries.setGeom(multiPolygon);
            forestryGeometriesRepository.save(forestryGeometries);
        } else {
            log.info("GeoJSON data not provided, skipping geometry processing");
        }

        log.info("Forestry saved with ID: {} and token: {}", savedForestry.getId(), savedForestry.getToken());

        // Конвертируем сохранённую сущность обратно в DTO
        ForestryDto resultDto = forestryMapper.toDto(savedForestry);
        return new AbstractMap.SimpleEntry<>(resultDto, generatedToken);
    }

    @Transactional
    public ForestryDto updateForestry(Long id, ForestryDto forestryDto, MultipartFile geoJsonFile) throws IOException {
        // Поиск существующего лесничества
        Forestry existingForestry = forestryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with ID: " + id));

        log.info("Updating forestry: existing name = {}, new name = {}", existingForestry.getName(), forestryDto.getName());

        // Проверка на null перед обновлением
        if (forestryDto.getName() == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        // Проверка уникальности имени лесничества
        if (!forestryDto.getName().equals(existingForestry.getName()) &&
                forestryRepository.existsByName(forestryDto.getName())) {
            throw new DataIntegrityViolationException(
                    "The name of the forestry unit should be unique. A forestry with a name \"" + forestryDto.getName() + "\" already exists.");
        }

        // Обновление полей лесничества
        existingForestry.setName(forestryDto.getName());
        existingForestry.setRegion(forestryDto.getRegion());
        existingForestry.setMapStyleUrl(forestryDto.getMapStyleUrl());
        existingForestry.setMapBoxToken(forestryDto.getMapBoxToken());
        existingForestry.setTokenExpirationDate(forestryDto.getTokenExpirationDate());

        // Сохранение обновлённого лесничества
        Forestry updatedForestry = forestryRepository.save(existingForestry);

        // Обновление геометрии (если передан GeoJSON файл)
        if (geoJsonFile != null && !geoJsonFile.isEmpty()) {
            String geoJson = new String(geoJsonFile.getBytes(), StandardCharsets.UTF_8);
            log.info("Updating GeoJSON content: {}", geoJson);
            MultiPolygon multiPolygon = GeoJsonUtils.parseGeoJson(geoJson);

            ForestryGeometries geometry = forestryGeometriesRepository.findByForestryId(id);
            if (geometry != null) {
                // Обновление существующей геометрии
                forestryGeometriesRepository.updateGeomByForestryId(id, multiPolygon);
                log.info("Geometry updated for forestry ID: {}", id);
            } else {
                // Если геометрия не существовала — создание новой записи
                ForestryGeometries newGeometry = new ForestryGeometries();
                newGeometry.setForestryId(id);
                newGeometry.setGeom(multiPolygon);
                forestryGeometriesRepository.save(newGeometry);
                log.info("New geometry created for forestry ID: {}", id);
            }
        }

        log.info("Forestry updated with ID: {}", id);

        // Конвертация обновлённой сущности обратно в DTO
        return forestryMapper.toDtoWithToken(updatedForestry);
    }

    @Transactional
    public ForestryDto addGeoJsonToForestry(Long id, MultipartFile geoJsonFile) throws IOException {
        // Поиск существующего лесничества
        Forestry existingForestry = forestryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with ID: " + id));

        // Обновление геометрии
        if (geoJsonFile != null && !geoJsonFile.isEmpty()) {
            String geoJson = new String(geoJsonFile.getBytes(), StandardCharsets.UTF_8);
            log.info("Adding GeoJSON content for forestry ID {}: {}", id, geoJson);
            MultiPolygon multiPolygon = GeoJsonUtils.parseGeoJson(geoJson);

            ForestryGeometries geometry = forestryGeometriesRepository.findByForestryId(id);
            if (geometry != null) {
                // Обновление существующей геометрии
                forestryGeometriesRepository.updateGeomByForestryId(id, multiPolygon);
                log.info("Geometry updated for forestry ID: {}", id);
            } else {
                // Если геометрия не существовала — создание новой записи
                ForestryGeometries newGeometry = new ForestryGeometries();
                newGeometry.setForestryId(id);
                newGeometry.setGeom(multiPolygon);
                forestryGeometriesRepository.save(newGeometry);
                log.info("New geometry created for forestry ID: {}", id);
            }
        } else {
            throw new IllegalArgumentException("GeoJSON файл не предоставлен.");
        }

        log.info("Forestry GeoJSON updated for ID: {}", id);

        // Конвертация сущности обратно в DTO
        return forestryMapper.toDtoWithToken(existingForestry);
    }


    // Метод для удаления GeoJSON для лесничества
    public void deleteForestryGeoJson(Long id) {
        // Удаление геометрии, связанной с лесничеством
        forestryGeometriesRepository.deleteByForestryId(id);
        log.info("Forestry GeoJSON deleted for forestry ID: {}", id);
    }

    // Метод для удаления лесничества
    public boolean deleteForestryById(Long id) {
        return forestryRepository.findById(id)
                .map(forestry -> {
                    // Проверка наличия геометрии перед удалением
                    ForestryGeometries geometry = forestryGeometriesRepository.findByForestryId(id);
                    if (geometry != null) {
                        // Удаление геометрии, связанной с лесничеством
                        forestryGeometriesRepository.deleteByForestryId(id);
                        log.info("Deleted geometry for forestry with ID: {}", id);
                    }

                    // Удаление лесничества
                    forestryRepository.delete(forestry);
                    log.info("Deleted forestry with ID: {}", id);

                    return true;
                }).orElseGet(() -> {
                    log.warn("Attempted to delete forestry with ID: {}, but it does not exist", id);
                    return false;
                });
    }
}

