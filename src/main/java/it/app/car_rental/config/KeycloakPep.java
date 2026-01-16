package it.app.car_rental.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.Base64;
import java.util.function.Supplier;

@Component
public class KeycloakPep implements AuthorizationManager<RequestAuthorizationContext> {

    @Value("${spring.security.oauth2.client.provider.keycloak-provider.token-uri}")
    private String keycloakTokenUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;

    public KeycloakPep(OAuth2AuthorizedClientService authorizedClientService) throws SSLException {
        this.authorizedClientService = authorizedClientService;

        // Configurazione SSL per accettare certificati auto-firmati
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();

        if (auth == null || !auth.isAuthenticated() || !(auth instanceof OAuth2AuthenticationToken)) {
            return new AuthorizationDecision(false);
        }

        String requestUri = context.getRequest().getRequestURI();

        // 1. IGNORA RISORSE STATICHE E INUTILI
        // Questo evita che i log si riempiano di errori per file che non esistono o non sono protetti
        if (requestUri.equals("/") ||
                requestUri.startsWith("/css") ||
                requestUri.startsWith("/images") ||
                requestUri.equals("/favicon.ico") ||
                requestUri.contains("wpad.dat")) {
            return new AuthorizationDecision(true);
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());

        if (client == null || client.getAccessToken() == null) {
            return new AuthorizationDecision(false);
        }

        String accessToken = client.getAccessToken().getTokenValue();
        System.out.println("PEP: Controllo policy su Keycloak per URI originale: " + requestUri);

        // 2. MAPPING DEGLI URI (Logica di Raggruppamento)
        // Trasformiamo /admin/delete in /admin, così Keycloak riconosce la risorsa
        String resourceName = requestUri;

        if (requestUri.startsWith("/admin")) {
            resourceName = "/admin";
        } else if (requestUri.startsWith("/user")) {
            resourceName = "/user";
        }
        // Nota: "/rent" rimane "/rent"

        try {
            String credentials = clientId + ":" + clientSecret;
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

            // 3. COSTRUZIONE RICHIESTA CON NOME RISORSA CORRETTO
            String body = "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket" +
                    "&audience=" + clientId +
                    "&permission=" + resourceName + // <--- Qui usiamo il nome raggruppato
                    "&response_mode=decision" +
                    "&subject_token=" + accessToken +
                    "&subject_token_type=urn:ietf:params:oauth:token-type:access_token";

            String response = webClient.post()
                    .uri(keycloakTokenUri)
                    .header("Authorization", basicAuth)
                    // 4. HEADER PER SIMULARE HTTPS (Essenziale con Docker)
                    .header("X-Forwarded-Proto", "https")
                    .header("X-Forwarded-Host", "localhost")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            boolean granted = response != null && response.contains("\"result\":true");
            System.out.println("PEP: Permesso per '" + resourceName + "' -> " + (granted ? "✅ CONCESSO" : "❌ NEGATO"));
            return new AuthorizationDecision(granted);

        } catch (Exception e) {
            System.out.println("PEP: Errore chiamata Keycloak: " + e.getMessage());
            return new AuthorizationDecision(false);
        }
    }
}