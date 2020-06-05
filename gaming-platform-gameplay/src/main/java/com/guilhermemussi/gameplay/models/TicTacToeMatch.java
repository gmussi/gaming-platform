package com.guilhermemussi.gameplay.models;

import io.quarkus.mongodb.panache.MongoEntity;

import javax.json.JsonObject;
import java.util.List;

@MongoEntity(collection = "matches")
public class TicTacToeMatch extends Match {
    public List<Integer> pos;

    public String startingPlayer;

    public String currentTurnPlayer;

    public TicTacToeMatch() {

    }

    public static boolean validateInput(String username, JsonObject json) {
        TicTacToeMatch match = TicTacToeMatch.findById(json.getString("matchId"));

        // make sure player playing is the current player
        if (!match.currentTurnPlayer.equals(username)) {
            return false; // not this player turn
        }

        // is the position already marked?
        int newpos = json.getInt("pos");
        if (match.pos.get(newpos) != 0) {
            return false; // this square is already played
        }

        return true;
    }
}
