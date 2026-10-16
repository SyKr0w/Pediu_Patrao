package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final UsuarioDetailsService details;
    private final AcessoNegadoHandler acessoNegado;
    public SecurityConfig(UsuarioDetailsService details,AcessoNegadoHandler acessoNegado) {
        this.details = details;
        this.acessoNegado = acessoNegado;
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationProvider authenticationProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(details);
        provider.setPasswordEncoder(encoder);
        return provider;
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/css/**", "/dist/**", "/plugins/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/usuarios/**", "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/auditoria/**", "/api/auditoria/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/api/pedidos/*/desconto", "/api/pedidos/*/cancelamento", "/pedidos/*/desconto", "/pedidos/*/cancelar").hasRole("GERENTE")
                .requestMatchers("/api/pedidos/**", "/pedidos/**").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                .requestMatchers("/api/clientes/**", "/clientes/**").hasAnyRole("GERENTE", "ATENDENTE")
                .requestMatchers("/api/produtos/**", "/produtos/**").hasAnyRole("GERENTE", "ATENDENTE")
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.accessDeniedHandler(acessoNegado))
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }
}
