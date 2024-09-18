package kz.eospatial.GeoForestry.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.dto.TokenUpdateRequest;
import kz.eospatial.GeoForestry.dto.UpdatePasswordRequest;
import kz.eospatial.GeoForestry.facades.AdminFacade;
import kz.eospatial.GeoForestry.services.ForestryManagementService;
import kz.eospatial.GeoForestry.user.UserService;
import kz.eospatial.GeoForestry.user.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminFacade adminFacade;

    @Autowired
    private ObjectMapper objectMapper;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createForestry(
            @RequestParam("forestry") String forestryDtoJson,
            @RequestParam(value = "geojson", required = false) MultipartFile geoJsonFile) {
        try {
            // Преобразуем JSON в ForestryDto
            ForestryDto forestryDto = objectMapper.readValue(forestryDtoJson, ForestryDto.class);

            // Создаем лесничество через фасад
            ResponseEntity<Map<String, Object>> response = adminFacade.addForestry(forestryDto, geoJsonFile);

            // Возвращаем результат с DTO и токеном в заголовке
            return response;
        } catch (Exception e) {
            log.error("Error creating forestry", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request data", "message", e.getMessage()));
        }
    }

    // Обновление лесничества с возможностью добавления/обновления GeoJSON
    @PatchMapping(value = "/forestries/{id}/update", consumes = { "multipart/form-data" })
    public ResponseEntity<Map<String, Object>> updateForestry(
            @PathVariable Long id,
            @RequestParam("forestry") String forestryDtoJson,
            @RequestParam(value = "geojson", required = false) MultipartFile geoJsonFile) {
        try {
            // Преобразуем JSON в ForestryDto
            ForestryDto forestryDto = objectMapper.readValue(forestryDtoJson, ForestryDto.class);
            log.info(forestryDto.toString());

            // Вызов фасада для обновления лесничества
            ResponseEntity<Map<String, Object>> response = adminFacade.updateForestry(id, forestryDto, geoJsonFile);
            return response;
        } catch (Exception e) {
            log.error("Error updating forestry", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request data", "message", e.getMessage()));
        }
    }

    // Добавление GeoJSON к существующему лесничеству
    @PostMapping("/forestries/{id}/geojson")
    public ResponseEntity<?> addGeoJsonToForestry(
            @PathVariable Long id,
            @RequestParam("geojson") MultipartFile geoJsonFile) {
        return adminFacade.addGeoJsonToForestry(id, geoJsonFile);
    }

    @DeleteMapping("/forestries/{id}/geojson")
    public ResponseEntity<?> deleteForestryGeoJson(@PathVariable Long id) {
        return adminFacade.deleteForestryGeoJson(id);
    }

    @DeleteMapping("/forestries/{id}")
    public ResponseEntity<?> deleteForestryById(@PathVariable Long id) {
        try {
            return adminFacade.deleteForestryById(id);
        } catch (Exception e) {
            log.error("Error deleting forestry with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to delete forestry", "message", e.getMessage()));
        }
    }

    @GetMapping("/forestries")
    public ResponseEntity<?> getAllForestries() {
        return adminFacade.getAllForestries();
    }

    @GetMapping("/forestries/id/{id}")
    public ResponseEntity<?> getForestryById(@PathVariable Long id) {
        return adminFacade.getForestryById(id);
    }

    @GetMapping("/forestries/name/{name}")
    public ResponseEntity<?> getForestryByName(@PathVariable String name) {
        return adminFacade.getForestryByName(name);
    }

    @GetMapping("/forestries/region/{region}")
    public ResponseEntity<?> getForestryByRegion(@PathVariable String region) {
        return adminFacade.getForestryByRegion(region);
    }

    @PostMapping("/addAdmin")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody Users admin) {
        return adminFacade.addAdmin(admin);
    }

    @PatchMapping("/users/{userId}/password")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long userId, @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        return adminFacade.updateUserPassword(userId, updatePasswordRequest.getNewPassword());
    }

    @PatchMapping("/forestries/{id}/regenerateToken")
    public ResponseEntity<?> regenerateToken(@PathVariable Long id, @RequestBody TokenUpdateRequest tokenUpdateRequest) {
        return adminFacade.regenerateTokenForForestry(id, tokenUpdateRequest);
    }


    @PatchMapping("/forestries/{id}/updateTokenExpiration")
    public ResponseEntity<?> updateTokenExpiration(@PathVariable Long id, @RequestBody TokenUpdateRequest request) {
        return adminFacade.updateTokenExpirationDate(id, request);
    }



    @GetMapping("/forestries/byTokenExpiration")
    public ResponseEntity<?> getForestriesByTokenExpirationDate(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return adminFacade.getForestriesByTokenExpirationDate(date, startDate, endDate);
    }

}
