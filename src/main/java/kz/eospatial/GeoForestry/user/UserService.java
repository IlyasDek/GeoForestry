package kz.eospatial.GeoForestry.user;

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

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Users save(Users user) {
        return repository.save(user);
    }

    // Метод для добавления нового администратора через веб-интерфейс
    @Transactional
    public Users addUser(String username, String email, String password, Role role) {
        if (repository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User with this name already exists");
        }

        if (repository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return save(user);
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        repository.findById(userId).ifPresent(user -> {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            repository.save(user);
            logger.info("Password for user with id {} has been updated.", userId);
        });
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        logger.info("Попытка загрузки пользователя с именем: {}", username);
//        return repository.findByUsername(username)
//                .map(user -> new org.springframework.security.core.userdetails.User(
//                        user.getUsername(),
//                        user.getPassword(),
//                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))))
//                .orElseThrow(() -> {
//                    logger.warn("Пользователь с именем {} не найден", username);
//                    return new UsernameNotFoundException("Пользователь не найден");
//                });
//    }

    public Users findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with name " + username + " not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))))
                .orElseThrow(() -> new UsernameNotFoundException("User with name " + username + " not found"));
    }
}
