package com.guilhermemussi.admin.model;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.Random;

@MongoEntity(collection = "matches")
public class Match extends PanacheMongoEntity {
    public static final Random random = new Random(234523542345L);

    @BsonId
    public String matchId;

    public List<String> players;

    public EndType endType;

    public static enum EndType {
        VICTORY, DRAW, DISCONNECTION
    }

}
