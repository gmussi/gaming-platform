package com.guilhermemussi.gameplay.models;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.LocalDate;
import java.util.UUID;

@MongoEntity(collection = "tickets")
public class SessionTicket extends PanacheMongoEntity {
    @BsonId
    public String ticketId;

    public String username;

    public LocalDate timeIssued;

    public Boolean used;

    public SessionTicket() {

    }

    public static SessionTicket getSessionTicket(String username) {
        SessionTicket ticket = new SessionTicket();
        ticket.ticketId = UUID.randomUUID().toString();
        ticket.username = username;
        ticket.timeIssued = LocalDate.now();
        ticket.used = false;
        ticket.persist();
        return ticket;
    }
}
