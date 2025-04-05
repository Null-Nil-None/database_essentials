package com.darren.backend;

import com.darren.backend.documents.AudioFile;
import com.darren.backend.documents.PlayerScore;
import com.darren.backend.documents.Sprite;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Base64;

/**
 * Service class responsible for handling all interactions with the MongoDB database.
 * This includes saving and retrieving player scores, sprite and audio file metadata.
 */
@Component
public class ServerService {
    private final MongoClient client;
    private final MongoDatabase db;

    /**
     * Constructs the service and establishes a MongoDB connection using credentials.
     *
     * @param secrets Bean that provides MongoDB credentials.
     */
    public ServerService(Secrets secrets) {
        // Format the connection URI with the username and password
        String uri = String.format(
            "mongodb+srv://%s:%s@cluster0.znflm.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0",
            secrets.getDbUsername(), secrets.getDbPassword()
        );

        // Create MongoDB client with the URI
        client = MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build()
        );

        // Use the specific database
        db = client.getDatabase("multimedia_db");
    }

    /**
     * Retrieves all player scores from the "scores" collection.
     *
     * @return A stream of PlayerScore objects.
     */
    public Flux<PlayerScore> getScores() {
        return Flux.from(db.getCollection("scores").find())
            .map(doc -> new PlayerScore(
                doc.getString("player_name"),
                doc.getInteger("score")
            ));
    }

    /**
     * Saves sprite metadata to the database after reading and encoding the file.
     *
     * @param file The uploaded sprite file.
     * @return A Mono containing the inserted document ID.
     */
    public Mono<String> saveSpriteMetadata(FilePart file) {
        return file.content()
            .flatMap(dataBuffer -> {
                // Read all bytes from the file buffer
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                return Mono.just(bytes);
            })
            .reduce(this::concat) // Combine byte chunks
            .map(fullBytes -> {
                String base64Content = Base64.getEncoder().encodeToString(fullBytes);

                // Build document to insert
                Document doc = new Document()
                    .append("file_name", file.filename())
                    .append("content_type", file.headers().getContentType() != null
                        ? file.headers().getContentType().toString()
                        : "unknown")
                    .append("size", fullBytes.length)
                    .append("content", base64Content);

                return doc;
            })
            .flatMap(doc -> Mono.from(db.getCollection("sprites").insertOne(doc)))
            .map(result -> result.getInsertedId().toString());
    }

    /**
     * Helper method to combine two byte arrays.
     *
     * @param a First byte array.
     * @param b Second byte array.
     * @return Concatenated byte array.
     */
    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Saves audio file metadata similarly to sprites.
     *
     * @param file The uploaded audio file.
     * @return A Mono containing the inserted document ID.
     */
    public Mono<String> saveAudioMetadata(FilePart file) {
        return file.content().reduce(new byte[0], (prev, buffer) -> {
            // Read bytes from the buffer and append to the accumulator
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);

            byte[] combined = new byte[prev.length + bytes.length];
            System.arraycopy(prev, 0, combined, 0, prev.length);
            System.arraycopy(bytes, 0, combined, prev.length, bytes.length);
            return combined;
        }).flatMap(bytes -> {
            String base64 = Base64.getEncoder().encodeToString(bytes);

            // Create document with metadata fields
            Document doc = new Document()
                .append("file_name", file.filename())
                .append("content_type", file.headers().getContentType() != null ? file.headers().getContentType().toString() : "unknown")
                .append("size", bytes.length)
                .append("content", base64);

            return Mono.from(db.getCollection("audio").insertOne(doc))
                .map(result -> result.getInsertedId().toString());
        });
    }

    /**
     * Fetches all stored sprite documents.
     *
     * @return A Flux of Sprite metadata.
     */
    public Flux<Sprite> getAllSprites() {
        return Flux.from(db.getCollection("sprites").find())
                .map(doc -> new Sprite(
                    doc.getString("file_name"),
                    doc.getString("content_type"),
                    doc.containsKey("size") && doc.get("size") instanceof Number
                    ? ((Number) doc.get("size")).longValue()
                    : 0L,
                    doc.getString("content")
                ));
    }

    /**
     * Fetches all stored audio files.
     *
     * @return A Flux of AudioFile metadata.
     */
    public Flux<AudioFile> getAllAudioFiles() {
        return Flux.from(db.getCollection("audio").find())
            .map(doc -> new AudioFile(
                doc.getString("file_name"),
                doc.getString("content_type"),
                doc.containsKey("size") && doc.get("size") instanceof Number
                    ? ((Number) doc.get("size")).longValue()
                    : 0L,
                doc.getString("content")
            ));
    }

    /**
     * Adds a new player score document.
     *
     * @param score The PlayerScore object containing player name and score.
     * @return A Mono containing the inserted document ID.
     */
    public Mono<String> addScore(PlayerScore score) {
        Document doc = new Document()
                .append("player_name", score.getPlayerName())
                .append("score", score.getScore());

        return Mono.from(db.getCollection("scores").insertOne(doc))
                .map(result -> result.getInsertedId().toString());
    }

    /**
     * Retrieves a sprite by filename.
     *
     * @param filename The name of the sprite to fetch.
     * @return A Mono of Sprite if found.
     */
    public Mono<Sprite> getSpriteByFilename(String filename) {
        return Mono.from(
            db.getCollection("sprites").find(new Document("file_name", filename)).first()
        ).map(doc -> new Sprite(
            doc.getString("file_name"),
            doc.getString("content_type"),
            doc.containsKey("size") && doc.get("size") instanceof Number
            ? ((Number) doc.get("size")).longValue()
            : 0L,
            doc.getString("content")
        ));
    }

    /**
     * Retrieves an audio file by filename.
     *
     * @param filename The name of the audio file to fetch.
     * @return A Mono of AudioFile if found.
     */
    public Mono<AudioFile> getAudioByFilename(String filename) {
        return Mono.from(
            db.getCollection("audio").find(new Document("file_name", filename)).first()
        ).map(doc -> new AudioFile(
            doc.getString("file_name"),
            doc.getString("content_type"),
            doc.containsKey("size") && doc.get("size") instanceof Number
            ? ((Number) doc.get("size")).longValue()
            : 0L,
            doc.getString("content")
        ));
    }
}
