package com.miniprojet.gestionClubsEvents.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)

public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    
        
}
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Chemin absolu vers le dossier d'upload
        String uploadDir = Paths.get(System.getProperty("user.home"), "gestion-clubs-uploads")
                                .toAbsolutePath()
                                .toString()
                                .replace("\\", "/"); // Windows fix

        // Format obligatoire : "file:/chemin/vers/dossier/"
        String location = "file:" + uploadDir + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600)
                .setCacheControl(CacheControl.noCache());
        registry.addResourceHandler("/images/**")
        .addResourceLocations("file:" + System.getProperty("user.home") + "/gestion-clubs-uploads/images/");

// Pour tes fichiers
        registry.addResourceHandler("/files/**")
        .addResourceLocations("file:" + System.getProperty("user.home") + "/gestion-clubs-uploads/files/");
        System.out.println("Images servies depuis : " + location);
    }
}