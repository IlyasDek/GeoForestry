package kz.eospatial.GeoForestry.config.jwt;

import kz.eospatial.GeoForestry.config.SignInRequest;
import kz.eospatial.GeoForestry.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Аутентификация пользователя (в данном случае администратора)
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        // Аутентификация пользователя с помощью AuthenticationManager
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        // Загрузка деталей пользователя напрямую через UserService, который реализует UserDetailsService
        var userDetails = userService.loadUserByUsername(request.getUsername());

        // Генерация JWT для пользователя
        var jwt = jwtService.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);
    }
}
