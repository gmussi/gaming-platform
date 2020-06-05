package com.guilhermemussi.gameplay;

import com.guilhermemussi.gameplay.config.JsonTextDecoder;
import com.guilhermemussi.gameplay.models.Player;
import com.guilhermemussi.gameplay.models.SessionTicket;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.websocket.CloseReason.CloseCodes.VIOLATED_POLICY;

@ServerEndpoint(value = "/player/{username}/{ticketId}", decoders = JsonTextDecoder.class)
@ApplicationScoped
public class PlayerResource {
    Logger LOGGER = Logger.getLogger(PlayerResource.class.getName());

    static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    @Channel("player-connected-out")
    Emitter<String> playerConnected;

    @Inject
    @Channel("player-disconnected-out")
    Emitter<String> playerDisconnected;

    @Inject
    @Channel("find-match-out")
    Emitter<JsonObject> findMatch;

    @Inject
    @Channel("player-move-out")
    Emitter<JsonObject> playerMove;

    @OnOpen
    public void onOpen(final Session session, @PathParam("username") String username, @PathParam("ticketId") String ticketId) throws IOException {
        LOGGER.info("open " + session.getUserPrincipal());

        SessionTicket ticket = SessionTicket.<SessionTicket>findByIdOptional(ticketId).orElseThrow(IllegalArgumentException::new);

        if (ticket.used) {
            session.close(new CloseReason(VIOLATED_POLICY, "Ticket has been used already. Get a new one."));
        }

        // burn the ticket
        ticket.used = true;
        ticket.update();

        // adds to the sessions vector for broadcasting
        sessions.put(username, session);

        LOGGER.info("Player " + ticket.username + " connected");

        // set state of player to connected
        Player player = Player.use(ticket.username);
        player.connected = true;
        player.update();

        // broadcast that player is connected
        playerConnected.send(username);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        // delete session
        sessions.remove(username);

        LOGGER.info("Player " + username + " disconnected");

        // update connected status of player
        Player player = Player.use(username);
        player.connected = false;
        player.update();

        // broadcast that player disconnection
        playerDisconnected.send(username);
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        // delete session
        this.onClose(session, username);

        LOGGER.log(Level.INFO, "User " + username + " disconnected with error", throwable);
    }

    @OnMessage
    public void onMessage(JsonObject json, @PathParam("username") String username) {
        LOGGER.info("Message from " + username + ": " + json);

        switch (json.getString("action")) {
            case "FIND_MATCH":
                findMatch.send(Json.createObjectBuilder()
                        .add("username", username)
                        .add("gameType", json.getString("gameType"))
                        .build());
                break;
            case "PLAY":
                playerMove.send(Json.createObjectBuilder()
                        .add("username", username)
                        .add("matchId", json.getString("matchId"))
                        .add("move", json.get("move"))
                        .build());
                break;
            default:
                LOGGER.warning("Action not known: " + json.getString("action"));
        }
    }

    @Incoming("player-events-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerEvent(JsonObject json) {
        LOGGER.info("Player event received: " + json.toString());
        String username = Objects.requireNonNull(json.getString("to"));
        if (sessions.containsKey(username)) {
            LOGGER.info("Publishing event to " + username);
            sessions.get(username).getAsyncRemote().sendObject(json.toString());
        }
    }
}
