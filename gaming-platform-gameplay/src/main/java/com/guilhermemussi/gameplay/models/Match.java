package com.guilhermemussi.gameplay.models;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.Random;

@MongoEntity(collection = "matches")
public class Match extends PanacheMongoEntityBase {
    public static final Random random = new Random(234523542345L);

    @BsonId
    public String matchId;

    public List<String> players;

    public String winner;

    public String quitter;

    public EndType endType;

    public GameType gameType;

    public static enum EndType {
        VICTORY, DRAW, DISCONNECTION, CANCELED
    }

    public static Match start(GameType gameType, String... players) {
        return gameType.startMatch(players);
    }
}
