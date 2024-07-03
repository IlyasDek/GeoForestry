package kz.eospatial.GeoForestry.facades;

import jakarta.persistence.EntityNotFoundException;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.dto.TokenUpdateRequest;
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
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminFacade {

    private static final Logger log = LoggerFactory.getLogger(AdminFacade.class);
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
            log.warn("Attempt to add forestry failed, name exists: {}", forestryDto.getName(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Conflict", "message",
                    e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating forestry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while adding forestry", "message", e.getMessage()));
        }
    }


    public ResponseEntity<?> updateForestry(Long id, ForestryDto forestryDto) {
        log.info("Updating forestry with ID: {}", id);
        try {
            ForestryDto updatedForestryDto = forestryManagementService.updateForestry(id, forestryDto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Forestry updated successfully");
            response.put("forestry", updatedForestryDto);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Forestry not found for update with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not Found", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating forestry with ID: {}, error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error occurred while updating forestry", "message", e.getMessage()));
        }
    }

    public ResponseEntity<?> deleteForestryById(Long id) {
        log.info("Deleting forestry with ID: {}", id);
        try {
            boolean isDeleted = forestryManagementService.deleteForestryById(id);
            if (!isDeleted) {
                log.warn("Forestry not found for deletion with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not Found",
                        "message", "Forestry not found for deletion with ID: " + id));
            }
            log.info("Forestry deleted with ID: {}", id);
            return ResponseEntity.ok(Map.of("message", "Forestry deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting forestry with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while deleting forestry", "message", e.getMessage()));
        }
    }

    public ResponseEntity<?> getAllForestries() {
        log.info("Fetching all forestries");
        try {
            List<ForestryDto> forestryDtos = forestryQueryService.getAllForestries();
            return ResponseEntity.ok(forestryDtos);
        } catch (Exception e) {
            log.error("Error fetching all forestries", e);
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("error", "Service Unavailable");
            errorDetails.put("message", "Unable to fetch the list of forestries at this time. Please try again later.");
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(errorDetails);
        }
    }


    public ResponseEntity<?> getForestryById(Long id) {
        log.info("Fetching forestry with ID: {}", id);
        try {
            ForestryDto forestryDto = forestryQueryService.getForestryById(id);
            return ResponseEntity.ok(forestryDto);
        } catch (EntityNotFoundException e) {
            log.warn("Forestry not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not Found",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching forestry with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while fetching forestry", "message", e.getMessage()));
        }
    }

    public ResponseEntity<?> getForestryByName(String name) {
        log.info("Fetching forestry with name: {}", name);
        try {
            ForestryDto forestryDto = forestryQueryService.getForestryByName(name);
            return ResponseEntity.ok(forestryDto);
        } catch (EntityNotFoundException e) {
            log.warn("Forestry not found with name: {}", name, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not Found",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching forestry with name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while fetching forestry", "message", e.getMessage()));
        }
    }

    public ResponseEntity<?> getForestryByRegion(String region) {
        log.info("Fetching forestry with region: {}", region);
        try {
            ForestryDto forestryDto = forestryQueryService.getForestryByRegion(region);
            return ResponseEntity.ok(forestryDto);
        } catch (EntityNotFoundException e) {
            log.warn("Forestry not found with region: {}", region, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not Found",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching forestry with region: {}", region, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while fetching forestry", "message", e.getMessage()));
        }
    }

    public ResponseEntity<?> addAdmin(Users admin) {
        log.info("Attempting to add new admin with username: {}", admin.getUsername());
        try {
            Users createdAdmin = userService.addUser(admin.getUsername(), admin.getEmail(), admin.getPassword(),
                    admin.getRole());
            log.info("Admin created successfully with username: {}", admin.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin created successfully.");
            response.put("admin", createdAdmin);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            log.warn("Attempt to add admin failed, username exists: {}", admin.getUsername(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Conflict",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding new admin: {}", admin.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while adding new admin", "message", e.getMessage()));
        }
    }

    public ResponseEntity<?> updateUserPassword(Long userId, String newPassword) {
        try {
            userService.updatePassword(userId, newPassword);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } catch (Exception e) {
            log.error("Error updating password: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while updating password", "message", e.getMessage()));
        }
    }

    public ResponseEntity<?> regenerateTokenForForestry(Long id, TokenUpdateRequest tokenUpdateRequest) {
        log.info("Attempting to regenerate token for forestry with ID: {} and new expiration date: {}", id,
                tokenUpdateRequest);
        try {
            LocalDate newExpirationDate = tokenUpdateRequest.getNewExpirationDate();
            String newToken = tokenManagementService.regenerateTokenForForestry(id, newExpirationDate);
            log.info("Token regenerated successfully for forestry with ID: {}. New Token: {}, " +
                    "New Expiration Date: {}", id, newToken, newExpirationDate);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token regenerated successfully with new expiration date.");
            response.put("newToken", newToken);
            response.put("newExpirationDate", newExpirationDate);

            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            log.error("Invalid date format for new expiration date: {}, error: {}", tokenUpdateRequest, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format for new expiration date."));
        } catch (EntityNotFoundException e) {
            log.error("Forestry not found with ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not Found",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error regenerating token for forestry with ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while regenerating token"));
        }
    }



    public ResponseEntity<?> updateTokenExpirationDate(Long id, TokenUpdateRequest request) {
        log.info("Attempting to update token expiration date for forestry with ID: {}", id);
        try {
            ForestryDto updatedForestry = tokenManagementService.updateTokenExpirationDate(id, request.getNewExpirationDate());
            log.info("Token expiration date updated successfully for forestry with ID: {}", id);
            return ResponseEntity.ok(updatedForestry);
        } catch (EntityNotFoundException e) {
            log.error("Forestry not found with ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not Found",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating token expiration date for forestry with ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",
                    "Internal server error occurred while updating token expiration date", "message", e.getMessage()));
        }
    }


    public ResponseEntity<?> getForestriesByTokenExpirationDate(LocalDate date, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching forestries with token expiration date range: {} to {}", startDate, endDate);
        if (date != null) {
            startDate = date;
            endDate = date;
        }
        try {
            List<ForestryDto> forestries = forestryQueryService.getForestriesByTokenExpirationDate(startDate, endDate);
            log.info("Forestries retrieved successfully for token expiration date range: {} to {}", startDate, endDate);
            return ResponseEntity.ok(forestries);
        } catch (Exception e) {
            log.error("Error fetching forestries by token expiration date: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error occurred while fetching forestries",
                            "message", e.getMessage()));
        }
    }



}
