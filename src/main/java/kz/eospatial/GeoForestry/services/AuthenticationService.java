package kz.eospatial.GeoForestry.services;

import kz.eospatial.GeoForestry.config.SignInRequest;
import kz.eospatial.GeoForestry.config.jwt.JwtAuthenticationResponse;
import kz.eospatial.GeoForestry.config.jwt.JwtService;
import kz.eospatial.GeoForestry.user.UserService;
import kz.eospatial.GeoForestry.user.Users;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);


    /**
     * Аутентификация пользователя (в данном случае администратора)
     *
     * @param request данные пользователя
     * @return токен
     */
//    public JwtAuthenticationResponse signIn(SignInRequest request) {
//        // Аутентификация пользователя с помощью AuthenticationManager
//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                request.getUsername(),
//                request.getPassword()
//        ));
//
//        // Загрузка деталей пользователя напрямую через UserService, который реализует UserDetailsService
//        var userDetails = userService.loadUserByUsername(request.getUsername());
//
//        // Генерация JWT для пользователя
//        var jwt = jwtService.generateToken(userDetails);
//        return new JwtAuthenticationResponse(jwt);
//    }
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        // Логика аутентификации
        authenticate(request.getUsername(), request.getPassword());

        // Генерация JWT для пользователя
        var userDetails = loadUserDetails(request.getUsername());
        var jwt = jwtService.generateToken(userDetails);

        Users user = userService.findByUsername(request.getUsername());

        return new JwtAuthenticationResponse(jwt, user.getId());
    }

    private void authenticate(String username, String password) {
        logger.info("Attempting to authenticate user: {}", username);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        logger.info("User {} authenticated successfully", username);
    }

    private UserDetails loadUserDetails(String username) {
        logger.info("Loading user details for: {}", username);
        var userDetails = userService.loadUserByUsername(username);
        logger.info("User details loaded successfully for: {}", username);
        return userDetails;
    }
}
