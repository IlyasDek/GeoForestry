package kz.eospatial.GeoForestry.user;

import kz.eospatial.GeoForestry.controllers.AdminController;
import kz.eospatial.GeoForestry.enums.Role;
import kz.eospatial.GeoForestry.exeptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Users save(Users user) {
        return repository.save(user);
    }

    // Метод для добавления нового администратора через веб-интерфейс
    @Transactional
    public Users addUser(String username, String email, String password, Role role) {
        if (repository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (repository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role); // Установка роли в зависимости от параметра
        return save(user);
    }


    public Users create(Users user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // Хеширование пароля перед сохранением
        return save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Попытка загрузки пользователя с именем: {}", username);
        return repository.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))))
                .orElseThrow(() -> {
                    logger.warn("Пользователь с именем {} не найден", username);
                    return new UsernameNotFoundException("Пользователь не найден");
                });
    }



}
