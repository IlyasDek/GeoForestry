package kz.eospatial.GeoForestry.controllers;

import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.services.ForestryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final ForestryService forestryService;

    public AdminController(ForestryService forestryService) {
        this.forestryService = forestryService;
    }

    @PostMapping("/forestries")
    public ResponseEntity<ForestryDto> addForestry(@Valid @RequestBody ForestryDto forestryDto) {
        logger.info("Adding forestry: {}", forestryDto);
        ForestryDto createdForestryDto = forestryService.addForestry(forestryDto);
        logger.info("Created forestry: {}", createdForestryDto);
        return ResponseEntity.ok(createdForestryDto);
    }

    @GetMapping("/forestries")
    public ResponseEntity<List<ForestryDto>> getAllForestries() {
        List<ForestryDto> forestryDtos = forestryService.getAllForestries();
        logger.info("Fetching all forestries");
        return ResponseEntity.ok(forestryDtos);
    }

    @GetMapping("/forestries/{id}")
    public ResponseEntity<ForestryDto> getForestryById(@PathVariable Long id) {
        logger.info("Fetching forestry with ID: {}", id);
        ForestryDto forestryDto = forestryService.getForestryById(id);
        if (forestryDto == null) {
            logger.warn("Forestry not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(forestryDto);
    }

    @PutMapping("/forestries/{id}")
    public ResponseEntity<ForestryDto> updateForestry(@PathVariable Long id, @Valid @RequestBody ForestryDto forestryDto) {
        logger.info("Updating forestry with ID: {}", id);
        ForestryDto updatedForestryDto = forestryService.updateForestry(id, forestryDto);
        if (updatedForestryDto == null) {
            logger.warn("Forestry not found for update with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Updated forestry: {}", updatedForestryDto);
        return ResponseEntity.ok(updatedForestryDto);
    }

    @DeleteMapping("/forestries/{id}")
    public ResponseEntity<Void> deleteForestry(@PathVariable Long id) {
        logger.info("Deleting forestry with ID: {}", id);
        boolean isDeleted = forestryService.deleteForestry(id);
        if (!isDeleted) {
            logger.warn("Forestry not found for deletion with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Forestry deleted with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
