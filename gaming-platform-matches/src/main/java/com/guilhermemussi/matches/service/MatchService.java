package com.guilhermemussi.matches.service;

import com.guilhermemussi.matches.config.MatchEvent;
import com.guilhermemussi.matches.models.GameType;
import com.guilhermemussi.matches.models.Match;
import com.guilhermemussi.matches.models.PlayerEvents;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.Jsonb;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@ApplicationScoped
public class MatchService {
    public static final Logger LOGGER = Logger.getLogger(MatchService.class.getName());

    public static final Jsonb jsonBuilder = JsonbBuilder.create();

    @Inject
    @Channel("player-events-out")
    Emitter<JsonObject> playerEvents;

    @Incoming("start-match-in")
    @Merge(Merge.Mode.MERGE)
    public void startMatch(JsonObject json) {
        // check game type, must not be null
        GameType gameType = GameType.valueOf(Objects.requireNonNull(json.getString("gameType")));

        // trust that the sender sent this correctly. validate that at least 1 player is in the list
        List<String> usernames = json.getJsonArray("players").getValuesAs(JsonString::getString);

        LOGGER.info("Creating match " + gameType + " between " + usernames.toString());

        if (!usernames.isEmpty()) {
            // launches match
            Match match = Match.start(gameType, usernames.toArray(String[]::new));

            LOGGER.info("Sending start event for match " + match.matchId);

            // notifies all players
            usernames.forEach((username) -> {
                String str = jsonBuilder.toJson(
                        new MatchEvent(match, username, PlayerEvents.START_MATCH));
                LOGGER.info(str);
                playerEvents.send(Json.createReader(new StringReader(str))
                        .readObject());
                    });
        }
    }

    @Incoming("player-disconnected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerDisconnected(String username) {
        List<Match> matches = Match.find("{'endType': null, 'players': ?1}", username).list();
        LOGGER.info("Canceling matches of " + username + ": " + matches.size());

        matches.forEach((match) -> {
            LOGGER.info("Canceling match " + match.matchId + " between " + match.players.toString());

            // end match with disconnection status
            match.endType = Match.EndType.DISCONNECTION;
            match.update();

            // notify players match is over
            match.players.forEach((user) -> playerEvents.send(
                    Json.createReader(new StringReader(jsonBuilder.toJson(
                            new MatchEvent(match, user, PlayerEvents.END_MATCH))))
                            .readObject())
            );
        });
    }

}
