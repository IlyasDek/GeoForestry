package kz.eospatial.GeoForestry.controllers;

import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.exeptions.UserAlreadyExistsException;
import kz.eospatial.GeoForestry.services.ForestryService;
import kz.eospatial.GeoForestry.user.UserService;
import kz.eospatial.GeoForestry.user.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final ForestryService forestryService;
    private final UserService userService;

    public AdminController(ForestryService forestryService, UserService userService) {
        this.forestryService = forestryService;
        this.userService = userService;
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

    @PostMapping("/addAdmin")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody Users admin) {
        try {
            Users createdAdmin = userService.addUser(admin.getUsername(), admin.getEmail(), admin.getPassword(), admin.getRole());
            return new ResponseEntity<>(createdAdmin, HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
}
