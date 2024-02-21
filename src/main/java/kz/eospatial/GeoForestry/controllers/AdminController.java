package kz.eospatial.GeoForestry.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.dto.TokenExpirationUpdateRequest;
import kz.eospatial.GeoForestry.exeptions.ForestryNameExistsException;
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

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addForestry(@Valid @RequestBody ForestryDto forestryDto) {
        try {
            AbstractMap.SimpleEntry<ForestryDto, String> result = forestryService.addForestry(forestryDto);
            ForestryDto createdForestryDto = result.getKey();
            String token = result.getValue();

            Map<String, Object> response = new HashMap<>();
            response.put("forestry", createdForestryDto);
            response.put("token", token);
            response.put("message", "Forestry created successfully with token.");

            return ResponseEntity.ok(response);
        } catch (ForestryNameExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating forestry: {}", e.getMessage());
            throw new RuntimeException("Internal server error occurred");
        }
    }

    @GetMapping("/forestries")
    public ResponseEntity<List<ForestryDto>> getAllForestries() {
        List<ForestryDto> forestryDtos = forestryService.getAllForestries();
        logger.info("Fetching all forestries");
        return ResponseEntity.ok(forestryDtos);
    }

    @GetMapping("/forestries/id/{id}")
    public ResponseEntity<ForestryDto> getForestryById(@PathVariable Long id) {
        logger.info("Fetching forestry with ID: {}", id);
        ForestryDto forestryDto = forestryService.getForestryById(id);
        if (forestryDto == null) {
            logger.warn("Forestry not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(forestryDto);
    }

    @GetMapping("/forestries/name/{name}")
    public ResponseEntity<?> getForestryByName(@PathVariable String name) {
        logger.info("Fetching forestry with name: {}", name);
        try {
            ForestryDto forestryDto = forestryService.getForestryByName(name);
            return ResponseEntity.ok(forestryDto);
        } catch (EntityNotFoundException e) {
            logger.warn("Forestry not found with name: {}", name);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching forestry with name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error occurred while fetching forestry"));
        }
    }

    @PutMapping("/forestries/name/{name}/update")
    public ResponseEntity<?> updateForestry(@PathVariable String name, @Valid @RequestBody ForestryDto forestryDto) {
        logger.info("Updating forestry with name: {}", name);
        try {
            ForestryDto updatedForestryDto = forestryService.updateForestry(name, forestryDto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Forestry updated successfully");
            response.put("forestry", updatedForestryDto);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.warn("Forestry not found for update with name: {}", name);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating forestry with name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error occurred while updating forestry"));
        }
    }

    @DeleteMapping("/forestries/name/{name}")
    public ResponseEntity<?> deleteForestryByName(@PathVariable String name) {
        logger.info("Deleting forestry with name: {}", name);
        boolean isDeleted = forestryService.deleteForestryByName(name);
        if (!isDeleted) {
            logger.warn("Forestry not found for deletion with name: {}", name);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", "Forestry not found for deletion with name: " + name);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        logger.info("Forestry deleted with name: {}", name);
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Forestry deleted successfully with name: " + name);
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PostMapping("/addAdmin")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody Users admin) {
        logger.info("Attempting to add new admin with username: {}", admin.getUsername());
        try {
            Users createdAdmin = userService.addUser(admin.getUsername(), admin.getEmail(), admin.getPassword(), admin.getRole());
            logger.info("Admin created successfully with username: {}", admin.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin created successfully.");
            response.put("admin", createdAdmin);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            logger.warn("Attempt to add admin failed, username exists: {}", admin.getUsername());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Conflict");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }
    }

    @PatchMapping("/forestries/name/{name}/regenerateToken")
    public ResponseEntity<?> regenerateToken(@PathVariable String name) {
        logger.info("Attempting to regenerate token for forestry named: {}", name);
        try {
            String newToken = forestryService.regenerateTokenForForestry(name);
            logger.info("Token regenerated successfully for forestry named: {}. New Token: {}", name, newToken);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token regenerated successfully.");
            response.put("newToken", newToken);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Forestry not found with name: {}, error: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error regenerating token for forestry: {}, error: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error occurred while regenerating token"));
        }
    }

    @PatchMapping("/forestries/name/{name}/updateTokenExpiration")
    public ResponseEntity<?> updateTokenExpiration(@PathVariable String name, @RequestBody TokenExpirationUpdateRequest request) {
        logger.info("Attempting to update token expiration date for forestry: {}", name);
        try {
            ForestryDto updatedForestry = forestryService.updateTokenExpirationDate(name, request.getNewExpirationDate());
            logger.info("Token expiration date updated successfully for forestry: {}", name);
            return ResponseEntity.ok(updatedForestry);
        } catch (EntityNotFoundException e) {
            logger.error("Forestry not found with name: {}, error: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating token expiration date for forestry: {}, error: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error occurred while updating token expiration date"));
        }
    }

    @GetMapping("/forestries/byTokenExpiration")
    public ResponseEntity<List<ForestryDto>> getForestriesByTokenExpirationDate(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        if (date != null) {
            // Если задана конкретная дата, используем её как начальную и конечную дату
            startDate = date;
            endDate = date;
        }
        logger.info("Fetching forestries with token expiration date range: {} to {}", startDate, endDate);
        List<ForestryDto> forestries = forestryService.getForestriesByTokenExpirationDate(startDate, endDate);
        logger.info("Forestries retrieved successfully for token expiration date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(forestries);
    }
}
