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


    public String regenerateTokenForForestry(Long id, LocalDate newExpirationDate) {
        Forestry forestry = forestryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with ID: " + id));
        String newToken = tokenService.generateToken();
        forestry.setToken(newToken);
        forestry.setTokenExpirationDate(newExpirationDate);
        forestryRepository.save(forestry);
        log.info("Regenerated token with new expiration date for forestry with ID: {}", id);
        return newToken;
    }


    public ForestryDto updateTokenExpirationDate(Long id, LocalDate newExpirationDate) {
        Forestry forestry = forestryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forestry not found with ID: " + id));
        forestry.setTokenExpirationDate(newExpirationDate);
        Forestry updatedForestry = forestryRepository.save(forestry);
        log.info("Updated token expiration date for forestry with ID: {}", id);
        return forestryMapper.toDto(updatedForestry);
    }
}
