package io.github.mohankandar.idp.platform.openapi;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Centralizes OpenAPI security for all services.
 * - Uniform API-key header across all apps/envs: "X-API-Key"
 * - Single scheme name: "apiKeyAuth" (matches annotations + components)
 * - Global security requirement applied when idp.openapi.global-security=true
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(IdpOpenApiProperties.class)
@SecurityScheme(
        name = "apiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key"
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class IdpOpenApiSecurityAutoConfiguration {

    private static final String API_KEY_HEADER = "X-API-Key";   // fixed across envs
    private static final String API_KEY_SCHEME = "apiKeyAuth";  // single canonical name
    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    @ConditionalOnProperty(prefix = "idp.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenAPI openAPI(IdpOpenApiProperties props) {

        io.swagger.v3.oas.models.security.SecurityScheme apiKeyScheme =
                new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .name(API_KEY_HEADER);

        io.swagger.v3.oas.models.security.SecurityScheme bearerScheme =
                new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

        OpenAPI openapi = new OpenAPI()
                .info(new Info()
                        .title(props.getTitle())
                        .description(props.getDescription())
                        .version(props.getVersion()))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, apiKeyScheme)
                        .addSecuritySchemes(BEARER_SCHEME, bearerScheme));

        if (props.isGlobalSecurity()) {
            openapi
                    .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                    .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
        }

        return openapi;
    }

    // Optional: if you want operations automatically tagged with security, even when
    // code/annotations forget it. Works with the globalSecurity flag as well.
    @Bean
    public OpenApiCustomizer idpGlobalSecurityCustomizer(IdpOpenApiProperties props) {
        return openApi -> {
            if (!props.isGlobalSecurity())
                return;
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(op -> {
                        op.addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME));
                        op.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
                    }));
        };
    }

    // ---- Pre-auth provider (functionally needed so ApiKeyAuthFilter results in authenticated=true) ----
    @Bean
    AuthenticationProvider idpPreAuthProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                if (!(authentication instanceof PreAuthenticatedAuthenticationToken token))
                    return null;
                var authed = new PreAuthenticatedAuthenticationToken(
                        token.getPrincipal(), token.getCredentials(), token.getAuthorities());
                authed.setDetails(token.getDetails());
                return authed; // authenticated = true
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }
}