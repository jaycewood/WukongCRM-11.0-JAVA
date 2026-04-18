package com.kakarote.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Explicitly expose packaged frontend assets for the gateway WebFlux runtime.
 */
@Configuration
public class StaticResourceConfiguration implements WebFluxConfigurer {

    private static final String[] RESOURCE_LOCATIONS = {
            "file:public/",
            "classpath:/public/",
            "classpath:/static/",
            "classpath:/resources/",
            "classpath:/META-INF/resources/"
    };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/index.html", "/favicon.ico")
                .addResourceLocations(RESOURCE_LOCATIONS);
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:public/static/", "classpath:/public/static/");
    }
}
