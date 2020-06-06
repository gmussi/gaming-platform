package com.guilhermemussi.gameplay.service;

import com.guilhermemussi.gameplay.config.MatchEvent;
import com.guilhermemussi.gameplay.models.Match;
import com.guilhermemussi.gameplay.models.PlayerEvents;
import com.guilhermemussi.gameplay.models.TicTacToeMatch;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.StringReader;
import java.util.logging.Logger;

@ApplicationScoped
public class GameplayService {
    public static final Logger LOGGER = Logger.getLogger(GameplayService.class.getName());

    public static final Jsonb jsonBuilder = JsonbBuilder.create();

    @Inject
    @Channel("player-events-out")
    Emitter<JsonObject> playerEvents;

    @Incoming("player-move-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerMove(JsonObject json) {
        LOGGER.info("Processing player event: " + json.toString());

        String username = json.getString("username");
        Match match = Match.findById(json.getString("matchId"));
        JsonObject move = json.getJsonObject("move");

        if (match.gameType.validateInput(username, match, move)) {
            // apply the move
            int winner = match.gameType.applyMove(username, match, move);

            final Match castedMatch;
            switch (match.gameType) {
                case TICTACTOE:
                    castedMatch = TicTacToeMatch.findById(match.matchId);
                    break;
                default:
                    castedMatch = match;
            }
            // notify players of the state of the game
            match.players.forEach((user) -> playerEvents.send(
                    Json.createReader(new StringReader(jsonBuilder.toJson(
                            new MatchEvent(castedMatch,
                                    user,
                                    winner == - 1 ? PlayerEvents.PLAY : PlayerEvents.END_MATCH))))
                            .readObject())
            );

        } else {
            LOGGER.warning("Invalid event: " + json);
        }
    }
}
