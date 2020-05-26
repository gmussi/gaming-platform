package com.guilhermemussi.admin.model;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "players")
public class Player extends PanacheMongoEntity {
    @BsonId
    public String name;

    public Boolean connected;
    public Boolean searching;

    public Integer defeats;
    public Integer victories;

    public Player() {

    }

    public synchronized static Player use(final String name) {
        return Player.<Player>findByIdOptional(name).orElseGet(() -> {
            Player player = new Player();
            player.name = name;
            player.defeats = 0;
            player.victories = 0;
            player.persist();
            return player;
        });
    }


}