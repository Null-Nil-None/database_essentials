package com.darren.backend.documents;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "scores")
public class PlayerScore {
    private String playerName;
    private int score;
    
    @JsonCreator
    public PlayerScore(@JsonProperty("player_name") String playerName, @JsonProperty("score") int score) {
        setPlayerName(playerName);
        setScore(score);
    }

    // Getters and setters
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String player_name) {
        this.playerName = player_name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}