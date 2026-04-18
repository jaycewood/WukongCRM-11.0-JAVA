package com.kakarote.gateway.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Serves packaged frontend assets explicitly to avoid relying on WebFlux static resource auto-configuration.
 */
@RestController
public class FrontendAssetController {

    private final ResourceLoader resourceLoader;

    public FrontendAssetController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping({"/index.html", "/favicon.ico", "/static/**"})
    public Mono<ResponseEntity<Resource>> asset(ServerHttpRequest request) {
        Resource resource = resolveResource(request.getURI().getPath());
        if (resource == null) {
            return Mono.error(new ResponseStatusException(NOT_FOUND));
        }
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return Mono.just(ResponseEntity.ok().contentType(mediaType).body(resource));
    }

    private Resource resolveResource(String path) {
        String relativePath = path.startsWith("/") ? path.substring(1) : path;
        Path filePath = Paths.get(System.getProperty("user.dir"), "public", relativePath).normalize();
        if (Files.isRegularFile(filePath) && Files.isReadable(filePath)) {
            Resource fileResource = new FileSystemResource(filePath);
            return fileResource;
        }
        Resource classpathResource = resourceLoader.getResource("classpath:/public/" + relativePath);
        if (classpathResource.exists() && classpathResource.isReadable()) {
            return classpathResource;
        }
        return null;
    }
}
