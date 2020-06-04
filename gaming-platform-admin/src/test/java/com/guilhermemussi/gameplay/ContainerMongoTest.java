package com.guilhermemussi.gameplay;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ContainerMongoTest {
    public static final GenericContainer mongodb = new GenericContainer<>("mongo:4.2").withExposedPorts(27017);
    static {
        mongodb.start();
        System.setProperty("quarkus.mongodb.connection-string",
                "mongodb://" + mongodb.getContainerIpAddress() + ":" + mongodb.getFirstMappedPort());
    }
}
