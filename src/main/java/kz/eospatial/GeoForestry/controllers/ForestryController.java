package kz.eospatial.GeoForestry.controllers;

import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.services.ForestryService;
import kz.eospatial.GeoForestry.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
@RequestMapping("/api/forestry")
public class ForestryController {

    private static final Logger log = LoggerFactory.getLogger(ForestryController.class);

    private final ForestryService forestryService;
    private final TokenService tokenService;

    public ForestryController(ForestryService forestryService, TokenService tokenService) {
        this.forestryService = forestryService;
        this.tokenService = tokenService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<ForestryDto> getForestryByToken(@PathVariable String token) {
        log.info("Received request to retrieve forestry with token: {}", token);

        if (!tokenService.validateToken(token)) {
            log.warn("Token validation failed for token: {}", token);
            return ResponseEntity.badRequest().build();
        }

        log.info("Token validation succeeded for token: {}", token);
        Optional<ForestryDto> forestryDto = forestryService.getForestryByToken(token);

        return forestryDto.map(dto -> {
            log.info("Forestry found with token: {}", token);
            ForestryDto responseDto = new ForestryDto(dto.getName(), dto.getMapStyleUrl(), dto.getLatitude(), dto.getLongitude(), dto.getTokenExpirationDate());
            return ResponseEntity.ok(responseDto);
        }).orElseGet(() -> {
            log.warn("No forestry found with token: {}", token);
            return ResponseEntity.notFound().build();
        });
    }
}