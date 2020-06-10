const INITIALIZING = "Initializing";
const CHECKING_ACCOUNT = "Checking account";
const REGISTERING = "Registering";
const AUTHENTICATING = "Authenticating";
const GETTING_TICKET= "Getting ticket";
const CONNECTING = "Connecting";
const MATCHMAKING = "Matchmaking";
const PLAYING = "Playing";
const VICTORY = "Victorious";
const DEFEAT = "Crushed";
const DRAW = "Draw";
const FAILURE = "Failure";
const DISCONNECTED = "Ready";

const Player = Backbone.Model.extend({
    "defaults": {
        "name": null,
        "status": INITIALIZING,
        "opponent": null,
        "isMyTurn": false,
        "isPlayer1": false,
        "matchId": null
    },

    automate() {
        this.set("status", CHECKING_ACCOUNT);

        // check if account exists
        let name = this.get("name");
        let that = this;
        jQuery.get(`${SERVER.ADMIN}/auth/available/${name}`)
            .done((available) => {
                if (available == "true") {
                    that.register();
                } else {
                    that.login();
                }
            })
            .fail((event) => {
                that.set("status", FAILURE);
            });
    },
    login() {
        let that = this;
        this.set("status", AUTHENTICATING);
        jQuery.post(`${SERVER.ADMIN}/auth/login`, {
            "username": this.get("name"),
            "password": this.get("name")
        }).done((token) => {
            that.set("token", token);
            that.getTicket();
        }).fail((event) => {
            that.set("status", FAILURE);
        });
    },
    register() {
        let that = this;
        this.set("status", REGISTERING);
        jQuery.post(`${SERVER.ADMIN}/auth/register`, {
            "username": this.get("name"),
            "password": this.get("name")
        }).done((token) => {
            that.set("token", token);
            that.getTicket();
        }).fail((event) => {
            that.set("status", FAILURE);
        });
    },
    disconnect() {
        this.get("ws").close();

        let that = this;
        if (this.get("automated")) {
            setTimeout(() => that.getTicket(), 500);
        }
    },
    getTicket() {
        let that = this;
        this.set("status", GETTING_TICKET);
        jQuery.ajax({
            url: `${SERVER.ADMIN}/ticket`,
            headers: {
                "Authorization": "Bearer " + this.get("token")
            }
        }).done((ticket) => that.initConnection(ticket));
    },
    initConnection(ticket) {
        this.set("status", CONNECTING);
        let name = this.get("name");

        let ws = new WebSocket(`${SERVER.GAMEPLAY}/player/${name}/${ticket}`);
        ws.onopen = (event) => this.onConnected(event);
        ws.onclose = (event) => this.onDisconnected(event);
        ws.onerror = (event) => this.onDisconnected(event);
        ws.onmessage = (event) => this.onPlayerEvent(JSON.parse(event.data));
        this.set("ws", ws);
    },
    onConnected(event) {
        this.findMatch();
    },
    findMatch() {
        this.set("status", MATCHMAKING);
        this.get("ws").send(JSON.stringify({
            "action": "FIND_MATCH",
            "gameType": this.get("gameType")
        }));
    },
    onDisconnected(event) {
        this.set({
            "opponent": null,
            "status": DISCONNECTED,
            "ws": null,
            "matchId": null
        });
    },
    onPlayerEvent(event) {
        switch (event.eventType) {
            case "START_MATCH":
                this.startMatch(event);
                break;
            case "END_MATCH":
                this.endMatch(event);
                break;
            case "PLAY":
                this.onPlayEvent(event.match);
                break;
            default:
                console.log("Unknown event type " , event, this);
                this.set("status", FAILURE);
        }
    },
    startMatch(event) {
        // to be overridden
    },
    endMatch(event) {
        // to be overridden
    },
    onMatchEnd() {
        this.findMatch();
    },
    onPlayEvent(event) {
        // to be overridden
    },
    onRemove() {
        let ws = this.get("ws");
        if (ws != null) {
            ws.close();
        }
    },
    cancelMatch() {
        let ws = this.get("ws");
        ws.send(JSON.stringify({
            "action": "CANCEL_MATCH",
            "matchId": this.get("matchId")
        }));
    }
});

const TicTacToePlayer = Player.extend({
    "defaults" : {
        "gameType": "TICTACTOE",
        "opponent": null,
        "pos": [0, 0, 0, 0, 0, 0, 0, 0, 0]
    },
    startMatch(event) {
        let opponent = this.get("name") == event.match.players[0] ? event.match.players[1] : event.match.players[0];
        this.set({
            "opponent": opponent,
            "status": PLAYING,
            "pos": [0, 0, 0, 0, 0, 0, 0, 0, 0],
            "isMyTurn": event.match.currentTurnPlayer == this.get("name"),
            "started": event.match.startingPlayer == this.get("name"),
            "matchId": event.match.matchId
        });
        if (this.get("isMyTurn") && this.get("automated")) {
            this.autoPlay();
        }
    },
    endMatch(event) {
        let endType = event.match.endType;

        this.set({
            "pos": [0, 0, 0, 0, 0, 0, 0, 0, 0],
            "status": endType == "DRAW" ? DRAW :
                endType == "DISCONNECTION" ? MATCHMAKING :
                    event.match.winner == this.get("name") ? VICTORY : DEFEAT
        });

        let that = this;
        setTimeout(() => that.onMatchEnd(), 500);
    },
    play(pos) {
        this.get("ws").send(JSON.stringify({
            "matchId": this.get("matchId"),
            "gameType": "TICTACTOE",
            "action": "PLAY",
            "move": {
                "pos": parseInt(pos)
            }
        }));
    },
    onPlayEvent(event) {
        this.set({
            "pos": event.pos,
            "isMyTurn": event.currentTurnPlayer == this.get("name")
        });

        if (this.get("automated")) {
            this.autoPlay();
        }
    },
    autoPlay() {
        let that = this;
        setTimeout(() => {
            if (this.get("isMyTurn") && this.get("status") == PLAYING) {
                let pos = this.get("pos");
                let playablePos = [];
                for (let i = 0; i < 9; i++) {
                    if (pos[i] == 0) {
                        playablePos.push(i);
                    }
                }
                that.play(playablePos[Math.floor(Math.random() * playablePos.length)]);
            }
        }, 500);
    }
});

class Players extends Backbone.Collection {
    addPlayer(player, view) {
        this.add(player);
        $("#players").append(view.$el);
        view.render();
        player.automate();
    }
    removePlayer(player, view) {
        player.onRemove();
        this.remove(player);
        view.$el.remove();
    }
};


const players = new Players();


