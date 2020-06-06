package com.guilhermemussi.gameplay.models;

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
        public boolean validateInput(String player, Match match, JsonObject move) {
            return TicTacToeMatch.validateInput(player, match, move);
        }

        @Override
        public int applyMove(String username, Match match, JsonObject move) {
            return TicTacToeMatch.applyMove(username, match, move);
        }
    },
    CHECKERS {
        @Override
        public Match startMatch(String... players) {
            return null;
        }

        @Override
        public boolean validateInput(String player, Match match, JsonObject mov) {
            return false;
        }

        @Override
        public int applyMove(String username, Match match, JsonObject move) { return 0; }
    },
    CHESS {
        @Override
        public Match startMatch(String... players) {
            return null;
        }

        @Override
        public boolean validateInput(String player, Match match, JsonObject mov) {
            return false;
        }

        @Override
        public int applyMove(String username, Match match, JsonObject move) { return 0; }
    },
    MATCH_FOUR {
        @Override
        public Match startMatch(String... players) {
            return null;
        }

        @Override
        public boolean validateInput(String player, Match match, JsonObject mov) {
            return false;
        }

        @Override
        public int applyMove(String username, Match match, JsonObject move) { return 0; }
    };
    public abstract Match startMatch(String... players);
    public abstract boolean validateInput(String player, Match match, JsonObject move);
    public abstract int applyMove(String username, Match match, JsonObject move);
}
