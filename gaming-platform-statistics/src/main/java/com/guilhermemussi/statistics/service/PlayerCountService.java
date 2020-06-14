package com.guilhermemussi.statistics.service;

import com.guilhermemussi.statistics.models.Player;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class PlayerCountService {
    public static final Logger LOGGER = Logger.getLogger(PlayerCountService.class.getName());

    public static final AtomicInteger playerCount = new AtomicInteger();
    public static final Set<String> connectedPlayers = new HashSet<>();

    @Inject
    @Channel("player-count")
    Emitter<Integer> count;

    void onStart(@Observes StartupEvent ev) {
        List<Player> players = Player.find("connected", true).list();
        connectedPlayers.addAll(players.stream().map(Player::getUsername).collect(Collectors.toSet()));
        playerCount.set(players.size());

        LOGGER.info("Player counting started with " + playerCount.get() + " players connected.");
    }

    @Incoming("player-connected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerConnected(String username) {
        connectedPlayers.add(username);
        int count = playerCount.incrementAndGet();

        LOGGER.info("New player connected (" + count + "): " + username);

        this.count.send(count);
    }

    @Incoming("player-disconnected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerDisconnected(String username) {
        connectedPlayers.remove(username);
        int count = playerCount.decrementAndGet();

        LOGGER.info("Player disconnected(" + count + "): " + username);

        this.count.send(count);
    }
}
