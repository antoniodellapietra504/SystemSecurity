package it.app.car_rental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Per semplicità nei test
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/images/**").permitAll() // Risorse statiche pubbliche
                        .anyRequest().authenticated() // Tutto il resto richiede login
                )
                .oauth2Login(oauth2 -> oauth2
                        // Usiamo un handler personalizzato per decidere dove mandare l'utente dopo il login
                        .successHandler(myAuthenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("https://localhost/auth/realms/CarRental/protocol/openid-connect/logout?redirect_uri=https://localhost")
                );

        return http.build();
    }

    // Logica di reindirizzamento post-login
    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Per ora mandiamo tutti alla pagina /user per testare che il login funzioni.
            // Nello step successivo, qui leggeremo i ruoli per smistare Admin vs User.
            response.sendRedirect("/user");
        };
    }
}
