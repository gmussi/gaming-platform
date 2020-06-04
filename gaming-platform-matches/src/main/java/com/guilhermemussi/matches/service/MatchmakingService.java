package com.guilhermemussi.matches.service;

import com.guilhermemussi.matches.models.Player;
import com.guilhermemussi.matches.models.GameType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class MatchmakingService {
    public static final Logger LOGGER = Logger.getLogger(MatchmakingService.class.getName());

    @Inject
    @Channel("start-match-out")
    Emitter<JsonObject> startMatch;

    @Incoming("find-match-in")
    public synchronized void findMatch(JsonObject json) {
        // validate input
        String username = Objects.requireNonNull(json.getString("username"));
        GameType gameType = GameType.valueOf(Objects.requireNonNull(json.getString("gameType")));

        LOGGER.info("Searching " + gameType + " match for " + username);

        // check if there are other players waiting for matchmaking in this game type
        Optional<Player> opponentOptional = Player.find("searching = ?1 AND _id != ?2 AND connected = ?3 AND gameType = ?4", true, username, true, gameType).firstResultOptional();

        if (opponentOptional.isPresent()) { // if there are players, start match
            Player opponent = opponentOptional.get();

            opponent.searching = false;
            opponent.gameType = null;
            opponent.update();

            LOGGER.info("Launching match between " + username + " and " + opponent.username);

            // launch an event to start this match
            startMatch.send(Json.createObjectBuilder()
                .add("gameType", gameType.toString())
                .add("players", Json.createArrayBuilder()
                        .add(username)
                        .add(opponent.username)
                        .build())
                .build());
        } else { // else, sets this player to wait
            Player player = Player.use(username);
            player.searching = true;
            player.gameType = gameType;
            player.update();
        }

    }
}
