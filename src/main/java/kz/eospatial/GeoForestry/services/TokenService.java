package kz.eospatial.GeoForestry.services;

import kz.eospatial.GeoForestry.models.TokenValidationResult;
import kz.eospatial.GeoForestry.repo.ForestryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private final ForestryRepository forestryRepository;

    // Убрана аннотация @Autowired
    public TokenService(ForestryRepository forestryRepository) {
        this.forestryRepository = forestryRepository;
    }

    public String generateToken() {
        String token = UUID.randomUUID().toString();
        log.info("Generated new token: {}", token);
        return token;
    }

    public TokenValidationResult validateToken(String token) {
        return forestryRepository.findByToken(token)
                .map(forestry -> {
                    if (forestry.getTokenExpirationDate() != null &&
                            !forestry.getTokenExpirationDate().isBefore(LocalDate.now())) {
                        log.info("Token validation for {}: valid", token);
                        return TokenValidationResult.valid();
                    } else {
                        log.warn("Token validation for {}: expired", token);
                        return TokenValidationResult.expired();
                    }
                })
                .orElseGet(() -> {
                    log.warn("Token validation for {}: not found", token);
                    return TokenValidationResult.notFound();
                });
    }
}