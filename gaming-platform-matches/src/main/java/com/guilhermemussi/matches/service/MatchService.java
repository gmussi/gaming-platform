package com.guilhermemussi.matches.service;

import com.guilhermemussi.matches.models.GameType;
import com.guilhermemussi.matches.models.Match;
import com.guilhermemussi.matches.models.PlayerEvents;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@ApplicationScoped
public class MatchService {
    public static final Logger LOGGER = Logger.getLogger(MatchService.class.getName());

    @Inject
    @Channel("player-events-out")
    Emitter<JsonObject> playerEvents;

    @Incoming("start-match-in")
    public void startMatch(JsonObject json) {
        // check game type, must not be null
        GameType gameType = GameType.valueOf(Objects.requireNonNull(json.getString("gameType")));

        // trust that the sender sent this correctly. validate that at least 1 player is in the list
        List<String> usernames = json.getJsonArray("players").getValuesAs(JsonString::getString);

        LOGGER.info("Creating match " + gameType + " between " + usernames.toString());

        if (!usernames.isEmpty()) {
            // launches match
            final Match match = Match.start(gameType, usernames.toArray(String[]::new));

            LOGGER.info("Sending player events for match " + match.matchId);

            // notifies all players
            usernames.forEach((username) -> playerEvents.send(Json.createObjectBuilder()
                    .add("to", username)
                    .add("eventType", PlayerEvents.START_MATCH.toString())
                    .add("matchId", match.matchId)
                    .add("players", Json.createArrayBuilder(match.players).build())
                    .add("gameType", match.gameType.toString())
                    .build()));
        }
    }

    @Incoming("player-disconnected-in")
    public void onPlayerDisconnected(String username) {
        LOGGER.info("Canceling matches of " + username);

        List<Match> matches = Match.find("endType = null and players = ?1", username).list();
        matches.forEach((match) -> {
            LOGGER.info("Canceling match " + match.matchId + " between " + match.players.toString());

            // end match with disconnection status
            match.endType = Match.EndType.DISCONNECTION;
            match.update();

            // notify players match is over
            match.players.forEach((user) -> {
                playerEvents.send(Json.createObjectBuilder()
                        .add("to", user)
                        .add("eventType", PlayerEvents.END_MATCH.toString())
                        .add("endType", Match.EndType.DISCONNECTION.toString())
                        .add("matchId", match.matchId)
                        .build());
            });
        });
    }

}
