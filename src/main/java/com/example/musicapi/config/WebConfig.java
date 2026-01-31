package com.example.musicapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Restringe o acesso a partir de domínios fora do domínio do serviço
        // Por padrão, não permitir origens externas. 
        // Aqui configuramos explicitamente para não permitir nada além do domínio base.
        registry.addMapping("/**")
                .allowedOrigins() // Lista vazia bloqueia origens externas no CORS
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(false);
    }
}
