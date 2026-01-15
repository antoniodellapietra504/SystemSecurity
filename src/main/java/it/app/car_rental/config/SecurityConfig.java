package it.app.car_rental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final KeycloakPep keycloakPep;

    // Iniettiamo il nostro "Vigile" (il PEP che hai creato prima)
    public SecurityConfig(KeycloakPep keycloakPep) {
        this.keycloakPep = keycloakPep;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabilitato per semplicità
                .authorizeHttpRequests(auth -> auth
                        // 1. Risorse pubbliche: Queste passano SENZA chiedere al PEP (per evitare loop)
                        .requestMatchers("/css/**", "/images/**", "/login**", "/logout**", "/error").permitAll()

                        // 2. TUTTO IL RESTO: Passa dal PEP Puro
                        // Spring ferma la richiesta e chiama keycloakPep.check()
                        .anyRequest().access(keycloakPep)
                )
                .oauth2Login(oauth2 -> oauth2
                        // Appena fai login, decidiamo dove mandarti (User -> /user, Admin -> /admin)
                        .successHandler(myAuthenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String logoutUrl = "https://localhost/auth/realms/CarRental/protocol/openid-connect/logout" +
                                    "?client_id=car-rental-client" +
                                    "&post_logout_redirect_uri=https://localhost";
                            response.sendRedirect(logoutUrl);
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    // Gestore del "Dopo Login": Smista l'utente nella sua home page corretta
    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser =
                    (org.springframework.security.oauth2.core.oidc.user.OidcUser) authentication.getPrincipal();

            Object realmAccessObj = oidcUser.getAttribute("realm_access");
            List<String> roles = new java.util.ArrayList<>();

            if (realmAccessObj instanceof Map) {
                Map<String, Object> realmAccess = (Map<String, Object>) realmAccessObj;
                if (realmAccess.containsKey("roles")) {
                    roles = (List<String>) realmAccess.get("roles");
                }
            }

            // Se è admin, lo mandiamo alla dashboard. Se è user, alla home.
            // NOTA: Se un Admin prova ad andare su /user (o viceversa), sarà il PEP a bloccarlo dopo il redirect.
            if (roles.contains("admin")) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/user");
            }
        };
    }
}