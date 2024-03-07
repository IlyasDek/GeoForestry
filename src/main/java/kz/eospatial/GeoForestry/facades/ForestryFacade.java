package kz.eospatial.GeoForestry.facades;

import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.models.TokenValidationResult;
import kz.eospatial.GeoForestry.services.ForestryQueryService;
import kz.eospatial.GeoForestry.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForestryFacade {

    private static final Logger log = LoggerFactory.getLogger(ForestryFacade.class);
    private final ForestryQueryService forestryService;
    private final TokenService tokenService;

    @Autowired
    public ForestryFacade(ForestryQueryService forestryService, TokenService tokenService) {
        this.forestryService = forestryService;
        this.tokenService = tokenService;
    }

    public ResponseEntity<?> getForestryByToken(String token) {
        log.info("Received request to retrieve forestry with token: {}", token);

        TokenValidationResult validationResult = tokenService.validateToken(token);
        if (!validationResult.isValid()) {
            log.warn("Token validation failed for token: {}", token);
            return ResponseEntity.badRequest().body(validationResult.getMessage());
        }

        log.info("Token validation succeeded for token: {}", token);
        Optional<ForestryDto> forestryDto = forestryService.getForestryByToken(token);

        return forestryDto.map(dto -> {
            log.info("Forestry found with token: {}", token);
            // Используйте метод маппера toDtoWithToken, если это необходимо для включения токена в DTO
            // Если нет, просто используйте toDto как вы делали до этого
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> {
            log.warn("No forestry found with token: {}", token);
            return ResponseEntity.notFound().build();
        });
    }
}
