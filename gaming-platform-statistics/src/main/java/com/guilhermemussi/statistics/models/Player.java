package com.guilhermemussi.statistics.models;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "players")
public class Player extends PanacheMongoEntityBase {
    @BsonId
    public String username;

    public Boolean connected;
    public Boolean searching;
    public GameType gameType;

    public Integer defeats;
    public Integer victories;

    public Player() {

    }

    public String getUsername() {
        return username;
    }
}
