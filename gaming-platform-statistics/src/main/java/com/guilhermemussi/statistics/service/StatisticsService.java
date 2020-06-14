package com.guilhermemussi.statistics.service;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

@ApplicationScoped
public class StatisticsService {
    @Inject
    @Channel("stats-change")
    Emitter<JsonObject> statsChange;

    @Incoming("player-count")
    public void onPlayerCount(Integer count) {
        statsChange.send(getPlayerCount(count));
    }

    public static JsonObject getPlayerCount(int count) {
        return Json.createObjectBuilder().add("type", "PLAYER_COUNT").add("count", count).build();
    }

    public static JsonObject getPlayerCount() {
        return getPlayerCount(PlayerCountService.playerCount.get());
    }
}
