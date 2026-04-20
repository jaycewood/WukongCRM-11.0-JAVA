package com.kakarote.gateway.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Serves packaged frontend assets explicitly to avoid relying on WebFlux static resource auto-configuration.
 */
@RestController
public class FrontendAssetController {

    private static final String LOGIN_RESET_SNIPPET =
            "<script>(function(){function resetAuthState(){try{localStorage.removeItem(\"authList\");" +
                    "localStorage.removeItem(\"loginUserInfo\")}catch(e){}try{var store=window.app&&" +
                    "window.app.$store;if(!store||!store.commit)return;store.commit(\"SET_ALLAUTH\",null);" +
                    "store.commit(\"SET_CRM\",{});store.commit(\"SET_BI\",{});store.commit(\"SET_MANAGE\",{});" +
                    "store.commit(\"SET_OA\",{});store.commit(\"SET_PROJECT\",{});store.commit(\"SET_HRM\",{});" +
                    "store.commit(\"SET_USERINFO\",null);store.commit(\"SET_USERLIST\",[]);" +
                    "store.commit(\"SET_DEPTLIST\",[]);try{localStorage.removeItem(\"loginUserInfo\")}" +
                    "catch(e){}}catch(e){}}function syncAuthState(){var path=location.pathname||\"\";" +
                    "\"/login\"===path||path.endsWith(\"/login\")?resetAuthState():0}var rawPushState=" +
                    "history.pushState,rawReplaceState=history.replaceState;history.pushState=function()" +
                    "{var ret=rawPushState.apply(this,arguments);return setTimeout(syncAuthState,0),ret};" +
                    "history.replaceState=function(){var ret=rawReplaceState.apply(this,arguments);" +
                    "return setTimeout(syncAuthState,0),ret};window.addEventListener(\"popstate\"," +
                    "syncAuthState);syncAuthState()})();</script>";

    private final ResourceLoader resourceLoader;

    public FrontendAssetController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping({"/index.html", "/favicon.ico", "/static/**"})
    public Mono<ResponseEntity<Resource>> asset(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        Resource resource = resolveResource(request.getURI().getPath());
        if (resource == null) {
            return Mono.error(new ResponseStatusException(NOT_FOUND));
        }
        if ("/index.html".equals(path)) {
            return Mono.just(ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(sanitizeIndexHtml(resource)));
        }
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return Mono.just(ResponseEntity.ok().contentType(mediaType).body(resource));
    }

    private Resource sanitizeIndexHtml(Resource resource) {
        try {
            String html = new String(StreamUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
            if (html.contains(LOGIN_RESET_SNIPPET)) {
                html = html.replace(LOGIN_RESET_SNIPPET, "");
            }
            return new ByteArrayResource(html.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to read index.html", e);
        }
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
