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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class TokenManagementService {

    private static final Logger log = LoggerFactory.getLogger(TokenManagementService.class);
    private final ForestryMapper forestryMapper = ForestryMapper.INSTANCE;
    private final ForestryRepository forestryRepository;
    private final TokenService tokenService;

    @Autowired
    public TokenManagementService(ForestryRepository forestryRepository, TokenService tokenService) {
        this.forestryRepository = forestryRepository;
        this.tokenService = tokenService;
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
}
