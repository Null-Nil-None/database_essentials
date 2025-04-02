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
import org.springframework.web.bind.annotation.GetMapping;

import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import reactor.core.publisher.Flux;

import java.util.Base64;


@Component
public class ServerService {
    private final MongoClient client;
    private final MongoDatabase db;

    public ServerService(Secrets secrets) {
        String uri = String.format(
            "mongodb+srv://%s:%s@cluster0.znflm.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0",
            secrets.getDbUsername(), secrets.getDbPassword()
        );

        client = MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build()
        );

        db = client.getDatabase("multimedia_db");
    }

    public Flux<PlayerScore> getScores() {
        return Flux.from(db.getCollection("scores").find())
            .map(doc -> new PlayerScore(
                doc.getString("player_name"),
                doc.getInteger("score")
            ));
    }

    public Mono<String> saveSpriteMetadata(FilePart file) {
        return file.content()
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                return Mono.just(bytes);
            })
            .reduce(this::concat)
            .map(fullBytes -> {
                String base64Content = Base64.getEncoder().encodeToString(fullBytes);

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

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public Mono<String> saveAudioMetadata(FilePart file) {
        return file.content().reduce(new byte[0], (prev, buffer) -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);

            byte[] combined = new byte[prev.length + bytes.length];
            System.arraycopy(prev, 0, combined, 0, prev.length);
            System.arraycopy(bytes, 0, combined, prev.length, bytes.length);
            return combined;
        }).flatMap(bytes -> {
            String base64 = Base64.getEncoder().encodeToString(bytes);

            AudioFile audio = new AudioFile(
                file.filename(),
                file.headers().getContentType() != null ? file.headers().getContentType().toString() : "unknown",
                bytes.length,
                base64
            );

            Document doc = new Document()
                .append("file_name", audio.getFileName())
                .append("content_type", audio.getContentType())
                .append("size", audio.getSize())
                .append("content", base64);

            return Mono.from(db.getCollection("audio").insertOne(doc))
                .map(result -> result.getInsertedId().toString());
        });
    }

    public Flux<Sprite> getAllSprites() {
        return Flux.from(db.getCollection("sprites").find())
                .map(doc -> new Sprite(
                    doc.getString("file_name"),
                    doc.getString("content_type"),
                    doc.containsKey("size") && doc.get("size") instanceof Number
                    ? ((Number) doc.get("size")).longValue()
                    : 0L, // The fallback value.
                    doc.getString("content")
                ));
    }

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
    
    public Mono<String> addScore(PlayerScore score) {
        Document doc = new Document()
                .append("player_name", score.getPlayerName())
                .append("score", score.getScore());

        return Mono.from(db.getCollection("scores").insertOne(doc))
                .map(result -> result.getInsertedId().toString());
    }

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
