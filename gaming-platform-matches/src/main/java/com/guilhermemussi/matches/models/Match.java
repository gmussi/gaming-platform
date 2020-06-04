package com.guilhermemussi.matches.models;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@MongoEntity(collection = "matches")
public class Match extends PanacheMongoEntity {
    public static final Random random = new Random(234523542345L);

    @BsonId
    public String matchId;

    public List<String> players;

    public EndType endType;

    public GameType gameType;

    public static enum EndType {
        VICTORY, DRAW, DISCONNECTION
    }

    public static Match start(GameType gameType, String... players) {
        Match match = new Match();
        match.matchId = UUID.randomUUID().toString();
        match.players = Arrays.asList(players);
        match.gameType = gameType;
        match.persist();
        return match;
    }
}
