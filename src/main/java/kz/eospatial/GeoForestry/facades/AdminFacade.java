package kz.eospatial.GeoForestry.facades;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.dto.TokenExpirationUpdateRequest;
import kz.eospatial.GeoForestry.exeptions.ForestryNameExistsException;
import kz.eospatial.GeoForestry.exeptions.UserAlreadyExistsException;
import kz.eospatial.GeoForestry.services.ForestryManagementService;
import kz.eospatial.GeoForestry.services.ForestryQueryService;
import kz.eospatial.GeoForestry.services.TokenManagementService;
import kz.eospatial.GeoForestry.user.UserService;
import kz.eospatial.GeoForestry.user.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminFacade {

    private static final Logger logger = LoggerFactory.getLogger(AdminFacade.class);
    private final ForestryManagementService forestryManagementService;
    private final ForestryQueryService forestryQueryService;
    private final TokenManagementService tokenManagementService;
    private final UserService userService;

    @Autowired
    public AdminFacade(ForestryManagementService forestryManagementService, ForestryQueryService forestryQueryService,
                       TokenManagementService tokenManagementService, UserService userService) {
        this.forestryManagementService = forestryManagementService;
        this.forestryQueryService = forestryQueryService;
        this.tokenManagementService = tokenManagementService;
        this.userService = userService;
    }

    public ResponseEntity<?> addForestry(ForestryDto forestryDto) {
        try {
            AbstractMap.SimpleEntry<ForestryDto, String> result = forestryManagementService.addForestry(forestryDto);
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

    public ResponseEntity<?> updateForestry(String name, ForestryDto forestryDto) {
        logger.info("Updating forestry with name: {}", name);
        try {
            ForestryDto updatedForestryDto = forestryManagementService.updateForestry(name, forestryDto);
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

    public ResponseEntity<?> deleteForestryByName(String name) {
        logger.info("Deleting forestry with name: {}", name);
        boolean isDeleted = forestryManagementService.deleteForestryByName(name);
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

    public ResponseEntity<List<ForestryDto>> getAllForestries() {
        logger.info("Fetching all forestries");
        List<ForestryDto> forestryDtos = forestryQueryService.getAllForestries();
        return ResponseEntity.ok(forestryDtos);
    }

    public ResponseEntity<ForestryDto> getForestryById(Long id) {
        logger.info("Fetching forestry with ID: {}", id);
        try {
            ForestryDto forestryDto = forestryQueryService.getForestryById(id);
            return ResponseEntity.ok(forestryDto);
        } catch (EntityNotFoundException e) {
            logger.warn("Forestry not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching forestry with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<?> getForestryByName(String name) {
        logger.info("Fetching forestry with name: {}", name);
        try {
            ForestryDto forestryDto = forestryQueryService.getForestryByName(name);
            return ResponseEntity.ok(forestryDto);
        } catch (EntityNotFoundException e) {
            logger.warn("Forestry not found with name: {}", name);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching forestry with name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error occurred while fetching forestry"));
        }
    }

    public ResponseEntity<?> addAdmin(Users admin) {
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
        } catch (Exception e) {
            logger.error("Error adding new admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error occurred while adding new admin"));
        }
    }

    public ResponseEntity<?> regenerateTokenForForestry(String name) {
        logger.info("Attempting to regenerate token for forestry named: {}", name);
        try {
            String newToken = tokenManagementService.regenerateTokenForForestry(name);
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

    public ResponseEntity<?> updateTokenExpirationDate(String name, TokenExpirationUpdateRequest request) {
        logger.info("Attempting to update token expiration date for forestry: {}", name);
        try {
            ForestryDto updatedForestry = tokenManagementService.updateTokenExpirationDate(name, request.getNewExpirationDate());
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

    public ResponseEntity<List<ForestryDto>> getForestriesByTokenExpirationDate(LocalDate date, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching forestries with token expiration date range: {} to {}", startDate, endDate);
        if (date != null) {
            // Если задана конкретная дата, используем её как начальную и конечную дату
            startDate = date;
            endDate = date;
        }
        try {
            List<ForestryDto> forestries = forestryQueryService.getForestriesByTokenExpirationDate(startDate, endDate);
            logger.info("Forestries retrieved successfully for token expiration date range: {} to {}", startDate, endDate);
            return ResponseEntity.ok(forestries);
        } catch (Exception e) {
            logger.error("Error fetching forestries by token expiration date: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
