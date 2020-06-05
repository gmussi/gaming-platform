package com.guilhermemussi.gameplay.service;

import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import java.util.logging.Logger;

@ApplicationScoped
public class GameplayService {
    public static final Logger LOGGER = Logger.getLogger(GameplayService.class.getName());

    @Incoming("player-move-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerMove(JsonObject json) {
        LOGGER.info("Processing player event: " + json.toString());
    }
}
