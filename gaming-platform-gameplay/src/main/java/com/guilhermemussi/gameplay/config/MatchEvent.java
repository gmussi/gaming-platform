package com.guilhermemussi.gameplay.config;

import com.guilhermemussi.gameplay.models.Match;
import com.guilhermemussi.gameplay.models.PlayerEvents;

public class MatchEvent {
    public Match match;
    public String to;
    public PlayerEvents eventType;

    public MatchEvent(Match match, String to, PlayerEvents eventType) {
        this.match = match;
        this.to = to;
        this.eventType = eventType;
    }
}
