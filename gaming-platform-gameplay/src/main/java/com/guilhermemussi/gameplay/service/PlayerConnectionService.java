package com.guilhermemussi.gameplay.service;

import com.guilhermemussi.gameplay.models.Player;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class PlayerConnectionService {
    public final static Logger LOGGER = Logger.getLogger(PlayerConnectionService.class.getName());

    @Incoming("player-connected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerConnected(String username) {
        LOGGER.info("on player connected: "+ username);

        Player player = Player.use(username);
        player.connected = true;
        player.update();
    }

    @Incoming("player-disconnected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerDisconnected(String username) {
        LOGGER.info("on player disconnected: "+ username);

        Player player = Player.use(username);
        player.connected = false;
        player.searching = false;
        player.update();
    }
}
