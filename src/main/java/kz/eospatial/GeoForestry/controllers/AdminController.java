package kz.eospatial.GeoForestry.controllers;

import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.dto.TokenExpirationUpdateRequest;
import kz.eospatial.GeoForestry.facades.AdminFacade;
import kz.eospatial.GeoForestry.user.UserService;
import kz.eospatial.GeoForestry.user.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
public class AdminController {

    private final AdminFacade adminFacade;
    private final UserService userService;

    public AdminController(AdminFacade adminFacade, UserService userService) {
        this.adminFacade = adminFacade;
        this.userService = userService;
    }

    @PostMapping("/forestries")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addForestry(@Valid @RequestBody ForestryDto forestryDto) {
        return adminFacade.addForestry(forestryDto);
    }

    @PutMapping("/forestries/name/{name}/update")
    public ResponseEntity<?> updateForestry(@PathVariable String name, @Valid @RequestBody ForestryDto forestryDto) {
        return adminFacade.updateForestry(name, forestryDto);
    }


    @DeleteMapping("/forestries/name/{name}")
    public ResponseEntity<?> deleteForestryByName(@PathVariable String name) {
        return adminFacade.deleteForestryByName(name);
    }

    @GetMapping("/forestries")
    public ResponseEntity<List<ForestryDto>> getAllForestries() {
        return adminFacade.getAllForestries();
    }

    @GetMapping("/forestries/id/{id}")
    public ResponseEntity<ForestryDto> getForestryById(@PathVariable Long id) {
        return adminFacade.getForestryById(id);
    }

    @GetMapping("/forestries/name/{name}")
    public ResponseEntity<?> getForestryByName(@PathVariable String name) {
        return adminFacade.getForestryByName(name);
    }

    @PostMapping("/addAdmin")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody Users admin) {
        return adminFacade.addAdmin(admin);
    }


    @PatchMapping("/forestries/name/{name}/regenerateToken")
    public ResponseEntity<?> regenerateToken(@PathVariable String name) {
        return adminFacade.regenerateTokenForForestry(name);
    }


    @PatchMapping("/forestries/name/{name}/updateTokenExpiration")
    public ResponseEntity<?> updateTokenExpiration(@PathVariable String name, @RequestBody TokenExpirationUpdateRequest request) {
        return adminFacade.updateTokenExpirationDate(name, request);
    }


    @GetMapping("/forestries/byTokenExpiration")
    public ResponseEntity<List<ForestryDto>> getForestriesByTokenExpirationDate(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return adminFacade.getForestriesByTokenExpirationDate(date, startDate, endDate);
    }

}
