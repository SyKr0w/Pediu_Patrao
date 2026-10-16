package com.umc.pediupatrao.config;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    @Bean CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder encoder,
            @Value("${app.bootstrap.admin-password:}") String senhaInicial) {
        return args -> {
            repo.findByUsername("admin").ifPresentOrElse(admin -> {
                if (!"ADMIN".equals(admin.getRole())) {admin.setRole("ADMIN"); repo.save(admin);}
            }, () -> {
                if (senhaInicial.isBlank()) {
                    log.warn("Sem usuario admin: defina PEDIUPATRAO_ADMIN_PASSWORD antes do primeiro inicio.");
                } else {
                    Usuario admin = new Usuario(); admin.setUsername("admin"); admin.setRole("ADMIN");
                    admin.setPassword(encoder.encode(senhaInicial));repo.save(admin);
                    log.info("Usuario administrador inicial criado.");
                }
            });
        };
    }
}
