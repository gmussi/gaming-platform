package com.guilhermemussi.admin;

import com.guilhermemussi.admin.config.TokenUtils;
import com.guilhermemussi.admin.model.Player;
import com.guilhermemussi.admin.model.SessionTicket;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.websocket.CloseReason.CloseCodes.VIOLATED_POLICY;

@ServerEndpoint("/player/{username}/{ticketId}")
@ApplicationScoped
public class PlayerResource {
    Logger LOGGER = Logger.getLogger(PlayerResource.class.getName());

    static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    @Channel("player-connected-out")
    Emitter<String> playerConnected;

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
        // playerDisconnected.send(username);
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        // delete session
        this.onClose(session, username);

        LOGGER.log(Level.INFO, "User " + username + " disconnected with error", throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {
        LOGGER.info("Message from " + username + ": " + message);
    }

}
