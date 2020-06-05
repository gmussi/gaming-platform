package com.guilhermemussi.matches.config;

import com.guilhermemussi.matches.models.Match;
import com.guilhermemussi.matches.models.PlayerEvents;

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
