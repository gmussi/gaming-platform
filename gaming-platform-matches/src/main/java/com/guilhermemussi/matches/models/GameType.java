package com.guilhermemussi.matches.models;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.UUID;

public enum GameType {
    TICTACTOE {
        @Override
        public Match startMatch(String... players) {
            TicTacToeMatch ticTacToeMatch = new TicTacToeMatch();
            ticTacToeMatch.matchId = UUID.randomUUID().toString();
            ticTacToeMatch.currentTurnPlayer = players[0];
            ticTacToeMatch.startingPlayer = players[0];
            ticTacToeMatch.pos = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0);
            ticTacToeMatch.players = Arrays.asList(players);
            ticTacToeMatch.gameType = TICTACTOE;
            ticTacToeMatch.persist();
            return ticTacToeMatch;
        }

        @Override
        public boolean validateInput(String player, JsonObject json) {
            return TicTacToeMatch.validateInput(player, json);
        }
    },
    CHECKERS {
        @Override
        public Match startMatch(String... players) {
            return null;
        }

        @Override
        public boolean validateInput(String player, JsonObject json) {
            return false;
        }
    },
    CHESS {
        @Override
        public Match startMatch(String... players) {
            return null;
        }

        @Override
        public boolean validateInput(String player, JsonObject json) {
            return false;
        }
    },
    MATCH_FOUR {
        @Override
        public Match startMatch(String... players) {
            return null;
        }

        @Override
        public boolean validateInput(String player, JsonObject json) {
            return false;
        }
    };
    public abstract Match startMatch(String... players);
    public abstract boolean validateInput(String player, JsonObject json);
}
