package com.guilhermemussi.gameplay.models;

import io.quarkus.mongodb.panache.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.json.JsonObject;
import java.util.List;

@MongoEntity(collection = "matches")
public class TicTacToeMatch extends Match {
    public List<Integer> pos;

    public String startingPlayer;

    public String currentTurnPlayer;

    public TicTacToeMatch() {

    }

    public static boolean validateInput(String player, Match match, JsonObject move) {
        TicTacToeMatch tictactoe = TicTacToeMatch.findById(match.matchId);

        // make sure player playing is the current player
        if (!player.equals(tictactoe.currentTurnPlayer)) {
            return false; // not this player turn
        }

        // is the position already marked?
        int newpos = move.getInt("pos");
        if (tictactoe.pos.get(newpos) != 0) {
            return false; // this square is already played
        }

        return true;
    }

    public static int applyMove(String username, Match match, JsonObject move) {
        TicTacToeMatch tictactoe = TicTacToeMatch.findById(match.matchId);

        // discover number of player
        int playerNum = username.equals(tictactoe.startingPlayer) ? 1 : 2;
        tictactoe.pos.set(move.getInt("pos"), playerNum);

        int winner = findWinner(tictactoe, playerNum);
        if (winner == 0)  {
            tictactoe.endType = EndType.DRAW;
        } else if (winner == 1) {
            tictactoe.endType = EndType.VICTORY;
            tictactoe.winner = tictactoe.startingPlayer;
        } else if (winner == 2) {
            tictactoe.endType = EndType.VICTORY;
            tictactoe.winner = tictactoe.players.get(0).equals(tictactoe.startingPlayer) ? tictactoe.players.get(1) : tictactoe.players.get(0);
        }
        tictactoe.currentTurnPlayer = tictactoe.players.get(0).equals(username) ? tictactoe.players.get(1) : tictactoe.players.get(0);
        tictactoe.update();
        return winner;
    }

    /**
     * Checks if the current player won the game.
     *
     * @param tictactoe
     * @param playerNum
     * @return 0 = tie, 1 = player1, 2 = player2, -1 = match not over yet
     */
    public static int findWinner(TicTacToeMatch tictactoe, int playerNum) {
        // check if there is a winner
        int[][] pos = {
                {tictactoe.pos.get(0), tictactoe.pos.get(1), tictactoe.pos.get(2)},
                {tictactoe.pos.get(3), tictactoe.pos.get(4), tictactoe.pos.get(5)},
                {tictactoe.pos.get(6), tictactoe.pos.get(7), tictactoe.pos.get(8)},
        };

        for (int[][] scenario : WIN_SCENARIOS) {
            boolean matched = true;
            // check each winning scenario to see if any matched
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (scenario[y][x] == 1) {
                        matched &= pos[y][x] == playerNum;
                    }
                }
            }
            if (matched) {
                // matched and the current player win
                return playerNum;
            }
        }

        // check if this was the last play of the game
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (pos[y][x] == 0) {
                    return -1;
                }
            }
        }

        // if no player won, and this was the last move, then the game ties
        return 0;
    }

    @BsonIgnore
    public static final int[][][] WIN_SCENARIOS = {
            {
                    {1,1,1},
                    {0,0,0},
                    {0,0,0}
            },
            {
                    {0,0,0},
                    {1,1,1},
                    {0,0,0}
            },
            {
                    {0,0,0},
                    {0,0,0},
                    {1,1,1}
            },
            {
                    {1,0,0},
                    {1,0,0},
                    {1,0,0}
            },
            {
                    {0,1,0},
                    {0,1,0},
                    {0,1,0}
            },
            {
                    {0,0,1},
                    {0,0,1},
                    {0,0,1}
            },
            {
                    {1,0,0},
                    {0,1,0},
                    {0,0,1}
            },
            {
                    {0,0,1},
                    {0,1,0},
                    {1,0,0}
            },
    };
}
