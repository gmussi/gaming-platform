package com.guilhermemussi.service;

import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@ApplicationScoped
public class PlayerCountService {
    public static final Logger LOGGER = Logger.getLogger(PlayerCountService.class.getName());

    public static final AtomicInteger playerCount = new AtomicInteger();
    public static final Set<String> connectedPlayers = new HashSet<>();

    @Incoming("player-connected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerConnected(String username) {
        LOGGER.info("New user connected: " + username);

        connectedPlayers.add(username);
        playerCount.incrementAndGet();
    }

    @Incoming("player-disconnected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerDisconnected(String username) {
        LOGGER.info("User disconnected: " + username);

        connectedPlayers.remove(username);
        playerCount.decrementAndGet();
    }
}
