package kz.eospatial.GeoForestry.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.config.SignInRequest;
import kz.eospatial.GeoForestry.services.AuthenticationService;
import kz.eospatial.GeoForestry.config.jwt.JwtAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;

//    @Operation(summary = "Регистрация пользователя")
//    @PostMapping("/sign-up")
//    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
//        return authenticationService.signUp(request);
//    }

    @Operation(summary = "User authorization")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        logger.info("Attempting to authorize user: {}", request.getUsername()); // Logging the authorization attempt
        try {
            JwtAuthenticationResponse response = authenticationService.signIn(request);
            logger.info("User {} successfully authorized", request.getUsername()); // Successful authorization
            return response;
        } catch (Exception e) {
            logger.error("Error authorizing user {}: {}", request.getUsername(), e.toString()); // Detailed error logging
            throw e;
        }
    }
}