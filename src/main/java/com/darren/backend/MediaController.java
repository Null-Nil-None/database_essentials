package com.darren.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;

import com.darren.backend.documents.AudioFile;
import com.darren.backend.documents.PlayerScore;
import com.darren.backend.documents.Sprite;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class MediaController {
    private final ServerService serverService;

    public MediaController(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping("/player_score")
    public Mono<ResponseEntity<String>> addScore(@RequestBody PlayerScore score) {
        return serverService.addScore(score)
                .map(id -> ResponseEntity.ok("Score recorded, ID: " + id));
    }
    
    @GetMapping("/my-ip")
    public Mono<String> getServerIp() {
        return Mono.fromCallable(() -> {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.ipify.org"))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        });
    }

    // =======================
    // Task 3 endpoints
    @GetMapping("/player_scores")
    public Flux<PlayerScore> getScores() {
        return serverService.getScores();
    }

    @GetMapping("/sprites")
    public Flux<Sprite> getAllSprites() {
        return serverService.getAllSprites();
    }

    @GetMapping("/audio")
    public Flux<AudioFile> getAllAudioFiles() {
        return serverService.getAllAudioFiles();
    }

    @PostMapping("/upload_sprite")
    public Mono<ResponseEntity<String>> uploadSprite(@RequestPart("file") FilePart file) {
        return serverService.saveSpriteMetadata(file)
                .map(id -> ResponseEntity.ok("Sprite metadata saved, ID: " + id));
    }

    @PostMapping("/upload_audio")
    public Mono<ResponseEntity<String>> uploadAudio(@RequestPart("file") FilePart file) {
        return serverService.saveAudioMetadata(file)
                .map(id -> ResponseEntity.ok("Audio metadata saved, ID: " + id));
    }

    @GetMapping("/sprite/{filename}")
    public Mono<ResponseEntity<Sprite>> getSpriteByFilename(@PathVariable String filename) {
        return serverService.getSpriteByFilename(filename)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/audio/{filename}")
    public Mono<ResponseEntity<AudioFile>> getAudioByFilename(@PathVariable String filename) {
        return serverService.getAudioByFilename(filename)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // =======================
    // Connection test
    @GetMapping("/test_connection")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Server is up!");
    }
}
