package com.guilhermemussi.admin.model;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@MongoEntity(collection = "credentials")
public class PlayerCredential extends PanacheMongoEntity {
    @BsonId
    public String username;
    public String password;

    public PlayerCredential() {

    }

    public static PlayerCredential register(String username, String password) {
        PlayerCredential credential = new PlayerCredential();
        credential.username = username;
        credential.password = password;
        credential.persist();
        return credential;
    }

    public static MessageDigest digester;
    static {
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String encodePassword(String password) {
        return new String(digester.digest(password.getBytes(StandardCharsets.UTF_8)));
    }
}
