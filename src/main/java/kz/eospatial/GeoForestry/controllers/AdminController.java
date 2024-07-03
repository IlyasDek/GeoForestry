package kz.eospatial.GeoForestry.controllers;

import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.dto.ForestryDto;
import kz.eospatial.GeoForestry.dto.TokenUpdateRequest;
import kz.eospatial.GeoForestry.dto.UpdatePasswordRequest;
import kz.eospatial.GeoForestry.facades.AdminFacade;
import kz.eospatial.GeoForestry.user.UserService;
import kz.eospatial.GeoForestry.user.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
    public ResponseEntity<?> addForestry(@Valid @RequestBody ForestryDto forestryDto) {
        return adminFacade.addForestry(forestryDto);
    }

    @PutMapping("/forestries/{id}/update")
    public ResponseEntity<?> updateForestry(@PathVariable Long id, @Valid @RequestBody ForestryDto forestryDto) {
        System.out.println("UPDATE POINT");
        return adminFacade.updateForestry(id, forestryDto);
    }


    @DeleteMapping("/forestries/{id}")
    public ResponseEntity<?> deleteForestryById(@PathVariable Long id) {
        return adminFacade.deleteForestryById(id);
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
