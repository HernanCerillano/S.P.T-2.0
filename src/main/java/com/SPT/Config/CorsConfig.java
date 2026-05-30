package com.SPT.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para el backend. Los orígenes se leen de la property {@code cors.allowed-origins}
 * (env var {@code CORS_ALLOWED_ORIGINS}) — coma-separada.
 *
 * Importante: se usa {@code allowedOriginPatterns} (no {@code allowedOrigins}) porque los
 * orígenes de Tauri no son HTTP estándar ({@code tauri://localhost},
 * {@code https://tauri.localhost}) y solo "Patterns" los acepta.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins == null || allowedOrigins.isBlank()
                ? new String[]{
                        "http://localhost:4200",
                        "http://127.0.0.1:4200",
                        "tauri://localhost",
                        "http://tauri.localhost",
                        "https://tauri.localhost"
                  }
                : allowedOrigins.split(",");

        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-Total-Count")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
