package com.guilhermemussi.gameplay.models;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "players")
public class Player extends PanacheMongoEntity {
    @BsonId
    public String username;

    public Boolean connected;
    public Boolean searching;
    public GameType gameType;

    public Integer defeats;
    public Integer victories;

    public Player() {

    }

    public synchronized static Player use(final String username) {
        return Player.<Player>findByIdOptional(username).orElseGet(() -> {
            Player player = new Player();
            player.username = username;
            player.defeats = 0;
            player.victories = 0;
            player.persist();
            return player;
        });
    }


}