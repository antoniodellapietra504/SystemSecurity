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
            org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser =
                    (org.springframework.security.oauth2.core.oidc.user.OidcUser) authentication.getPrincipal();

            // --- INIZIO DEBUG ---
            System.out.println("\n\n=========================================");
            System.out.println("DEBUG LOGIN UTENTE: " + oidcUser.getName());
            System.out.println("TUTTI GLI ATTRIBUTI: " + oidcUser.getAttributes());

            // Cerchiamo realm_access
            Object realmAccessObj = oidcUser.getAttribute("realm_access");
            System.out.println("realm_access trovato? " + realmAccessObj);
            // =========================================\n

            java.util.List<String> roles = new java.util.ArrayList<>();

            // Logica di estrazione sicura
            if (realmAccessObj instanceof java.util.Map) {
                java.util.Map<String, Object> realmAccess = (java.util.Map<String, Object>) realmAccessObj;
                if (realmAccess.containsKey("roles")) {
                    roles = (java.util.List<String>) realmAccess.get("roles");
                }
            }

            System.out.println("RUOLI ESTRATTI: " + roles);

            // Controllo Ruolo (CASE SENSITIVE!)
            if (roles.contains("admin")) {
                System.out.println(">>> RILEVATO ADMIN -> Redirect /admin");
                response.sendRedirect("/admin");
            } else {
                System.out.println(">>> NESSUN RUOLO ADMIN -> Redirect /user");
                response.sendRedirect("/user");
            }
        };
    }
}
