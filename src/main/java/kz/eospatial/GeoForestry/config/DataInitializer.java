package kz.eospatial.GeoForestry.config;

import kz.eospatial.GeoForestry.enums.Role;
import kz.eospatial.GeoForestry.user.UserRepository;
import kz.eospatial.GeoForestry.user.Users;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Optional<Users> existingAdmin = userRepository.findByUsername("g_admin");

        if (existingAdmin.isPresent()) {
            System.out.println("Администратор уже существует в системе.");
        } else {
            Users admin = new Users();
            admin.setUsername("g_admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("mycoolforest9"));
            admin.setRole(Role.ROLE_SUPER_ADMIN);
            userRepository.save(admin);
            System.out.println("Администратор успешно создан.");
        }
    }
}
