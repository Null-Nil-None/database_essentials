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

/**
 * REST controller that handles endpoints for media uploads and retrievals,
 * as well as score submissions.
 */
@RestController
public class MediaController {
    private final ServerService serverService;

    public MediaController(ServerService serverService) {
        this.serverService = serverService;
    }

    /**
     * Adds a player's score after validating the input.
     *
     * @param score The player score object containing name and score.
     * @return A response entity with success or failure message.
     */
    @PostMapping("/player_score")
    public Mono<ResponseEntity<String>> addScore(@RequestBody PlayerScore score) {
        // Ensure score is within reasonable bounds
        if (score.getScore() < 0 || score.getScore() > 1000000) {
            return Mono.just(ResponseEntity.badRequest().body("Score out of valid range"));
        }

        // Ensure player name is not null or blank
        if (score.getPlayerName() == null || score.getPlayerName().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid player name"));
        }

        return serverService.addScore(score)
                .map(id -> ResponseEntity.ok("Score recorded, ID: " + id));
    }

    /**
     * Gets the server's public IP address from an external API.
     *
     * @return A Mono containing the IP address as a string.
     */
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

    /**
     * Retrieves all player scores from the database.
     *
     * @return A Flux stream of PlayerScore objects.
     */
    @GetMapping("/player_scores")
    public Flux<PlayerScore> getScores() {
        return serverService.getScores();
    }

    /**
     * Retrieves all sprites.
     *
     * @return A Flux stream of Sprite objects.
     */
    @GetMapping("/sprites")
    public Flux<Sprite> getAllSprites() {
        return serverService.getAllSprites();
    }

    /**
     * Retrieves all audio files.
     *
     * @return A Flux stream of AudioFile objects.
     */
    @GetMapping("/audio")
    public Flux<AudioFile> getAllAudioFiles() {
        return serverService.getAllAudioFiles();
    }

    /**
     * Uploads sprite metadata and stores it in the database.
     *
     * @param file The uploaded sprite file.
     * @return A response with confirmation or error.
     */
    @PostMapping("/upload_sprite")
    public Mono<ResponseEntity<String>> uploadSprite(@RequestPart("file") FilePart file) {
        // Validate filename format
        if (!isValidFilename(file.filename())) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid filename"));
        }

        return serverService.saveSpriteMetadata(file)
                .map(id -> ResponseEntity.ok("Sprite metadata saved, ID: " + id));
    }

    /**
     * Uploads audio file metadata and stores it in the database.
     *
     * @param file The uploaded audio file.
     * @return A response with confirmation or error.
     */
    @PostMapping("/upload_audio")
    public Mono<ResponseEntity<String>> uploadAudio(@RequestPart("file") FilePart file) {
        // Validate filename format
        if (!isValidFilename(file.filename())) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid filename"));
        }

        return serverService.saveAudioMetadata(file)
                .map(id -> ResponseEntity.ok("Audio metadata saved, ID: " + id));
    }

    /**
     * Retrieves a sprite by filename after validation.
     *
     * @param filename The name of the sprite file.
     * @return The matching sprite or a not found response.
     */
    @GetMapping("/sprite/{filename}")
    public Mono<ResponseEntity<Sprite>> getSpriteByFilename(@PathVariable String filename) {
        // Check filename validity before querying
        if (!isValidFilename(filename)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return serverService.getSpriteByFilename(filename)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves an audio file by filename after validation.
     *
     * @param filename The name of the audio file.
     * @return The matching audio file or a not found response.
     */
    @GetMapping("/audio/{filename}")
    public Mono<ResponseEntity<AudioFile>> getAudioByFilename(@PathVariable String filename) {
        // Check filename validity before querying
        if (!isValidFilename(filename)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return serverService.getAudioByFilename(filename)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Basic endpoint to check if the server is operational.
     *
     * @return A simple OK message.
     */
    @GetMapping("/test_connection")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Server is up!");
    }

    /**
     * Utility method to validate filenames.
     * Accepts alphanumeric characters, dots, underscores, and hyphens only.
     *
     * @param filename The filename string to check.
     * @return True if valid, false otherwise.
     */
    private boolean isValidFilename(String filename) {
        return filename != null && filename.matches("^[a-zA-Z0-9._-]{1,100}$");
    }
}
