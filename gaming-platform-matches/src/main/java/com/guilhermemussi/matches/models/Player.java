package com.guilhermemussi.matches.models;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "players")
public class Player extends PanacheMongoEntityBase {
    @BsonId
    public java.lang.String username;

    public Boolean connected;
    public Boolean searching;
    public GameType gameType;

    public Integer defeats;
    public Integer victories;

    public Player() {

    }

    public synchronized static Player use(final java.lang.String username) {
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