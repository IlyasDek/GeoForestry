package kz.eospatial.GeoForestry.services;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.exeptions.ForestryNameExistsException;
import kz.eospatial.GeoForestry.mappers.ForestryMapper;
import kz.eospatial.GeoForestry.models.Forestry;
import kz.eospatial.GeoForestry.repo.ForestryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.AbstractMap;
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

        // Проверка на уникальность имени лесничества
        if (forestryRepository.existsByName(forestryDto.getName())) {
            throw new ForestryNameExistsException("Лесничество с именем " + forestryDto.getName() + " уже существует.");
        }

        // Преобразование DTO в модель
        Forestry forestry = forestryMapper.toModel(forestryDto);

        // Генерация токена для лесничества
        String generatedToken = tokenService.generateToken();
        forestry.setToken(generatedToken);

        // Сохранение лесничества в базе данных
        Forestry savedForestry = forestryRepository.save(forestry);

        // Логирование успешного сохранения
        log.info("Forestry saved with ID: {} and token: {}", savedForestry.getId(), savedForestry.getToken());

        // Преобразование сохранённой модели обратно в DTO
        ForestryDto resultDto = forestryMapper.toDto(savedForestry);

        // Возвращение DTO и сгенерированного токена в виде пары
        return new AbstractMap.SimpleEntry<>(resultDto, generatedToken);
    }


    public ForestryDto updateForestry(String name, ForestryDto forestryDto) {
        Forestry existingForestry = forestryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with name: " + name));
        existingForestry.setName(forestryDto.getName());
        existingForestry.setMapStyleUrl(forestryDto.getMapStyleUrl());
        existingForestry.setLatitude(forestryDto.getLatitude());
        existingForestry.setLongitude(forestryDto.getLongitude());
        existingForestry.setTokenExpirationDate(forestryDto.getTokenExpirationDate());
        log.info("Updating forestry with name: {}", name);
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

    public List<ForestryDto> getForestriesByTokenExpirationDate(LocalDate date) {
        log.info("Retrieving all forestries with token expiration date: {}", date);
        return forestryRepository.findAllByTokenExpirationDate(date).stream()
                .map(forestryMapper::toDto)
                .collect(Collectors.toList());
    }
}
