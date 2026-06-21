package com.toeic.vocab.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI toeicVocabOpenApi() {
        return new OpenAPI()
            .components(new Components().addSecuritySchemes(
                BEARER_AUTH_SCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ))
            .info(new Info()
                .title("TOEIC Vocabulary API")
                .version("v1")
                .description("REST API for a simple TOEIC vocabulary learning app with JWT auth, admin management and user-based study progress")
                .contact(new Contact().name("Codex Refactor")));
    }

    @Bean
    public OpenApiCustomizer bearerAuthOpenApiCustomizer(
            @Value("${api.prefix:/api/v1}") String apiPrefix) {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
                if (isPublicPath(path, apiPrefix)) {
                    operation.setSecurity(Collections.emptyList());
                    return;
                }

                if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
                    operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
                }
            }));
        };
    }

    private boolean isPublicPath(String path, String apiPrefix) {
        return path.equals(apiPrefix + "/auth/login")
                || path.equals(apiPrefix + "/auth/register")
                || path.startsWith(apiPrefix + "/public/");
    }
}
