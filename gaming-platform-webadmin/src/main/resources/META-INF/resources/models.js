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
            .done((exists) => {
                console.log(exists, exists == "true")
                if (exists == "true") {
                    that.login();
                } else {
                    that.register();
                }
            })
            .fail((event) => {
                that.set("status", FAILURE);
                console.log(that, event);
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
            console.log(that, event);
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
            console.log(that, event);
        });
    },
    disconnect() {
        console.log("Disconnecting myself: " + this.get("name"), this.get("ws"))
        this.get("ws").close();
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
        this.set("status", MATCHMAKING);
        this.get("ws").send(JSON.stringify({
            "action": "FIND_MATCH",
            "gameType": this.get("gameType")
        }));
    },
    onDisconnected(event) {
        console.log(this.get("name") + " disconnected.");
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
        setTimeout(() => that.onMatchEnd(), 1000);
    },
    play(pos) {
        console.log("Sending MatchEvent to server ", pos, this);

        this.get("ws").send(JSON.stringify({
            "matchId": this.get("matchId"),
            "gameType": "TICTACTOE",
            "action": "PLAY",
            "move": {
                "pos": pos
            }
        }));
    },
});

class Players extends Backbone.Collection {
    addPlayer(player, view) {
        this.add(player);
        $("#players").append(view.$el);
        view.render();
        player.automate();
    }
};


const players = new Players();


