package com.guilhermemussi.gameplay.service;

import com.guilhermemussi.gameplay.models.Player;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlayerConnectionService {
    @Incoming("player-connected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerConnected(String username) {
        Player player = Player.use(username);
        player.connected = true;
        player.update();
    }

    @Incoming("player-disconnected-in")
    @Merge(Merge.Mode.MERGE)
    public void onPlayerDisconnected(String username) {
        Player player = Player.use(username);
        player.connected = false;
        player.searching = false;
        player.update();
    }
}
