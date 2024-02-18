package kz.eospatial.GeoForestry.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.eospatial.GeoForestry.config.SignInRequest;
import kz.eospatial.GeoForestry.config.jwt.AuthenticationService;
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

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        logger.info("Авторизация пользователя: {}", request.getUsername());
        try {
            JwtAuthenticationResponse response = authenticationService.signIn(request);
            logger.info("Авторизация пользователя {} прошла успешно", request.getUsername());
            return response;
        } catch (Exception e) {
            logger.error("Ошибка при авторизации пользователя {}: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }
}